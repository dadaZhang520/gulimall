package com.dadazhang.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.dadazhang.common.constant.MQConstant;
import com.dadazhang.common.enume.OrderEnum;
import com.dadazhang.common.enume.WareEnum;
import com.dadazhang.common.to.SkuHasStockTo;
import com.dadazhang.common.utils.R;
import com.dadazhang.gulimall.ware.feign.OrderFeignService;
import com.dadazhang.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.dadazhang.gulimall.ware.entity.WareOrderTaskEntity;
import com.dadazhang.gulimall.ware.exception.NoStockException;
import com.dadazhang.gulimall.ware.feign.ProductFeignService;
import com.dadazhang.gulimall.ware.service.WareOrderTaskDetailService;
import com.dadazhang.gulimall.ware.service.WareOrderTaskService;
import com.dadazhang.gulimall.ware.vo.HasStockVo;
import com.dadazhang.common.to.OrderTo;
import com.dadazhang.gulimall.ware.vo.SkuInfoVo;
import com.dadazhang.gulimall.ware.vo.StockLockVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.Query;

import com.dadazhang.gulimall.ware.dao.WareSkuDao;
import com.dadazhang.gulimall.ware.entity.WareSkuEntity;
import com.dadazhang.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    OrderFeignService orderFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();

        String wareId = (String) params.get("wareId");

        String skuId = (String) params.get("skuId");

        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }

        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1）、添加库存前首先判断是否有这条库存记录
        List<WareSkuEntity> wareSkuEntities = this.list(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities == null || wareSkuEntities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            try {
                R r = productFeignService.skuById(skuId);
                if (r.getCode() == 0) {
                    SkuInfoVo skuInfoVo = r.getData(new TypeReference<SkuInfoVo>() {
                    });
                    wareSkuEntity.setSkuName(skuInfoVo.getSkuName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.baseMapper.insert(wareSkuEntity);
        } else {
            this.baseMapper.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockTo> orderHasStock(List<HasStockVo> hasStockVos) {

        return hasStockVos.stream().map(item -> {
            Long stock = this.baseMapper.getSkuHasStock(item.getSkuId(), item.getCount());

            SkuHasStockTo skuHasStockTo = new SkuHasStockTo();
            skuHasStockTo.setSkuId(item.getSkuId());
            skuHasStockTo.setStock(stock != null && stock >= 0);

            return skuHasStockTo;

        }).collect(Collectors.toList());
    }

    @Override
    public List<SkuHasStockTo> skuHasStockBySkuIds(Long[] skuIds) {
        return Arrays.stream(skuIds).map(skuId -> {
            Long stock = this.baseMapper.getSkuHasStock(skuId, 0);

            SkuHasStockTo skuHasStockTo = new SkuHasStockTo();
            skuHasStockTo.setSkuId(skuId);
            skuHasStockTo.setStock(stock != null && stock >= 1);

            return skuHasStockTo;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean skuHasStockBySkuId(Long skuId) {

        WareSkuEntity wareSkuEntity = this.getOne(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId));

        return wareSkuEntity != null && wareSkuEntity.getStock() > 0;
    }

    /**
     * 锁定库存
     */
    @Transactional
    @Override
    public void lockStock(StockLockVo stockLockVo) {
        OrderTo order = stockLockVo.getOrder();

        //1）、创建库存单
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(order.getOrderSn());
        taskEntity.setConsignee(order.getReceiverName());
        taskEntity.setConsigneeTel(order.getReceiverPhone());
        taskEntity.setDeliveryAddress(order.getReceiverProvince() + order.getReceiverCity() + order.getReceiverRegion() + order.getReceiverDetailAddress());
        taskEntity.setCreateTime(new Date());
        wareOrderTaskService.save(taskEntity);

        //2）、查询当前商品的库存仓库
        for (StockLockVo.StockLockItemVo lockVo : stockLockVo.getOrderItem()) {
            List<Long> wareIds = this.baseMapper.orderHasStockBySkuId(lockVo.getSkuId());
            if (wareIds == null || wareIds.size() == 0) {
                throw new NoStockException(lockVo.getSkuId());
            }
            //锁定库存
            for (Long wareId : wareIds) {
                Integer res = baseMapper.lockStockNum(lockVo.getSkuId(), wareId, lockVo.getLockNum());
                if (res == 1) {
                    //3）、创建详细的库存单
                    WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
                    taskDetailEntity.setTaskId(taskEntity.getId());
                    taskDetailEntity.setSkuId(lockVo.getSkuId());
                    taskDetailEntity.setSkuNum(lockVo.getLockNum());
                    taskDetailEntity.setWareId(wareId);
                    taskDetailEntity.setLockStatus(WareEnum.WareTaskStatus.LOCK_STOCK.getCode());
                    wareOrderTaskDetailService.save(taskDetailEntity);
                    //4）、给mq中保存锁定的信息
                    rabbitTemplate.convertAndSend(MQConstant.RABBITMQ_STOCK_EXCHANGE,
                            MQConstant.RABBITMQ_STOCK_DELAY_ROUTING_KEY,
                            taskDetailEntity);
                    break;
                } else {
                    throw new NoStockException(lockVo.getSkuId());
                }
            }
        }
    }

    /**
     * 解锁库存
     */
    @Override
    public void unStockLock(WareOrderTaskDetailEntity detailEntity) throws RuntimeException {
        //1）、判断是否存在库存单，不存在就不需要解锁库存
        Long taskId = detailEntity.getTaskId();
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(taskId);
        if (taskEntity != null) {
            //2）、重新查询一下库存详细单的状态，是否已经被解锁过了
            WareOrderTaskDetailEntity detailEntity1 = wareOrderTaskDetailService.getById(detailEntity.getId());
            if (detailEntity1 != null && detailEntity1.getLockStatus().equals(WareEnum.WareTaskStatus.LOCK_STOCK.getCode())) {
                //3）、判断订单是否生成
                String orderSn = taskEntity.getOrderSn();
                R r = orderFeignService.getOrderByOrderSn(orderSn);
                if (r.getCode() == 0) {
                    OrderTo order = r.getData(new TypeReference<OrderTo>() {
                    });
                    //如果订单不存在，或者订单超时取消。就需要执行解锁库存
                    if (order == null || order.getStatus().equals(OrderEnum.OrderStatusEnum.CANCLED.getCode())) {
                        releaseStock(detailEntity.getSkuId(), detailEntity.getWareId(), detailEntity.getSkuNum());
                        //解锁完库存可以修改库存单的状态
                        detailEntity.setLockStatus(WareEnum.WareTaskStatus.RELEASE_STOCK.getCode());
                        wareOrderTaskDetailService.updateById(detailEntity);
                    }
                } else {
                    throw new RuntimeException("订单服务发生异常");
                }
            }
        }
    }

    /**
     * 由于存在可能网络等其他原因导致库存解锁失败
     * 由库存在取消订单后发送实时消息来，验证库存是否解锁
     */
    @Override
    public void unStockLock(OrderTo orderTo) {
        //1）、查询当前订单的库存单的状态是否是解锁状态
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getWareOrderTaskByOrderSn(orderTo.getOrderSn());
        if (taskEntity != null) {
            List<WareOrderTaskDetailEntity> detailEntities = wareOrderTaskDetailService.getWareOrderTaskDetailByTaskId(taskEntity.getId());
            if (detailEntities != null && detailEntities.size() > 0) {
                for (WareOrderTaskDetailEntity detail : detailEntities) {
                    //状态是未解锁状态就解锁库存
                    if (detail.getLockStatus().equals(WareEnum.WareTaskStatus.LOCK_STOCK.getCode())) {
                        //调用解锁库存方法
                        releaseStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum());
                        //修改解锁库存状态
                        detail.setLockStatus(WareEnum.WareTaskStatus.RELEASE_STOCK.getCode());
                        wareOrderTaskDetailService.updateById(detail);
                    }
                }
            }
        }
    }

    /**
     * 订单完成后的库存操作
     *
     * @param orderSn
     */
    @Override
    public void orderFinishHandler(String orderSn) {
        //1.）获取订单的库存单信息
        WareOrderTaskEntity orderTaskEntity = wareOrderTaskService.getWareOrderTaskByOrderSn(orderSn);
        if (orderTaskEntity != null) {
            //1.1）、获取库存单的项
            List<WareOrderTaskDetailEntity> detailEntities =
                    wareOrderTaskDetailService.getWareOrderTaskDetailByTaskId(orderTaskEntity.getId());
            if (detailEntities != null && detailEntities.size() > 0) {
                for (WareOrderTaskDetailEntity detailEntity : detailEntities) {
                    //1.2）、如果没有释放锁定库存需要释放
                    if (detailEntity.getLockStatus().equals(WareEnum.WareTaskStatus.LOCK_STOCK.getCode())) {
                        releaseStock(detailEntity.getSkuId(), detailEntity.getWareId(), detailEntity.getSkuNum());
                    }
                    //2）、扣除商品的实际库存量
                    subtractRealStock(detailEntity.getSkuId(), detailEntity.getWareId(), detailEntity.getSkuNum());
                    //3）、修改当前库存单的状态，为已扣除商品的库存数量
                    detailEntity.setLockStatus(WareEnum.WareTaskStatus.SUBTRACT_STOCK.getCode());
                    wareOrderTaskDetailService.updateById(detailEntity);
                }
            }
        }
    }

    /**
     * 释放库存
     */
    public void releaseStock(Long skuId, Long wareId, Integer skuNum) {
        this.baseMapper.releaseStock(skuId, wareId, skuNum);
    }

    /**
     * 扣除商品的实际库存量
     */
    @Override
    public void subtractRealStock(Long skuId, Long wareId, Integer skuNum) {
        this.baseMapper.subtractRealStock(skuId, wareId, skuNum);
    }
}