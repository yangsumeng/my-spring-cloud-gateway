package com.my.gateway.utils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

public class MainTest {
    public static void main(String[] args) {
       // Flux.just("just11213","just11213","aaa","vvv","just112232313").subscribe(System.out::println);
//        Mono.just("just11213").subscribe(new Consumer<String>() {
//            @Override
//            public void accept(String o) {
//                System.out.println(o);
//            }
//        });
        String str = "/qpi/123/123456";
        String subString = str.substring("/api/".length()).split("/")[0];

    }
}
