package com.eng.controller;


import com.eng.util.Result;
import com.eng.domain.dto.BlogCommentsDTO;
import com.eng.service.BlogCommentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
@CrossOrigin(origins = "*")
public class BlogCommentsController {

    @Autowired
    private BlogCommentsService blogCommentsService;


    /*
    *
    * 发表评论
    * */
    @PostMapping("/send")
    public Result sendComments(@RequestBody BlogCommentsDTO blogCommentsDTO){
        return blogCommentsService.sendComments(blogCommentsDTO);
    }

    /*
    *
    * 点赞评论
    * */
    @PutMapping("/{id}")
    public Result commentsLike(@PathVariable("id") Long CommentId){
        return blogCommentsService.commentsLike(CommentId);
    }

}
