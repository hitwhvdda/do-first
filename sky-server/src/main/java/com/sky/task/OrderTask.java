package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;
    /**
     * 处理超时订单
     */
    @Scheduled(cron="0 0/1 * * * ?")
    //@Scheduled(cron = "0/5 * * * * ?" )
    public void processTimeoutOrder(){
        log.info("处理超时订单");
        LocalDateTime localTime = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderMapper.selectByStatusAndTime(Orders.UN_PAID,localTime);
        if(ordersList!=null && !ordersList.isEmpty()){
            for(Orders orders:ordersList){
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelTime(LocalDateTime.now());
                orders.setCancelReason("订单超时，取消");
                orderMapper.update(orders);
            }
        }
    }

    @Scheduled(cron = "0 0 1 1/1 * ? ")
    //@Scheduled(cron = "1/5 * * * * ?" )
    public void processUnfinishedOrder(){
        log.info("处理待配送订单未完成");
        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = orderMapper.selectByStatusAndTime(Orders.DELIVERY_IN_PROGRESS,localDateTime);
        if(ordersList!=null && !ordersList.isEmpty()){
            for(Orders orders:ordersList){
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
