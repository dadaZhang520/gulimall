package com.dadazhang.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.dadazhang.common.constant.MQConstant;
import com.dadazhang.common.to.OrderTo;
import com.dadazhang.common.to.SkuHasStockTo;
import com.dadazhang.common.to.mq.SeckillOrderTo;
import com.dadazhang.common.utils.R;
import com.dadazhang.common.vo.MemberVo;
import com.dadazhang.gulimall.order.constant.OrderConstant;
import com.dadazhang.gulimall.order.entity.OrderEntity;
import com.dadazhang.gulimall.order.entity.OrderItemEntity;
import com.dadazhang.common.enume.OrderEnum;
import com.dadazhang.gulimall.order.entity.PaymentInfoEntity;
import com.dadazhang.gulimall.order.exception.NoStockException;
import com.dadazhang.gulimall.order.feign.CartFeignService;
import com.dadazhang.gulimall.order.feign.MemberFeignService;
import com.dadazhang.gulimall.order.feign.ProductFeignService;
import com.dadazhang.gulimall.order.feign.WareFeignService;
import com.dadazhang.gulimall.order.interceptor.LoginUserInterceptor;
import com.dadazhang.gulimall.order.service.OrderItemService;
import com.dadazhang.gulimall.order.service.PaymentInfoService;
import com.dadazhang.gulimall.order.to.OrderCreateTo;
import com.dadazhang.gulimall.order.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.Query;

