package com.eng.domain.vo;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfoVO {
    private String userName;
    private String icon;
    private String city;
    private Integer works;
    private Integer fans;
    private Integer followHuman;

}
