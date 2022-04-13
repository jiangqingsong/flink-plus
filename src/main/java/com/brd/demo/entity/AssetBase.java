package com.brd.demo.entity;

import lombok.Data;

/**
 * @Author: leo.j
 * @desc: 资产基础表
 * @Date: 2022/4/1 9:56 上午
 */
@Data
public class AssetBase {
    public String asset_id;
    public String name;
    public String ips;
    public String ip_address;
    public String mac;
    public String type;
    public String os_info;
    public String physical_address;
    public String attribution_group;
    public String collection_time;
    public String engine_room;
    public String engine_box;
    public String manufacturers;
    public String sn;
    public String model;
    public String business_system;
    public String responsible_person;
    public String telephone;
    public String asset_desc;
    public String confidentiality;
    public String integrality;
    public String usability;
    public String importance;
    public String is_permitted;
    public String backup_field_1;
    public String backup_field_2;
    public String backup_field_3;
    public String asset_ip;
    public String longitude;
    public String latitude;
    public String responsible_unit;
    public String responsible_department;
    public String responsible_group;
    public String asset_source_time;
    public String access;

    public AssetBase() {
    }


}
