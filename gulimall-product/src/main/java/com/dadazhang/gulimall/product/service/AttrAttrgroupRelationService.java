package com.dadazhang.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.dadazhang.gulimall.product.vo.AttrGroupRelationVo;

import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:28:42
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void deleteAttrRelation(AttrGroupRelationVo[] params);

    void addAttrRelation(AttrGroupRelationVo[] vos);
}

