package com.eng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eng.domain.po.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;


public interface UserMapper extends BaseMapper<User> {


    @Update("update user set balance = balance - #{money} where user_id = #{id} and balance = #{LocalBalance} and (balance - #{money}) >= 0")
    boolean deduct(@Param("id")Long id,@Param("LocalBalance")int balance,@Param("money")int money);


    @Update("update user set balance = balance + #{money} where user_id = #{id}")
    boolean addBalance(@Param("id")Long id,@Param("money")int money);
}
