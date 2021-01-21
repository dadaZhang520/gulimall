package com.dadazhang.gulimall.product.app;

import java.util.*;

import com.dadazhang.gulimall.product.entity.AttrEntity;
import com.dadazhang.gulimall.product.service.AttrAttrgroupRelationService;
import com.dadazhang.gulimall.product.service.AttrService;
import com.dadazhang.gulimall.product.service.CategoryService;
import com.dadazhang.gulimall.product.vo.AttrGroupRelationVo;
import com.dadazhang.gulimall.product.vo.AttrGroupWithAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.dadazhang.gulimall.product.entity.AttrGroupEntity;
import com.dadazhang.gulimall.product.service.AttrGroupService;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.R;


/**
 * 属性分组
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:28:42
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    /**
     * /product/attrgroup/{catelogId}/withattr
     */
    @GetMapping("/{catalogId}/withattr")
    public R getAttrGroupWithAttr(@PathVariable("catalogId") Long catalogId) {
        List<AttrGroupWithAttrVo> vos = attrGroupService.getAttrGroupByCatelogId(catalogId);
        
        return R.ok().put("data", vos);
    }


    /**
     * 列表
     */
    @RequestMapping("/list/{catalogId}")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catalogId") Long catalogId) {
        PageUtils page = attrGroupService.queryPage(params, catalogId);

        return R.ok().put("page", page);
    }

    /**
     * 获取分组下所关联的属性
     */
    @GetMapping("/{attrgroupId}/attr/relation")
    public R relationAttr(@PathVariable("attrgroupId") Long attrGroupId) {
        List<AttrEntity> entities = attrService.getRelationAttr(attrGroupId);
        return R.ok().put("data", entities);
    }

    /**
     * 获取不相关的属性
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R noRelationAttr(@RequestParam HashMap<String, Object> params, @PathVariable("attrgroupId") Long attrGroupId) {
        PageUtils page = attrService.getNoRelationAttr(params, attrGroupId);
        return R.ok().put("page", page);
    }

    @PostMapping("/attr/relation")
    public R addAttrRelation(@RequestBody AttrGroupRelationVo[] vos) {
        attrAttrgroupRelationService.addAttrRelation(vos);
        return R.ok();
    }

    /**
     * 删除相关属性
     *
     * @param attrGroupRelationVos
     * @return
     */
    @PostMapping("/attr/relation/delete")
    public R deleteAttrRelation(@RequestBody AttrGroupRelationVo[] attrGroupRelationVos) {
        attrAttrgroupRelationService.deleteAttrRelation(attrGroupRelationVos);
        return R.ok();
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        //获取子分类的id
        Long catalogId = attrGroup.getCatelogId();
        //获得所有父分类的id
        attrGroup.setCatelogPath(categoryService.getCateLogPath(catalogId));

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeRelationGroup(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
