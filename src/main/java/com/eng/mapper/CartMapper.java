package com.eng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eng.domain.po.Cart;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface CartMapper extends BaseMapper<Cart> {
    @Update("UPDATE cart SET num = num + #{number} WHERE user_id = #{userId} AND item_id = #{itemId}")
    void updateNum(@Param("number") Integer number,@Param("itemId") Long itemId, @Param("userId") Long userId);
}
