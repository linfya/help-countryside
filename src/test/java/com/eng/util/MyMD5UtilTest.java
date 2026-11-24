package com.eng.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 支付密码加盐 MD5 的加密与校验。
 */
class MyMD5UtilTest {

    @Test
    @DisplayName("同一密码加密两次结果不同（盐随机）")
    void encryptTwiceShouldDiffer() throws Exception {
        String first = MyMD5Util.getEncryptedPwd("123456");
        String second = MyMD5Util.getEncryptedPwd("123456");
        assertNotEquals(first, second);
    }

    @Test
    @DisplayName("正确密码可以通过校验")
    void correctPasswordShouldPass() throws Exception {
        String encrypted = MyMD5Util.getEncryptedPwd("123456");
        assertTrue(MyMD5Util.validPassword("123456", encrypted));
    }

    @Test
    @DisplayName("错误密码无法通过校验")
    void wrongPasswordShouldFail() throws Exception {
        String encrypted = MyMD5Util.getEncryptedPwd("123456");
        assertFalse(MyMD5Util.validPassword("654321", encrypted));
    }
}
