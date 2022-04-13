package com.brd.asset.flink.fun;

import com.brd.asset.constants.AlarmItem;
import com.brd.asset.constants.AssetConstant;
import com.brd.asset.pojo.AssetBase;
import com.brd.asset.pojo.AssetScanOrigin;
import com.brd.asset.pojo.OpenServiceOfPort;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @Author: leo.j
 * @desc:  异常资产处理
 * @Date: 2022/3/25 3:30 下午
 */
public class AbnormalAssetMap extends RichFlatMapFunction<AssetScanOrigin, Tuple3<String, String, AlarmItem>> {
    private static Logger LOG = Logger.getLogger(AbnormalAssetMap.class);

    private String jdbcUrl;
    private String userName;
    private String password;
    private Timer timer;
    private Map<String, AssetBase> ip2baseMap = new HashMap<>();

    //读取资产基础表
    private PreparedStatement ps;
    private BasicDataSource dataSource;
    private Connection connection;

    //纳管资产
    private Set<String> accessAssets = new HashSet<>();
    //开放端口数阈值
    private Integer openPortThreshold;
    //进程黑名单
    private List<String> processBlackList;


    public AbnormalAssetMap(String jdbcUrl, String userName, String password, Integer openPortThreshold, String processBlackList) {
        this.jdbcUrl = jdbcUrl;
        this.userName = userName;
        this.password = password;
        this.openPortThreshold = openPortThreshold;
        if ("".equals(processBlackList)) {
            this.processBlackList = Arrays.asList();
        } else {
            this.processBlackList = Arrays.asList(processBlackList.split(","));
        }
    }
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    //读取资产基础表数据
                    loadAssetData();
                    //获取纳管资产
                    getAccessAssets();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000, 1*1*60*1000);
    }


    @Override
    public void flatMap(AssetScanOrigin asset, Collector<Tuple3<String, String, AlarmItem>> collector) throws Exception {
        //获取所有资产ip列表
        Set<String> ips = ip2baseMap.keySet();
        String ip = asset.getDeviceIPAddress();

        //1.异常开放端口
        List<OpenServiceOfPort> openServiceOfPort = asset.getOpenServiceOfPort();
        if (openServiceOfPort.size() > openPortThreshold) {
            collector.collect(Tuple3.of(asset.getTaskID(), ip, AlarmItem.ABNORMAL_OPEN_PORT));
        }
        //2.异常资产信息变更(对同一台资产os info，与上一次资产采集的信息对比)
        if (ips.contains(ip)) {
            AssetBase assetBase = ip2baseMap.get(ip);
            String osInfo = assetBase.getOs_info();
            if (osInfo != null && !osInfo.equals(asset.getOSInfo())) {
                collector.collect(Tuple3.of(asset.getTaskID(), ip, AlarmItem.ABNORMAL_ASSET_CHANGE));
            }
        }

        //4.发现无主资产
        if (ips.contains(ip)) {
            AssetBase lastAsset = ip2baseMap.get(ip);
            String attributionGroup = lastAsset.getAttribution_group();
            String responsiblePerson = lastAsset.getResponsible_person();
            if (attributionGroup == null || "".equals(attributionGroup) || responsiblePerson == null || "".equals(responsiblePerson)) {
                collector.collect(Tuple3.of(asset.getTaskID(), ip, AlarmItem.ABNORMAL_ASSET_NO_GROUP));
            }
        }
        //5.发现未知资产
        if (!accessAssets.contains(ip)) {
            collector.collect(Tuple3.of(asset.getTaskID(), ip, AlarmItem.ABNORMAL_ASSET_UNKNOWN));
        }
        //6.异常进程信息
        if (processBlackList.contains(ip)) {
            collector.collect(Tuple3.of(asset.getTaskID(), ip, AlarmItem.ABNORMAL_PROESS));
        }
    }
    /**
     * 加载资产基本表数据
     */
    public void loadAssetData() {
        String sql = "select * from " + AssetConstant.TAB_ASSET_BASE;
        try {
            //防止异常断开连接
            if(this.dataSource.isClosed()){
                this.dataSource = new BasicDataSource();
            }
            if(this.connection.isClosed()){
                this.connection = getCon(dataSource);
            }
            ps = this.connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery(sql);
            while (rs.next()) {
                String ip = rs.getString("ips");
                AssetBase assetBase = getAssetBase(rs);
                ip2baseMap.put(ip, assetBase);
            }
        } catch (Exception e) {
            LOG.error("读取资产表数据报错1：" + e.getMessage());
        }
    }

    /**
     * 获取assetBase
     *
     * @param rs
     * @return
     */
    public AssetBase getAssetBase(ResultSet rs) {
        AssetBase assetBase = null;
        try {
            assetBase = new AssetBase(rs.getString("asset_id"), rs.getString("name"), rs.getString("ips"), rs.getString("ip_address"), rs.getString("mac"), rs.getString("type"), rs.getString("os_info"), rs.getString("physical_address"), rs.getString("attribution_group"), rs.getString("collection_time"), rs.getString("engine_room"), rs.getString("engine_box"), rs.getString("manufacturers"), rs.getString("sn"), rs.getString("model"), rs.getString("business_system"), rs.getString("responsible_person"), rs.getString("telephone"), rs.getString("asset_desc"), rs.getString("confidentiality"), rs.getString("integrality"), rs.getString("usability"), rs.getString("importance"), rs.getString("is_permitted"), rs.getString("backup_field_1"), rs.getString("backup_field_2"), rs.getString("backup_field_3"), rs.getString("asset_ip"), rs.getString("longitude"), rs.getString("latitude"), rs.getString("responsible_unit"), rs.getString("responsible_department"), rs.getString("responsible_group"), rs.getString("asset_source_time"), rs.getString("access"));
        } catch (SQLException e) {
            LOG.error("读取资产表数据报错2：" + e.getMessage());
        }
        return assetBase;
    }
    /**
     * 获取纳管资产
     */
    public void getAccessAssets() {
        boolean isClear = false;
        for (AssetBase assetBase : ip2baseMap.values()) {
            if(!isClear){
                accessAssets.clear();
                isClear = true;
            }
            //过滤纳管资产ip
            if ("_PERMITTED".equals(assetBase.getIs_permitted())) {
                accessAssets.add(assetBase.getIps());
            }
        }
    }

    public Connection getCon(BasicDataSource dataSource) {
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(userName);
        dataSource.setPassword(password);
        //设置连接池的一些参数
        dataSource.setInitialSize(5);
        dataSource.setMinIdle(2);
        dataSource.setValidationQuery("select 1");
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);
        dataSource.setTimeBetweenEvictionRunsMillis(30000);
        dataSource.setMinEvictableIdleTimeMillis(1800000);
        dataSource.setNumTestsPerEvictionRun(3);

        Connection con = null;
        try {
            con = dataSource.getConnection();
        } catch (Exception e) {
            LOG.info("-----------异常资产评估：mysql get connection has exception , msg = " + e.getMessage());
        }
        return con;

    }
}
