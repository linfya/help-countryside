package com.eng.controller;

import com.eng.domain.dto.UserInfoDTO;
import com.eng.service.UserInfoService;
import com.eng.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 个人主页相关接口：资料、头像、关注与粉丝。
 */
@RestController
@RequestMapping("/own")
@CrossOrigin(origins = "*")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    /** 查看指定用户的主页信息 */
    @GetMapping("/info/{id}")
    public Result userMainInfo(@PathVariable Long id) {
        return userInfoService.userMainInfo(id);
    }

    /** 修改头像 */
    @PostMapping("/icon")
    public Result updateUserIcon(@RequestParam("file") MultipartFile file,
                                 @RequestHeader("Authorization") String token) {
        return userInfoService.updateUserIcon(file, token);
    }

    /** 修改个人资料 */
    @PutMapping("/info")
    public Result userMainInfoChange(@RequestBody UserInfoDTO userInfoDTO,
                                     @RequestHeader("Authorization") String token) {
        return userInfoService.userMainInfoChange(userInfoDTO, token);
    }

    /** 获取自己的主页信息 */
    @GetMapping("/getInfo")
    public Result getUserMain() {
        return userInfoService.getInfoMain();
    }

    /** 关注 / 取关他人 */
    @PutMapping("/follow")
    public Result followHuman(@RequestParam("id") Long id) {
        return userInfoService.followHuman(id);
    }

    /** 我关注的人 */
    @GetMapping("/like")
    public Result myLikeHuman() {
        return userInfoService.myLikeHuman();
    }

    /** 我的粉丝 */
    @GetMapping("/fans")
    public Result myFans() {
        return userInfoService.myFans();
    }

    /** 系统通知（SSE / Redis Stream 长轮询） */
    @GetMapping("/system")
    public Result systemInform() {
        return userInfoService.systemInform();
    }
}
