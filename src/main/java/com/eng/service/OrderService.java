package com.eng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eng.util.Result;
import com.eng.domain.dto.OrderDTO;
import com.eng.domain.po.Order;

public interface OrderService extends IService<Order> {
    Result addOrder(OrderDTO orderDTO);


    Result getOrderList(Integer curran, int type);

    Result getSellOrderList(Integer curran, int type);

    Result getOrderInfo(Long id);

    Result deleteOrder(Long orderId);
}
