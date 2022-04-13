package com.brd.demo.sink;

import com.brd.asset.common.TimeUtils;
import com.brd.asset.constants.AlarmItem;
import com.brd.asset.constants.AssetConstant;
import com.brd.demo.common.MysqlSinkUtil;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.util.UUID;

/**
 * @Author: leo.j
 * @desc:
 * @Date: 2022/4/1 10:43 上午
 */
public class AbnormalAssetSink {
    public static SinkFunction<Tuple3<String, String, AlarmItem>> getSink(){
        JdbcStatementBuilder<Tuple3<String, String, AlarmItem>> statementBuilder2 = (statement, data) -> {
            statement.setString(1, UUID.randomUUID().toString().replaceAll("-", ""));
            statement.setString(2, "异常资产");
            statement.setString(3, data.f2.getEventId());
            statement.setString(4, "III");
            statement.setString(5, TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
            statement.setString(6, "1");
            statement.setString(7, data.f1);
            statement.setString(8, data.f1);
            statement.setString(9, data.f2.getEventDesc());
            statement.setString(10, data.f0);
        };
        String saveAlarmSql = "insert into " + AssetConstant.TAB_ALARMS + " (event_id, event_title, event_type, event_level," +
                "event_time, event_count, event_target_ip, event_affected_dev, event_description, task_id) values " +
                "(?,?,?,?,?,?,?,?,?,?)";
        SinkFunction<Tuple3<String, String, AlarmItem>> sink = JdbcSink.sink(saveAlarmSql, statementBuilder2, MysqlSinkUtil.getJdbcExecutionOptions(), MysqlSinkUtil.getJdbcConnectionOptions());
        return sink;
    }
}
