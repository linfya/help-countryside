package com.eng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eng.util.Result;
import com.eng.domain.dto.SendGoodsDTO;
import com.eng.domain.po.OrderLocation;

public interface OrderLocationService extends IService<OrderLocation> {
    Result toSendGoods(SendGoodsDTO sendGoodsDTO);

    Object translationInfo(Long orderId);
}
