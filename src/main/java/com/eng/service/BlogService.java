package com.eng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eng.domain.dto.BlogDTO;
import com.eng.util.Result;
import com.eng.domain.dto.SearchDTO;
import com.eng.domain.dto.VideoMessageDTO;
import com.eng.domain.po.Blog;
import org.springframework.web.multipart.MultipartFile;

public interface BlogService extends IService<Blog> {
    Result uploadFile(MultipartFile file);

    Result getOrCreateDraft(String UploadType);

    Result updateBlog(BlogDTO blogDTO,String UploadType);

    Result uploadFinish(BlogDTO blogDTO);

    Result blogLike(Long BlogId, String type);

    Result queryBlog(Integer curran,String type);

    Result queryBlogById(Long id,Integer curran);

    Result myWorks(Integer curran);

    Result myLikeBlog(Integer curran);

    Result myCollectBlog(Integer curran, String type);

    Result searchBlog(Integer curran, SearchDTO searchDTO);

    Result deleteBlog(String url);

    Result myFollowWorks(Integer curran);

    Result uploadVideosFile(MultipartFile file);

    Result videoMessage(VideoMessageDTO messageDTO,Long id);

    Result getVideoMessage(Long id);
}
