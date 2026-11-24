package com.eng.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemComments {
    @TableId(value = "id",type = IdType.AUTO)
    private Long commentId;

    /*
    * 用户id
    * */
    private Long userId;

    /*
    * 评论文章id
    * */
    private Long itemId;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String icon;

    /*
    * 回复内容
    * */
    private String content;

    /*
    * 评分星级
    * */
    private int grade;

    /*
     * 创建时间
     * */
    private LocalDateTime createTime;

    /*
     * 修改时间
     * */
    private LocalDateTime updateTime;


}
