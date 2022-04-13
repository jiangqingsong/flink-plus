package com.brd.asset.pojo;

import java.util.List;

/**
 * @author leo.J
 * @description 远程扫描采集原始数据
 * @date 2020-06-09 13:48
 */
public class AssetScanOrigin {
    public String ResourceName;
    public String TaskID;
    public String ScanTime;
    public String DeviceIPAddress;
    public String IPAddressOwnership;
    public String DeviceType;
    public String OSInfo;
    public String SystemFingerprintInfo;
    public List<OpenServiceOfPort> OpenServiceOfPort;
    public List<MessageOrientedMiddlewareScan> MessageOrientedMiddleware;
    public List<DataBaseInfoScan> dataBaseInfos;
    public String Runing;

    public AssetScanOrigin() {
    }

    public String getResourceName() {
        return ResourceName;
    }

    public void setResourceName(String resourceName) {
        ResourceName = resourceName;
    }

    public String getTaskID() {
        return TaskID;
    }

    public void setTaskID(String taskID) {
        TaskID = taskID;
    }

    public String getScanTime() {
        return ScanTime;
    }

    public void setScanTime(String scanTime) {
        ScanTime = scanTime;
    }

    public String getDeviceIPAddress() {
        return DeviceIPAddress;
    }

    public void setDeviceIPAddress(String deviceIPAddress) {
        DeviceIPAddress = deviceIPAddress;
    }

    public String getIPAddressOwnership() {
        return IPAddressOwnership;
    }

    public void setIPAddressOwnership(String IPAddressOwnership) {
        this.IPAddressOwnership = IPAddressOwnership;
    }

    public String getDeviceType() {
        return DeviceType;
    }

    public void setDeviceType(String deviceType) {
        DeviceType = deviceType;
    }

    public String getOSInfo() {
        return OSInfo;
    }

    public void setOSInfo(String OSInfo) {
        this.OSInfo = OSInfo;
    }

    public String getSystemFingerprintInfo() {
        return SystemFingerprintInfo;
    }

    public void setSystemFingerprintInfo(String systemFingerprintInfo) {
        SystemFingerprintInfo = systemFingerprintInfo;
    }

    public List<com.brd.asset.pojo.OpenServiceOfPort> getOpenServiceOfPort() {
        return OpenServiceOfPort;
    }

    public void setOpenServiceOfPort(List<com.brd.asset.pojo.OpenServiceOfPort> openServiceOfPort) {
        OpenServiceOfPort = openServiceOfPort;
    }

    public List<MessageOrientedMiddlewareScan> getMessageOrientedMiddleware() {
        return MessageOrientedMiddleware;
    }

    public void setMessageOrientedMiddleware(List<MessageOrientedMiddlewareScan> messageOrientedMiddleware) {
        MessageOrientedMiddleware = messageOrientedMiddleware;
    }

    public List<DataBaseInfoScan> getDataBaseInfos() {
        return dataBaseInfos;
    }

    public void setDataBaseInfos(List<DataBaseInfoScan> dataBaseInfos) {
        this.dataBaseInfos = dataBaseInfos;
    }

    public String getRuning() {
        return Runing;
    }

    public void setRuning(String runing) {
        Runing = runing;
    }

    @Override
    public String toString() {
        return "AssetScanOrigin{" +
                "ResourceName='" + ResourceName + '\'' +
                ", TaskID='" + TaskID + '\'' +
                ", ScanTime='" + ScanTime + '\'' +
                ", DeviceIPAddress='" + DeviceIPAddress + '\'' +
                ", IPAddressOwnership='" + IPAddressOwnership + '\'' +
                ", DeviceType='" + DeviceType + '\'' +
                ", OSInfo='" + OSInfo + '\'' +
                ", SystemFingerprintInfo='" + SystemFingerprintInfo + '\'' +
                ", OpenServiceOfPort=" + OpenServiceOfPort +
                ", MessageOrientedMiddleware=" + MessageOrientedMiddleware +
                ", dataBaseInfos=" + dataBaseInfos +
                ", Runing='" + Runing + '\'' +
                '}';
    }
}
