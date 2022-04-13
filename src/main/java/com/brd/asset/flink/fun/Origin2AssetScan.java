package com.brd.asset.flink.fun;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.brd.asset.common.czip.IpLocation;
import com.brd.asset.common.czip.Location;
import com.brd.asset.constants.ScanCollectConstant;
import com.brd.asset.pojo.AssetScanOrigin;
import com.brd.asset.pojo.DataBaseInfoScan;
import com.brd.asset.pojo.MessageOrientedMiddlewareScan;
import com.brd.asset.pojo.OpenServiceOfPort;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.log4j.Logger;

import java.util.List;


/**
 * @Author: leo.j
 * @desc: 原始资产数据转AssetScan对象
 * @Date: 2022/3/25 12:45 下午
 */
public class Origin2AssetScan extends RichMapFunction<JSONObject, AssetScanOrigin> {
    private static Logger LOG = Logger.getLogger(Origin2AssetScan.class);

    private String locationPath;
    private IpLocation ipLocation;

    public Origin2AssetScan(String locationPath) {
        this.locationPath = locationPath;
    }


    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        ipLocation = new IpLocation(locationPath);
    }

    @Override
    public AssetScanOrigin map(JSONObject o) {
        try {
            String resourceName = o.get(ScanCollectConstant.RESOURCE_NAME).toString();
            String taskId = o.get(ScanCollectConstant.TASK_ID).toString();
            String scanTime = o.get(ScanCollectConstant.SCAN_TIME).toString();
            //resource info
            JSONObject resourceObj = JSON.parseObject(o.get(ScanCollectConstant.RESOURCE_INFO).toString());


            if (resourceObj.size() > 0) {
                //deviceIpAddress
                String deviceIpAddress = resourceObj.get(ScanCollectConstant.DEVICE_IP_ADDRESS).toString();
                String ipAddressOwnership = resourceObj.get(ScanCollectConstant.IP_ADDRESS_OWNERSHIP).toString();
                String deviceType = resourceObj.get(ScanCollectConstant.DEVICE_TYPE).toString();
                String osInfo = resourceObj.get(ScanCollectConstant.OS_INFO).toString();
                String systemFingerprintInfo = resourceObj.get(ScanCollectConstant.SYSTEM_FINGERPRINT_INFO).toString();
                //openServiceOfPort [obj]
                String openServiceOfPortJson = resourceObj.get(ScanCollectConstant.OPEN_SERVICE_OF_PORT).toString();
                List<OpenServiceOfPort> openServiceOfPorts = JSON.parseArray(openServiceOfPortJson, OpenServiceOfPort.class);
                //[obj]
                String messageOrientedMiddlewareJson = resourceObj.get(ScanCollectConstant.MESSAGE_ORIENTED_MIDDLEWARE).toString();
                List<MessageOrientedMiddlewareScan> messageOrientedMiddlewares = JSON.parseArray(messageOrientedMiddlewareJson, MessageOrientedMiddlewareScan.class);
                //[obj]
                String dataBaseInfoJson = resourceObj.get(ScanCollectConstant.DATA_BASE_INFO).toString();
                List<DataBaseInfoScan> dataBaseInfos = JSON.parseArray(dataBaseInfoJson, DataBaseInfoScan.class);
                String running = resourceObj.get(ScanCollectConstant.RUNNING).toString();

                String province = "";
                //ip归属省份
                try {
                    Location location = ipLocation.fetchIPLocation(deviceIpAddress.trim());
                    province = location.country;
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
                AssetScanOrigin scan = new AssetScanOrigin();
                scan.setResourceName(resourceName);
                scan.setTaskID(taskId);
                scan.setScanTime(scanTime);
                scan.setDeviceIPAddress(deviceIpAddress);
                scan.setIPAddressOwnership("".equals(province) ? "中国" : province);
                scan.setDeviceType(deviceType);
                scan.setOSInfo(osInfo);
                scan.setSystemFingerprintInfo(systemFingerprintInfo);
                scan.setOpenServiceOfPort(openServiceOfPorts);
                scan.setMessageOrientedMiddleware(messageOrientedMiddlewares);
                scan.setDataBaseInfos(dataBaseInfos);
                scan.setRuning(running);
                return scan;
            }
        } catch (Exception e) {
            LOG.error("资产原始数据转AssetScanOrigin对象出错：" + e.getMessage(), e);
        }
        return null;
    }
}
