package com.eng.domain.po;


import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;


@Data
@AllArgsConstructor
public class UserInfo {

    @TableId(value = "user_id")
    private Long userId;

    /*
    * 城市
    * */
    private String city;


    /*
    * 个人介绍，不超过100字
    * */
    private String introduce;


    /*
    * 生日
    * */
    private LocalDate birthday;


    /*
    * 性别
    * */
    private Integer gender;

    private Integer items;

    /*
    * 个人作品数量
    * */
    private Integer works;


    /*
    * 关注数量
    * */
    private Integer fans;

    /*
    * 粉丝数量
    * */
    private Integer followHuman;

    public UserInfo(){}





}
