package com.dadazhang.gulimall.order.listener;

import com.dadazhang.gulimall.order.entity.OrderEntity;
import com.dadazhang.gulimall.order.service.OrderService;
import com.dadazhang.gulimall.order.util.AlipayTemplate;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@RabbitListener(queues = "order.release.queue")
@Service
public class OrderAutoCancelListener {

    @Autowired
    OrderService orderService;

    @Autowired
    AlipayTemplate alipayTemplate;

    @RabbitHandler
    public void handlerOrderCancel(OrderEntity orderEntity, Message message, Channel channel) throws IOException {
        //执行取消订单的逻辑
        try {
            //取消成功
            orderService.cancelOrder(orderEntity);
            //调用支付宝自动关单功能
            alipayTemplate.tradeClose(orderEntity.getOrderSn());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),
                    false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(),
                    false, true);
        }
    }
}
