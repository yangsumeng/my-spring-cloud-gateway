package com.my.gateway.filter;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Result {
    private String code;

    private String msg;
}
