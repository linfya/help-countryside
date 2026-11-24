package com.eng.controller;


import cn.hutool.core.bean.BeanUtil;
import com.eng.util.Result;
import com.eng.domain.dto.ItemDTO;
import com.eng.domain.po.Item;
import com.eng.domain.vo.ItemVO;
import com.eng.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/update")
public class ItemUpdateController {

    @Autowired
    private ItemService itemService;



    /*
     * 查询修改商品
     * */

    @GetMapping()
    public Result getUpdateItem(@RequestParam("id")Long itemId ){
        Item one = itemService.query().eq("id", itemId).one();
        ItemVO itemVO = BeanUtil.copyProperties(one, ItemVO.class);
        return Result.ok(itemVO);
    }

    /*
     * 修改商品
     * */
    @PutMapping("/myItem")
    public Result updateMyItem(@RequestParam("id")Long itemId , @RequestBody ItemDTO itemDTO){
        return itemService.updateMyItem(itemId,itemDTO);
    }


    /*
     * 上传商品图片
     * */
    @PostMapping("/images")
    public Result uploadMyItemFile(@RequestParam(value = "file",required = false) MultipartFile file,@RequestParam("id")Long itemId){
        return itemService.uploadMyItemFile(file,itemId);
    }


    /*
     * 上传详情图片
     * */
    @PostMapping("/description")
    public Result uploadDescription(@RequestParam(value = "file",required = false) MultipartFile file,@RequestParam("id")Long itemId){
        return itemService.uploadMyItemDescription(file,itemId);
    }


    /*
     * 删除商品图片
     * */
    @DeleteMapping("/image")
    public Result deleteImage(@RequestParam("url") String url,@RequestParam("id")Long itemId){
        return itemService.deleteMyItemImage(url,itemId);
    }
    /*
     * 删除商品详情图片
     * */
    @DeleteMapping("/description")
    public Result deleteDescription(@RequestParam("url") String url,@RequestParam("id")Long itemId){
        return itemService.deleteMyItemDescription(url,itemId);
    }


}
