package com.eng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eng.domain.po.Item;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ItemMapper extends BaseMapper<Item> {
    @Update("update item set stock = stock - #{num} where id = #{id} and (stock - #{num}) >= 0")
    void deleteStock(@Param("id") Long id, @Param("num") Integer num);


    @Update("update item set sold = sold + 1 where id = #{id}")
    void addSold(@Param("id") Long id);

}
