package com.eng.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;


@Data
public class Address {

    @TableId(value = "address_id",type = IdType.AUTO)
    private Long addressId;
    private Long userId;
    private String humanName;
    private String phone;
    private String userArea;
    private String userAddress;
    private int status;
}
