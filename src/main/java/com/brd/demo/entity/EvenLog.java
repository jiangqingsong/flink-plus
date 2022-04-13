package com.brd.demo.entity;


import java.sql.Timestamp;

/**
 * @Author: leo.j
 * @desc:
 * @Date: 2022/3/29 1:46 下午
 */

public class EvenLog {
    public String protocol;
    public Double traffic;
    public long time;

    public EvenLog() {
    }

    public EvenLog(String protocol, Double traffic, long time) {
        this.protocol = protocol;
        this.traffic = traffic;
        this.time = time;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setTraffic(Double traffic) {
        this.traffic = traffic;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getProtocol() {
        return protocol;
    }

    public Double getTraffic() {
        return traffic;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "EvenLog{" +
                "protocol='" + protocol + '\'' +
                ", traffic='" + traffic + '\'' +
                ", time='" + new Timestamp(time) + '\'' +
                '}';
    }
}
