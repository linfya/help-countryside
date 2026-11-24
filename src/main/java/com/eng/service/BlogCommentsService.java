package com.eng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eng.util.Result;
import com.eng.domain.dto.BlogCommentsDTO;
import com.eng.domain.po.BlogComments;

public interface BlogCommentsService extends IService<BlogComments> {

    Result sendComments(BlogCommentsDTO blogCommentsDTO);

    Result commentsLike(Long id);
}
