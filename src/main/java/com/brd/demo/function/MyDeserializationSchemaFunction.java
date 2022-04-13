package com.brd.demo.function;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.ververica.cdc.debezium.DebeziumDeserializationSchema;
import io.debezium.data.Envelope;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

/**
 * @Author: leo.j
 * @desc:
 * @Date: 2022/3/30 4:47 下午
 */
public class MyDeserializationSchemaFunction implements DebeziumDeserializationSchema<String> {
    @Override
    public void deserialize(SourceRecord sourceRecord, Collector<String> collector) throws Exception {
        Struct valueStruct = (Struct) sourceRecord.value();
        Struct sourceStrut = valueStruct.getStruct("source");
        //获取数据库的名称
        String database = sourceStrut.getString("db");
        //获取表名
        String table = sourceStrut.getString("table");

        //获取类型（c-->insert,u-->update）
        String type = Envelope.operationFor(sourceRecord).toString().toLowerCase();
        if ("create".equals(type)) {
            type = "insert";
        }
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("database", database);
        jsonObj.put("table", table);
        jsonObj.put("type", type);

        //获取数据data
        Struct afterStruct = valueStruct.getStruct("after");
        JSONObject afterDataJsonObj = new JSONObject();
        JSONObject beforeDataJsonObj = new JSONObject();

        if (afterStruct != null) {
            for (Field field : afterStruct.schema().fields()) {
                String fieldName = field.name();
                Object fieldValue = afterStruct.get(field);
                afterDataJsonObj.put(fieldName, fieldValue);
            }
        }

        Struct beforeStruct = valueStruct.getStruct("before");
        if (beforeStruct != null) {
            for (Field field : beforeStruct.schema().fields()) {
                String fieldName = field.name();
                Object fieldValue = beforeStruct.get(field);
                beforeDataJsonObj.put(fieldName, fieldValue);
            }
        }
        jsonObj.put("afterData", afterDataJsonObj);
        jsonObj.put("beforeData", beforeDataJsonObj);

        //向下游传递数据
        collector.collect(jsonObj.toJSONString());

    }

    @Override
    public TypeInformation<String> getProducedType() {
        return TypeInformation.of(String.class);
    }
}
