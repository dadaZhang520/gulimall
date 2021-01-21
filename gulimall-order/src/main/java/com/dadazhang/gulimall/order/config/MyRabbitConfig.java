package com.dadazhang.gulimall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
public class MyRabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter createMessageConverter() {

        return new Jackson2JsonMessageConverter();
    }

    /**
     * 设置rabbitTemplate中发送时的回调
     * 1.）confirmCallback 发送到exchange中的回调
     * 2.）returnCallback 发送到queue中的回调
     */
    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *
             * @param correlationData 关联的数据
             * @param ack exchange是否确认收到
             * @param cause 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                log.info("关联数据correlationData: {};mq是否受到ack: {};接受失败原因: {}", correlationData, ack, cause);
            }
        });

        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             *
             * @param message 发送到队列的消息
             * @param replyCode 回复的Code
             * @param replyText 回复的文本
             * @param exchange 发送的交换机
             * @param roundKey 发送的路由Key
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String roundKey) {
                log.info("发送到队列的消息message: {};队列回调的Code: {};队列回调的文本: {};发送的交换机: {};发送到的路由Key: {}",
                        message, replyCode, replyText, exchange, roundKey);
            }
        });
    }
}
