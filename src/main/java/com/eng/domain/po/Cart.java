package com.eng.domain.po;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Cart {
    /*
    * 购物车商品id
    * */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    /*
    * 用户id
    * */
    private Long userId;
    /*
    * 商品id
    * */
    private Long itemId;
    /*
    * 购买数量
    * */
    private Integer num;
    /*
    * 商品名称
    * */
    private String name;

    /*
    * 商品价格
    * */
    private Integer price;
    /*
    * 图片
    * */
    private String image;
    /*
     * 创建时间
     * */
    private LocalDateTime createTime;

    /*
     * 更改时间
     * */
    private LocalDateTime updateTime;

    public Cart(){}
}
