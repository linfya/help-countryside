package com.eng.domain.vo;

import lombok.Data;

import java.time.LocalDate;


@Data
public class MyInfoVO {
    private String userName;
    private String icon;
    private Integer gender;
    private LocalDate birthday;
    private String introduce;

}
