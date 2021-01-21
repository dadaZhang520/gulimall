package com.dadazhang.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dadazhang.common.to.mq.SeckillOrderTo;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.gulimall.order.entity.OrderEntity;
import com.dadazhang.gulimall.order.vo.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-20 13:27:45
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    OrderSubmitResponseVo submitOrder(OrderSubmitVo orderSubmitVo);

    OrderEntity getOrderByOrderSn(String orderSn);

    void cancelOrder(OrderEntity orderEntity);

    PayVo payOrder(String orderSn);

    PageUtils queryPageWithItem(Map<String, Object> params);

    void payedNotify(PayAsyncVo payAsyncVo);

    void createSeckillOrder(SeckillOrderTo seckillOrderTo) throws ExecutionException, InterruptedException;
}

