package com.eng.domain.dto;


import lombok.Data;

@Data
public class BlogDTO {
    /*
     * 文章标题
     * */
    private String title;

    /*
     * 类型
     * */
    private String type;

    /*
     * 文字描述
     * */
    private String content;


}
