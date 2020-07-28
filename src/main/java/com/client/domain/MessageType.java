package com.client.domain;

import lombok.Getter;

@Getter
public enum  MessageType {
    LOGIN("LI"),
    LOGOUT("LO"),
    DATA("DT");

    private final byte [] messageCode;

    MessageType(String code){
        this.messageCode = code.getBytes();
    }

}
