package com.brd.demo.common;

import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;

/**
 * @Author: leo.j
 * @desc:
 * @Date: 2022/4/1 10:39 上午
 */
public class MysqlSinkUtil {
    /**
     * 获取连接配置
     * @return
     */
    public static JdbcConnectionOptions getJdbcConnectionOptions(){
        JdbcConnectionOptions connectionOptions = new JdbcConnectionOptions
                .JdbcConnectionOptionsBuilder()
                .withUrl("jdbc:mysql://localhost:3306/test?characterEncoding=utf8&autoReconnect=true&useSSL=false")
                .withDriverName("com.mysql.cj.jdbc.Driver")
                .withUsername("root")
                .withPassword("123456")
                .withConnectionCheckTimeoutSeconds(30) // 连接超时时间 单位：秒
                .build();
        return connectionOptions;
    }

    /**
     * 获取执行配置
     * @return
     */
    public static JdbcExecutionOptions getJdbcExecutionOptions(){
        JdbcExecutionOptions executionOptions = new JdbcExecutionOptions.Builder()
                .withBatchSize(10)
                .withBatchIntervalMs(200)
                .withMaxRetries(5)
                .build();
        return executionOptions;
    }
}
