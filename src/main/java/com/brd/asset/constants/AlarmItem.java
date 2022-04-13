package com.brd.asset.constants;

/**
 * @Author: leo.j
 * @desc: 异常资产告警枚举类
 * @Date: 2021/12/24 2:47 下午
 */
public enum AlarmItem {
    ABNORMAL_OPEN_PORT("caeb35e1f83740cdae23256c43993ce3", "开放了超过阈值数量的端口/服务"),
    ABNORMAL_ASSET_CHANGE("4e9c17e2ce384e62b6c9513363734c03", "疑似资产被替换"),
    ABNORMAL_ASSET_DOWN("c95a6d62ef3d43b8995258375fce1d4f", "原有IP消失问题"),
    ABNORMAL_ASSET_NO_GROUP("20f75493ed6545599b6243e05acb8e64", "资产未配置所属单位、责任人"),
    ABNORMAL_ASSET_UNKNOWN("0340328f7c3349298f723391676d0b04", "未在纳管范围内却已经在线联网的资产"),
    ABNORMAL_PROESS("8e498d7349094e49a245c33ca1139471", "由后门、木马、病毒等恶意程序引起的端口进程异常变化");
    //事件ID
    private String eventId;
    //事件描述
    private String eventDesc;

    AlarmItem(String eventId, String eventDesc) {
        this.eventId = eventId;
        this.eventDesc = eventDesc;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventDesc() {
        return eventDesc;
    }
}
