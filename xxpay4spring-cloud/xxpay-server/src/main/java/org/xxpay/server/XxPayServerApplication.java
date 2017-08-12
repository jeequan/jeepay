package org.xxpay.server;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @Description: xxpay服务注册中心
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@EnableEurekaServer
@SpringBootApplication
public class XxPayServerApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(XxPayServerApplication.class).web(true).run(args);
	}

}
