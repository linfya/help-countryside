package com.eng.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eng.domain.dto.UserDTO;
import com.eng.domain.dto.UserLoginDTO;
import com.eng.domain.dto.UserPayPasswordDTO;
import com.eng.domain.po.User;
import com.eng.domain.po.UserInfo;
import com.eng.domain.vo.LoginVO;
import com.eng.mapper.UserMapper;
import com.eng.service.UserService;
import com.eng.util.MyMD5Util;
import com.eng.util.RegexUtil;
import com.eng.util.Result;
import com.eng.util.SMS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.eng.util.RedisConstants.LOGIN_CODE_KEY;
import static com.eng.util.RedisConstants.LOGIN_CODE_TTL;
import static com.eng.util.RedisConstants.LOGIN_USER_KEY;
import static com.eng.util.RedisConstants.LOGIN_USER_TTL;

/**
 * 用户服务：手机验证码登录、验证码发送、支付密码设置、余额增减。
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /** 默认头像 */
    private static final String DEFAULT_ICON =
            "https://helpcountryside-1321661270.cos.ap-shanghai.myqcloud.com/icon/default.jpg";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserInfoServiceImpl userInfoService;

    @Autowired
    private SMS sms;

    /**
     * 手机号 + 验证码登录，新用户自动注册。
     */
    @Override
    public Result userLogin(UserLoginDTO userLoginDTO) {
        String phone = userLoginDTO.getPhone();
        String code = userLoginDTO.getCode();

        String realCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        if (code == null || !code.equals(realCode)) {
            return Result.fail("验证码错误！");
        }

        LoginVO loginVO = new LoginVO();
        User user = query().eq("phone", phone).one();
        if (user == null) {
            user = createUserWithPhone(phone, loginVO);
        }

        Long userId = user.getUserId();
        loginVO.setId(userId);

        if (userInfoService.getById(userId) == null) {
            userInfoService.save(new UserInfo(userId, null, "", LocalDate.now(), 1, 0, 0, 0, 0));
        }
        userInfoService.update().eq("user_id", userId).set("city", userLoginDTO.getCity()).update();

        // 生成会话 Token，并把用户信息以 Hash 结构写入 Redis
        String token = RandomUtil.randomString(8);
        UserDTO userLogin = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userLogin, new HashMap<>(),
                CopyOptions.create().setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) ->
                                fieldValue == null ? "0" : fieldValue.toString()));
        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, userMap);
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);

        loginVO.setToken(token);
        return Result.ok(loginVO);
    }

    /**
     * 新用户自动注册：生成随机用户名与默认头像。
     */
    private User createUserWithPhone(String phone, LoginVO loginVO) {
        loginVO.setFirst(true);

        User user = new User();
        user.setPhone(phone);
        user.setIcon(DEFAULT_ICON);
        user.setUserName("HC" + RandomUtil.randomString(5));
        save(user);
        return user;
    }

    /**
     * 发送 6 位数字验证码，5 分钟内有效。
     */
    @Override
    public Result sendCode(String phone) {
        if (RegexUtil.isPhoneInvalid(phone)) {
            return Result.fail("电话号格式错误");
        }
        String code = RandomUtil.randomNumbers(6);
        sms.sendMessage(phone, code);
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        return Result.ok("验证码发送成功");
    }

    /**
     * 设置支付密码，加盐 MD5 后入库。
     */
    @Override
    public Result payPassword(UserPayPasswordDTO userPayPasswordDTO) {
        try {
            String encryptedPwd = MyMD5Util.getEncryptedPwd(userPayPasswordDTO.getPayPassword());
            lambdaUpdate()
                    .eq(User::getUserId, userPayPasswordDTO.getUserId())
                    .set(User::getPayPassword, encryptedPwd)
                    .update();
            return Result.ok();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 扣减余额，SQL 中带余额校验，避免扣成负数。
     */
    @Override
    public boolean deduct(Long id, int money) {
        User user = getById(id);
        return baseMapper.deduct(id, user.getBalance(), money);
    }

    /**
     * 增加余额。
     */
    @Override
    public boolean addBalance(Long id, int money) {
        return baseMapper.addBalance(id, money);
    }
}
