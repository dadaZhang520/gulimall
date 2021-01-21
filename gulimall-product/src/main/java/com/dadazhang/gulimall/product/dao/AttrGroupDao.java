package com.dadazhang.gulimall.product.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.dadazhang.gulimall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dadazhang.gulimall.product.vo.SkuItemVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:28:42
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SkuItemVo.SpuItemBaseAttrVo> queryGroupInfoBySpuId(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);
}
