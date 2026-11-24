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
public class Blog {
    /*
    * 文章id
    * */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /*
    * 用户id
    * */
    private Long userId;

    /*
    * 文章标题
    * */
/*    @TableField(value = "title",condition = SqlCondition.LIKE)*/
    private String title;



    /*
    * 文章图片
    * */
    private String images;


    /*
    * 首张文章图片
    * */
    private String firstImage;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String icon;

    @TableField(exist = false)
    private String city;

    @TableField(exist = false)
    private boolean HumanIsMyLiked = false;

    @TableField(exist = false)
    private boolean BlogIsMyLiked;

    @TableField(exist = false)
    private boolean BlogIsMyCollect;

    @TableField(exist = false)
    private List<BlogComments> blogComments;

    /*
    * 类型
    * */
    private String type;

    private String uploadType;

    /*
    * 文字描述
    * */
    private String content;

    /*
    * 点赞数量
    * */
    private Integer liked;

    /*
    * 评论数量
    * */
    private Integer comments;

    /*
    * 创建时间
    * */
    private LocalDateTime createTime;

    /*
    * 修改时间
    * */
    private LocalDateTime updateTime;


    /*
    * 推文状态
    * */
    private  Integer status;

    public Blog(){

    }


    public Blog(Long id, Long userId, String title, String firstImages, String images, String type, String content, Integer liked, Integer comments, LocalDateTime createTime, LocalDateTime updateTime, Integer status,String uploadType) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.firstImage = firstImages;
        this.images = images;
        this.type = type;
        this.content = content;
        this.liked = liked;
        this.comments = comments;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.status = status;
        this.uploadType = uploadType;
    }
}