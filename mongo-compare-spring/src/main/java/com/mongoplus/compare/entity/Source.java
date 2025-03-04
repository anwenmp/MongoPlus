package com.mongoplus.compare.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author anwen
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Source {

    private String msg;

    private Integer code;

    private LocalDateTime responseTime;

    private String channel;

    private Map<String,Object> responseJSON;

    private String source;

    private String reqId;

    private String vendorCode;

    private Map<String,Object> requestJSON;

    private LocalDateTime requestTime;

    private String application;

    private String action;

    private String region;

    private String status;

}
