package com.eng.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.eng.domain.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static com.eng.util.RedisConstants.LOGIN_USER_KEY;

/**
 * Token 解析拦截器：从请求头 Authorization 取出 Token，去 Redis 查登录用户并放入 ThreadLocal。
 * 采用宽松策略，Token 无效时不拦截，由具体接口自己判断是否要求登录。
 */
@RequiredArgsConstructor
public class TokensInterception implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isBlank(authorization)) {
            return true;
        }

        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(LOGIN_USER_KEY + authorization);
        if (userMap.isEmpty()) {
            return true;
        }

        UserLocal.saveUser(BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserLocal.removeUser();
    }
}
