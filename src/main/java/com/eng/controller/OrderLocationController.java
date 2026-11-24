package com.eng.controller;


import com.eng.util.Result;
import com.eng.domain.dto.SendGoodsDTO;
import com.eng.service.OrderLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/location")
public class OrderLocationController {

    @Autowired
    private OrderLocationService orderLocationService;

    /*
    * 发货
    * */
    @PutMapping()
    public Result toSendGoods(@RequestBody SendGoodsDTO sendGoodsDTO){
        return orderLocationService.toSendGoods(sendGoodsDTO);
    }

    /*
    * 查询物流详情信息
    * */
    @GetMapping("/info")
    public Result translationInfo(@RequestParam("orderId") Long orderId){
        return Result.ok(orderLocationService.translationInfo(orderId));
    }


}
