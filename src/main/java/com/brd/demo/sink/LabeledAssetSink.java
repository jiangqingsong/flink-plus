package com.brd.demo.sink;

import com.alibaba.fastjson.JSON;
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
 * @Date: 2022/4/1 10:46 上午
 */
public class LabeledAssetSink {
    public static SinkFunction<Tuple2<AssetScanOrigin, String>> getSink(){
        String saveWideOriginSql = "insert into  " + AssetConstant.TAB_NAME_LABEL + "(resource_name,task_id,scan_time," +
                "device_ip_address,ip_address_ownership,device_type,os_info,system_fingerprint_info," +
                "open_service_of_port,message_oriented_middleware,data_base_info,running," +
                "label_id,collect_type)" +
                "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
        JdbcStatementBuilder<Tuple2<AssetScanOrigin, String>> statementBuilder3 = (statement, data) -> {
            statement.setString(1, data.f0.getResourceName());
            statement.setString(2, data.f0.getTaskID());
            statement.setString(3, data.f0.getScanTime());
            statement.setString(4, data.f0.getDeviceIPAddress());
            statement.setString(5, data.f0.getIPAddressOwnership());
            statement.setString(6, data.f0.getDeviceType());
            statement.setString(7, data.f0.getOSInfo());//String
            statement.setString(8, data.f0.getSystemFingerprintInfo());
            statement.setString(9, JSON.toJSONString(data.f0.getOpenServiceOfPort()));//String
            statement.setString(10, JSON.toJSONString(data.f0.getMessageOrientedMiddleware()));
            statement.setString(11, JSON.toJSONString(data.f0.getDataBaseInfos()));//Integer
            statement.setString(12, data.f0.getRuning());
            statement.setString(13, data.f1);
            statement.setInt(14, AssetConstant.SCAN_COLLECT);
        };

        SinkFunction<Tuple2<AssetScanOrigin, String>> sink = JdbcSink.sink(saveWideOriginSql, statementBuilder3, MysqlSinkUtil.getJdbcExecutionOptions(), MysqlSinkUtil.getJdbcConnectionOptions());
        return sink;
    }
}
