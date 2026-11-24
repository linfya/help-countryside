package com.eng.controller;


import com.eng.util.Result;
import com.eng.domain.dto.CartDTO;
import com.eng.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "*")
public class CartController {


    @Autowired
    private CartService cartService;

    /*
     * 添加购物车
     * */
    @PostMapping("/add")
    public Result addCart(@RequestBody CartDTO cartDTO){
        return cartService.addCart(cartDTO);
    }


    /*
     *
     * 查看购物车列表
     * */
    @GetMapping("/list")
    public Result cartList(@RequestParam(value = "curran", defaultValue = "1") Integer curran){
        return cartService.cartList(curran);
    }

    /*
    *
    * 删除购物车条目
    * */
    @DeleteMapping("/{id}")
    public Result deleteCart(@PathVariable("id") Long id){
        cartService.removeById(id);
        return Result.ok();
    }


    /*
    *
    * 批量删除购物车条目
    * */
    @DeleteMapping
    public Result deleteCartList(@RequestParam("ids") List<Long> ids){
        return cartService.deleteCarList(ids);
    }

}
