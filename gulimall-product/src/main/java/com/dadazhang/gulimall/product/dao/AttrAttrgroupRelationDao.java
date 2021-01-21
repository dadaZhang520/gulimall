package com.dadazhang.gulimall.product.dao;

import com.dadazhang.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:28:42
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    void deleteAttrRelation(@Param("entities") List<AttrAttrgroupRelationEntity> entities);

    void addAttrRelation(@Param("entities") List<AttrAttrgroupRelationEntity> entities);
}
