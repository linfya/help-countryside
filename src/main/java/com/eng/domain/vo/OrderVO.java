package com.eng.domain.vo;


import lombok.Data;

@Data
public class OrderVO {


    /*
     * 订单id
     * */
    private Long id;

    /*
    * 发布人用户名
    * */
    private String UserName;

    /**
     * 购买数量
     */
    private Integer num;

    /**
     * 商品标题
     */
    private String name;

    /**
     * 价格,单位：分
     */
    private Integer price;


    /**
     * 商品图片
     */
    private String image;
    /**
     * 订单的状态，1、未付款 2、已付款,未发货 3、已发货,未确认 4、确认收货，交易成功 5、交易取消，订单关闭 6、交易结束，已评价
     */
    private Integer status;


}
