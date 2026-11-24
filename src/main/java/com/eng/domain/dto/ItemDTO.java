package com.eng.domain.dto;


import lombok.Data;

@Data
public class ItemDTO {

    /*
     * 商品名称
     * */
    private String name;

    /*
     * 价格
     * */
    private Long price;

    /*
     * 商品分类
     * */
    private String type;


    /*
    * 库存
    * */
    private Integer stock;

}
