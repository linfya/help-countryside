package com.eng.domain.dto;

import lombok.Data;

@Data
public class OrderInfoDTO {
    /*
    * 商品id
    * */
    private Long id;

    /*
    * 商品数量
    * */
    private Integer num;

    /*
    * 备注
    * */
    private String remark;

}
