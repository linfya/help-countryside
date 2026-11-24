package com.eng.domain.vo;

import lombok.Data;

@Data
public class FollowHumanVO {
    private Long id;
    private String userName;
    private String icon;
    private String introduce;
    private boolean isMyLiked;

}
