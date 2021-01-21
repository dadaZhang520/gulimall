package com.dadazhang.gulimall.product.service.impl;

import com.dadazhang.gulimall.product.constant.ProductConstant;
import com.dadazhang.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.dadazhang.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.dadazhang.gulimall.product.entity.AttrEntity;
import com.dadazhang.gulimall.product.service.AttrAttrgroupRelationService;
import com.dadazhang.gulimall.product.service.AttrService;
import com.dadazhang.gulimall.product.vo.AttrGroupWithAttrVo;
import com.dadazhang.gulimall.product.vo.SkuItemVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.Query;

import com.dadazhang.gulimall.product.dao.AttrGroupDao;
import com.dadazhang.gulimall.product.entity.AttrGroupEntity;
import com.dadazhang.gulimall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catalogId) {
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> obj.eq("attr_group_id", key).or().like("attr_group_name", key));
        }
        if (catalogId != null && catalogId > 0) {
            //select * from pms_attr_group where catelog_id = catelogId and (attr_group_name=%key% or attr_group_id=key);
            wrapper.eq("catelog_id", catalogId);
        }
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
        page.setRecords(page.getRecords().stream().
                sorted(Comparator.comparingInt(attrGroup -> attrGroup.getSort() == null ? 0 : attrGroup.getSort()))
                .collect(Collectors.toList()));
        return new PageUtils(page);
    }

    @Override
    public void removeRelationGroup(List<Long> asList) {
        attrAttrgroupRelationDao.delete(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", asList));
        removeByIds(asList);
    }

    @Override
    public List<AttrGroupWithAttrVo> getAttrGroupByCatelogId(Long catalogId) {

        //1)通过分类id查询所有关联的分组属性
        List<AttrGroupEntity> groupEntities = list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catalogId));

        return groupEntities.stream().map(group -> {
            AttrGroupWithAttrVo attrGroupWithAttrVo = new AttrGroupWithAttrVo();
            BeanUtils.copyProperties(group, attrGroupWithAttrVo);
            List<AttrEntity> relationAttr = attrService.getRelationAttr(group.getAttrGroupId());
            attrGroupWithAttrVo.setAttrs(relationAttr);
            return attrGroupWithAttrVo;
        }).collect(Collectors.toList());

    }

    @Override
    public List<SkuItemVo.SpuItemBaseAttrVo> getAttrGroupBySpuId(Long spuId, Long catalogId) {
        List<SkuItemVo.SpuItemBaseAttrVo> vos =  this.baseMapper.queryGroupInfoBySpuId(spuId,catalogId);
        return vos;
    }

}