package com.eng.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eng.util.Result;
import com.eng.util.UserLocal;
import com.eng.domain.dto.ItemCommentsDTO;
import com.eng.domain.dto.UserDTO;
import com.eng.domain.po.ItemComments;
import com.eng.domain.po.Order;
import com.eng.domain.po.User;
import com.eng.mapper.ItemCommentsMapper;
import com.eng.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.eng.util.SysConstants.PAY_COMMENT;

@Service
public class ItemCommentsServiceImpl extends ServiceImpl<ItemCommentsMapper, ItemComments> implements ItemCommentsService {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;


    @Autowired
    private ItemService itemService;

    @Override
    public Result sendComments(ItemCommentsDTO itemCommentsDTO) {
        UserDTO user = UserLocal.getUser();
        if (user == null){
            return Result.fail("未登录");
        }
        ItemComments itemComments = BeanUtil.copyProperties(itemCommentsDTO, ItemComments.class);
        Order order = orderService.query().eq("id", itemCommentsDTO.getOrderId()).one();
        itemComments.setItemId(order.getItemId());
        itemComments.setUserId(user.getUserId());
        save(itemComments);
        orderInfoService.updateStatus(itemCommentsDTO.getOrderId(),6,PAY_COMMENT, LocalDateTime.now());
        return Result.ok("评论成功");
    }

    @Override
    public Result getItemCommentsList(Long itemId) {
        List<ItemComments> item = query()
                .orderByDesc("create_time")
                .eq("item_id", itemId)
                .list();
        for (ItemComments it :
                item) {
            User user = userService.getById(it.getUserId());
            it.setUserName(user.getUserName());
            it.setIcon(user.getIcon());
        }
        return Result.ok(item);

    }

}
