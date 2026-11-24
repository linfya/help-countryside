package com.eng.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eng.util.Result;
import com.eng.util.TencentCosUtil;
import com.eng.util.UserLocal;
import com.eng.domain.dto.*;
import com.eng.domain.po.*;
import com.eng.domain.vo.BlogVO;
import com.eng.domain.vo.VideosVO;
import com.eng.mapper.BlogMapper;
import com.eng.service.BlogService;
import com.eng.service.ItemService;
import com.eng.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.eng.util.RedisConstants.*;
import static com.eng.util.SysConstants.*;
import static com.eng.domain.PageQuery.DEFAULT_PAGE_NUM;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private BlogCommentsServiceImpl blogCommentsService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ItemService itemService;

    @Autowired
    private TencentCosUtil tencentCosUtil;

    @Override
    public Result uploadFile(MultipartFile file) {
        int length = file.getOriginalFilename().length();
        String suffix = file.getOriginalFilename()
                .substring(file.getOriginalFilename().lastIndexOf(".") + 1,length);
        if (!"jpg,jpeg,gif,png".toUpperCase().contains(suffix.toUpperCase())){
            return Result.fail("文件格式错误");
        }

        ArrayList<String> images = new ArrayList<>();
        UserDTO user = UserLocal.getUser();
        Long userId = user.getUserId();
        Blog one = lambdaQuery()
                .eq(Blog::getUserId, userId)
                .eq(Blog::getUploadType,"image")
                .eq(Blog::getStatus, 0)
                .one();
        if(one.getImages() != null){
            List<String> list = JSONUtil.parseArray(one.getImages()).toList(String.class);
            images.addAll(list);
        }
        String upload = tencentCosUtil.upload(file,"blogFile/" + userId + "/");
        images.add(upload);
        String jsonStr = JSONUtil.toJsonStr(images);
        lambdaUpdate()
                .set(Blog::getImages,jsonStr)
                .eq(Blog::getUploadType,"image")
                .eq(Blog::getUserId,userId)
                .eq(Blog::getStatus,0)
                .update();
        return Result.ok(upload);
    }

    @Override
    public Result uploadVideosFile(MultipartFile file) {
        int length = file.getOriginalFilename().length();
        String suffix = file.getOriginalFilename()
                .substring(file.getOriginalFilename().lastIndexOf(".") + 1,length);
        if (!"mp4,rmvb,mkv,avi".toUpperCase().contains(suffix.toUpperCase())){
            return Result.fail("文件格式错误");
        }
        UserDTO user = UserLocal.getUser();
        Long userId = user.getUserId();
        VideosVO videosVO = tencentCosUtil.uploadVideos(file, "blogFile/" + userId + "/");
        lambdaUpdate()
                .set(Blog::getImages,videosVO.getVideosUrl())
                .set(Blog::getFirstImage,videosVO.getVideosImageUrl())
                .eq(Blog::getUserId,userId)
                .eq(Blog::getUploadType,"video")
                .eq(Blog::getStatus,0)
                .update();
        return Result.ok(videosVO.getVideosImageUrl());
    }

    @Override
    public Result videoMessage(VideoMessageDTO messageDTO,Long id) {
        String jsonStr = JSONUtil.toJsonStr(messageDTO);
        stringRedisTemplate.opsForZSet().add(VIDEO_MESSAGE_KEY + id,jsonStr,messageDTO.getMessageTime());
        return Result.ok();
    }

    @Override
    public Result getVideoMessage(Long id) {
        Set<String> messages = stringRedisTemplate.opsForZSet().range(VIDEO_MESSAGE_KEY + id, 0, -1);
        ArrayList<VideoMessageDTO> messageDTOS = new ArrayList<>();
        for (String s :
                messages) {
            VideoMessageDTO message = JSONUtil.toBean(s, VideoMessageDTO.class);
            messageDTOS.add(message);
        }
        String jsonStr = JSONUtil.toJsonStr(messageDTOS);
        return Result.ok(jsonStr);
    }


    @Override
    public Result deleteBlog(String url) {
        Long userId = UserLocal.getUser().getUserId();
        Blog one = lambdaQuery()
                .eq(Blog::getUserId, userId)
                .eq(Blog::getStatus, 0)
                .one();
        List<String> list = JSONUtil.parseArray(one.getImages()).toList(String.class);
        boolean remove = list.remove(url);
        if (!remove){
            return Result.fail("失败");
        }
        String jsonStr = JSONUtil.toJsonStr(list);
        lambdaUpdate()
                .set(Blog::getImages, jsonStr)
                .eq(Blog::getUserId, userId)
                .eq(Blog::getUploadType,"image")
                .eq(Blog::getStatus, 0)
                .update();
        return Result.ok();
    }

    public Result deleteVideo(){
        Long userId = UserLocal.getUser().getUserId();
        lambdaUpdate()
                .set(Blog::getImages,"")
                .set(Blog::getFirstImage,"")
                .eq(Blog::getUserId, userId)
                .eq(Blog::getUploadType,"video")
                .eq(Blog::getStatus, 0)
                .update();
        return Result.ok();
    }


    @Override
    public Result getOrCreateDraft(String UploadType) {
        UserDTO user = UserLocal.getUser();
        Long userId = user.getUserId();
        Blog userBlog = query().eq("user_id", userId).eq("status",0).eq("upload_type",UploadType).one();
        if (userBlog == null){
            save(new Blog(null,userId,null,null,null,null,"",0,0,null,null,0,UploadType));
            return Result.ok();
        }
        BlogVO blogVO = BeanUtil.copyProperties(userBlog, BlogVO.class);
        return Result.ok(blogVO);
    }

    @Override
    public Result updateBlog(BlogDTO blogDTO,String UploadType){
        UserDTO user = UserLocal.getUser();
        Long userId = user.getUserId();
        boolean update = lambdaUpdate()
                .set(Blog::getTitle, blogDTO.getTitle())
                .set(Blog::getContent, blogDTO.getContent())
                .set(Blog::getType, blogDTO.getType())
                .eq(Blog::getUserId,userId)
                .eq(Blog::getUploadType,UploadType)
                .eq(Blog::getStatus,0)
                .update();
        if (!update){
            return Result.fail("保存失败");
        }
        return Result.ok("保存成功");

    }

    @Override
    public Result uploadFinish(BlogDTO blogDTO) {
        UserDTO user = UserLocal.getUser();
        Long userId = user.getUserId();
        Blog one = lambdaQuery()
                .eq(Blog::getUserId, userId)
                .eq(Blog::getStatus, 0)
                .eq(Blog::getUploadType,"image")
                .one();
        if (one.getImages() == null){
            return Result.fail("图片不能为空");
        }
        String first = firstImages(one);
        boolean update = lambdaUpdate()
                .set(Blog::getTitle, blogDTO.getTitle())
                .set(Blog::getContent, blogDTO.getContent())
                .set(Blog::getFirstImage,first)
                .set(Blog::getType, blogDTO.getType())
                .set(Blog::getCreateTime,DateUtil.now())
                .set(Blog::getUpdateTime,DateUtil.now())
                .set(Blog::getStatus,1)
                .eq(Blog::getUploadType,"image")
                .eq(Blog::getStatus,0)
                .eq(Blog::getUserId,userId)
                .update();
        userInfoService.update().setSql("works = works + 1").eq("user_id",userId).update();
        if (!update){
            return Result.fail("上传失败");
        }
        return Result.ok("上传成功");

    }

    public Result uploadVideoFinish(BlogDTO blogDTO) {
        UserDTO user = UserLocal.getUser();
        Long userId = user.getUserId();
        boolean update = lambdaUpdate()
                .set(Blog::getTitle, blogDTO.getTitle())
                .set(Blog::getContent, blogDTO.getContent())
                .set(Blog::getType, blogDTO.getType())
                .set(Blog::getCreateTime,DateUtil.now())
                .set(Blog::getUpdateTime,DateUtil.now())
                .set(Blog::getStatus,1)
                .eq(Blog::getStatus,0)
                .eq(Blog::getUploadType,"video")
                .eq(Blog::getUserId,userId)
                .update();
        userInfoService.update().setSql("works = works + 1").eq("user_id",userId).update();
        if (!update){
            return Result.fail("上传失败");
        }
        return Result.ok("上传成功");

    }

    @Override
    public Result blogLike(Long BlogId, String type) {
        Long userId = UserLocal.getUser().getUserId();
        //1.查询点赞状态码
        if (type.equals("liked")){
            Blog byId = getById(BlogId);
            Long blogUserId = byId.getUserId();
            Boolean isMember = stringRedisTemplate.opsForSet().isMember(BLOG_LIKED_KEY + BlogId, userId.toString());
            if (BooleanUtil.isFalse(isMember)){
                boolean liked = update().setSql("liked = liked + 1").eq("id", BlogId).update();
                if (liked){
                    setToAdd(BLOG_LIKED_KEY,USER_LIKED_BLOG,BlogId,userId);
                }
                return Result.ok("点赞成功");
            }else {
                boolean liked = update().setSql("liked = liked - 1").eq("id", BlogId).update();
                if (liked){
                    removeToAdd(BLOG_LIKED_KEY,USER_LIKED_BLOG,BlogId,userId);
                }
                return Result.fail("消除点赞成功");
            }
        }else if (type.equals("collect")){
            return collect(BLOG_COLLECT_KEY, USER_COLLECT_BLOG, BlogId, userId);
        }else {
            return collect(ITEM_COLLECT_KEY, USER_COLLECT_ITEM, BlogId, userId);
        }
        /*return Result.fail("类型选择错误");*/
    }

    private Result collect(String blogOrItem,String userKey,Long id,Long userId){
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(blogOrItem + id, userId.toString());
        if (BooleanUtil.isFalse(isMember)){
            setToAdd(blogOrItem,userKey,id,userId);
            return Result.ok("收藏成功");
        }else {
            removeToAdd(blogOrItem,userKey,id,userId);
            return Result.fail("消除收藏成功");
        }
    }

    private void setToAdd(String blogOrItem,String userKey,Long id,Long userId){
        stringRedisTemplate.opsForSet().add(blogOrItem + id, userId.toString());
        stringRedisTemplate.opsForList().leftPush(userKey + userId,id.toString());
    }

    private void removeToAdd(String blogOrItem,String userKey,Long id,Long userId){
        stringRedisTemplate.opsForSet().remove(blogOrItem + id, userId.toString());
        stringRedisTemplate.opsForList().remove(userKey + userId,0,id.toString());
    }




    @Override
    public Result searchBlog(Integer curran, SearchDTO searchDTO) {
        String sortType = "";
        if (searchDTO.getSortType() == 0){
            sortType = "liked";
        }else if (searchDTO.getSortType() == 1){
            sortType = "create_time";
        }
        else {
            sortType = "title";
        }
        Page<Blog> page = query()
                .orderByDesc(sortType)
                .eq("status",1)
                .like(!StringUtils.isBlank(searchDTO.getSearchContent()),"title",searchDTO.getSearchContent() )
                .page(new Page<>(curran,DEFAULT_PAGE_NUM));
        List<Blog> records = page.getRecords();
        records.forEach(blog -> {
            this.queryBlogUser(blog);
            this.isMyLike(blog);
        });
        PageDTO<Blog> blogPageDTO = new PageDTO<>(page.getTotal(), page.getPages(), records);
        return Result.ok(blogPageDTO);
    }



    @Override
    public Result myFollowWorks(Integer curran) {
        Long userId = UserLocal.getUser().getUserId();
        List<String> range = stringRedisTemplate.opsForList().range(USER_LIKED_HUMAN + userId, 0, -1);
        Page<Blog> page = query()
                .orderByDesc("create_time")
                .eq("status",1)
                .in("user_id",range)
                .page(new Page<>(curran,DEFAULT_PAGE_NUM));
        List<Blog> records = page.getRecords();
        records.forEach(blog -> {
            this.queryBlogUser(blog);
            this.isMyLike(blog);
        });
        PageDTO<Blog> blogPageDTO = new PageDTO<>(page.getTotal(), page.getPages(), records);
        return Result.ok(blogPageDTO);
    }


    @Override
    public Result queryBlog(Integer curran,String type) {
        Page<Blog> page = new Page<>();
        if (type.equals("关注")){
            UserDTO user = UserLocal.getUser();
            if (user == null){
                return Result.fail("未登录");
            }
            List<String> range = stringRedisTemplate.opsForList().range(USER_LIKED_HUMAN + user.getUserId(), 0, -1);
            if (range.isEmpty()){
                return Result.ok(new PageDTO<String>(0l,0l,range));
            }
            page = query()
                    .orderByDesc("create_time")
                    .eq("status",1)
                    .in("user_id",range)
                    .page(new Page<>(curran,DEFAULT_PAGE_NUM));
        } else if (type.equals("商城")) {
            Page<Item> item = itemService.query()
                    .orderByDesc("update_time")
                    .eq("status",1)
                    .page(new Page<>(curran, DEFAULT_PAGE_NUM));
            List<Item> records = item.getRecords();
            PageDTO<Item> itemPageDTO = new PageDTO<>(item.getTotal(), item.getPages(), records);
            return Result.ok(itemPageDTO);
        } else {
            ArrayList<String> types = new ArrayList<>();
            types.add(GREEN_ORGANIC);
            types.add(GREEN_CULTURE);
            types.add(GREEN_TRAVEL);
            page = query()
                    .orderByDesc("liked")
                    .eq(types.contains(type),"type",type)
                    .eq("status",1)
                    .page(new Page<>(curran,DEFAULT_PAGE_NUM));
        }
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog -> {
            this.queryBlogUser(blog);
            this.isMyLike(blog);
        });
        PageDTO<Blog> blogPageDTO = new PageDTO<>(page.getTotal(), page.getPages(), records);
        return Result.ok(blogPageDTO);
    }


    @Override
    public Result queryBlogById(Long id,Integer curran) {
        Blog blog = getById(id);
        if (blog == null){
            return Result.fail("推文不存在");
        }
        queryBlogUser(blog);
        List<BlogComments> list = blogCommentsService.query()
                .orderByDesc("liked")
                .eq("blog_id", id)
                .list();
        // 获取当前页数据
        //List<BlogComments> records = page.getRecords();
        // 查询用户
        list.forEach(blogComments -> {
            Long CommentUserId = blogComments.getUserId();
            User user = userService.getById(CommentUserId);
            UserInfo byId = userInfoService.getById(CommentUserId);
            blogComments.setCity(byId.getCity());
            blogComments.setUserName(user.getUserName());
            blogComments.setIcon(user.getIcon());
                }
        );
        UserDTO nowUser = UserLocal.getUser();
        if (nowUser == null){
            //PageDTO<BlogComments> blogCommentsPageDTO = new PageDTO<>(page.getTotal(), page.getPages(), records);
            blog.setBlogComments(list);
            return Result.ok(blog);
        }
        Long nowUserUserId = nowUser.getUserId();
        list.forEach(blogComments -> {
            Long commentId = blogComments.getCommentId();
            Boolean commentLikeMember = stringRedisTemplate.opsForSet().isMember(COMMENTS_LIKES_KEY + commentId, nowUserUserId.toString());
            blogComments.setMyLiked(BooleanUtil.isTrue(commentLikeMember));
                }
        );
        Long userId = blog.getUserId();
        Boolean humanLikeMember = stringRedisTemplate.opsForSet().isMember(HUMAN_LIKED_KEY + userId, nowUserUserId.toString());
        blog.setHumanIsMyLiked(BooleanUtil.isTrue(humanLikeMember));
        Boolean member = stringRedisTemplate.opsForSet().isMember(BLOG_COLLECT_KEY + id, nowUserUserId.toString());
        blog.setBlogIsMyCollect(BooleanUtil.isTrue(member));
        //PageDTO<BlogComments> blogCommentsPageDTO = new PageDTO<>(page.getTotal(), page.getPages(), records);
        isMyLike(blog);
        blog.setBlogComments(list);
        return Result.ok(blog);
    }

    @Override
    public Result myWorks(Integer curran) {
        Long userId = UserLocal.getUser().getUserId();
        Page<Blog> page = query()
                .orderByDesc("create_time")
                .eq("user_id",userId)
                .eq("status",1)
                .page(new Page<>(curran,DEFAULT_PAGE_NUM));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        records.forEach(blog -> {
            this.isMyLike(blog);
        });
        PageDTO<Blog> blogPageDTO = new PageDTO<>(page.getTotal(), page.getPages(), records);
        return Result.ok(blogPageDTO);
    }

    @Override
    public Result myLikeBlog(Integer curran) {
        return likedAndCollect(USER_LIKED_BLOG,curran);
    }

    @Override
    public Result myCollectBlog(Integer curran, String type) {
        if (type.equals("推文")) {
            return likedAndCollect(USER_COLLECT_BLOG,curran);
        }else {
            UserDTO user = UserLocal.getUser();
            Long userId = user.getUserId();
            List<String> range = stringRedisTemplate.opsForList().range(USER_COLLECT_ITEM + userId, 0, -1);
            if (range.isEmpty()){
                return Result.ok("列表为空");
            }
            Page<Item> page = itemService.query()
                    .orderByDesc("create_time")
                    .in("id",range)
                    .page(new Page<>(curran,DEFAULT_PAGE_NUM));
            List<Item> records = page.getRecords();
            PageDTO<Item> itemPageDTO = new PageDTO<>(page.getTotal(), page.getPages(), records);
            return Result.ok(itemPageDTO);
        }
    }


    public Result likedAndCollect(String key,Integer curran){
        UserDTO user = UserLocal.getUser();
        Long userId = user.getUserId();
        List<String> range = stringRedisTemplate.opsForList().range(key + userId, 0, -1);
        if (range.isEmpty()){
            return Result.ok("列表为空");
        }
        Page<Blog> page = query()
                .orderByDesc("create_time")
                .in("id",range)
                .page(new Page<>(curran,DEFAULT_PAGE_NUM));
        List<Blog> records = page.getRecords();
        records.forEach(blog -> {
            this.queryBlogUser(blog);
            this.isMyLike(blog);
        });
        PageDTO<Blog> blogPageDTO = new PageDTO<>(page.getTotal(), page.getPages(), records);
        return Result.ok(blogPageDTO);
    }

    private void isMyLike(Blog blog) {
        UserDTO user = UserLocal.getUser();
        if (user == null){
            return;
        }
        Long id = blog.getId();
        Long userId = user.getUserId();
        Boolean member = stringRedisTemplate.opsForSet().isMember(BLOG_LIKED_KEY + id, userId.toString());
        blog.setBlogIsMyLiked(BooleanUtil.isTrue(member));
    }

    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        UserInfo byId = userInfoService.getById(userId);
        blog.setCity(byId.getCity());
        blog.setUserName(user.getUserName());
        blog.setIcon(user.getIcon());
    }

    private String firstImages(Blog blog){
        List<String> list = JSONUtil.parseArray(blog.getImages()).toList(String.class);
        String s = list.get(0);
        return s;
    }

    private boolean tryLock(String key){
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.MICROSECONDS);
        return  BooleanUtil.isTrue(flag);
    }


    private void unlock(String key){
        stringRedisTemplate.delete(key);
    }



}
