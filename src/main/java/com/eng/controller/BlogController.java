package com.eng.controller;


import com.eng.util.Result;
import com.eng.domain.dto.SearchDTO;
import com.eng.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blog")
@CrossOrigin(origins = "*")
public class BlogController {

    @Autowired
    private BlogService blogService;


    /*
     * 搜索首页推文
     * */
    @PostMapping("/search")
    public Result searchBlog(@RequestParam(value = "curran", defaultValue = "1") Integer curran, @RequestBody SearchDTO searchDTO){
        return blogService.searchBlog(curran,searchDTO);

    }

    /*
    * 查询首页推文
    * */
    @GetMapping("/page")
    public Result queryBlog(@RequestParam(value = "curran", defaultValue = "1") Integer curran,@RequestParam("type")String type){
        return blogService.queryBlog(curran,type);

    }

    /*
    * 查询单个推文*/
    @GetMapping("/{id}")
    public Result queryBlogById(@PathVariable("id") Long id,@RequestParam(value = "curran", defaultValue = "1") Integer curran){
        return blogService.queryBlogById(id,curran);
    }


    /*
    * 点赞
    * */
    @PutMapping("/like/{id}")
    public Result blogLike(@PathVariable("id") Long blogId,@RequestParam("type") String type){
        return blogService.blogLike(blogId,type);
    }


    /*
     * 我的作品
     * */
    @GetMapping("/myWorks")
    public Result myWorks(@RequestParam(value = "curran", defaultValue = "1") Integer curran){
        return blogService.myWorks(curran);
    }

    /*
     * 我点赞的作品
     * */
    @GetMapping("/likedBlog")
    public Result myLikeBlog(@RequestParam(value = "curran", defaultValue = "1")Integer curran){
        return blogService.myLikeBlog(curran);
    }

    /*
    * 我收藏的作品
    * */
    @GetMapping("/collectBlog")
    public Result myCollectBlog(@RequestParam(value = "curran", defaultValue = "1")Integer curran,@RequestParam("type") String type){
        return blogService.myCollectBlog(curran,type);
    }

    /*
    * 我关注用户作品
    * */
    @GetMapping("/followWork")
    public Result myFollowWorks(@RequestParam(value = "curran", defaultValue = "1") Integer curran){
        return blogService.myFollowWorks(curran);
    }

}
