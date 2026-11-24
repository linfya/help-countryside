package com.eng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eng.util.Result;
import com.eng.domain.dto.UserLoginDTO;
import com.eng.domain.dto.UserPayPasswordDTO;
import com.eng.domain.po.User;

public interface UserService extends IService<User> {

    Result userLogin(UserLoginDTO userLoginDTO);

    Result sendCode(String phone);

    Result payPassword(UserPayPasswordDTO userPayPasswordDTO);

    boolean deduct(Long id,int money);

    boolean addBalance(Long id, int money);
}
