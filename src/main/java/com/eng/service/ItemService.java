package com.eng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eng.util.Result;
import com.eng.domain.dto.ItemDTO;
import com.eng.domain.dto.OrderInfoDTO;
import com.eng.domain.po.Item;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ItemService extends IService<Item>{
    Result queryItem(Integer curran);

    Result queryItemById(Long id);

    Result addItem(ItemDTO itemDTO);

    Result bacthItem(List<OrderInfoDTO> batchItemDTOS);

    Result uploadFile(MultipartFile file);

    Result uploadItem();

    Result uploadDescription(MultipartFile file);

    Result updateItem(ItemDTO itemDTO);

    Result deleteImage(String url);

    Result deleteDescription(String url);

    Result getMyItem();

    Result updateMyItem(Long itemId, ItemDTO itemDTO);

    Result uploadMyItemFile(MultipartFile file, Long itemId);

    Result uploadMyItemDescription(MultipartFile file, Long itemId);

    Result deleteMyItemImage(String url, Long itemId);

    Result deleteMyItemDescription(String url, Long itemId);
}
