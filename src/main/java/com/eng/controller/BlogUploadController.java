package com.eng.controller;

import com.eng.util.Result;
import com.eng.domain.dto.BlogDTO;
import com.eng.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
@CrossOrigin(origins = "*")
public class BlogUploadController {
    @Autowired
    private BlogService blogService;



    /*
     * 编辑推文
     * */
    @GetMapping("/blog")
    public Result getDraft(){
        return blogService.getOrCreateDraft("image");
    }


    /*
     * 上传推文图片
     * */
    @PostMapping("/blog/images")
    public Result uploadFile(@RequestParam(value = "file",required = false) MultipartFile file
    ){
        Result result = blogService.uploadFile(file);
        return Result.ok(result);
    }


    /*
     * 修改推文
     * */
    @PutMapping("/blog/update")
    public Result updateBlog(@RequestBody BlogDTO blogDTO){
        return blogService.updateBlog(blogDTO,"image");
    }

    /*
     * 上传推文
     * */
    @PutMapping("/blog/finish")
    public Result uploadFinish(@RequestBody BlogDTO blogDTO){
        return blogService.uploadFinish(blogDTO);
    }

    /*
    * 删除推文图片
    * */
    @DeleteMapping()
    public Result deleteBlog(@RequestParam("url") String url){
        return blogService.deleteBlog(url);
    }
}
