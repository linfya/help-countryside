package com.eng.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class PayDTO {
    private String password;
    private List<String> orderIds;

}
