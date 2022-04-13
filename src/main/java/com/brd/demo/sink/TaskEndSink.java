package com.brd.demo.sink;

import com.brd.asset.common.TimeUtils;
import com.brd.asset.constants.AssetConstant;
import com.brd.demo.common.MysqlSinkUtil;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * @Author: leo.j
 * @desc:
 * @Date: 2022/4/1 10:36 上午
 */
public class TaskEndSink {
    /**
     * 资产结束数据Sink
     * @return
     */
    public static SinkFunction<String> getSink(){
        JdbcStatementBuilder<String> statementBuilder1 = (statement, taskId) -> {
            statement.setInt(1, 2);
            statement.setString(2, TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
            statement.setString(3, taskId);
        };
        String taskEndSql = "update " + AssetConstant.ASSET_TASK_INFO_TAB + " set task_state=?, finish_time=? where task_id=?;";
        SinkFunction<String> sink = JdbcSink.sink(taskEndSql, statementBuilder1, MysqlSinkUtil.getJdbcExecutionOptions(), MysqlSinkUtil.getJdbcConnectionOptions());
        return sink;
    }
}
