package com.eng.config;

import com.eng.util.LoginInterception;
import com.eng.util.TokensInterception;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC 配置：注册登录态拦截器。
 */
@Configuration
@RequiredArgsConstructor
public class MvcConfig implements WebMvcConfigurer {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // order 0：先解析 Token，把登录用户放进 ThreadLocal
        registry.addInterceptor(new TokensInterception(stringRedisTemplate))
                .addPathPatterns("/**")
                .order(0);
        // order 1：再校验是否登录，未登录直接返回 401
        registry.addInterceptor(new LoginInterception())
                .excludePathPatterns("/shop/**", "/blog/hot")
                .order(1);
    }
}
