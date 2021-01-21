package com.dadazhang.gulimall.order.config;

import com.dadazhang.common.constant.MQConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class MyRabbitMqConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /*@RabbitListener(queues = "order.release.queue")
    public void handler(){

    }*/

    @Bean
    public Exchange orderEventExchange() {
        //String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange("order-event-exchange", true, false);
    }

    @Bean
    public Queue orderReleaseQueue() {
        //String name, boolean durable, boolean exclusive, boolean autoDelete
        return new Queue("order.release.queue", true, false, false);
    }

    @Bean
    public Queue orderDelayQueue() {
        //String name, boolean durable, boolean exclusive, boolean autoDelete,Map<String, Object> arguments
        HashMap<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "order-event-exchange");
        args.put("x-dead-letter-routing-key", "order.release.queue");
        args.put("x-message-ttl", 1800000);
//        args.put("x-message-ttl", 15000);
        return new Queue("order.delay.queue", true, false, false, args);
    }

    @Bean
    public Queue seckillOrderQueue(){
        return new Queue("order.seckill.queue",true,false,false);
    }

    @Bean
    public Binding orderReleaseQueueBinding() {
       /* String destination,
         Binding.DestinationType destinationType,
         String exchange,
         String routingKey,
         @Nullable Map<String, Object> arguments*/
        return new Binding("order.release.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.queue",
                null);
    }

    @Bean
    public Binding orderDelayQueueBinding() {
       /* String destination,
         Binding.DestinationType destinationType,
         String exchange,
         String routingKey,
         @Nullable Map<String, Object> arguments*/
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.delay.queue",
                null);
    }

    /**
     * 绑定库存释放队列
     */
    @Bean
    public Binding stockReleaseQueueBinding() {
        return new Binding("stock.release.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "stock.release.queue",
                null);
    }

    @Bean
    public Binding seckillOrderQueueBinding() {
        return new Binding("order.seckill.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.seckill.queue",
                null);
    }
}
