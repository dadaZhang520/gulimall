package com.dadazhang.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.gulimall.product.entity.AttrEntity;
import com.dadazhang.gulimall.product.vo.AttrResqVo;
import com.dadazhang.gulimall.product.vo.AttrVo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:28:42
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params, Long catalogId, String type);

    void saveAttr(AttrVo attr);

    AttrResqVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attrVo);

    List<AttrEntity> getRelationAttr(Long attrGroupId);

    PageUtils getNoRelationAttr(HashMap<String, Object> params, Long attrGroupId);

    void  removeRelationAttr(List<Long> asList);
}

