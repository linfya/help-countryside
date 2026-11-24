package com.eng.domain.dto;


import lombok.Data;

import java.util.List;

@Data
public class OrderDTO {
    /*
    * 地址id
    * */
    private Long addressId;

    /*
    * 商品列表
    * */
    private List<OrderInfoDTO> items;
}
