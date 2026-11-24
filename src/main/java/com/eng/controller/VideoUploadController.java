package com.eng.controller;


import com.eng.util.Result;
import com.eng.domain.dto.BlogDTO;
import com.eng.domain.dto.VideoMessageDTO;
import com.eng.service.impl.BlogServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/video")
public class VideoUploadController {

    @Autowired
    private BlogServiceImpl blogService;


    /*
     * 编辑推文
     * */
    @GetMapping()
    public Result getDraft(){
        return blogService.getOrCreateDraft("video");
    }

    /*
     * 上传推文视频
     * */
    @PostMapping()
    public Result uploadVideosFile(@RequestParam(value = "file",required = false) MultipartFile file
    ){
        Result result = blogService.uploadVideosFile(file);
        return Result.ok(result);
    }

    /*
     * 删除推文视频
     * */
    @DeleteMapping()
    public Result deleteVideo(){
        return blogService.deleteVideo();
    }

    /*
     * 修改推文
     * */
    @PutMapping("/update")
    public Result updateBlog(@RequestBody BlogDTO blogDTO){
        return blogService.updateBlog(blogDTO,"video");
    }

    /*
     * 上传推文
     * */
    @PutMapping("/finish")
    public Result uploadFinish(@RequestBody BlogDTO blogDTO){
        return blogService.uploadVideoFinish(blogDTO);
    }


    /*
    * 发送弹幕
    * */
    @PostMapping("/message")
    public Result videoMessage(@RequestBody VideoMessageDTO messageDTO,@RequestParam("id")Long id){
        return blogService.videoMessage(messageDTO,id);
    }

    /*
    * 获取弹幕列表
    * */
    @GetMapping("/message/list/{id}")
    public Result getVideoMessage(@PathVariable("id")Long id){
        return blogService.getVideoMessage(id);
    }


}
