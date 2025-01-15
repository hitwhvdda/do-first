package com.sky.service;

import com.sky.dto.*;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.*;

import java.util.List;

public interface OrderService {
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 取消订单
     * @param id
     */
    void cancelOrder(Long id);

    OrderVO getOrderDetail(Long id);

    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    void repeat(Long id);

    PageResult serverPageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    void confirmOrder(Long id);

    void rejectOrder(OrdersCancelDTO ordersCancelDTO);

    OrderStatisticsVO statistic();

    void adminCancelOrder(OrdersCancelDTO ordersCancelDTO);

    void delivery(Long id);

    void finishOrder(Long id);

    void reminder(Long id);
}
