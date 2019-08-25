package com.my.gateway.gateway;

import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.InMemoryRouteDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;

/**
 * @author yangsm
 * 默认使用的是 InMemoryRouteDefinitionRepository
 * 缓存路由
 * 部署每个省份一个longquanAPI  每个连个超过1000个连接
 * 忽略 使用默认的InMemoryRouteDefinitionRepository
 */
//@Component
public class LongQuanRouteDefinitionRepository extends InMemoryRouteDefinitionRepository {

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        List<RouteDefinition> routeDefinitions = new ArrayList<>();
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(UUID.randomUUID().toString());

        PredicateDefinition predicateDefinition = new PredicateDefinition();
        // gateway 约定好的类型
        predicateDefinition.setName("Path");
        Map<String, String> predicateParams = new HashMap<>(8);
        predicateParams.put("pattern", "/baidu");
        predicateParams.put("pathPattern", "/baidu");
        predicateDefinition.setArgs(predicateParams);
        routeDefinition.setPredicates(Arrays.asList(predicateDefinition));
        URI uri = UriComponentsBuilder.fromHttpUrl("http://www.baidu.com").build().toUri();
        routeDefinition.setUri(uri);
        //routeDefinition.setFilters();
        routeDefinitions.add(routeDefinition);
        return Flux.fromIterable(routeDefinitions);
    }

}
