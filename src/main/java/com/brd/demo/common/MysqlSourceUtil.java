package com.brd.demo.common;

import com.alibaba.ververica.cdc.connectors.mysql.MySQLSource;
import com.alibaba.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.alibaba.ververica.cdc.debezium.DebeziumDeserializationSchema;
import com.brd.demo.function.MyDeserializationSchemaFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

/**
 * @Author: leo.j
 * @desc:
 * @Date: 2022/4/1 1:10 下午
 */
public class MysqlSourceUtil<T> {
    public SourceFunction getSourceFunction(){
        SourceFunction sourceFunction = new Builder(new MyDeserializationSchemaFunction()).build();
        return sourceFunction;
    }

    public static class Builder<T>{
        private DebeziumDeserializationSchema debeziumDeserializationSchema;

        public Builder(DebeziumDeserializationSchema debeziumDeserializationSchema) {
            this.debeziumDeserializationSchema = debeziumDeserializationSchema;
        }

        public SourceFunction<T> build(){
            return MySQLSource.<T>builder()
                    .hostname("192.168.5.93")
                    .port(3306)
                    .databaseList("smop-sdc")
                    .tableList("smop-sdc.asset_base")
                    .username("root")
                    .password("root")
                    .startupOptions(StartupOptions.initial())
                    .deserializer(debeziumDeserializationSchema)
                    .build();
        }
    }
}
