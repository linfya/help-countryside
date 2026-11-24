
package com.eng.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eng.util.MyMD5Util;
import com.eng.util.Result;
import com.eng.util.UserLocal;
import com.eng.util.WebSocketServer;
import com.eng.domain.dto.PayDTO;
import com.eng.domain.po.*;
import com.eng.mapper.OrderInfoMapper;
import com.eng.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

import static com.eng.util.RedisConstants.ORDER_TIMEOUT_QUEUE;
import static com.eng.util.RedisConstants.USER_NOTICE_KEY;
import static com.eng.util.SysConstants.PAY_CONFIRM;
import static com.eng.util.SysConstants.PAY_SUCCESS;


@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper,OrderInfo> implements OrderInfoService {

    @Autowired
    private UserService userService;


    @Autowired
    private OrderService orderService;


    @Autowired
    private ItemServiceImpl itemService;


    @Autowired
    private AddressService addressService;

    @Autowired
    private OrderLocationService orderLocationService;


    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public void updateStatus(Long orderId, int status, String type, LocalDateTime localDateTime) {
            lambdaUpdate()
                    .eq(OrderInfo::getOrderId,orderId)
                    .set(OrderInfo::getStatus,status)
                    .set(type.equals("success"),OrderInfo::getPayTime,localDateTime)
                    .set(type.equals("success"),OrderInfo::getCloseTime,null)
                    .set(type.equals("confirm"),OrderInfo::getEndTime,localDateTime)
                    .set(type.equals("cancel"),OrderInfo::getCloseTime,localDateTime)
                    .set(type.equals("send"),OrderInfo::getConsignTime,localDateTime)
                    .set(type.equals("comment"),OrderInfo::getCommentTime,localDateTime)
                    .set(OrderInfo::getUpdateTime,localDateTime)
                    .update();
            orderService.lambdaUpdate()
                    .eq(Order::getId,orderId)
                    .set(Order::getUpdateTime,localDateTime)
                    .update();
    }


    @Override
    public Result toPayment(PayDTO payDTO) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        List<String> orderIds = payDTO.getOrderIds();
        int amount = 0;
        for (String orderId :
                orderIds) {
            OrderInfo one = query().eq("order_id", orderId).one();
            Order order = orderService.query().eq("id", orderId).one();
            if (one.getStatus() == 5){
                return Result.fail("订单已取消");
            }
            if (one.getStatus() == 2){
                return Result.fail("订单已支付");
            }
            amount += order.getPrice();
        }
        Long userId = UserLocal.getUser().getUserId();
        User user = userService.getById(userId);
        boolean password = MyMD5Util.validPassword(payDTO.getPassword(), user.getPayPassword());
        if (!password) {
            return Result.fail("支付密码错误");
        }

        boolean deduct = userService.deduct(userId, amount);
        if (!deduct) {
            return Result.fail("支付失败");
        }
        for (String orderId :
                orderIds) {
            OrderInfo one = query().eq("order_id", orderId).one();
            Address address = addressService.getById(one.getAddressId());
            OrderLocation orderLocation = BeanUtil.copyProperties(address, OrderLocation.class);
            orderLocation.setOrderId(Long.parseLong(orderId));
            orderLocation.setCreateTime(LocalDateTime.now());
            orderLocation.setUpdateTime(LocalDateTime.now());

            orderLocationService.save(orderLocation);
            updateStatus(Long.parseLong(orderId), 2, PAY_SUCCESS, LocalDateTime.now());
            Order order = orderService.getById(orderId);
            itemService.addSold(order.getItemId());
            stringRedisTemplate.opsForZSet().remove(ORDER_TIMEOUT_QUEUE, orderId);

            Item item = itemService.query().eq("id", order.getItemId()).one();
            /*HashMap<String, Object> like = new HashMap<>();
            like.put("type",0);
            like.put("orderId",orderId);
            like.put("itemName",item.getName());
            String jsonStr = JSONUtil.toJsonStr(like);*/
            String notice  = "订单:" + orderId + "付款成功，请尽快发货";
            try {
                stringRedisTemplate.opsForZSet().add(USER_NOTICE_KEY + item.getCreater(),notice, System.currentTimeMillis());
                WebSocketServer.sendInfo(notice,item.getCreater().toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return Result.ok();
    }

    @Override
    public Result toConfirm(Long orderId) {
        updateStatus(orderId,4,PAY_CONFIRM,LocalDateTime.now());
        Order order = orderService.query().eq("id", orderId).one();
        Item one = itemService.query().eq("id", order.getItemId()).one();
        userService.addBalance(one.getCreater(), order.getPrice());


        /*HashMap<String, Object> like = new HashMap<>();
        like.put("type",2);
        like.put("orderId",orderId);
        like.put("price",order.getPrice());
        String jsonStr = JSONUtil.toJsonStr(like);*/
        String notice  = "订单:" + orderId + "确认收货，余额到账" + order.getPrice() + "元";
        try {
            stringRedisTemplate.opsForZSet().add(USER_NOTICE_KEY + one.getCreater(),notice, System.currentTimeMillis());
            WebSocketServer.sendInfo(notice,one.getCreater().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Result.ok();
    }
}
