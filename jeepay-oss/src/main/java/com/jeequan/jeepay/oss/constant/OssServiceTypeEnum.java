package com.jeequan.jeepay.oss.constant;

import lombok.Data;
import lombok.Getter;

/*
* oss 服务枚举值
* @author terrfly
* @site https://www.jeepay.vip
* @date 2021/7/12 10:48
*/
@Getter
public enum OssServiceTypeEnum {

    LOCAL("local"), //本地存储

    ALIYUN_OSS("aliyun-oss");  //阿里云oss

    /** 名称 **/
    private String serviceName;

    OssServiceTypeEnum(String serviceName){
        this.serviceName = serviceName;
    }
}
