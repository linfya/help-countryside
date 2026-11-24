package com.eng.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eng.util.Result;
import com.eng.util.WebSocketServer;
import com.eng.domain.dto.LocationDTO;
import com.eng.domain.dto.LocationOutDTO;
import com.eng.domain.dto.SendGoodsDTO;
import com.eng.domain.po.Item;
import com.eng.domain.po.Order;
import com.eng.domain.po.OrderInfo;
import com.eng.domain.po.OrderLocation;
import com.eng.mapper.OrderLocationMapper;
import com.eng.service.ItemService;
import com.eng.service.OrderInfoService;
import com.eng.service.OrderLocationService;
import com.eng.service.OrderService;
import com.show.api.ShowApiRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

import static com.eng.util.RedisConstants.USER_NOTICE_KEY;
import static com.eng.util.SysConstants.PAY_SEND;

@Service
public class OrderLocationServiceImpl extends ServiceImpl<OrderLocationMapper, OrderLocation> implements OrderLocationService {

    @Lazy
    @Autowired
    private OrderInfoService orderInfoService;



    @Autowired
    private OrderService orderService;


    @Autowired
    private ItemService itemService;


    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public Result toSendGoods(SendGoodsDTO transportNum) {
        OrderLocation orderLocation = query().eq("order_id", transportNum.getOrderId()).one();
        orderLocation.setLocationNumber(transportNum.getTransportNum());
        update()
                .eq("order_id", transportNum.getOrderId())
                        .set("location_number",transportNum.getTransportNum())
                                .update();
        orderInfoService.updateStatus(transportNum.getOrderId(),3,PAY_SEND, LocalDateTime.now());


        Order order = orderService.query().eq("id", transportNum.getOrderId()).one();
        Item item = itemService.query().eq("id", order.getItemId()).one();
        OrderInfo one = orderInfoService.query().eq("order_id", transportNum.getOrderId()).one();

       /* HashMap<String, Object> like = new HashMap<>();
        like.put("type",1);
        like.put("orderId",transportNum.getOrderId());
        like.put("itemName",item.getName());
        String jsonStr = JSONUtil.toJsonStr(like);*/
        String notice  = "您的订单:" + transportNum.getOrderId() + "已发货";
        try {
            stringRedisTemplate.opsForZSet().add(USER_NOTICE_KEY + one.getUserId(),notice, System.currentTimeMillis());
            WebSocketServer.sendInfo(notice,one.getUserId().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Result.ok();
    }

    @Override
    public Object translationInfo(Long orderId) {
        OrderLocation orderLocation = query().eq("order_id", orderId).one();
        if (orderLocation == null){
            return null;
        }
        String res=new ShowApiRequest("http://route.showapi.com/2650-3","1518701","458c3d8847a247ca8d82c972299d6645")
                .addTextPara("com","auto")
                .addTextPara("nu",orderLocation.getLocationNumber())
                .addTextPara("phone","")
                .addTextPara("delivery_address","")
                .addTextPara("shipping_address","")
                .post();
        LocationOutDTO bean = JSONUtil.toBean(res, LocationOutDTO.class);
        LocationDTO bean1 = JSONUtil.toBean(bean.getShowapi_res_body(), LocationDTO.class);
        return bean1.getData();
    }
}
