package com.eng.domain.dto;


import lombok.Data;

import java.time.LocalDate;

@Data
public class UserInfoDTO {
    private String userName;
    private Integer gender;
    private LocalDate birthday;
    private String introduce;





}
