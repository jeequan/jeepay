package org.xxpay;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.xxpay.gateway.filter.AccessFilter;

/**
 * @Description: xxpay网关服务
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@EnableZuulProxy
@SpringCloudApplication
public class XxPayGatewayApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(XxPayGatewayApplication.class).web(true).run(args);
	}

	@Bean
	public AccessFilter accessFilter() {
		return new AccessFilter();
	}

}
