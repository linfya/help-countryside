package com.eng.domain.dto;

import lombok.Data;

@Data
public class BlogCommentsDTO {


    /*
     * 评论文章id
     * */
    private Long blogId;

    /*
     * 回复内容
     * */
    private String content;

}
