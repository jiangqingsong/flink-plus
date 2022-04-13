package com.brd.demo.task;

/**
 * Copyright (c) 2020-2030 尚硅谷 All Rights Reserved
 * <p>
 * Project:  FlinkTutorial
 * <p>
 * Created by  wushengran
 */

import com.brd.demo.entity.EvenLog;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.time.Duration;


public class ProcessLateDataExample {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 读取socket文本流
        SingleOutputStreamOperator<EvenLog> stream =
                env.socketTextStream("192.168.5.94", 7777)
                        .map(new MapFunction<String, EvenLog>() {
                            @Override
                            public EvenLog map(String value) throws Exception {
                                String[] fields = value.split(",");
                                return new EvenLog(fields[0].trim(), Double.valueOf(fields[1].trim()), Long.valueOf(fields[2].trim()));
                            }
                        })
                        // 方式一：设置watermark延迟时间
                        .assignTimestampsAndWatermarks(WatermarkStrategy.<EvenLog>forBoundedOutOfOrderness(Duration.ZERO)
                                .withTimestampAssigner(new SerializableTimestampAssigner<EvenLog>() {
                                    @Override
                                    public long extractTimestamp(EvenLog element, long recordTimestamp) {
                                        return element.time;
                                    }
                                }));
        // 定义侧输出流标签
        OutputTag<EvenLog> outputTag = new OutputTag<EvenLog>("late") {};

        SingleOutputStreamOperator<Tuple2<String, String>> result = stream.keyBy(data -> data.protocol)
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                .allowedLateness(Time.seconds(2))
                .sideOutputLateData(outputTag)
                .aggregate(new AggregateFunction<EvenLog, Double, Double>() {
                    @Override
                    public Double createAccumulator() {
                        return 0.0;
                    }

                    @Override
                    public Double add(EvenLog value, Double accumulator) {
                        return accumulator + value.getTraffic();
                    }

                    @Override
                    public Double getResult(Double accumulator) {
                        return accumulator;
                    }

                    @Override
                    public Double merge(Double a, Double b) {
                        return null;
                    }
                }, new ProcessWindowFunction<Double, Tuple2<String, String>, String, TimeWindow>() {
                    @Override
                    public void process(String key, Context context, Iterable<Double> elements, Collector<Tuple2<String, String>> out) throws Exception {
                        long start = context.window().getStart();
                        long end = context.window().getEnd();
                        out.collect(Tuple2.of(key, start + "~" + end + ": " + elements.iterator().next()));
                    }
                });


        result.getSideOutput(outputTag).print("late");

        result.print("AGG");
        // 为方便观察，可以将原始数据也输出
        stream.print("input");

        env.execute();
    }

}

