package com.eng.controller;

import com.eng.util.Result;
import com.eng.domain.dto.ItemDTO;
import com.eng.domain.dto.OrderInfoDTO;
import com.eng.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/item")
@CrossOrigin(origins = "*")
public class ItemController {

    @Autowired
    private ItemService itemService;


    /*
    * 批量查询商品
    * */
    @GetMapping()
    public Result queryItem(@RequestParam(value = "curran", defaultValue = "1") Integer curran){
        return itemService.queryItem(curran);
    }

    /*
    * 查询单个商品
    * */
    @GetMapping("/{id}")
    public Result queryItemById(@PathVariable("id") Long id){
        return itemService.queryItemById(id);
    }


    /*
    * 查询我的商品
    * */
    @GetMapping("/me")
    public Result getMyItem(){
        return itemService.getMyItem();
    }


    /*
    * 发布新的商品
    * */
    @GetMapping("/upload")
    public Result uploadItem(){
        return itemService.uploadItem();
    }



    /*
    * 保存商品
    * */
    @PutMapping("/add/update")
    public Result updateItem(@RequestBody ItemDTO itemDTO){
        return itemService.updateItem(itemDTO);
    }


    /*
    * 完成发布新商品
    * */
    @PutMapping("/add")
    public Result addItem(@RequestBody ItemDTO itemDTO){
        return itemService.addItem(itemDTO);

    }


    /*
    * 上传商品图片
    * */
    @PostMapping("/add/images")
        public Result uploadFile(@RequestParam(value = "file",required = false) MultipartFile file){
        return itemService.uploadFile(file);
    }


    /*
     * 上传详情图片
     * */
    @PostMapping("/add/description")
    public Result uploadDescription(@RequestParam(value = "file",required = false) MultipartFile file){
        return itemService.uploadDescription(file);
    }

    /*
     * 删除商品图片
     * */
    @DeleteMapping("/image")
    public Result deleteImage(@RequestParam("url") String url){
        return itemService.deleteImage(url);
    }

    /*
     * 删除商品详情图片
     * */
    @DeleteMapping("/description")
    public Result deleteDescription(@RequestParam("url") String url){
        return itemService.deleteDescription(url);
    }

    /*
    * 批量扣减库存
    * */
    @PutMapping("/batch")
    public Result batchItem(@RequestBody List<OrderInfoDTO> batchItemDTOS){
        return itemService.bacthItem(batchItemDTOS);
    }



}
