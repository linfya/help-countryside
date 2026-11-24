package com.eng.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eng.util.Result;
import com.eng.util.UserLocal;
import com.eng.domain.dto.BlogCommentsDTO;
import com.eng.domain.po.BlogComments;
import com.eng.mapper.BlogCommentsMapper;
import com.eng.service.BlogCommentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.eng.util.RedisConstants.COMMENTS_LIKES_KEY;

@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements BlogCommentsService {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Lazy
    @Autowired
    private BlogServiceImpl blogService;

    @Override
    public Result sendComments(BlogCommentsDTO blogCommentsDTO) {
        BlogComments blogComments = BeanUtil.copyProperties(blogCommentsDTO, BlogComments.class);
        Long userId = UserLocal.getUser().getUserId();
        blogComments.setUserId(userId);
        boolean save = save(blogComments);
        boolean update = blogService.update()
                .setSql("comments = comments + 1")
                .eq("id", blogCommentsDTO.getBlogId())
                .update();
        if (!save || !update){
            return Result.fail("评论失败");
        }
        return Result.ok("评论成功");
    }

    @Override
    public Result commentsLike(Long id) {
        Long userId = UserLocal.getUser().getUserId();
        //1.查询点赞状态码
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(COMMENTS_LIKES_KEY + id, userId.toString());
        if (BooleanUtil.isFalse(isMember)){
            boolean liked = update().setSql("liked = liked + 1").eq("id", id).update();
            if (liked){
                stringRedisTemplate.opsForSet().add(COMMENTS_LIKES_KEY + id, userId.toString());
            }
            return Result.ok("点赞成功");
        }else {
            boolean liked = update().setSql("liked = liked - 1").eq("id", id).update();
            if (liked){
                stringRedisTemplate.opsForSet().remove(COMMENTS_LIKES_KEY+ id, userId.toString());
            }
            return Result.fail("取消成功");
        }
    }
}
