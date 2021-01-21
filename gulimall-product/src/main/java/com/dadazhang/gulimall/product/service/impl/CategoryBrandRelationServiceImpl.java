package com.dadazhang.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dadazhang.gulimall.product.entity.BrandEntity;
import com.dadazhang.gulimall.product.entity.CategoryEntity;
import com.dadazhang.gulimall.product.service.BrandService;
import com.dadazhang.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.Query;

import com.dadazhang.gulimall.product.dao.CategoryBrandRelationDao;
import com.dadazhang.gulimall.product.entity.CategoryBrandRelationEntity;
import com.dadazhang.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catalogId = categoryBrandRelation.getCatelogId();
        //通过商品品牌Id和商品分类id查出商品品牌名称，和商品分类名称
        BrandEntity brandEntity = brandService.getById(brandId);
        CategoryEntity categoryEntity = categoryService.getById(catalogId);
        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());
        this.save(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setBrandName(name);
        this.update(categoryBrandRelationEntity, new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
    }

    @Override
    public void updateCategory(Long catId, String name) {
        baseMapper.updateCategoryById(catId, name);
    }

    @Override
    public List<BrandEntity> getBrandByCatId(Long catId) {
        List<CategoryBrandRelationEntity> categoryBrandRelationEntities = list(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        return categoryBrandRelationEntities.stream().map(
                categoryBrandRelationEntity -> brandService.getById(categoryBrandRelationEntity.getBrandId())
        ).collect(Collectors.toList());
    }

}