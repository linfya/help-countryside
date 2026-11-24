package com.eng.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class Item {
    /*
    * 商品id
    * */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /*
    * 商品名称
    * */
    private String name;

    /*
     * 价格
     * */
    private Integer price;

    /*
     * 库存
     * */
    private Integer stock;

    /*
    * 商品分类
    * */
    private String type;

    /*
    * 首张图片
    * */
    private String firstImage;


    /*
    * 图片
    * */
    private String image;


    /*
    * 详情图片
    * */
    private String description;

    /*
    * 销量
    * */
    private Integer sold;

    /*
    * 评论数量
    * */
    private Integer commentCount;

    /*
    * 状态
    * */
    private Integer status;

    /*
    * 创建时间
    * */
    private LocalDateTime createTime;

    /*
    * 更改时间
    * */
    private LocalDateTime updateTime;

    private String uploadType;

    /*
    * 创建id
    * */
    private Long creater;

    @TableField(exist = false)
    private List<ItemComments> firstComment;

    @TableField(exist = false)
    private String city;


    public Item(Long id, String name, Integer price, Integer stock, String type, String firstImage,String image, String description, Integer sold, Integer commentCount, Integer status, Long creater,String uploadType) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.type = type;
        this.firstImage = firstImage;
        this.image = image;
        this.description = description;
        this.sold = sold;
        this.commentCount = commentCount;
        this.status = status;
        this.creater = creater;
        this.uploadType = uploadType;
    }

    public Item(){}
}
