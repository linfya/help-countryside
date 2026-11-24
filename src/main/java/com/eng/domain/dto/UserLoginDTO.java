package com.eng.domain.dto;


import lombok.Data;

@Data
public class UserLoginDTO {
    private String phone;
    private String code;
    private String city;


}
