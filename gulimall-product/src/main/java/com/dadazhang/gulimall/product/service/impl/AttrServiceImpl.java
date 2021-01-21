package com.dadazhang.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dadazhang.gulimall.product.constant.ProductConstant;
import com.dadazhang.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.dadazhang.gulimall.product.dao.AttrGroupDao;
import com.dadazhang.gulimall.product.dao.CategoryDao;
import com.dadazhang.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.dadazhang.gulimall.product.entity.AttrGroupEntity;
import com.dadazhang.gulimall.product.service.CategoryService;
import com.dadazhang.gulimall.product.vo.AttrResqVo;
import com.dadazhang.gulimall.product.vo.AttrVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.Query;

import com.dadazhang.gulimall.product.dao.AttrDao;
import com.dadazhang.gulimall.product.entity.AttrEntity;
import com.dadazhang.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catalogId, String type) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("attr_type", "base".equalsIgnoreCase(type) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());

        String key = (String) params.get("key");

        if (!StringUtils.isEmpty(key)) {
            wrapper.eq("attr_id", key).or().like("attr_name", key);
        }
        if (catalogId != null && catalogId > 0) {
            wrapper.and(w -> w.eq("catelog_id", catalogId));
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );

        //需要查询商品分类名称和商品分组名称
        PageUtils pageUtils = new PageUtils(page);

        List<AttrResqVo> attrResqVoList = page.getRecords().stream().map((attrEntity) -> {
            AttrResqVo attrResqVo = new AttrResqVo();
            BeanUtils.copyProperties(attrEntity, attrResqVo);

            Long attrId = attrEntity.getAttrId();
            Integer attrType = attrEntity.getAttrType();
            if (attrType == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
                if (attrAttrgroupRelationEntity != null) {
                    Long attrGroupId = attrAttrgroupRelationEntity.getAttrGroupId();
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
                    attrResqVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            Long catelogId1 = attrEntity.getCatelogId();
            if (catelogId1 != null) {
                attrResqVo.setCatelogName(categoryDao.selectById(catelogId1).getName());
            }
            return attrResqVo;
        }).collect(Collectors.toList());

        pageUtils.setList(attrResqVoList);
        return pageUtils;
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        //从AttrVo中复制出AttrEntity的属性
        BeanUtils.copyProperties(attr, attrEntity);
        //添加Attr
        this.save(attrEntity);
        //添加AttrGroup和Attr的关系
        if (attr.getAttrGroupId() != null && attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationDao.insert(relationEntity);
        }
    }

    @Cacheable(value = "attr", key = "'attrinfo:'+#root.args[0]")
    @Override
    public AttrResqVo getAttrInfo(Long attrId) {
        AttrResqVo attrResqVo = new AttrResqVo();
        AttrEntity attrEntity = this.baseMapper.selectById(attrId);
        BeanUtils.copyProperties(attrEntity, attrResqVo);

        Long attrId1 = attrEntity.getAttrId();
        Integer attrType = attrEntity.getAttrType();

        if (attrType == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId1));

            if (relationEntity != null) {
                Long attrGroupId = relationEntity.getAttrGroupId();
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
                attrResqVo.setAttrGroupId(attrGroupEntity.getAttrGroupId());
                attrResqVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }
        }

        Long catelogId = attrEntity.getCatelogId();
        if (catelogId != null) {
            Long[] cateLogPath = categoryService.getCateLogPath(catelogId);
            attrResqVo.setCatelogPath(cateLogPath);
        }
        return attrResqVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attrVo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);
        this.updateById(attrEntity);
        //修改商品分组和商品属性关联表的信息
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        Integer count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
        if (attrVo.getAttrGroupId() == null) {
            if (count > 0) {
                attrAttrgroupRelationDao.delete(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
            }
        } else {
            relationEntity.setAttrId(attrVo.getAttrId());
            relationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            if (count > 0) {
                attrAttrgroupRelationDao.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
            } else {
                attrAttrgroupRelationDao.insert(relationEntity);
            }
        }
    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrGroupId) {
        List<AttrAttrgroupRelationEntity> relationEntityList = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupId));

        List<Long> ids = relationEntityList.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        if (ids.size() > 0) {
            return listByIds(ids);
        }
        return null;
    }

    @Override
    public PageUtils getNoRelationAttr(HashMap<String, Object> map, Long attrGroupId) {
        //获取当前分组信息
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectOne(new QueryWrapper<AttrGroupEntity>().eq("attr_group_id", attrGroupId));
        //获取当前分类下的其他分组
        Long catelogId = attrGroupEntity.getCatelogId();
        List<AttrGroupEntity> group = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        //获取所有分组id
        List<Long> groupIds = group.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
        //通过所有分组id获取所有当前分类下没有绑定的属性
        QueryWrapper<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntityQueryWrapper = new QueryWrapper<>();
        if (groupIds.size() > 0) {
            attrAttrgroupRelationEntityQueryWrapper.in("attr_group_id", groupIds);
        }
        List<AttrAttrgroupRelationEntity> relations = attrAttrgroupRelationDao.selectList(attrAttrgroupRelationEntityQueryWrapper);
        //获取所有绑定的属性
        List<Long> attrIds = relations.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        //剔除所有获取的属性id
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (attrIds.size() > 0) {
            wrapper.notIn("attr_id", attrIds);
        }
        String key = (String) map.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> w.eq("attr_id", key).or().like("attr_name", key));
        }
        return new PageUtils(this.page(new Query<AttrEntity>().getPage(map), wrapper));
    }

    @Override
    public void removeRelationAttr(List<Long> asList) {
        //1）通过属性id查询当前属性的所有信息
        List<AttrEntity> attrEntities = this.baseMapper.selectList(new QueryWrapper<AttrEntity>().in("attr_id", asList));
        List<Long> attrids = attrEntities.stream().map(attr -> attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() ? attr.getAttrId() : null).collect(Collectors.toList());
        //2)删除关联表的当前属性信息
        if (attrids.size() > 0) {
            attrAttrgroupRelationDao.delete(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_id", attrids));
        }
        //3)通过属性id删除当前的属性信息
        removeByIds(asList);
    }
}