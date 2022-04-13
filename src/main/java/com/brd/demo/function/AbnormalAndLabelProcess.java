package com.brd.demo.function;

import com.brd.asset.constants.AlarmItem;
import com.brd.asset.pojo.AssetScanOrigin;
import com.brd.asset.pojo.OpenServiceOfPort;
import com.brd.demo.entity.AssetBase;
import com.brd.demo.entity.AssetCdcResult;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.*;

/**
 * @Author: leo.j
 * @desc: 1、异常资产评估
 *        2、资产基础表更新数据侧输出
 * @Date: 2022/4/1 11:00 上午
 */
public class AbnormalAndLabelProcess extends BroadcastProcessFunction<Tuple2<AssetScanOrigin, String>, AssetCdcResult, Tuple3<String, String, AlarmItem>> {

    //纳管资产
    private Set<String> accessAssets = new HashSet<>();
    //资产基本表
    private Map<String, AssetBase> ip2baseMap = new HashMap<>();

    private MapStateDescriptor<String, AssetBase> assetBaseMapStateDescriptor;


    private int openPortThreshold;
    private List<String> processBlackList;

    public AbnormalAndLabelProcess(MapStateDescriptor<String, AssetBase> assetBaseMapStateDescriptor, int openPortThreshold, String processBlack) {
        this.assetBaseMapStateDescriptor = assetBaseMapStateDescriptor;
        this.openPortThreshold = openPortThreshold;
        if ("".equals(processBlack)) {
            this.processBlackList = Arrays.asList();
        } else {
            this.processBlackList = Arrays.asList(processBlack.split(","));
        }
    }

    @Override
    public void processElement(Tuple2<AssetScanOrigin, String> tuple2, ReadOnlyContext ctx, Collector<Tuple3<String, String, AlarmItem>> out) throws Exception {
        AssetScanOrigin asset = tuple2.f0;

        OutputTag<Tuple2<AssetScanOrigin, String>> insertTag = new OutputTag<Tuple2<AssetScanOrigin, String>>("insert-tag") {};
        OutputTag<Tuple2<AssetScanOrigin, String>> updateTag = new OutputTag<Tuple2<AssetScanOrigin, String>>("update-tag") {};

        ReadOnlyBroadcastState<String, AssetBase> assetBaseBroadcastState = ctx.getBroadcastState(assetBaseMapStateDescriptor);
        //数据更新
        ip2baseMap.clear();
        assetBaseBroadcastState.immutableEntries().iterator().forEachRemaining(i -> {
            String ip = i.getKey();
            AssetBase assetBase = i.getValue();
            ip2baseMap.put(ip, assetBase);
            //accessAssets.add(ip);
        });
        getAccessAssets();

        //获取所有资产ip列表
        Set<String> ips = ip2baseMap.keySet();
        String ip = asset.getDeviceIPAddress();
        // 资产基础表新增或更新数据输出
        if(ips.contains(ip)){
            //更新
            ctx.output(updateTag, tuple2);
        }else {
            //新增
            ctx.output(insertTag, tuple2);
        }

        //-------- 异常资产评估 --------
        //1.异常开放端口
        List<OpenServiceOfPort> openServiceOfPort = asset.getOpenServiceOfPort();
        if (openServiceOfPort.size() > openPortThreshold) {
            out.collect(Tuple3.of(asset.getTaskID(), ip, AlarmItem.ABNORMAL_OPEN_PORT));
        }
        //2.异常资产信息变更(对同一台资产os info，与上一次资产采集的信息对比)
        if (ips.contains(ip)) {
            AssetBase assetBase = ip2baseMap.get(ip);
            String osInfo = assetBase.getOs_info();
            if (osInfo != null && !osInfo.equals(asset.getOSInfo())) {
                out.collect(Tuple3.of(asset.getTaskID(), ip, AlarmItem.ABNORMAL_ASSET_CHANGE));
            }
        }

        //4.发现无主资产
        if (ips.contains(ip)) {
            AssetBase lastAsset = ip2baseMap.get(ip);
            String attributionGroup = lastAsset.getAttribution_group();
            String responsiblePerson = lastAsset.getResponsible_person();
            if (attributionGroup == null || "".equals(attributionGroup) || responsiblePerson == null || "".equals(responsiblePerson)) {
                out.collect(Tuple3.of(asset.getTaskID(), ip, AlarmItem.ABNORMAL_ASSET_NO_GROUP));
            }
        }
        //5.发现未知资产
        if (!accessAssets.contains(ip)) {
            out.collect(Tuple3.of(asset.getTaskID(), ip, AlarmItem.ABNORMAL_ASSET_UNKNOWN));
        }
        //6.异常进程信息
        if (processBlackList.contains(ip)) {
            out.collect(Tuple3.of(asset.getTaskID(), ip, AlarmItem.ABNORMAL_PROESS));
        }
    }

    @Override
    public void processBroadcastElement(AssetCdcResult value, Context ctx, Collector<Tuple3<String, String, AlarmItem>> out) throws Exception {
        BroadcastState<String, AssetBase> assetBaseBroadcastState = ctx.getBroadcastState(assetBaseMapStateDescriptor);

        AssetBase beforeAssetBase = value.getBeforeAssetBase();
        AssetBase afterAssetBase = value.getAfterAssetBase();
        String type = value.getType();
        if(assetBaseBroadcastState != null){
            String afterIp = afterAssetBase.getIps();
            String beforeIp = beforeAssetBase.getIps();
            if("insert".equals(type)){
                assetBaseBroadcastState.put(afterIp, afterAssetBase);
            }else if("update".equals(type)){
                assetBaseBroadcastState.remove(beforeIp);
                assetBaseBroadcastState.put(afterIp, afterAssetBase);
            }else {
                //delete
                assetBaseBroadcastState.remove(beforeIp);
            }
        }
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
}
