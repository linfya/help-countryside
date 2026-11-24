package com.eng.util;

import com.eng.domain.dto.UserDTO;

/**
 * 基于 ThreadLocal 保存当前请求的登录用户，请求结束时由拦截器清理，避免线程复用导致数据串号。
 */
public class UserLocal {

    private static final ThreadLocal<UserDTO> USER_HOLDER = new ThreadLocal<>();

    private UserLocal() {
    }

    public static void saveUser(UserDTO user) {
        USER_HOLDER.set(user);
    }

    public static UserDTO getUser() {
        return USER_HOLDER.get();
    }

    public static void removeUser() {
        USER_HOLDER.remove();
    }
}
