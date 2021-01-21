package com.dadazhang.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dadazhang.common.to.SkuHasStockTo;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.dadazhang.gulimall.ware.entity.WareSkuEntity;
import com.dadazhang.gulimall.ware.vo.HasStockVo;
import com.dadazhang.common.to.OrderTo;
import com.dadazhang.gulimall.ware.vo.StockLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-20 13:35:46
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockTo> orderHasStock(List<HasStockVo> hasStockVos);

    List<SkuHasStockTo> skuHasStockBySkuIds(Long[] skuIds);

    boolean skuHasStockBySkuId(Long skuId);

    void lockStock(StockLockVo stockLockVo);

    void unStockLock(WareOrderTaskDetailEntity detailEntity);

    void unStockLock(OrderTo orderTo);

    void releaseStock(Long skuId, Long wareId, Integer skuNum);

    void subtractRealStock(Long skuId, Long wareId, Integer skuNum);

    void orderFinishHandler(String orderSn);
}

