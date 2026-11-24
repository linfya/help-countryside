package com.eng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eng.util.Result;
import com.eng.domain.dto.ItemCommentsDTO;
import com.eng.domain.po.ItemComments;

public interface ItemCommentsService extends IService<ItemComments> {

    Result sendComments(ItemCommentsDTO blogCommentsDTO);

    Result getItemCommentsList(Long itemId);
}
