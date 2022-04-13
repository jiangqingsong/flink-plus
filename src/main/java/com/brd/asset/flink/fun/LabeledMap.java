package com.brd.asset.flink.fun;

import com.alibaba.fastjson.JSONObject;
import com.brd.asset.pojo.AssetScanOrigin;
import com.brd.asset.pojo.DataBaseInfoScan;
import com.brd.asset.pojo.MessageOrientedMiddlewareScan;
import com.brd.asset.pojo.OpenServiceOfPort;
import com.mysql.cj.jdbc.Driver;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * @Author: leo.j
 * @desc: 回填标签信息
 * @Date: 2022/3/25 1:22 下午
 */
public class LabeledMap extends RichMapFunction<AssetScanOrigin, Tuple2<AssetScanOrigin, String>> {
    private static Logger LOG = Logger.getLogger(LabeledMap.class);
    private Connection connection = null;
    private PreparedStatement ps = null;
    private volatile boolean isRunning = true;

    private String jdbcUrl;
    private String userName;
    private String password;

    private Map<String, Tuple3<Integer, String, String>> labelMap;
    private Timer timer;

    public LabeledMap(String jdbcUrl, String userName, String password) {
        this.jdbcUrl = jdbcUrl;
        this.userName = userName;
        this.password = password;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        DriverManager.registerDriver(new Driver());
        connection = DriverManager.getConnection(jdbcUrl, userName, password);
        labelMap = new HashMap<>();
        //开启一个定时任务，定期更新配置数据
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    //获取标签库数据
                    getData();
                    LOG.info("标签库更新！");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000, 1*30*60*1000);
    }

    @Override
    public Tuple2<AssetScanOrigin, String> map(AssetScanOrigin scan) throws Exception {
        if(labelMap != null){
            //待检测漏洞的Tuple<name, version>列表
            List<org.apache.flink.api.java.tuple.Tuple2<String, String>> toCheckList = new ArrayList<>();

            String taskID = scan.getTaskID();
            List<MessageOrientedMiddlewareScan> messageOrientedMiddlewares = scan.getMessageOrientedMiddleware();
            List<DataBaseInfoScan> dataBaseInfos = scan.getDataBaseInfos();
            List<OpenServiceOfPort> openServiceOfPorts = scan.getOpenServiceOfPort();
            String systemFingerprintInfo = scan.getSystemFingerprintInfo();
            for(MessageOrientedMiddlewareScan m: messageOrientedMiddlewares){
                toCheckList.add(Tuple2.of(m.getName(), m.getVersion()));
            }
            for(DataBaseInfoScan d: dataBaseInfos){
                toCheckList.add(org.apache.flink.api.java.tuple.Tuple2.of(d.getName(), d.getVersion()));
            }
            for(OpenServiceOfPort o: openServiceOfPorts){
                toCheckList.add(org.apache.flink.api.java.tuple.Tuple2.of(o.getName(), o.getVersion()));
            }

            String labelInputInfo = StringUtils.join(toCheckList, ",")
                    + "," + scan.getOSInfo() + "," + systemFingerprintInfo
                    + "," + scan.getDeviceType() + "," +scan.getRuning();

            Set<Tuple3<Integer, String, String>> matchedLabels = matchLabel(labelInputInfo);

            boolean isSwitchOrRoute = false;
            if(matchedLabels.size() == 0){
                return Tuple2.of(scan, "");
            }else {
                String separator = ",";
                List<String> labelIds = new ArrayList<>();
                List<String> type1List = new ArrayList<>();
                List<String> type2List = new ArrayList<>();
                for(Tuple3<Integer, String, String> label: matchedLabels){
                    labelIds.add(String.valueOf(label.f0));
                    type1List.add(label.f1);
                    type2List.add(label.f2);
                }
                if(isSwitchOrRoute){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("TaskID", taskID);
                    jsonObject.put("IPAddress", scan.getDeviceIPAddress());
                }
                return Tuple2.of(scan, StringUtils.join(labelIds, separator));
            }
        }
        return null;
    }
    /**
     * 获取漏洞数据和label数据
     * @throws Exception
     */
    public void getData() throws Exception{

        String sql1 = "select l.id as id,l.label_1 as label1,l.label_2 as label2,k.keyword as keyword from label l join label_key k on l.id = k.label_id";
        ps = connection.prepareStatement(sql1);

        if (isRunning) {
            //组装标签数据
            ResultSet rs1 = ps.executeQuery();
            while (rs1.next()) {
                int id = rs1.getInt("id");
                String label1 = rs1.getString("label1");
                String label2 = rs1.getString("label2");
                String keyword = rs1.getString("keyword");
                labelMap.put(keyword, Tuple3.of(id, label1, label2));
            }
            LOG.info("======= labelMap size======" + labelMap.size());
        }
    }
    /**
     *
     * @param input 指纹信息
     * @return labels
     */
    public Set<Tuple3<Integer, String, String>> matchLabel(String input){
        Set<Tuple3<Integer, String, String>> labels = new HashSet<Tuple3<Integer, String, String>>();
        Set<String> keywords = labelMap.keySet();
        for(String keyword: keywords){
            if(input.toLowerCase().contains(keyword.toLowerCase())){
                labels.add(labelMap.get(keyword));
            }
        }
        return labels;
    }
}
