package com.brd.demo;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.ververica.cdc.connectors.mysql.MySQLSource;
import com.alibaba.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.brd.demo.function.MyDeserializationSchemaFunction;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.apache.hadoop.yarn.webapp.hamlet2.Hamlet;

/**
 * @Author: leo.j
 * @desc: flink CDC 实时读取mysql增量数据
 * @Date: 2022/3/30 4:47 下午
 */
public class FlinkCdcForMysql {
    public static void main(String[] args) throws Exception {

        //TODO 1.基础环境
        //1.1流处理执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2设置并行度
        env.setParallelism(1);//设置并行度为1方便测试

        /*//TODO 2.检查点配置
        //2.1 开启检查点
        env.enableCheckpointing(5000L, CheckpointingMode.EXACTLY_ONCE);//5秒执行一次，模式：精准一次性
        //2.2 设置检查点超时时间
        env.getCheckpointConfig().setCheckpointTimeout(60*1000);
        //2.3 设置重启策略
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(2, 2*1000));//两次，两秒执行一次
        //2.4 设置job取消后检查点是否保留
        env.getCheckpointConfig().enableExternalizedCheckpoints(
                CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);//保留
        //2.5 设置状态后端-->保存到hdfs
        env.setStateBackend(new FsStateBackend("hdfs://192.168.231.121:8020/ck"));
        //2.6 指定操作hdfs的用户
        System.setProperty("HADOOP_USER_NAME", "gaogc");*/


        /**
         * text -> {
         *                     String[] split = text.trim().split(",");
         *                     return Tuple2.of(split[0].trim(), Integer.valueOf(split[1].trim()));
         *                 }
         */

        final OutputTag<String> errorData = new OutputTag<String>("error-data"){};
        SingleOutputStreamOperator<Tuple2<String, Integer>> socketDS = env.socketTextStream("192.168.5.94", 7776)
                .process(new ProcessFunction<String, Tuple2<String, Integer>>() {
                    @Override
                    public void processElement(String value, Context ctx, Collector<Tuple2<String, Integer>> out) throws Exception {
                        try {

                            String[] split = value.trim().split(",");
                            Tuple2<String, Integer> tuple2 = Tuple2.of(split[0].trim(), Integer.valueOf(split[1].trim()));
                            out.collect(tuple2);
                        }catch (Exception e){
                            ctx.output(errorData, value);
                        }
                    }
                }).returns(Types.TUPLE(Types.STRING, Types.INT));
        socketDS.getSideOutput(errorData).print("errorData: ");
        //3.1 创建MySQLSource
        SourceFunction<String> sourceFunction = MySQLSource.<String>builder()
                .hostname("localhost")
                .port(3306)
                .databaseList("test")//库
                .tableList("test.user")//表
                .username("root")
                .password("123456")
                .startupOptions(StartupOptions.initial())//启动的时候从第一次开始读取
                .deserializer(new MyDeserializationSchemaFunction())//这里使用自定义的反序列化器将数据封装成json格式
                .build();

        //3.2 从源端获取数据
        DataStreamSource<String> userBaseDS = env.addSource(sourceFunction);
        userBaseDS.print();
        MapStateDescriptor<String, String> userMapStateDesc = new MapStateDescriptor<>("userMap", BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.STRING_TYPE_INFO);
        BroadcastStream<String> userBroadcast = userBaseDS.broadcast(userMapStateDesc);

        socketDS.connect(userBroadcast).process(new BroadcastProcessFunction<Tuple2<String, Integer>, String, String>() {
            private MapStateDescriptor<String, String> userBaseStateDesc = new MapStateDescriptor<>("userMap", BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.STRING_TYPE_INFO);
            @Override
            public void processElement(Tuple2<String, Integer> value, ReadOnlyContext ctx, Collector<String> out) throws Exception {
                String name = value.f0;
                String info = ctx.getBroadcastState(userBaseStateDesc).get(name);
                if(info != null && JSONObject.parseObject(info).getString("name").equals(name) ){
                    out.collect(name + ": 信息完全！");
                }
            }

            @Override
            public void processBroadcastElement(String value, Context ctx, Collector<String> out) throws Exception {
                if(value != null){
                    BroadcastState<String, String> broadcastState = ctx.getBroadcastState(userBaseStateDesc);
                    JSONObject object = JSONObject.parseObject(value);
                    String before = object.getString("beforeData");
                    String after = object.getString("afterData");
                    String type = object.getString("type");
                    JSONObject afterUserInfo = JSONObject.parseObject(after);
                    String afterName = afterUserInfo.getString("name");
                    JSONObject beforeUserInfo = JSONObject.parseObject(before);
                    String beforeName = beforeUserInfo.getString("name");

                    //判断type，删除||修改都要处理状态
                    if("update".equals(type)){
                        broadcastState.remove(beforeName);
                        broadcastState.put(afterName, afterUserInfo.toJSONString());
                    }else if("delete".equals(type)) {
                        broadcastState.remove(beforeName);
                    }else {
                        broadcastState.put(afterName, afterUserInfo.toJSONString());
                    }
                }
            }
        }).print("connected ds");
        //打印测试

        //执行
        env.execute();
    }
}
