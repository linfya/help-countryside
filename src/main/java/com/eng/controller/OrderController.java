package com.eng.controller;


import com.eng.util.Result;
import com.eng.domain.dto.OrderDTO;
import com.eng.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@CrossOrigin(origins = "*")
public class OrderController {


    @Autowired
    private OrderService orderService;


    /*
    * 创建订单
    * */
    @PostMapping("/add")
    public Result addOrder(@RequestBody OrderDTO orderDTO){
        return orderService.addOrder(orderDTO);
    }


    /*
    * 删除订单
    * */
    @PutMapping("/delete")
    public Result deleteOrder(@RequestParam("id") Long orderId){
        return orderService.deleteOrder(orderId);
    }

    /*
    * 查询我的订单列表
    * */
    @GetMapping("/buy")
    public Result getOrderList(@RequestParam(value = "curran", defaultValue = "1") Integer curran,@RequestParam("type")int type){
        return orderService.getOrderList(curran,type);
    }

    /*
    * 查询商家订单列表
    * */
    @GetMapping(("/sell"))
    public Result getSellOrderList(@RequestParam(value = "curran", defaultValue = "1") Integer curran,@RequestParam("type")int type){
        return orderService.getSellOrderList(curran,type);

    }

    /*
    * 查询订单详情
    * */
    @GetMapping("/info/{id}")
    public Result getOrderInfo(@PathVariable("id") Long id){
        return orderService.getOrderInfo(id);
    }


}
