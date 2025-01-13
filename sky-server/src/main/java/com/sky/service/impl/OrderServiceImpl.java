package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //先查异常 购物车空不 地址空不
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook addressBook = addressBookMapper.getById(addressBookId);
        if(addressBook==null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        if(shoppingCarts==null || shoppingCarts.isEmpty()){
            throw  new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //可以下单，那就先放订单信息
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        String address = addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail();
        orders.setAddress(address);
        //插入后要返回主键值 方便订单详情插入
        orderMapper.submitOrder(orders);

        //准备根据购物车插入订单详情信息
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for(ShoppingCart cart:shoppingCarts){
            OrderDetail orderDetail1 = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail1);
            orderDetail1.setOrderId(orders.getId());
            orderDetailList.add(orderDetail1);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        //插入之后清空一下我们的购物车
        shoppingCartMapper.delete(userId);
        //准备返回数据
        OrderSubmitVO orderSubmitVO = new OrderSubmitVO();
        orderSubmitVO.setOrderTime(orders.getOrderTime());
        orderSubmitVO.setOrderAmount(orders.getAmount());
        orderSubmitVO.setOrderNumber(orders.getNumber());
        orderSubmitVO.setId(orders.getId());
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
        //生成空json，跳过微信支付
        JSONObject jsonObject = new JSONObject();
        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
/*      // 模拟支付成功，更新数据库订单状态
        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumberAndUserId(ordersPaymentDTO.getOrderNumber(), userId);
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);*/
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 取消订单
     * @param id
     */
    @Override
    public void cancelOrder(Long id) {
        //要有健壮性，不能只考虑功能的实现，还要安全，订单还是要先查存在的不，然后也不是所有订单状态都能取消的
        Orders orders = orderMapper.getById(id);
        if(orders==null)
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        if(orders.getStatus()>2)
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        //现在终于才可以正常取消了，我们不涉及掉接口退款，直接改状态
        orders.setStatus(Orders.CANCELLED);
        orders.setPayStatus(Orders.REFUND);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason("用户取消");
        orderMapper.update(orders);
    }

    /**
     * 查看订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO getOrderDetail(Long id) {
        Orders order = orderMapper.getById(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.listByOrderId(order.getId());
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 客户端的分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        //准备返回数据
        List<OrderVO> list = new ArrayList<>();

        // 查询出订单明细，并封装入OrderVO进行响应
        if (page != null && page.getTotal() > 0) {
            for (Orders order : page) {
                Long orderId = order.getId();// 订单id

                // 查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.listByOrderId(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);
                orderVO.setOrderDetailList(orderDetails);

                list.add(orderVO);
            }
        }
        //gettotal还用人家的，自己构造一个响应列表就行了
        return new PageResult(page.getTotal(), list);
    }

    /**
     * 服务端的分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult serverPageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        //准备返回数据
        List<OrderVO> list = new ArrayList<>();

        // 查询出订单明细，并封装入OrderVO进行响应
        if (page != null && page.getTotal() > 0) {
            for (Orders order : page) {
                Long orderId = order.getId();// 订单id

                // 查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.listByOrderId(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);
                orderVO.setOrderDetailList(orderDetails);
                // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
                List<String> orderDishList = orderDetails.stream().map(x -> {
                    String orderDish = x.getName() + "*" + x.getNumber() + ";";
                    return orderDish;
                }).collect(Collectors.toList());
                orderVO.setOrderDishes(String.join("",orderDishList));
                list.add(orderVO);
            }
        }
        //gettotal还用人家的，自己构造一个响应列表就行了
        return new PageResult(page.getTotal(), list);
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    @Transactional
    public void repeat(Long id) {
        List<OrderDetail> orderDetailList = orderDetailMapper.listByOrderId(id);
        //有相关的订单详情才行
        if(orderDetailList==null || orderDetailList.isEmpty())
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        for(OrderDetail orderDetail:orderDetailList){
            ShoppingCart shoppingCart =new ShoppingCart();
            BeanUtils.copyProperties(orderDetail,shoppingCart);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setUserId(BaseContext.getCurrentId());
            //这里我觉得应该去查菜品和套餐是否还在起售，有没起售的不能加入购物车
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 接单
     * @param id
     */
    @Override
    public void confirmOrder(Long id) {
        Orders orders = orderMapper.getById(id);
        if(orders==null)
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        if(orders.getStatus()!=2)
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        orders.setId(id);
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersCancelDTO
     */
    @Override
    public void rejectOrder(OrdersCancelDTO ordersCancelDTO) {
        //本来应该退款，我们没收钱，直接改状态就行
        Orders orders = orderMapper.getById(ordersCancelDTO.getId());
        if(orders==null)
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        if(orders.getStatus()!=2)
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        if(orders.getPayStatus()==Orders.PAID)
            orders.setPayStatus(Orders.REFUND);
        orders.setRejectionReason(orders.getRejectionReason());
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public OrderStatisticsVO statistic() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(orderMapper.statisticByStatus(Orders.CONFIRMED));
        orderStatisticsVO.setToBeConfirmed(orderMapper.statisticByStatus(Orders.TO_BE_CONFIRMED));
        orderStatisticsVO.setDeliveryInProgress(orderMapper.statisticByStatus(Orders.DELIVERY_IN_PROGRESS));
        return orderStatisticsVO;
    }

    @Override
    public void adminCancelOrder(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = orderMapper.getById(ordersCancelDTO.getId());
        if(orders==null)
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        //付钱了就给他退 没付钱就直接取消
        if(orders.getPayStatus()==Orders.PAID) {
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(orders.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {
        Orders orders = orderMapper.getById(id);
        if(orders==null)
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        if(orders.getStatus()!=Orders.CONFIRMED)
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    @Override
    public void finishOrder(Long id) {
        Orders orders = orderMapper.getById(id);
        if(orders==null)
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        if(orders.getStatus()!=Orders.DELIVERY_IN_PROGRESS)
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        orders.setStatus(Orders.COMPLETED);
        orderMapper.update(orders);
    }
}
