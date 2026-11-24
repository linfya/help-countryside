package com.eng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eng.util.Result;
import com.eng.domain.dto.CartDTO;
import com.eng.domain.po.Cart;

import java.util.Collection;

public interface CartService extends IService<Cart> {
    Result addCart(CartDTO cartDTO);

    Result cartList(Integer curran);

    Result deleteCarList(Collection<Long> ids);
}
