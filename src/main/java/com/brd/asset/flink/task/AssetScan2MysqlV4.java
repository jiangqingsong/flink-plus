package com.brd.asset.flink.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.ververica.cdc.connectors.mysql.MySQLSource;
import com.alibaba.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.brd.asset.common.FlinkUtils;
import com.brd.asset.constants.AlarmItem;
import com.brd.asset.constants.ScanCollectConstant;
import com.brd.asset.flink.fun.LabeledMap;
import com.brd.asset.flink.fun.Origin2AssetScan;
import com.brd.asset.pojo.AssetScanOrigin;
import com.brd.demo.entity.AssetBase;
import com.brd.demo.entity.AssetCdcResult;
import com.brd.demo.function.AbnormalAndLabelProcess;
import com.brd.demo.function.AssetBaseDeserializationSchemaFunction;
import com.brd.demo.sink.AbnormalAssetSink;
import com.brd.demo.sink.AssetBaseSink;
import com.brd.demo.sink.LabeledAssetSink;
import com.brd.demo.sink.TaskEndSink;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

/**
 * @Author: leo.j
 * @desc:
 * @Date: 2022/3/25 10:52 上午
 */
public class AssetScan2MysqlV4 {
    public static void main(String[] args) throws Exception {
        ParameterTool parameterTool = ParameterTool.fromArgs(args);


        String jaasConfig = parameterTool.get("jaasConfig");
        //String propPath = parameterTool.get("conf_path");
        String propPath = "/Users/mac/dev/brd_2021/flink-plus/src/main/resources/asset_scan_cfg.properties";
        //获取配置数据
        ParameterTool paramFromProps = ParameterTool.fromPropertiesFile(propPath);
        String taskName = paramFromProps.get("task.name");
        String consumerTopic = paramFromProps.get("consumer.topic");
        String groupId = paramFromProps.get("consumer.groupId");
        Long timeout = paramFromProps.getLong("timeout");
        //sink mysql config
        String jdbcUrl = paramFromProps.get("jdbcUrl");
        String userName = paramFromProps.get("userName");
        String password = paramFromProps.get("password");
        //ip数据库地址
        String locationPath = paramFromProps.get("locationPath");

        Integer openPortThreshold = paramFromProps.getInt("openPortThreshold");
        String processBlackList = paramFromProps.get("processBlackList");


        //todo  做checkpoint&&stateBackend
        final StreamExecutionEnvironment env = FlinkUtils.getEnv();
        //将 ParameterTool 注册为全作业参数的参数
        env.getConfig().setGlobalJobParameters(ParameterTool.fromArgs(args));
        //用socket测试
        //todo socket 改 kafka
        DataStreamSource<String> socketTextStream = env.socketTextStream("192.168.5.94", 7776);
        socketTextStream.print();

        final OutputTag<JSONObject> taskEndTag = new OutputTag<JSONObject>("task-end") {
        };
        SingleOutputStreamOperator<JSONObject> signedDS = socketTextStream.process(new ProcessFunction<String, JSONObject>() {
            @Override
            public void processElement(String value, Context ctx, Collector<JSONObject> out) throws Exception {
                JSONObject originObject = JSON.parseObject(value);
                //resource info
                JSONObject resourceObj = JSON.parseObject(originObject.get(ScanCollectConstant.RESOURCE_INFO).toString());
                if (resourceObj.size() == 0) {
                    ctx.output(taskEndTag, originObject);
                } else {
                    out.collect(originObject);
                }
            }
        });
        DataStream<JSONObject> taskEndStream = signedDS.getSideOutput(taskEndTag);
        //任务结束更新状态
        SingleOutputStreamOperator<String> taskIdEndStream = taskEndStream.map(data -> data.getString(ScanCollectConstant.TASK_ID));
        taskIdEndStream.print();
        taskIdEndStream.addSink(TaskEndSink.getSink());

        //flink cdc 同步asset_base数据
        SourceFunction<AssetCdcResult> assetBaseFunction = MySQLSource.<AssetCdcResult>builder()
                .hostname("localhost")
                .port(3306)
                .databaseList("test")
                .tableList("test.assets_base")
                .username("root")
                .password("123456")
                .startupOptions(StartupOptions.initial())
                .deserializer(new AssetBaseDeserializationSchemaFunction())
                .build();

        //3.2 获取资产基础表数据 全量+增量
        DataStreamSource<AssetCdcResult> assetBaseDS = env.addSource(assetBaseFunction);
        //assetBaseDS.print("flink cdc: ");
        //assetBaseDS做广播状态 assetBaseStateDescriptor
        final MapStateDescriptor<String, AssetBase> assetBaseMapStateDescriptor = new MapStateDescriptor<>(
                "assetBaseState", BasicTypeInfo.STRING_TYPE_INFO, TypeInformation.of(new TypeHint<AssetBase>() {
        }));
        BroadcastStream<AssetCdcResult> assetBaseBroadcastDS = assetBaseDS.broadcast(assetBaseMapStateDescriptor);

        //json转AssetScan对象
        SingleOutputStreamOperator<AssetScanOrigin> assetOriginStream = signedDS.map(new Origin2AssetScan(locationPath));

        //拉宽原始数据入库
        SingleOutputStreamOperator<Tuple2<AssetScanOrigin, String>> labeledAssetStream = assetOriginStream.map(new LabeledMap(jdbcUrl, userName, password));
        labeledAssetStream.addSink(LabeledAssetSink.getSink());
        //宽表和asset_base表关联并分流：异常资产&&更新asset_base表
        SingleOutputStreamOperator<Tuple3<String, String, AlarmItem>> abnormalAndLabeledDS = labeledAssetStream.connect(assetBaseBroadcastDS)
                .process(new AbnormalAndLabelProcess(assetBaseMapStateDescriptor, openPortThreshold, processBlackList));
        //异常资产告警入库
        abnormalAndLabeledDS.addSink(AbnormalAssetSink.getSink());

        //更新asset_base表
        final OutputTag<Tuple2<AssetScanOrigin, String>> insertTag = new OutputTag<Tuple2<AssetScanOrigin, String>>("insert-tag") {};
        final OutputTag<Tuple2<AssetScanOrigin, String>> updateTag = new OutputTag<Tuple2<AssetScanOrigin, String>>("update-tag") {};
        DataStream<Tuple2<AssetScanOrigin, String>> insertAssetBaseDS = abnormalAndLabeledDS.getSideOutput(insertTag);
        DataStream<Tuple2<AssetScanOrigin, String>> updateAssetBaseDS = abnormalAndLabeledDS.getSideOutput(updateTag);
        insertAssetBaseDS.addSink(AssetBaseSink.getInsertSink());
        updateAssetBaseDS.addSink(AssetBaseSink.getUpdateSink());

        env.execute(taskName);
    }

}
