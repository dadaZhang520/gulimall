package com.dadazhang.gulimall.ware.dao;

import com.dadazhang.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-20 13:35:46
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Long getSkuHasStock(@Param("skuId") Long skuId, @Param("count") Integer count);

    List<Long> orderHasStockBySkuId(@Param("skuId") Long skuId);

    Integer lockStockNum(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("lockNum") Integer lockNum);

    void releaseStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    void subtractRealStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);
}
