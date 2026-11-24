package com.eng.domain.dto;


import lombok.Data;

@Data
public class ItemCommentsDTO {

    private Long orderId;
    /*
     * 回复内容
     * */
    private String content;

    /*
     * 评分星级
     * */
    private int grade;
}
