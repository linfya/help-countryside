package com.eng.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eng.util.Result;
import com.eng.util.UserLocal;
import com.eng.domain.dto.CartDTO;
import com.eng.domain.po.Cart;
import com.eng.domain.po.Item;
import com.eng.mapper.CartMapper;
import com.eng.service.CartService;
import com.eng.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;


@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {


    @Autowired
    private ItemService itemService;

    @Override
    public Result addCart(CartDTO cartDTO) {
        Long userId = UserLocal.getUser().getUserId();
        Long count = query().eq("user_id", userId).eq("item_id", cartDTO.getItemId()).count();
        if (count > 0){
            baseMapper.updateNum(cartDTO.getNum(),cartDTO.getItemId(),userId);
            return Result.ok();
        }
        Item item = itemService.getById(cartDTO.getItemId());
        if (item == null){
            return Result.fail("商品不存在");
        }
        Cart cart = BeanUtil.copyProperties(cartDTO, Cart.class);
        cart.setName(item.getName());
        cart.setPrice(item.getPrice());
        cart.setImage(item.getFirstImage());
        cart.setUserId(userId);
        save(cart);
        return Result.ok();
    }

    @Override
    public Result cartList(Integer curran) {
        Long userId = UserLocal.getUser().getUserId();
        List<Cart> list = query()
                .orderByDesc("create_time")
                .eq("user_id", userId)
                .list();
        return Result.ok(list);
    }

    @Override
    public Result deleteCarList(Collection<Long> ids) {
        Long userId = UserLocal.getUser().getUserId();
        // 1.构建删除条件，userId和itemId
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<Cart>();
        LambdaQueryWrapper<Cart> in = queryWrapper.lambda()
                .eq(Cart::getUserId, userId)
                .in(Cart::getId, ids);
        boolean remove = remove(in);
        if (remove){
            return Result.ok("删除成功");
        }else {
            return Result.ok("删除失败");
        }
    }
}
