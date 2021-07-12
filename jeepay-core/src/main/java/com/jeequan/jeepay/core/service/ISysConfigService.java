package com.jeequan.jeepay.core.service;

import com.jeequan.jeepay.core.model.DBApplicationConfig;

public interface ISysConfigService {

    /** 获取应用的配置参数 **/
    DBApplicationConfig getDBApplicationConfig();

}
