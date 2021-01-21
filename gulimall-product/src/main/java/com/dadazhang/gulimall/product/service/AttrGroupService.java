package com.dadazhang.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.gulimall.product.entity.AttrGroupEntity;
import com.dadazhang.gulimall.product.vo.AttrGroupWithAttrVo;
import com.dadazhang.gulimall.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:28:42
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catalogId);

    void removeRelationGroup (List<Long> asList);

    List<AttrGroupWithAttrVo> getAttrGroupByCatelogId(Long catalogId);

    List<SkuItemVo.SpuItemBaseAttrVo> getAttrGroupBySpuId(Long spuId, Long catalogId);
}

