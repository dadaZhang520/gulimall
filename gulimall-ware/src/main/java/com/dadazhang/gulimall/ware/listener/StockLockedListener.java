package com.dadazhang.gulimall.ware.listener;

import com.dadazhang.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.dadazhang.gulimall.ware.service.WareSkuService;
import com.dadazhang.common.to.OrderTo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@RabbitListener(queues = "stock.release.queue")
@Service
public class StockLockedListener {

    @Autowired
    WareSkuService wareSkuService;

    @RabbitHandler
    public void handlerStockLock(WareOrderTaskDetailEntity detailEntity, Message message, Channel channel) throws IOException {
        //接受到异常就证明库存解锁失败，需要重新就消息放在队列中
        try {
            wareSkuService.unStockLock(detailEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
           /*
           channel.basicNack(
           long message.getMessageProperties().getDeliveryTag(), 拒绝消息的tag
           multi false, 是否批量拒绝
           queue true); 是否重新放到队列
           */
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    /**
     * 处理取消的订单消息
     */
    @RabbitHandler
    public void handlerStockLock(OrderTo orderTo, Message message, Channel channel) throws IOException {
        //接受到异常就证明库存解锁失败，需要重新就消息放在队列中
        try {
            wareSkuService.unStockLock(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
           /*
           channel.basicNack(
           long message.getMessageProperties().getDeliveryTag(), 拒绝消息的tag
           multi false, 是否批量拒绝
           queue true); 是否重新放到队列
           */
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

}
