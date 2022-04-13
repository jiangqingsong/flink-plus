package com.brd.demo.task;

import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @Author: leo.j
 * @desc: 模拟mysql数据存入state，实时处理分析
 * @Date: 2022/3/30 3:07 下午
 */
public class MockMysql2State {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        //protocol|status
        SingleOutputStreamOperator<Tuple2<String, Integer>> mysqlBinlogStream = env.socketTextStream("192.168.5.94", 7776)
                .map(text -> {
                    String[] split = text.trim().split(",");
                    return Tuple2.of(split[0].trim(), Integer.valueOf(split[1].trim()));
                });
        //主流数据
        SingleOutputStreamOperator<Tuple3<String, Integer, Long>> mainStream = env.socketTextStream("192.168.5.94", 7775)
                .map(text -> {
                    String[] split = text.trim().split(",");
                    return Tuple3.of(split[0].trim(), Integer.valueOf(split[1].trim()), Long.valueOf(split[2].trim()));
                });


        /**
         * MapStateDescriptor<String, Rule> ruleStateDescriptor = new MapStateDescriptor<>(
         * 			"RulesBroadcastState",
         * 			BasicTypeInfo.STRING_TYPE_INFO,
         * 			TypeInformation.of(new TypeHint<Rule>() {}));
         */
        MapStateDescriptor<String, Tuple2<String, Integer>> broadcastStateDesc
                = new MapStateDescriptor<String, Tuple2<String, Integer>>("mysqlBinlog", BasicTypeInfo.STRING_TYPE_INFO,
                TupleTypeInfo.of(new TypeHint<Tuple2<String, Integer>>() {}));
        BroadcastStream<Tuple2<String, Integer>> broadcastStream = mysqlBinlogStream.broadcast(broadcastStateDesc);

        mainStream.connect(broadcastStream).process(new BroadcastProcessFunction<Tuple3<String, Integer, Long>, Tuple2<String, Integer>, String>() {
            @Override
            public void processElement(Tuple3<String, Integer, Long> value, ReadOnlyContext ctx, Collector<String> out) throws Exception {

            }

            @Override
            public void processBroadcastElement(Tuple2<String, Integer> value, Context ctx, Collector<String> out) throws Exception {

            }

            private final MapStateDescriptor<String, Tuple2<String, Integer>> broadcastStateDesc
                    = new MapStateDescriptor<String, Tuple2<String, Integer>>("mysqlBinlog", BasicTypeInfo.STRING_TYPE_INFO,
                    TupleTypeInfo.of(new TypeHint<Tuple2<String, Integer>>() {}));
        });


    }

}
