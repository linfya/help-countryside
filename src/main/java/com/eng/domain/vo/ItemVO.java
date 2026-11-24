package com.eng.domain.vo;

import lombok.Data;

@Data
public class ItemVO {
    /*
     * 商品名称
     * */
    private String name;

    /*
     * 价格
     * */
    private Long price;

    /*
     * 库存
     * */
    private Integer stock;

    /*
     * 商品分类
     * */
    private String type;


    /*
     * 图片
     * */
    private String image;


    /*
     * 详情图片
     * */
    private String description;



}
