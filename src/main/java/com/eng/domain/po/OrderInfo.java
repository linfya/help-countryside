package com.eng.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("`order_info`")
public class OrderInfo {
    /**
     * 订单详情id
     */

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;


    /*
    * 订单id
    * */
    private Long orderId;


    /**
     * 用户id
     */
    private Long userId;

    /*
    * 地址id
    * */
    private Long addressId;

    /**
     * 订单的状态，1、未付款 2、已付款,未发货 3、已发货,未确认 4、确认收货，交易成功 5、交易取消，订单关闭 6、交易结束，已评价
     */
    private Integer status;

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
     * 逻辑删除
     */
    private Integer isDelete;

    public OrderInfo(){}
}
