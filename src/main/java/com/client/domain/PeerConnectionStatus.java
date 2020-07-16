package com.client.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PeerConnectionStatus {
    private String status;
    private int errorCode;
    private String statusDescription;
}
