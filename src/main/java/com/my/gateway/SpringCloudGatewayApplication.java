package com.my.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author yangsm
 * (exclude = DataSourceAutoConfiguration.class)
 * 访问 http://localhost:7766/actuator/gateway/routes
 * http://localhost:7766/actuator 查看所有请求
 */
public class SpringCloudGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudGatewayApplication.class, args);
    }

}
