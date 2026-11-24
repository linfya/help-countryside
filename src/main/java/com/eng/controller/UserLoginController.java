package com.eng.controller;


import com.eng.util.Result;
import com.eng.domain.dto.UserLoginDTO;
import com.eng.domain.dto.UserPayPasswordDTO;
import com.eng.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserLoginController {

    @Autowired
    private UserService userService;

    /*
    * 发送验证码
    * */
    @PostMapping("/code")
    public Result sendCode(@RequestParam("phone") String phone){
        return userService.sendCode(phone);
    }


    /*
    * 登录
    * */
    @PostMapping("/login")
    public Result userLogin(@RequestBody UserLoginDTO userLoginDTO) {
        return userService.userLogin(userLoginDTO);
    }

    /*
    * 设置支付密码
    *
    * */
    @PutMapping("/payPwd")
    public Result payPassword(@RequestBody UserPayPasswordDTO userPayPasswordDTO){
        return userService.payPassword(userPayPasswordDTO);
    }




}
