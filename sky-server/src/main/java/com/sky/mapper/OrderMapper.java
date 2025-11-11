package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void insert(Orders order);

    @Select("select * from orders where status=#{status} and order_time<#{orderTime}")
    List<Orders> getOrdersByStatus(Integer status, LocalDateTime orderTime);

    void update(Orders e);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id=#{orderId}")
    Orders getById(Long id);

    @Select("select count(*) from orders where  status=#{toBeConfirmed}")
    Integer getStatusCount(Integer toBeConfirmed);

    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String outTradeNo);
    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{check_out_time} where number = #{orderNumber}")
    void updateStatus(int orderStatus, int orderPaidStatus, LocalDateTime check_out_time, String orderNumber);

    BigDecimal sumByMap(Map m);

    Integer countByMap(Map m);

    List<GoodsSalesDTO> getSalesTop10(Map map);
}
