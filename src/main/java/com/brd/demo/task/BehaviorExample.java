package com.brd.demo.task;

import com.brd.demo.entity.EvenLog;
import com.brd.demo.entity.LoginEvent;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.RichIterativeCondition;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Author: leo.j
 * @desc:
 * @Date: 2022/4/8 11:06 上午
 */
public class BehaviorExample {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<LoginEvent> streamSource = env.fromElements(
                new LoginEvent("001", "TMD", 1000L),
                new LoginEvent("002", "hehe", 2000L),
                new LoginEvent("001", "hehe", 3000L),
                new LoginEvent("002", "xixi", 4000L),
                new LoginEvent("001", "TMD", 4100L),
                new LoginEvent("001", "TMD", 5000L)
        ).assignTimestampsAndWatermarks(WatermarkStrategy.<LoginEvent>forBoundedOutOfOrderness(Duration.ZERO)
                        .withTimestampAssigner(new SerializableTimestampAssigner<LoginEvent>() {
                            @Override
                            public long extractTimestamp(LoginEvent element, long recordTimestamp) {
                                return element.timestamp;
                            }
                        }));


        Pattern<LoginEvent, LoginEvent> pattern = Pattern.<LoginEvent>begin("start").where(new SimpleCondition<LoginEvent>() {
            @Override
            public boolean filter(LoginEvent value) throws Exception {
                return value.message.contains("TMD");
            }
        }).times(3).within(Time.seconds(5));
        PatternStream<LoginEvent> patternStream = CEP.pattern(streamSource.keyBy(data -> data.userId), pattern);
        patternStream.process(new PatternProcessFunction<LoginEvent, String>() {
            @Override
            public void processMatch(Map<String, List<LoginEvent>> match, Context ctx, Collector<String> out) throws Exception {
                out.collect(Arrays.toString(match.get("start").toArray()));
            }
        }).print("不雅信息：");


        Pattern<LoginEvent, LoginEvent> pattern1 = Pattern.<LoginEvent>begin("start").where(new SimpleCondition<LoginEvent>() {
            @Override
            public boolean filter(LoginEvent value) throws Exception {
                return value != null;
            }
        }).next("second").where(new RichIterativeCondition<LoginEvent>() {
            @Override
            public boolean filter(LoginEvent value, Context<LoginEvent> ctx) throws Exception {
                LoginEvent startEvent = ctx.getEventsForPattern("start").iterator().next();
                if (!value.message.equals(startEvent.message)) {
                    return true;
                }
                return false;
            }
        }).within(Time.seconds(2));

        PatternStream<LoginEvent> patternStream1 = CEP.pattern(streamSource.keyBy(data -> data.userId), pattern1);
        patternStream1.select(new PatternSelectFunction<LoginEvent, String>() {
            @Override
            public String select(Map<String, List<LoginEvent>> pattern) throws Exception {
                LoginEvent startEvent = pattern.get("start").get(0);
                LoginEvent secondEvent = pattern.get("second").get(0);
                return "diff: " + startEvent.toString() + " | " + secondEvent.toString();
            }
        }).print("diff msg");

        env.execute("发言监控");

    }

}
