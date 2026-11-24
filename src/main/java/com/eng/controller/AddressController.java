package com.eng.controller;


import com.eng.util.Result;
import com.eng.util.UserLocal;
import com.eng.domain.dto.AddressDTO;
import com.eng.domain.po.Address;
import com.eng.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/address")
@RestController
@CrossOrigin(origins = "*")
public class AddressController {

    @Autowired
    private AddressService addressService;

    /*
     * 我的地址
     * */
    @GetMapping()
    public Result myAddress(@RequestParam("curran") Integer curran){
        return addressService.myAddress(curran);
    }

    /*
    *
    * 新增地址
    * */
    @PostMapping("/add")
    public Result addAddress(@RequestBody AddressDTO addressDTO){
        return addressService.addAddress(addressDTO);
    }

    /*
    * 修改地址
    * */
    @PutMapping("/update/{id}")
    public Result uploadAddress(@RequestBody AddressDTO addressDTO,@PathVariable("id") Long id){
        return addressService.uploadAddress(addressDTO,id);
    }
    /*
    * 删除地址
    * */
    @DeleteMapping("/delete/{id}")
    public Result deleteAddress(@PathVariable("id")Long id){
        return addressService.deleteAddress(id);
    }

    /*
    * 修改默认地址
    * */
    @PutMapping("/default/{id}")
    public Result defaultAddress(@PathVariable Long id,int setStatus,int eqStatus){
        return addressService.defaultAddress(id,setStatus,eqStatus);
    }


   /*
   * 根据id查询地址
   * */
    @GetMapping("/{id}")
    public Result queryAddressById(@PathVariable("id") Long id){
        Long userId = UserLocal.getUser().getUserId();
        Address one = addressService.query().eq("user_id", userId).eq("address_id", id).one();
        return Result.ok(one);
    }

    /*
    * 查询默认地址
    * */
    @GetMapping("/default")
    public Result queryDefaultAddress(){
        Long userId = UserLocal.getUser().getUserId();
        Address one = addressService.query().eq("user_id", userId).eq("status", 2).one();
        if (one == null){
            Address one1 = addressService.query().eq("user_id", userId).eq("status", 1).one();
            if (one1 == null){
                Address one2 = addressService.query().eq("user_id", userId).last("limit 1").one();
                return Result.ok(one2);
            }
            return Result.ok(one1);
        }
        return Result.ok(one);
    }

    /*
    * 修改订单地址
    * */
    @PutMapping("/order/{id}")
    public Result updateOrderAddress(@PathVariable("id") Long id){
        addressService.defaultAddress(id,0,2);
        return Result.ok();
    }

    /*
    *
    * */





}
