package com.brd.asset.pojo;

/**
 * @Author: leo.j
 * @desc:
 * @Date: 2021/12/23 1:38 下午
 */
public class AssetBase {
    private String asset_id;
    private String name;
    private String ips;
    private String ip_address;
    private String mac;
    private String type;
    private String os_info;
    private String physical_address;
    private String attribution_group;
    private String collection_time;
    private String engine_room;
    private String engine_box;
    private String manufacturers;
    private String sn;
    private String model;
    private String business_system;
    private String responsible_person;
    private String telephone;
    private String asset_desc;
    private String confidentiality;
    private String integrality;
    private String usability;
    private String importance;
    private String is_permitted;
    private String backup_field_1;
    private String backup_field_2;
    private String backup_field_3;
    private String asset_ip;
    private String longitude;
    private String latitude;
    private String responsible_unit;
    private String responsible_department;
    private String responsible_group;
    private String asset_source_time;
    private String access;

    public AssetBase(String asset_id, String name, String ips, String ip_address, String mac, String type, String os_info, String physical_address, String attribution_group, String collection_time, String engine_room, String engine_box, String manufacturers, String sn, String model, String business_system, String responsible_person, String telephone, String asset_desc, String confidentiality, String integrality, String usability, String importance, String is_permitted, String backup_field_1, String backup_field_2, String backup_field_3, String asset_ip, String longitude, String latitude, String responsible_unit, String responsible_department, String responsible_group, String asset_source_time, String access) {
        this.asset_id = asset_id;
        this.name = name;
        this.ips = ips;
        this.ip_address = ip_address;
        this.mac = mac;
        this.type = type;
        this.os_info = os_info;
        this.physical_address = physical_address;
        this.attribution_group = attribution_group;
        this.collection_time = collection_time;
        this.engine_room = engine_room;
        this.engine_box = engine_box;
        this.manufacturers = manufacturers;
        this.sn = sn;
        this.model = model;
        this.business_system = business_system;
        this.responsible_person = responsible_person;
        this.telephone = telephone;
        this.asset_desc = asset_desc;
        this.confidentiality = confidentiality;
        this.integrality = integrality;
        this.usability = usability;
        this.importance = importance;
        this.is_permitted = is_permitted;
        this.backup_field_1 = backup_field_1;
        this.backup_field_2 = backup_field_2;
        this.backup_field_3 = backup_field_3;
        this.asset_ip = asset_ip;
        this.longitude = longitude;
        this.latitude = latitude;
        this.responsible_unit = responsible_unit;
        this.responsible_department = responsible_department;
        this.responsible_group = responsible_group;
        this.asset_source_time = asset_source_time;
        this.access = access;
    }

    public String getAsset_id() {
        return asset_id;
    }

    public String getName() {
        return name;
    }

    public String getIps() {
        return ips;
    }

    public String getIp_address() {
        return ip_address;
    }

    public String getMac() {
        return mac;
    }

    public String getType() {
        return type;
    }

    public String getPhysical_address() {
        return physical_address;
    }

    public String getAttribution_group() {
        return attribution_group;
    }

    public String getCollection_time() {
        return collection_time;
    }

    public String getEngine_room() {
        return engine_room;
    }

    public String getEngine_box() {
        return engine_box;
    }

    public String getManufacturers() {
        return manufacturers;
    }

    public String getSn() {
        return sn;
    }

    public String getModel() {
        return model;
    }

    public String getBusiness_system() {
        return business_system;
    }

    public String getResponsible_person() {
        return responsible_person;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getAsset_desc() {
        return asset_desc;
    }

    public String getConfidentiality() {
        return confidentiality;
    }

    public String getIntegrality() {
        return integrality;
    }

    public String getUsability() {
        return usability;
    }

    public String getImportance() {
        return importance;
    }

    public String getIs_permitted() {
        return is_permitted;
    }

    public String getBackup_field_1() {
        return backup_field_1;
    }

    public String getBackup_field_2() {
        return backup_field_2;
    }

    public String getBackup_field_3() {
        return backup_field_3;
    }

    public String getAsset_ip() {
        return asset_ip;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getResponsible_unit() {
        return responsible_unit;
    }

    public String getResponsible_department() {
        return responsible_department;
    }

    public String getResponsible_group() {
        return responsible_group;
    }

    public String getAsset_source_time() {
        return asset_source_time;
    }

    public String getAccess() {
        return access;
    }

    public String getOs_info() {
        return os_info;
    }
}
