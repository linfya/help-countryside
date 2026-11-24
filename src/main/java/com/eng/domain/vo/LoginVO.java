package com.eng.domain.vo;

import lombok.Data;

@Data
public class LoginVO {
    private Long id;
    private boolean isFirst = false;
    private String token;

}