import com.dadazhang.gulimall.order.dao.OrderDao;
import com.dadazhang.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Slf4j
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private final ThreadLocal<OrderSubmitVo> orderSubmitVoThreadLocal = new ThreadLocal<>();

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    PaymentInfoService paymentInfoService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();

        //获取当前线程的ServletRequestAttributes数据
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        //1.）查询会员信息
        //1.1）获取当前用户的member_id
        MemberVo memberVo = LoginUserInterceptor.loginUser.get();
        Long memberId = memberVo.getId();
        //1.2）获取当前用户的发货地址
        CompletableFuture<Void> memberFuture = CompletableFuture.runAsync(() -> {
            //1.3）给异步线程的RequestContextHolder设置主线程的RequestContextHolder数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            R memberR = memberFeignService.getAddressByMemberId(memberId);
            if (memberR.getCode() == 0) {
                List<MemberAddressVo> addressVos = memberR.getData(new TypeReference<List<MemberAddressVo>>() {
                });
                orderConfirmVo.setAddress(addressVos);
            }
        }, executor);

        //2.）获取下单的商品信息
        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            //2.1）给异步线程的RequestContextHolder设置主线程的RequestContextHolder数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            R cartR = cartFeignService.getUserCartItem();
            if (cartR.getCode() == 0) {
                List<OrderItemVo> orderItemVo = cartR.getData(new TypeReference<List<OrderItemVo>>() {
                });
                orderConfirmVo.setItems(orderItemVo);
            }
        }, executor).thenRunAsync(() -> {
            List<HasStockVo> hasStockVos = orderConfirmVo.getItems().stream().map(item -> {
                HasStockVo hasStockVo = new HasStockVo();
                hasStockVo.setSkuId(item.getSkuId());
                hasStockVo.setCount(item.getCount());
                return hasStockVo;
            }).collect(Collectors.toList());
            R r = wareFeignService.skuHasStockBySkuIds(hasStockVos);
            if (r.getCode() == 0) {
                List<SkuHasStockTo> skuHasStockToList = r.getData(new TypeReference<List<SkuHasStockTo>>() {
                });
                Map<Long, Boolean> map = skuHasStockToList.stream()
                        .collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getStock));
                orderConfirmVo.setHasStock(map);
            }
        }, executor);

        //等待异步任务完成
        CompletableFuture<Void> future = CompletableFuture.allOf(memberFuture, cartFuture);
        future.get();

        //3.）获取member的积分信息
        orderConfirmVo.setIntegration(memberVo.getIntegration());

        //TODO:4.添加防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberId, token, 30, TimeUnit.SECONDS);
        orderConfirmVo.setOrderToken(token);

        return orderConfirmVo;
    }

    @Transactional
    @Override
    public OrderSubmitResponseVo submitOrder(OrderSubmitVo orderSubmitVo) {

        OrderSubmitResponseVo responseVo = new OrderSubmitResponseVo();

        //获取用户信息
        MemberVo member = LoginUserInterceptor.loginUser.get();
        //1.）判断令牌是否存在，在分布式情况下【需要保证令牌的对比和删除是一个原子操作】
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                Collections.singletonList(OrderConstant.USER_ORDER_TOKEN_PREFIX + member.getId()),
                orderSubmitVo.getOrderToken());
        System.out.println("res =" + result);
        //返回1，令牌验证通过。返回0，令牌验证不通过。
        if (result == 0L) {
            responseVo.setCode(1);
            return responseVo;
        } else {
            //共享 OrderSubmitVo对象
            orderSubmitVoThreadLocal.set(orderSubmitVo);
            //令牌验证成功 2.）生成订单
            OrderCreateTo orderCreateTo = createOrder();
            //2.1）验价
            if (Math.abs(orderCreateTo.getOrder().getPayAmount().subtract(orderSubmitVo.getPayPrice()).doubleValue()) < 0.01) {
                //验价成功后，3.）保存订单
                saveOrder(orderCreateTo);
                //4.）库存锁定 只要有异常，就回滚订单数据
                StockLockVo stockLockVo = new StockLockVo();
                stockLockVo.setOrder(orderCreateTo.getOrder());
                List<StockLockVo.StockLockItemVo> lockVos = orderCreateTo.getItems().stream().map(item -> {
                    StockLockVo.StockLockItemVo itemVo = new StockLockVo.StockLockItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setLockNum(item.getSkuQuantity());
                    return itemVo;
                }).collect(Collectors.toList());
                stockLockVo.setOrderItem(lockVos);
                R r = wareFeignService.lockStock(stockLockVo);
                //如果库存锁定成功，订单流程全部完成
                if (r.getCode() == 0) {
                    //订单提交成功，添加到mq中，30分钟自动取消未支付订单
                    rabbitTemplate.convertAndSend(MQConstant.RABBITMQ_ORDER_EXCHANGE,
                            MQConstant.RABBITMQ_ORDER_DELAY_ROUTING_KEY,
                            orderCreateTo.getOrder());
                    //业务执行完成，返回controller
                    responseVo.setOrder(orderCreateTo.getOrder());
                    responseVo.setCode(0);
                } else {
                    throw new NoStockException(r.getMsg());
                }
            }
        }
        return responseVo;
    }

    /**
     * 通过订单号获取订单
     */
    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {

        return getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    /**
     * 取消订单
     */
    @Override
    public void cancelOrder(OrderEntity orderEntity) {
        OrderEntity order = getById(orderEntity.getId());

        if (order != null && order.getStatus().equals(OrderEnum.OrderStatusEnum.CREATE_NEW.getCode())) {
            order.setStatus(OrderEnum.OrderStatusEnum.CANCLED.getCode());
            this.updateById(order);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(order, orderTo);
            //取消订单后，为保险起见，在通知解锁库存服务，看到是否因网络等其他原因，没有解锁到库存
            rabbitTemplate.convertAndSend(MQConstant.RABBITMQ_ORDER_EXCHANGE,
                    MQConstant.RABBITMQ_STOCK_RELEASE_ROUTING_KEY,
                    orderTo);
        }
    }

    /**
     * 构建支付宝pay
     */
    @Override
    public PayVo payOrder(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity order = this.getOrderByOrderSn(orderSn);
        payVo.setTradeOn(orderSn);
        payVo.setTotalAmount(order.getPayAmount().setScale(2, RoundingMode.UP).toString());
        OrderItemEntity itemEntity = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn)).get(0);
        if (itemEntity != null) {
            payVo.setSubject(itemEntity.getSpuName());
            payVo.setBody(itemEntity.getSkuAttrsVals());
        }
        return payVo;
    }

    /**
     * 查询订单的列表
     */
    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        //查询当前用户的订单列表
        MemberVo memberVo = LoginUserInterceptor.loginUser.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", memberVo.getId()).orderByDesc("id")
        );
        //构造当前订单的订单项
        List<OrderEntity> orderLists = page.getRecords().stream().peek(item -> {
                    List<OrderItemEntity> orderItems = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", item.getOrderSn()));
                    item.setOrderItems(orderItems);
                }
        ).collect(Collectors.toList());

        page.setRecords(orderLists);

        return new PageUtils(page);
    }

    @Override
    public void payedNotify(PayAsyncVo payAsyncVo) {
        //1）、保存订单的交易流水
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setOrderSn(payAsyncVo.getOut_trade_no());
        paymentInfoEntity.setAlipayTradeNo(payAsyncVo.getTrade_no());
        paymentInfoEntity.setPaymentStatus(payAsyncVo.getTrade_status());
        paymentInfoEntity.setCallbackTime(payAsyncVo.getNotify_time());
        paymentInfoService.save(paymentInfoEntity);

        if (payAsyncVo.getTrade_status().equals("TRADE_SUCCESS")) {
            //2）、修改订单的状态
            this.baseMapper.updateOrderStatus(payAsyncVo.getOut_trade_no(),
                    OrderEnum.OrderStatusEnum.PAYED.getCode());
            //3）、减扣实际库存
            wareFeignService.orderFinishHandler(payAsyncVo.getOut_trade_no());
        }
    }

    /**
     * 创建一个秒杀订单
     */
    @Override
    public void createSeckillOrder(SeckillOrderTo seckillOrderTo) throws ExecutionException, InterruptedException {
        //保存秒杀订单
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrderTo.getOrderSn());
        orderEntity.setMemberId(seckillOrderTo.getMemberId());
        orderEntity.setPayAmount(seckillOrderTo.getPrice().multiply(new BigDecimal(seckillOrderTo.getNum())));
        orderEntity.setCreateTime(new Date());
        orderEntity.setStatus(OrderEnum.OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setDeleteStatus(OrderEnum.OrderDelStatusEnum.NOT_DELETE.getCode());
        this.save(orderEntity);

        //保存订单项
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(seckillOrderTo.getOrderSn());

        //调用远程服务获取spu信息
        R r = productFeignService.getSpuInfoBySkuId(seckillOrderTo.getSkuId());

        SpuInfoVo spuInfoVo = r.getData(new TypeReference<SpuInfoVo>() {
        });

        CompletableFuture<Void> descInfoFuture = CompletableFuture.runAsync(() -> {
            //远程查询spu 图片，
            R descR = productFeignService.getDescInfoById(spuInfoVo.getId());
            if (descR.getCode() == 0) {
                SpuInfoVo.SpuDescInfo descInfo = descR.getData(new TypeReference<SpuInfoVo.SpuDescInfo>() {
                });
                orderItemEntity.setSpuPic(descInfo.getDecript());
            }
        }, executor);

        CompletableFuture<Void> brandInfoFuture = CompletableFuture.runAsync(() -> {
            // 远程查询 商品的品牌名称
            R brandR = productFeignService.getBrandInfo(spuInfoVo.getBrandId());
            if (brandR.getCode() == 0) {
                SpuInfoVo.BrandInfo brandInfo = brandR.getData(new TypeReference<SpuInfoVo.BrandInfo>() {
                });
                orderItemEntity.setSpuBrand(brandInfo.getName());
            }
        }, executor);

        CompletableFuture<Void> skuInfoFuture = CompletableFuture.runAsync(() -> {
            // 远程查询 sku商品信息
            R skuR = productFeignService.getSkuInfoById(seckillOrderTo.getSkuId());
            if (skuR.getCode() == 0) {
                SkuInfoVo skuInfoVo = skuR.getData(new TypeReference<SkuInfoVo>() {
                });
                orderItemEntity.setSkuName(skuInfoVo.getSkuName());
                orderItemEntity.setSkuPic(skuInfoVo.getSkuDefaultImg());
                orderItemEntity.setSkuPrice(skuInfoVo.getPrice());
            }
        }, executor);

        CompletableFuture.allOf(descInfoFuture, brandInfoFuture, skuInfoFuture).get();

        orderItemEntity.setSpuId(spuInfoVo.getId());
        orderItemEntity.setSpuName(spuInfoVo.getSpuName());
        orderItemEntity.setCategoryId(spuInfoVo.getCatalogId());
        orderItemEntity.setSkuId(seckillOrderTo.getSkuId());
        orderItemEntity.setSkuQuantity(seckillOrderTo.getNum());

        orderItemEntity.setPromotionAmount(new BigDecimal("0.00"));
        orderItemEntity.setCouponAmount(new BigDecimal("0.00"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0.00").divide(new BigDecimal(1000)));
        orderItemEntity.setRealAmount(seckillOrderTo.getPrice()
                .multiply(new BigDecimal(seckillOrderTo.getNum()))
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getIntegrationAmount()));
        orderItemEntity.setGiftGrowth(seckillOrderTo.getPrice().intValue() * seckillOrderTo.getNum());
        orderItemEntity.setGiftIntegration(seckillOrderTo.getPrice().intValue() * seckillOrderTo.getNum());

        this.orderItemService.save(orderItemEntity);
    }

    /**
     * 保存订单
     */
    private void saveOrder(OrderCreateTo orderCreateTo) {
        //保存订单
        OrderEntity order = orderCreateTo.getOrder();
        save(order);

        //保存订单项
        orderItemService.saveBatch(orderCreateTo.getItems());
    }

    /**
     * 创建订单
     */
    private OrderCreateTo createOrder() {
        //共享提交的订单数据
        OrderSubmitVo orderSubmitVo = orderSubmitVoThreadLocal.get();

        OrderCreateTo orderCreateTo = new OrderCreateTo();

        //1.） 构造订单对象
        //1.1 ）调用远程服务查询收货信息
        R r = wareFeignService.getFare(orderSubmitVo.getAddressId());
        FareVo fareVo = r.getData(new TypeReference<FareVo>() {
        });

        //1.2）构建订单的基本信息
        OrderEntity order = builderOrder(fareVo.getMemberAddressVo());
        order.setFreightAmount(fareVo.getFarePrice());

        // 2.） 构造订单的item对象
        List<OrderItemEntity> orderItems = BuilderOrderItems(order.getOrderSn());
        orderCreateTo.setItems(orderItems);

        //1.3）构建订单的价格
        OrderEntity orderComplete = orderPriceComputer(order, orderItems);
        orderCreateTo.setOrder(orderComplete);

        //3.） 构造应付价格
        orderCreateTo.setPayPrice(order.getPayAmount());

        //4.） 快递价格
        orderCreateTo.setFarePrice(fareVo.getFarePrice());

        return orderCreateTo;
    }

    /**
     * 构建订单的价格计算
     */
    private OrderEntity orderPriceComputer(OrderEntity order, List<OrderItemEntity> orderItems) {

        BigDecimal totalPrice = new BigDecimal("0.00"); //付款总额
        BigDecimal payPrice = new BigDecimal("0.00"); //应付价格
        BigDecimal promotionPrice = new BigDecimal("0.00"); //促销优化金额
        BigDecimal integrationPrice = new BigDecimal("0.00"); //积分抵扣金额
        BigDecimal couponPrice = new BigDecimal("0.00"); //优惠券抵扣金额
        Integer integration = 0; //积分抵扣金额
        Integer growth = 0;//优惠券抵扣金额

        for (OrderItemEntity orderItem : orderItems) {
            integration += orderItem.getGiftIntegration();
            growth += orderItem.getGiftGrowth();
            totalPrice = totalPrice.add(orderItem.getSkuPrice().multiply(new BigDecimal(orderItem.getSkuQuantity())));
            payPrice = payPrice.add(orderItem.getRealAmount());
            promotionPrice = promotionPrice.add(orderItem.getPromotionAmount());
            integrationPrice = integrationPrice.add(orderItem.getIntegrationAmount().divide(new BigDecimal(1000)));
            couponPrice = couponPrice.add(orderItem.getCouponAmount());
        }

        //设置价格信息
        order.setTotalAmount(totalPrice);
        order.setPayAmount(payPrice.add(order.getFreightAmount()));
        order.setPromotionAmount(promotionPrice);
        order.setIntegrationAmount(integrationPrice);
        order.setCouponAmount(couponPrice);

        //设置积分、成长值
        order.setIntegration(integration);
        order.setGrowth(growth);

        return order;
    }

    /**
     * 构造订单的OrderItemEntity集合
     */
    private List<OrderItemEntity> BuilderOrderItems(String orderSn) {

        //远程调用购物车服务，获取最新的购物车信息，生成order的订单项
        R r = cartFeignService.getUserCartItem();
        List<OrderItemVo> orderItemVo = r.getData(new TypeReference<List<OrderItemVo>>() {
        });
        //生成OrderItemEntity
        return orderItemVo.stream().map(item -> {
            OrderItemEntity orderItemEntity = null;
            try {
                orderItemEntity = builderOrderItem(item);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            //设置订单编号
            orderItemEntity.setOrderSn(orderSn);

            return orderItemEntity;
        }).collect(Collectors.toList());
    }

    /**
     * 构建每一个OrderItemEntity
     */
    private OrderItemEntity builderOrderItem(OrderItemVo item) throws ExecutionException, InterruptedException {

        OrderItemEntity orderItemEntity = new OrderItemEntity();

        //调用远程服务获取spu信息
        R r = productFeignService.getSpuInfoBySkuId(item.getSkuId());

        SpuInfoVo spuInfoVo = r.getData(new TypeReference<SpuInfoVo>() {
        });

        CompletableFuture<Void> descInfoFuture = CompletableFuture.runAsync(() -> {
            //远程查询spu 图片，
            R descR = productFeignService.getDescInfoById(spuInfoVo.getId());
            if (descR.getCode() == 0) {
                SpuInfoVo.SpuDescInfo descInfo = descR.getData(new TypeReference<SpuInfoVo.SpuDescInfo>() {
                });
                orderItemEntity.setSpuPic(descInfo.getDecript());
            }
        }, executor);

        CompletableFuture<Void> brandInfoFuture = CompletableFuture.runAsync(() -> {
            // 远程查询 商品的品牌名称
            R brandR = productFeignService.getBrandInfo(spuInfoVo.getBrandId());
            if (brandR.getCode() == 0) {
                SpuInfoVo.BrandInfo brandInfo = brandR.getData(new TypeReference<SpuInfoVo.BrandInfo>() {
                });
                orderItemEntity.setSpuBrand(brandInfo.getName());
            }
        }, executor);

        CompletableFuture<Void> basicInfoFuture = CompletableFuture.runAsync(() -> {

            orderItemEntity.setSpuId(spuInfoVo.getId());
            orderItemEntity.setSpuName(spuInfoVo.getSpuName());
            orderItemEntity.setSpuPic(spuInfoVo.getSpuDescription());
            orderItemEntity.setCategoryId(spuInfoVo.getCatalogId());

            //设置商品的sku信息
            orderItemEntity.setSkuId(item.getSkuId());
            orderItemEntity.setSkuName(item.getTitle());
            orderItemEntity.setSkuPrice(item.getPrice());
            orderItemEntity.setSkuPic(item.getImg());
            orderItemEntity.setSkuQuantity(item.getCount());
            orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(item.getSaleAttr(), ";"));

            //订单项使用的优惠价格
            orderItemEntity.setPromotionAmount(new BigDecimal("0.00"));
            orderItemEntity.setCouponAmount(new BigDecimal("0.00"));
            orderItemEntity.setIntegrationAmount(new BigDecimal("0.00").divide(new BigDecimal(1000)));
            orderItemEntity.setRealAmount(item.getTotalPrice()
                    .subtract(orderItemEntity.getPromotionAmount())
                    .subtract(orderItemEntity.getCouponAmount())
                    .subtract(orderItemEntity.getIntegrationAmount()));

            //设置积分信息
            orderItemEntity.setGiftGrowth(orderItemEntity.getRealAmount().intValue());
            orderItemEntity.setGiftIntegration(orderItemEntity.getRealAmount().intValue());
        }, executor);

        CompletableFuture.allOf(descInfoFuture, brandInfoFuture, basicInfoFuture).get();

        return orderItemEntity;
    }

    /**
     * 构造订单对象
     */
    private OrderEntity builderOrder(MemberAddressVo memberAddressVo) {

        MemberVo memberVo = LoginUserInterceptor.loginUser.get();

        OrderEntity order = new OrderEntity();
        // 生成订单号
        String orderSn = IdWorker.getTimeId().substring(0, 16);
        order.setOrderSn(orderSn);

        //设置member_id
        order.setMemberId(memberVo.getId());
        order.setMemberUsername(memberVo.getUsername());

        //设置收货信息
        order.setReceiverProvince(memberAddressVo.getProvince());
        order.setReceiverCity(memberAddressVo.getCity());
        order.setReceiverRegion(memberAddressVo.getRegion());
        order.setReceiverDetailAddress(memberAddressVo.getDetailAddress());
        order.setReceiverPhone(memberAddressVo.getPhone());
        order.setReceiverPostCode(memberAddressVo.getPostCode());
        order.setReceiverName(memberAddressVo.getName());

        //自动确认时间
        order.setAutoConfirmDay(7);

        //设置订单状态
        order.setStatus(OrderEnum.OrderStatusEnum.CREATE_NEW.getCode());
        order.setDeleteStatus(OrderEnum.OrderDelStatusEnum.NOT_DELETE.getCode());

        // 订单生成时间
        order.setCreateTime(new Date());

        return order;
    }

}
