package com.brd.demo.sink;

import com.brd.asset.constants.AssetConstant;
import com.brd.asset.pojo.AssetScanOrigin;
import com.brd.demo.common.MysqlSinkUtil;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * @Author: leo.j
 * @desc:
 * @Date: 2022/4/1 1:51 下午
 */
public class AssetBaseSink {
    public static SinkFunction<Tuple2<AssetScanOrigin, String>> getInsertSink(){
        JdbcStatementBuilder<Tuple2<AssetScanOrigin, String>> statementBuilder = (statement, data) -> {
            statement.setString(1, data.f0.getDeviceIPAddress());
            statement.setString(2, data.f0.getDeviceIPAddress());
            statement.setString(3, data.f0.getOSInfo());
            statement.setString(4, String.valueOf(System.currentTimeMillis()));
            statement.setString(5, data.f1);
            statement.setString(6, data.f0.getDeviceIPAddress());
        };
        String sql = "insert into " + AssetConstant.TAB_ASSET_BASE + " (asset_id, ips, ip_address, os_info, collection_time, type) " +
                "values (?,?,?,?,?,?)";
        SinkFunction<Tuple2<AssetScanOrigin, String>> sink = JdbcSink.sink(sql, statementBuilder, MysqlSinkUtil.getJdbcExecutionOptions(), MysqlSinkUtil.getJdbcConnectionOptions());
        return sink;
    }

    public static SinkFunction<Tuple2<AssetScanOrigin, String>> getUpdateSink(){
        JdbcStatementBuilder<Tuple2<AssetScanOrigin, String>> statementBuilder = (statement, data) -> {
            statement.setString(1, data.f0.getDeviceIPAddress());
            statement.setString(2, data.f0.getDeviceIPAddress());
            statement.setString(3, data.f0.getOSInfo());
            statement.setString(4, String.valueOf(System.currentTimeMillis()));
            statement.setString(5, data.f1);
            statement.setString(6, data.f0.getDeviceIPAddress());
        };
        String sql = "update " + AssetConstant.TAB_ASSET_BASE + " set ips=?, ip_address=?, os_info=?, collection_time=?, type=? where ips=?";
        SinkFunction<Tuple2<AssetScanOrigin, String>> sink = JdbcSink.sink(sql, statementBuilder, MysqlSinkUtil.getJdbcExecutionOptions(), MysqlSinkUtil.getJdbcConnectionOptions());
        return sink;
    }
}
