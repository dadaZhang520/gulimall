package com.dadazhang.gulimall.ware.config;

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

/*    @RabbitListener(queues = "stock.release.queue")
    public void handler(){

    }*/

    @Bean
    public Exchange stockEventExchange() {
        //String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange("stock-event-exchange", true, false);
    }

    @Bean
    public Queue stockReleaseQueue() {
        //String name, boolean durable, boolean exclusive, boolean autoDelete
        return new Queue("stock.release.queue", true, false, false);
    }

    @Bean
    public Queue stockDelayQueue() {
        //String name, boolean durable, boolean exclusive, boolean autoDelete,Map<String, Object> arguments
        HashMap<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "stock-event-exchange");
        args.put("x-dead-letter-routing-key", "stock.release.queue");
        args.put("x-message-ttl", 1920000);
//        args.put("x-message-ttl", 17000);
        return new Queue("stock.delay.queue", true, false, false, args);
    }

    @Bean
    public Binding releaseQueueBinding() {
       /* String destination,
         Binding.DestinationType destinationType,
         String exchange,
         String routingKey,
         @Nullable Map<String, Object> arguments*/
        return new Binding("stock.release.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.queue",
                null);
    }

    @Bean
    public Binding delayQueueBinding() {
       /* String destination,
         Binding.DestinationType destinationType,
         String exchange,
         String routingKey,
         @Nullable Map<String, Object> arguments*/
        return new Binding("stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.delay.queue",
                null);
    }
}
