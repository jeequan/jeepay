package org.xxpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * @Description: xxpay配置中心服务端
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
public class XxPayConfigApplication {
    public static void main(String[] args) {
        SpringApplication.run(XxPayConfigApplication.class, args);
    }
}
