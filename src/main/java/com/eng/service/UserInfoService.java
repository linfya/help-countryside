package com.eng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eng.util.Result;
import com.eng.domain.dto.UserInfoDTO;
import com.eng.domain.po.UserInfo;
import org.springframework.web.multipart.MultipartFile;

public interface UserInfoService extends IService<UserInfo> {
    Result userMainInfo(Long id);

    Result userMainInfoChange(UserInfoDTO userInfoDTO, String token);

    Result userMain();

    Result followHuman(Long id);


    Result identifyPlant(MultipartFile image);

    Result myLikeHuman();

    Result myFans();

    Result systemInform();

    Result updateUserIcon(MultipartFile file, String token);

    Result getInfoMain();
}
