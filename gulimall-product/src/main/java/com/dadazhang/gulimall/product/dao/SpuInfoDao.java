package com.dadazhang.gulimall.product.dao;

import com.dadazhang.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu信息
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:28:42
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    SpuInfoEntity getInfoBySkuId(@Param("skuId") Long skuId);
}
