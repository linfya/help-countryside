package com.eng.controller;


import cn.hutool.json.JSONUtil;
import com.eng.util.Result;
import com.eng.util.UserLocal;
import com.eng.domain.po.User;
import com.eng.service.UserInfoService;
import com.eng.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

import static com.eng.util.RedisConstants.USER_NOTICE_KEY;

@RestController
@RequestMapping("/me")
@CrossOrigin(origins = "*")
public class UserController {


    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserService userService;


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /*
     * 个人页面
     * */
    @GetMapping()
    public Result userMain(){
        return userInfoService.userMain();
    }


    /*
    *
    * 扫描植物接口
    * */
    @PostMapping("/plant")
    public Result identifyPlant(@RequestParam(value = "image",required = false) MultipartFile image){
        return userInfoService.identifyPlant(image);
    }


    @GetMapping("/balance")
    public Result getBalance(){
        Long userId = UserLocal.getUser().getUserId();
        User byId = userService.getById(userId);
        return Result.ok(byId.getBalance());
    }

    @GetMapping("/notice")
    public Result getNotice(){
        Long userId = UserLocal.getUser().getUserId();
        Set<String> messages = stringRedisTemplate.opsForZSet().range(USER_NOTICE_KEY + userId, 0, -1);
        if (messages == null){
            return Result.ok("通知列表为空");
        }
        String jsonStr = JSONUtil.toJsonStr(messages);
        return Result.ok(jsonStr);
    }




}
