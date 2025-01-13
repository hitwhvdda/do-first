package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import com.sky.service.OrderService;
import com.sky.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/order")
@Api(tags = "C端订单接口")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;
    @PostMapping("/submit")
    @ApiOperation("订单提交")
    public Result<OrderSubmitVO> submitOrder(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("订单提交 {}",ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        //模拟支付成功 更新数据库订单状态
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        return Result.success(orderPaymentVO);
    }
    @PutMapping("/cancel/{id}")
    @ApiOperation("订单取消")
    public Result cancelOrder(@PathVariable Long id){
        log.info("订单取消{}",id);
        orderService.cancelOrder(id);
        return Result.success();
    }

    @GetMapping("/orderDetail/{id}")
    @ApiOperation("获取订单详细信息")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id){
        log.info("获取订单详细信息{}",id);
        OrderVO orderVO= orderService.getOrderDetail(id);
        return Result.success(orderVO);
    }
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("历史订单查询:{}",ordersPageQueryDTO);
        PageResult pageResult= orderService.pageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result orderAgain(@PathVariable Long id){
        log.info("再来一单");
        //本质是再加到购物车里面
        orderService.repeat(id);
        return Result.success();
    }


}
