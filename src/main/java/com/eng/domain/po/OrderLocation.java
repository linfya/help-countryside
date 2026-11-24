package com.eng.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderLocation {


    /**
     * 订单id，与订单表一对一
     */
    @TableId(value = "order_id", type = IdType.INPUT)
    private Long orderId;

    /**
     * 物流单号
     */
    private String locationNumber;


    /**
     * 收件人
     */
    private String humanName;
    /**
     * 收件人手机号码
     */
    private String phone;


    private String userArea;


    private String userAddress;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
