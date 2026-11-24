package com.eng.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderInfoVO {

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

    private String humanName;



    private String phone;


    private String userArea;


    private String userAddress;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;



    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 发货时间
     */
    private LocalDateTime consignTime;

    /**
     * 交易完成时间
     */
    private LocalDateTime endTime;

    /**
     * 交易关闭时间
     */
    private LocalDateTime closeTime;

    /**
     * 评价时间
     */
    private LocalDateTime commentTime;


    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 订单的状态，1、未付款 2、已付款,未发货 3、已发货,未确认 4、确认收货，交易成功 5、交易取消，订单关闭 6、交易结束，已评价
     */
    private Integer status;


    private Object data;


}
