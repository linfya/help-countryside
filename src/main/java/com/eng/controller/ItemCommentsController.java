package com.eng.controller;


import com.eng.util.Result;
import com.eng.domain.dto.ItemCommentsDTO;
import com.eng.service.ItemCommentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/itemComments")
@CrossOrigin(origins = "*")
public class ItemCommentsController {

    @Autowired
    private ItemCommentsService itemCommentsService;


    /*
    *
    * 发表评论
    * */
    @PostMapping("/send")
    public Result sendComments(@RequestBody ItemCommentsDTO itemCommentsDTO){
        return itemCommentsService.sendComments(itemCommentsDTO);
    }


    /*
    * 查看评论列表
    * */
    @GetMapping("/list/{id}")
    public Result getItemCommentsList(@PathVariable("id") Long itemId){
        return itemCommentsService.getItemCommentsList(itemId);
    }


}
