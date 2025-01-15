package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 提交订单
     * @param orders
     */

    void submitOrder(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);
    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select count(*) from orders where status = #{status}")
    Integer statisticByStatus(Integer status);

    @Select("select * from orders where status=#{status} and order_time < #{localDateTime}")
    List<Orders> selectByStatusAndTime(Integer status, LocalDateTime localDateTime);

    @Select("select * from orders where user_id=#{userId} and number=#{orderNumber}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);

    @Select("select sum(amount) from orders where status=#{status} and order_time between #{begin} and #{end}")
    Double getAmountByDay(Map map);

    Integer getOrderCount(Map map);

    List<GoodsSalesDTO> getGoodSale(LocalDateTime beginTime, LocalDateTime endTime);
}
