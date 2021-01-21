package com.dadazhang.common.constant;

public class MQConstant {

    public static final String RABBITMQ_ORDER_EXCHANGE = "order-event-exchange";

    public static final String RABBITMQ_STOCK_EXCHANGE = "stock-event-exchange";

    public static final String RABBITMQ_ORDER_DELAY_ROUTING_KEY = "order.delay.queue";

    public static final String RABBITMQ_ORDER_RELEASE_ROUTING_KEY = "order.release.queue";

    public static final String RABBITMQ_ORDER_SECKILLROUTING_KEY = "order.seckill.queue";

    public static final String RABBITMQ_STOCK_DELAY_ROUTING_KEY = "stock.delay.queue";

    public static final String  RABBITMQ_STOCK_RELEASE_ROUTING_KEY = "stock.release.queue";
}
