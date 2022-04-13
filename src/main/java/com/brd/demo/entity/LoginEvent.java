package com.brd.demo.entity;

import lombok.Data;

/**
 * @Author: leo.j
 * @desc:
 * @Date: 2022/4/8 11:07 上午
 */
@Data
public class LoginEvent {
    public String userId;
    public String message;
    public Long timestamp;

    public LoginEvent() {
    }

    public LoginEvent(String userId, String message, Long timestamp) {
        this.userId = userId;
        this.message = message;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "LoginEvent{" +
                "userId='" + userId + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
