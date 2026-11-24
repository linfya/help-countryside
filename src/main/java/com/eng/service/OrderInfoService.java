package com.eng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eng.util.Result;
import com.eng.domain.dto.PayDTO;
import com.eng.domain.po.OrderInfo;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

public interface OrderInfoService extends IService<OrderInfo> {
    void updateStatus(Long orderId, int status, String type, LocalDateTime localDateTime);

    Result toPayment(PayDTO payDTO) throws UnsupportedEncodingException, NoSuchAlgorithmException;

    Result toConfirm(Long orderId);
}
