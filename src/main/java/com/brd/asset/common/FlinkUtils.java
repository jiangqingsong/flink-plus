package com.brd.asset.common;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.concurrent.TimeUnit;

/**
 * @Author: leo.j
 * @desc:
 * @Date: 2022/3/25 10:54 上午
 */
public class FlinkUtils {

    private static final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    public static StreamExecutionEnvironment getEnv(){
        env.enableCheckpointing(1 * 60 * 1000);
        //设置模式为：exactly_one，仅一次语义
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        //确保检查点之间有1s的时间间隔【checkpoint最小间隔】
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(1000);
        //检查点必须在10s之内完成，或者被丢弃【checkpoint超时时间】
        env.getCheckpointConfig().setCheckpointTimeout(10000);
        //同一时间只允许进行一次检查点
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
        env.setRestartStrategy(RestartStrategies.failureRateRestart(
                3,
                //一个时间段内的最大失败次数
                Time.of(1, TimeUnit.MINUTES), // 衡量失败次数的是时间段
                Time.of(3, TimeUnit.SECONDS) // 间隔
        ));
        return env;
    }
}
