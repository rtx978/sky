package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.github.pagehelper.Page;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    WebSocketServer webSocketServer;
    @Autowired
    WeChatPayUtil weChatPayUtil ;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    AddressBookMapper addressBookMapper;
    @Autowired
    ShoppingCartMapper shoppingCartMapper;
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        int cnt = addressBookMapper.count();
        if(cnt==0){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        ShoppingCart sc=new ShoppingCart();
        sc.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> ls= shoppingCartMapper.list(sc);
        if(ls==null||ls.size()==0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, order);
        order.setOrderTime(LocalDateTime.now());
        order.setPayStatus(1);
        order.setStatus(1);
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setPhone(addressBook.getPhone());
        order.setConsignee(addressBook.getConsignee());
        order.setUserId(BaseContext.getCurrentId());
        order.setAddress(addressBook.getDetail());

        orderMapper.insert(order);
        List<OrderDetail> list=new LinkedList<>();
        for(ShoppingCart e:ls){
            OrderDetail temp=new OrderDetail();
            BeanUtils.copyProperties(e,temp);
            temp.setOrderId(order.getId());
            list.add( temp);
        }
        orderDetailMapper.insertBatch(list);
        shoppingCartMapper.clean(BaseContext.getCurrentId());
        return OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();
    }

    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        OrdersPageQueryDTO temp=new OrdersPageQueryDTO();
        temp.setUserId(BaseContext.getCurrentId());
        temp.setStatus(ordersPageQueryDTO.getStatus());

        Page<Orders> page=orderMapper.pageQuery(ordersPageQueryDTO);
        System.out.println( page);
        List<OrderVO> list=new LinkedList<>();
        if(page!=null&&page.size()>0) {
            for (Orders e : page) {
                long orderId = e.getId();
                List<OrderDetail> oo = orderDetailMapper.getByOrderId(orderId);
                OrderVO tempVO = new OrderVO();
                BeanUtils.copyProperties(e, tempVO);
//                System.out.println( tempVO);
                tempVO.setOrderDetailList(oo);
                list.add(tempVO);
            }
        }
        return new PageResult(page.getTotal(),list);
    }

    @Override
    public OrderVO orderDetail(Long id) {
        Orders or=orderMapper.getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(or,orderVO);
        orderVO.setOrderDetailList(orderDetailMapper.getByOrderId(id));
        return orderVO;
    }


    public void cancel(Long id) throws Exception {
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if(ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            weChatPayUtil.refund(
                    ordersDB.getNumber(), //商户订单号
                    ordersDB.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额

            ordersDB.setPayStatus(Orders.REFUND);
        }
        ordersDB.setStatus(Orders.CANCELLED);
        ordersDB.setCancelReason("用户取消");
        ordersDB.setCancelTime(LocalDateTime.now());
        orderMapper.update(ordersDB);

    }

    @Override
    public void repetition(Long id) {
        List<OrderDetail> ls=orderDetailMapper.getByOrderId(id);
        System.out.println(ls);
        List<ShoppingCart> list=new ArrayList<>();
        for(OrderDetail e:ls){
            ShoppingCart temp=new ShoppingCart();
            BeanUtils.copyProperties(e,temp,"id");
            temp.setUserId(BaseContext.getCurrentId());
            temp.setCreateTime(LocalDateTime.now());
            list.add(temp);
        }
        shoppingCartMapper.insertBatch(list);
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders > page=orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> list=getOrderVOList(page);
        return new PageResult(page.getTotal(),list);
    }

    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = orderMapper.getStatusCount(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.getStatusCount(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.getStatusCount(Orders.DELIVERY_IN_PROGRESS);
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 接单
     *
     * @param ordersConfirmDTO
     */
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();

        orderMapper.update(orders);
    }

    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        Orders or=orderMapper.getById(ordersCancelDTO.getId());
        if(or.getPayStatus()==1){
            String s=weChatPayUtil.refund(
                    or.getNumber(), //商户订单号
                    or.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01)//原订单金额
            );
            log.info("申请退款"+s);
        }
        or.setStatus(Orders.CANCELLED);
        or.setCancelReason(ordersCancelDTO.getCancelReason());
        or.setCancelTime(LocalDateTime.now());
        orderMapper.update(or);
    }
    public void delivery(Long id){
        Orders or=orderMapper.getById(id);
        or.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(or);
    }
    /**
     * 完成订单
     *
     * @param id
     */
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        Orders or=orderMapper.getById(ordersRejectionDTO.getId());
        if(or==null||!or.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw  new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        int payStatus=or.getPayStatus();
//        if(payStatus==Orders.PAID){
//            String refund=weChatPayUtil.refund(
//                    or.getNumber(), //商户订单号
//                    or.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01)//原订单金额
//            );
//            log.info("refund=="+refund);
//        }
        or.setStatus(Orders.CANCELLED);
        or.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        or.setCancelReason(ordersRejectionDTO.getRejectionReason());
        or.setCancelTime(LocalDateTime.now());
        orderMapper.update(or);
    }


    private List<OrderVO> getOrderVOList(Page<Orders> page){
        List<OrderVO> list=new LinkedList<>();
        List<Orders> ordersList=page.getResult();
        if(CollectionUtils.isEmpty(ordersList)==false){
            for(Orders e:ordersList){
                OrderVO tempVO=new OrderVO();
                BeanUtils.copyProperties(e,tempVO);
                String orderDishes =getOrderDishesStr(e);
                tempVO.setOrderDishes(orderDishes);
                tempVO.setOrderDetailList(orderDetailMapper.getByOrderId(e.getId()));
                System.out.println("tempvo== "+tempVO);
                list.add(tempVO);
            }
        }
        return list;
    }
    private String getOrderDishesStr(Orders orders){
        List<OrderDetail> orderDetailList=orderDetailMapper.getByOrderId(orders.getId());
        List< String> ans=orderDetailList.stream().map(x->{
            String s=x.getName()+"*"+x.getNumber()+"份;";
            return s;
        }).collect(Collectors.toList());
        return String.join("",ans);
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

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code","ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        //通过websocket向客户端浏览器推送消息 type orderId content
        int OrderPaidStatus=Orders.PAID;
        int OrderStatus=Orders.TO_BE_CONFIRMED;
        //发现没有将支付时间 check_out属性赋值，所以在这里更新
        LocalDateTime check_out_time = LocalDateTime.now();

        //获取订单号码
        String orderNumber = ordersPaymentDTO.getOrderNumber();

        log.info("调用updateStatus，用于替换微信支付更新数据库状态的问题");
        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, orderNumber);
        Orders temp=orderMapper.getByNumber(orderNumber);
        Map map = new HashMap();
        map.put("type",1);
        map.put("orderId",temp.getId());
        map.put("content","订单号："+temp.getNumber());
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);

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


    public void reminder(Long id) {
        Orders ordersDB = orderMapper.getById(id);
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Map<String, Object> map = new HashMap();
        map.put("type",2);
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号："+ordersDB.getNumber());

        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

}
