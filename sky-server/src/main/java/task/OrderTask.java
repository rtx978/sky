package task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    OrderMapper orderMapper;
    @Scheduled(cron="0 * * * *")
    public void processTimeoutOrder(){
        log.info("开始处理超时订单:{}",LocalDateTime.now());
        List<Orders> ordersList = orderMapper.getOrdersByStatus(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-16) );
        for(Orders e:ordersList){
            e.setStatus(Orders.CANCELLED);
            e.setCancelReason("支付超时");
            e.setCancelTime(LocalDateTime.now());
            orderMapper.update(e);
        }
    }
    @Scheduled(cron="0 0 1 * *")
    public void processDeliveryTimeOrder(){
        log.info("开始处理派送中的订单: {}",LocalDateTime.now());
        List<Orders> ordersList = orderMapper.getOrdersByStatus(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusHours(-1) );
        for(Orders e:ordersList){
            e.setStatus(Orders.COMPLETED);
            orderMapper.update(e);
        }

    }
}
