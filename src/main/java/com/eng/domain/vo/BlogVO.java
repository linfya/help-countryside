package com.eng.domain.vo;


import lombok.Data;

@Data
public class BlogVO {
    /*
     * 文章标题
     * */
    private String title;


    /*
     * 首张文章图片
     * */
    private String firstImage;

    /*
     * 文章图片
     * */
    private String images;

    /*
     * 类型
     * */
    private String type;

    /*
     * 文字描述
     * */
    private String content;




}
