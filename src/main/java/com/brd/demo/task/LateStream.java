package com.brd.demo.task;

import com.brd.demo.entity.EvenLog;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * @Author: leo.j
 * @desc:
 * @Date: 2022/3/29 1:43 下午
 */
public class LateStream {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);
        //eg: http,336.6,1000
        DataStreamSource<String> streamSource = env.socketTextStream("192.168.5.94", 7777);
        streamSource.print();
        SingleOutputStreamOperator<EvenLog> eventLogStream = streamSource.map(new MapFunction<String, EvenLog>() {
            @Override
            public EvenLog map(String value) throws Exception {
                String[] split = value.split(",");
                return new EvenLog(split[0].trim(), Double.valueOf(split[1].trim()), Long.valueOf(split[2].trim()));
            }
        }).assignTimestampsAndWatermarks(WatermarkStrategy.<EvenLog>forBoundedOutOfOrderness(Duration.ofSeconds(2))
                .withTimestampAssigner(new SerializableTimestampAssigner<EvenLog>() {
                    @Override
                    public long extractTimestamp(EvenLog element, long recordTimestamp) {
                        return element.time;
                    }
                }));



        SingleOutputStreamOperator<Double> aggregate = eventLogStream.keyBy(e -> e.protocol)
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                .aggregate(new AggregateFunction<EvenLog, Double, Double>() {
                    @Override
                    public Double createAccumulator() {
                        return 0.0;
                    }

                    @Override
                    public Double add(EvenLog evenLog, Double aDouble) {
                        return evenLog.getTraffic() + aDouble;
                    }

                    @Override
                    public Double getResult(Double aDouble) {
                        return aDouble;
                    }

                    @Override
                    public Double merge(Double aDouble, Double acc1) {
                        return null;
                    }
                });
        aggregate.print();

        env.execute("test late stream.");

    }

}
