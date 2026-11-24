package com.eng.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eng.util.Result;
import com.eng.util.UserLocal;
import com.eng.domain.dto.OrderDTO;
import com.eng.domain.dto.OrderInfoDTO;
import com.eng.domain.dto.PageDTO;
import com.eng.domain.po.*;
import com.eng.domain.vo.OrderInfoVO;
import com.eng.domain.vo.OrderVO;
import com.eng.mapper.OrderMapper;
import com.eng.service.*;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.eng.util.RedisConstants.ORDER_TIMEOUT_QUEUE;
import static com.eng.util.SysConstants.PAY_CANCEL;
import static com.eng.util.SysConstants.PAY_OUT_TIME;
import static com.eng.domain.PageQuery.DEFAULT_PAGE_NUM;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    /** 订单超时扫描的分布式锁 */
    private static final String ORDER_TIMEOUT_LOCK = "lock:order:timeout";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ItemService itemService;

    @Autowired
    private AddressService addressService;


    @Lazy
    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;


    @Autowired
    private OrderLocationService orderLocationServic;


    @Override
    public Result addOrder(OrderDTO orderDTO) {
        Long userId = UserLocal.getUser().getUserId();
        List<OrderInfoDTO> item = orderDTO.getItems();

        Map<Long, Integer> num = item.stream().collect(Collectors.toMap(OrderInfoDTO::getId, OrderInfoDTO::getNum));
        Map<Long, String> remark = item.stream().collect(Collectors.toMap(OrderInfoDTO::getId, OrderInfoDTO::getRemark));
        Set<Long> ids = num.keySet();

        List<Item> items = itemService.query().in("id", ids).list();
        ArrayList<String> OrderIds = new ArrayList<>();

        for (Item i :
                items) {
            Order order = new Order();
            long l = Long.parseLong(RandomUtil.randomNumbers(12));
            order.setId(l);
            order.setNum(num.get(i.getId()));
            order.setItemId(i.getId());
            order.setName(i.getName());
            order.setImage(i.getFirstImage());
            order.setPrice(i.getPrice() * num.get(i.getId()));
            order.setRemark(remark.get(i.getId()));
            save(order);

            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setStatus(1);
            orderInfo.setUserId(userId);
            orderInfo.setAddressId(orderDTO.getAddressId());
            orderInfo.setCreateTime(LocalDateTime.now());
            orderInfo.setCloseTime(LocalDateTime.now().plusMinutes(PAY_OUT_TIME));
            orderInfo.setOrderId(order.getId());
            orderInfo.setIsDelete(0);
            orderInfoService.save(orderInfo);
            OrderIds.add(order.getId().toString());
            addToDelayQueue(order.getId());
        }
        Address one = addressService.query().eq("user_id", userId).eq("address_id", orderDTO.getAddressId()).one();
        if (one.getStatus() == 2){
            addressService.lambdaUpdate()
                    .eq(Address::getAddressId,orderDTO.getAddressId())
                    .eq(Address::getUserId,userId)
                    .set(Address::getStatus,0)
                    .update();
        }
        cartService.deleteCarList(ids);
        try {
            itemService.bacthItem(item);
        } catch (Exception e) {
            throw new RuntimeException("库存不足！");
        }
        return Result.ok(OrderIds);
    }

    @Override
    public Result getOrderList(Integer curran, int type) {
        Long userId = UserLocal.getUser().getUserId();
        List<OrderInfo> list = orderInfoService.query()
                .eq("1,2,3,4,5,6".contains(String.valueOf(type)),"status", type)
                .eq("user_id", userId)
                .eq("is_delete",0)
                .list();
        if (list.isEmpty()){
            return Result.ok(new PageDTO<OrderInfo>(0l,0l,list));
        }
        Map<Long,Integer> orderStatus = new HashMap<>();
        for (OrderInfo orderInfo:list
             ) {
            orderStatus.put(orderInfo.getOrderId(),orderInfo.getStatus());
        }
        Set<Long> orderIds = orderStatus.keySet();
        Page<Order> page = query().orderByDesc("update_time")
                .in("id", orderIds)
                .page(new Page<>(curran, DEFAULT_PAGE_NUM));
        ArrayList<OrderVO> orderVOS = new ArrayList<>();
        for (Order order :
                page.getRecords()) {
            Item one = itemService.query().eq("id", order.getItemId()).one();
            User user = userService.getById(one.getCreater());
            OrderVO orderVO = BeanUtil.copyProperties(order, OrderVO.class);
            orderVO.setUserName(user.getUserName());
            orderVO.setStatus(orderStatus.get(order.getId()));
            orderVOS.add(orderVO);
        }
        PageDTO<OrderVO> orderVOPageDTO = new PageDTO<>(page.getTotal(), page.getPages(), orderVOS);
        return Result.ok(orderVOPageDTO);
    }

    @Override
    public Result getSellOrderList(Integer curran, int type) {
        Long userId = UserLocal.getUser().getUserId();
        List<Item> items = itemService.query().eq("creater", userId).eq("status", 1).list();
        if (items.isEmpty()){
            return Result.ok(new PageDTO<Item>(0l,0l,items));
        }
        ArrayList<Long> itemIds = new ArrayList<>();
        for (Item item:items
             ) {
            itemIds.add(item.getId());
        }
        List<Order> orderList = query().in("item_id", itemIds).list();
        if (orderList.isEmpty()){
            return Result.ok(new PageDTO<Item>(0l,0l,items));
        }
        Map<Long,OrderVO> orderIds = new HashMap<>();
        ArrayList<OrderVO> orderVOS = new ArrayList<>();
        for (Order order :
                orderList) {
            OrderVO orderVO = BeanUtil.copyProperties(order, OrderVO.class);
            orderIds.put(order.getId(),orderVO);
        }
        Set<Long> ids = orderIds.keySet();
        Page<OrderInfo> page = orderInfoService.query()
                .in("order_id", ids)
                .orderByDesc("update_time")
                .eq("1,2,3,4,5,6".contains(String.valueOf(type)), "status", type)
                .eq("is_delete", 0)
                .page(new Page<>(curran, DEFAULT_PAGE_NUM));
        List<OrderInfo> list = page.getRecords();
        for (OrderInfo orderInfo : list) {
            OrderVO orderVO = orderIds.get(orderInfo.getOrderId());
            User user = userService.getById(orderInfo.getUserId());
            orderVO.setUserName(user.getUserName());
            orderVO.setStatus(orderInfo.getStatus());
            orderVOS.add(orderVO);
        }
        PageDTO<OrderVO> orderVOPageDTO = new PageDTO<>(page.getTotal(), page.getPages(), orderVOS);
        return Result.ok(orderVOPageDTO);
    }

    @Override
    public Result getOrderInfo(Long id) {
        Order order = query().eq("id", id).one();
        OrderInfo orderInfo = orderInfoService.query().eq("order_id", id).one();
        Item one = itemService.query().eq("id", order.getItemId()).one();
        User user = userService.getById(one.getCreater());
        Address address = addressService.query().eq("address_id", orderInfo.getAddressId()).one();
        OrderInfoVO orderInfoVO = BeanUtil.copyProperties(order, OrderInfoVO.class);
        orderInfoVO.setPhone(address.getPhone());
        orderInfoVO.setHumanName(address.getHumanName());
        orderInfoVO.setUserArea(address.getUserArea());
        orderInfoVO.setUserAddress(address.getUserAddress());
        orderInfoVO.setPayTime(orderInfo.getPayTime());
        orderInfoVO.setCommentTime(orderInfo.getCommentTime());
        orderInfoVO.setConsignTime(orderInfo.getConsignTime());
        orderInfoVO.setEndTime(orderInfo.getEndTime());
        orderInfoVO.setCloseTime(orderInfo.getCloseTime());
        orderInfoVO.setUserName(user.getUserName());
        orderInfoVO.setStatus(orderInfo.getStatus());
        orderInfoVO.setData(orderLocationServic.translationInfo(id));
        return Result.ok(orderInfoVO);

    }

    @Override
    public Result deleteOrder(Long orderId) {
        orderInfoService.lambdaUpdate()
                .eq(OrderInfo::getOrderId,orderId)
                .set(OrderInfo::getIsDelete,1)
                .update();
        return Result.ok();
    }


    public void addToDelayQueue(Long orderId) {
        stringRedisTemplate.opsForZSet().add(ORDER_TIMEOUT_QUEUE, orderId.toString(), System.currentTimeMillis() + PAY_OUT_TIME *  60000);
    }

    /**
     * 轮询超时未支付的订单并自动取消。
     * 用 Redisson 分布式锁保证多实例部署时同一时刻只有一个节点在处理。
     */
    public void pollAndProcessDelayedMessages() {
        RLock lock = redissonClient.getLock(ORDER_TIMEOUT_LOCK);
        if (!lock.tryLock()) {
            return;
        }
        try {
            Set<String> messages = stringRedisTemplate.opsForZSet()
                    .rangeByScore(ORDER_TIMEOUT_QUEUE, 0, System.currentTimeMillis());
            if (messages == null || messages.isEmpty()) {
                return;
            }
            messages.forEach(this::processMessage);
            stringRedisTemplate.opsForZSet().remove(ORDER_TIMEOUT_QUEUE, messages.toArray());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void processMessage(String message) {
        orderInfoService.updateStatus(Long.parseLong(message), 5, PAY_CANCEL, LocalDateTime.now());
    }

}
