package com.my.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.my.gateway.utils.AESUtil;
import com.my.gateway.constants.LongQuanConstants;
import com.my.gateway.service.LongQuanGatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 * url
 * @author yangsm
 */
@Component
@Slf4j
public class ChangeUriGlobalFilter implements GlobalFilter, Ordered {

    private static final String GATE_WAY_PREFIX = "/api/";

    @Autowired
    private LongQuanGatewayService longQuanGatewayService;

    @Autowired
    private RouteDefinitionRepository routeDefinitionRepository;

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, GatewayFilterChain gatewayFilterChain) {
        try{
            ServerHttpRequest request = serverWebExchange.getRequest();
            /*
             * 判断是否是首次请求
             */
            boolean isFirstRequest = getFirstRequestHeader(request);

            //获取当前路由
            Route route = serverWebExchange.getAttribute(GATEWAY_ROUTE_ATTR);

//            //更改标记 为 不是第一次请求
//            Mono<Void> mono = getCorrectResultMono(serverWebExchange, gatewayFilterChain, request);

            //如果是第一次请求 增加路由 初始化路由增加这个标志  路由级别更高
            if(isFirstRequest){
                //获取加密的URL
                String encryptUrl = getEncryptUrlFromPath(request);

                /*
                 *  从 routeDefinitionRepository中获取路由列表 默认子类InMemoryRouteDefinitionRepository
                 *  判断路由是否已经存在  不存在添加
                 *  isHas 是否已经存在路由
                 *  修改为 isFirstRequest 判断 ， 如果是第一次，路由中肯没有数据
                 *  如果不是第一次， 路由中一定有数据否则无法进入这个filter
                 */
//                AtomicBoolean isHas = checkHasRouterByRouterId(encryptUrl);
//                if(isHas.get()){ //已经存在
//                    return mono;
//                }

                String realUrl = AESUtil.decrypt(encryptUrl,"123");
                log.info("请求的真实URL: {}",realUrl);
                //增加个简单的校验
                if(realUrl == null || !realUrl.startsWith("http")){
                    return getErrorMono(serverWebExchange, Result.builder().code("200").msg("longquan Invalid encryption URL").build());
                }
                // 开始增加路由
                URI uri = UriComponentsBuilder.fromUriString(realUrl).build().toUri();
                RouteDefinition routeDefinition = new RouteDefinition();
                PredicateDefinition predicateDefinition = new PredicateDefinition();
//            Map<String, String> filterParams = new HashMap<>(8);
                //ID是url的密文
                routeDefinition.setId(encryptUrl);
                // gateway 约定好的类型name
                predicateDefinition.setName("Path");
                Map<String, String> predicateParams = new HashMap<>(8);
                String newUrl = String.format("/api/%s/**",encryptUrl);
                predicateParams.put("_genkey_0", newUrl);
                predicateDefinition.setArgs(predicateParams);


                /*
                 *截器2位      http://ip:port/api/encryptUrl/XXX  截取为  realUrl/XXX
                 *  filters:
                 *      - StripPrefix=2
                 */
                FilterDefinition stripPrefixFilter = new FilterDefinition();
                stripPrefixFilter.setName("StripPrefix");
                Map<String, String> stripPrefixParams = new HashMap<>(8);
                stripPrefixParams.put("_genkey_0","2");
                stripPrefixFilter.setArgs(stripPrefixParams);

                //增加头过滤器
                //？   gateway是否可以绑定不同的路由
                FilterDefinition addHeaderFilter = new FilterDefinition();
                addHeaderFilter.setName("AddRequestHeader");
                Map<String, String> addHeaderParams = new HashMap<>(8);
                addHeaderParams.put("_genkey_0","longquan-time");
                addHeaderParams.put("_genkey_1",String.valueOf(System.currentTimeMillis()));
                addHeaderFilter.setArgs(addHeaderParams);

                /////////////////////*****************************************************
                FilterDefinition removeRequestHeaderFilter = new FilterDefinition();
                removeRequestHeaderFilter.setName("RemoveRequestHeader");
                Map<String, String> removeRequestHeaderParams = new HashMap<>(8);
                removeRequestHeaderParams.put("_genkey_0","first-request");
                removeRequestHeaderFilter.setArgs(removeRequestHeaderParams);
                /////////////////////*****************************************************

                routeDefinition.setPredicates(Arrays.asList(predicateDefinition));
                routeDefinition.setFilters(Arrays.asList(stripPrefixFilter,addHeaderFilter,removeRequestHeaderFilter));/////////////////////*****************************************************
                routeDefinition.setUri(uri);
                routeDefinition.setOrder(0);


                longQuanGatewayService.save(routeDefinition);



                //创建新路由并修改跳转地址
//                Route newRoute = Route.async()
//                        .asyncPredicate(route.getPredicate())
//                        .filters(route.getFilters())
//                        .id(route.getId())
//                        .order(route.getOrder())
//                        .uri(uri).build();
//                serverWebExchange.getAttributes().put(GATEWAY_ROUTE_ATTR,newRoute);

                ServerHttpRequest.Builder mutate = request.mutate();
                mutate.header(LongQuanConstants.FIRST_REQUEST_HEADER_KEY, "false");
                ServerHttpRequest newRequest = mutate.build();
                return gatewayFilterChain.filter(serverWebExchange.mutate().request(newRequest).build());

            } else {
                ServerHttpRequest.Builder mutate = request.mutate();
                mutate.header(LongQuanConstants.FIRST_REQUEST_HEADER_KEY, "false");
                ServerHttpRequest newRequest = mutate.build();
                return gatewayFilterChain.filter(serverWebExchange.mutate().request(newRequest).build());
            }
        } catch (Exception e) {
            log.error("gate way 解析错误",e);
            return Mono.empty();
        }
    }

    private AtomicBoolean checkHasRouterByRouterId(String encryptUrl) {
        AtomicBoolean isHas = new AtomicBoolean(false);
        Flux<RouteDefinition> routeDefinitionFlux = routeDefinitionRepository.getRouteDefinitions();
        routeDefinitionFlux.toIterable().forEach(routeDefinition -> {
            if(routeDefinition.getId().equals(encryptUrl)){  //已经存在
                isHas.set(true);
            }
        });
        return isHas;
    }

    /**
     * 路径未 /api/[encryptUrl]/XX/XX
     */
    private String getEncryptUrlFromPath(ServerHttpRequest request) {
        String requestUri = request.getPath().pathWithinApplication().value();
        requestUri = requestUri.substring(GATE_WAY_PREFIX.length());
        return requestUri.split("/")[0];
    }

    /**
     * 获取正确的返回结果
     * @param serverWebExchange
     * @param gatewayFilterChain
     * @param request
     * @return
     */
    private Mono<Void> getCorrectResultMono(ServerWebExchange serverWebExchange, GatewayFilterChain gatewayFilterChain, ServerHttpRequest request) {
        ServerHttpRequest.Builder mutate = request.mutate();
        mutate.header(LongQuanConstants.FIRST_REQUEST_HEADER_KEY, "false");
        ServerHttpRequest newRequest = mutate.build();
        return gatewayFilterChain.filter(serverWebExchange.mutate().request(newRequest).build());
    }

    /**
     * first-request true 首次提交
     * @param request
     * @return
     */
    private boolean getFirstRequestHeader(ServerHttpRequest request) {
        boolean firstRequest = false;
        List<String> firstRequestList = request.getHeaders().get(LongQuanConstants.FIRST_REQUEST_HEADER_KEY);
        if (!CollectionUtils.isEmpty(firstRequestList)) {
            if("true".equals(firstRequestList.get(0))){
                firstRequest = true;
            }
        }
        return firstRequest;
    }

    private Mono<Void> getErrorMono(ServerWebExchange serverWebExchange, Result body) {
        serverWebExchange.getResponse().setStatusCode(HttpStatus.OK);
        byte[] bytes = JSONObject.toJSONString(body).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = serverWebExchange.getResponse().bufferFactory().wrap(bytes);
        return serverWebExchange.getResponse().writeWith(Flux.just(buffer));
    }

    /**
     * 重定向      浏览器支持   js代码中$.ajax GET支持   $.get不支持
     * 参数：
     * POST:
     */
    private Mono<Void> getRedictMono(ServerWebExchange exchange ,GatewayFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();
        //303状态码表示由于请求对应的资源存在着另一个URI，应使用GET方法定向获取请求的资源
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().set(HttpHeaders.LOCATION, "");
        return chain.filter(exchange);
    }

    /**
     * 参考： spring cloud gateway 源码 RedirectToGatewayFilterFactory
     * 重定向 ： 浏览器支持   js代码中$.ajax GET支持   $.get也支持
     * 参数：
     * POST:
     */
    private Mono<Void> getRedictMono(ServerWebExchange exchange ,String url) {
        if (!exchange.getResponse().isCommitted()) {
            final ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.SEE_OTHER);
            response.getHeaders().set(HttpHeaders.LOCATION, url);
            return response.setComplete();
        }
        return Mono.empty();
    }

    @Override
    public int getOrder() {
        //最低优先级
        return Ordered.LOWEST_PRECEDENCE;
    }
}
