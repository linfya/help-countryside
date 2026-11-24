package com.eng.domain.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private String humanName;
    private String phone;
    private  String userArea;
    private String userAddress;
    private boolean toDefault;


    public AddressDTO(){}
}
