package com.jeequan.jeepay.pay.util;

import com.jeequan.jeepay.pay.config.SystemYmlConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/*
* 支付平台 获取系统文件工具类
*
* @author terrfly
* @site https://www.jeepay.vip
* @date 2021/6/8 17:45
*/
@Component
public class ChannelCertConfigKitBean {

    @Autowired private SystemYmlConfig systemYmlConfig;

    public String getCertFilePath(String certFilePath){
        return systemYmlConfig.getOssFile().getPrivatePath() + File.separator + certFilePath;
    }

    public File getCertFile(String certFilePath){
        return new File(getCertFilePath(certFilePath));
    }
}
