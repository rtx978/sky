package com.sky.controller.admin;

import com.sky.dto.*;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.sky.result.PageResult;

@RequestMapping("/admin/order")
@RestController("adminOrderController")
@Api(tags = "订单接口")
@Slf4j
public class OrderController {
    @Autowired
    OrderService orderService;
    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("订单搜索：{}", ordersPageQueryDTO);

        return Result.success(        orderService.conditionSearch(ordersPageQueryDTO));
    }
    @GetMapping("/statistics")
    @ApiOperation("统计接口")
    public Result<OrderStatisticsVO> statistics(){
        return Result.success(orderService.statistics());
    }

    @GetMapping("/details/{id}")
    @ApiOperation("订单详情接口")
    public Result<OrderVO> details(@PathVariable("id") Long id){
        log.info("订单详情接口：{}", id);
        return Result.success(orderService.orderDetail(id));
    }
    @PutMapping("/confirm")
    @ApiOperation("确认接单")
    public Result confirm(@RequestBody OrdersConfirmDTO  id){
        log.info("订单确认：{}", id);
        orderService.confirm(id);
        return Result.success();
    }
    /**
     * 拒单
     *
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        log.info("订单拒单：{}", ordersRejectionDTO);
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) throws   Exception{
        log.info("取消订单：{}", ordersCancelDTO);
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable("id") Long id){
        log.info("派送订单：{}", id);
        orderService.delivery(id);
        return Result.success();
    }
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable("id") Long id) {
        orderService.complete(id);
        return Result.success();
    }

}
