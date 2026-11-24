package com.eng.config;

import com.eng.service.impl.OrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DelayQueueSchedule {
    @Autowired
    private OrderServiceImpl orderService;


    // 每隔一段时间进行轮询并处理延迟消息
    @Scheduled(fixedRate = 1000)
    public void pollAndProcessDelayedMessages() {
        orderService.pollAndProcessDelayedMessages();
    }
}