package com.eng.domain.dto;

import lombok.Data;

@Data
public class CartDTO {

    /*
     * 商品id
     * */
    private Long itemId;
    /*
     * 购买数量
     * */
    private Integer num;

}
