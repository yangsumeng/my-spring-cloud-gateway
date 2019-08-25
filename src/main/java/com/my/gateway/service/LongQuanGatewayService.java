package com.my.gateway.service;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Mono;

/**
 * 方法可以动态的保存路由
 */
@Service
public class LongQuanGatewayService implements ApplicationEventPublisherAware {
    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    private ApplicationEventPublisher publisher;

    public String save( RouteDefinition definition) {
//        PredicateDefinition predicate = new PredicateDefinition();
//        Map<String, String> predicateParams = new HashMap<>(8);
//
//        definition.setId("baiduRoute");
//        /*
//         * predicate.setName("Path"); 设置predicat名称，这个名称不是乱起的，Spring会根据名称去查找对应的FilterFactory，
//         * 目前支持的名称有：After、Before、Between、Cookie、Header、Host、Method、Path、Query、RemoteAddr。
//         */
//        predicate.setName("Path");
//        predicateParams.put("pattern", "/baidu");
//        predicateParams.put("pathPattern", "/baidu");
//        predicate.setArgs(predicateParams);
//        definition.setPredicates(Arrays.asList(predicate));
//        URI uri = UriComponentsBuilder.fromHttpUrl("http://www.baidu.com").build().toUri();
//        definition.setUri(uri);
        //注意最后一定要调用subscribe()，否则不执行
        routeDefinitionWriter.save(Mono.just(definition)).subscribe();
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
        return "success";
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}
