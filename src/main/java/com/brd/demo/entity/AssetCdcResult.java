package com.brd.demo.entity;

import lombok.Data;

/**
 * @Author: leo.j
 * @desc: flink CDC 同步过来数据包装
 * @Date: 2022/4/1 10:15 上午
 */
@Data
public class AssetCdcResult {
    public String database;
    public String table;
    public String type;
    public AssetBase beforeAssetBase;
    public AssetBase afterAssetBase;

    public AssetCdcResult() {
    }
}
