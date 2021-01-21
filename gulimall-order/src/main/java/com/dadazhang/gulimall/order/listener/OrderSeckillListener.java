package com.dadazhang.gulimall.order.listener;

import com.dadazhang.common.to.mq.SeckillOrderTo;
import com.dadazhang.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
@Service
@RabbitListener(queues = "order.seckill.queue")
public class OrderSeckillListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void handlerOrderSeckill(SeckillOrderTo seckillOrderTo, Message message, Channel channel) throws IOException {
        System.out.println("收到一个秒杀单:" + seckillOrderTo);
        try {
            orderService.createSeckillOrder(seckillOrderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}
