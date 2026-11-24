package com.eng.controller;

import com.eng.util.Result;
import com.eng.domain.dto.PayDTO;
import com.eng.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/pay")
public class PayOrderController {


    @Autowired
    private OrderInfoService orderInfoService;

    /*
    * 支付功能
    * */
    @PutMapping()
    public Result toPayment(@RequestBody PayDTO payDTO) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        return orderInfoService.toPayment(payDTO);
    }

    /*
    *确认收货
    * */
    @PutMapping("/confirm")
    public Result toConfirm(@RequestParam("id") Long orderId){
        return orderInfoService.toConfirm(orderId);
    }



}
