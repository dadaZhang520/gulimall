package com.dadazhang.gulimall.product.app;

import java.util.Arrays;
import java.util.Map;

import com.dadazhang.gulimall.product.vo.AttrResqVo;
import com.dadazhang.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.dadazhang.gulimall.product.service.AttrService;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.R;


/**
 * 商品属性
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:28:42
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    /**
     * 列表
     */
    @GetMapping("/{attrType}/list/{catId}")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catId") Long catalogId,
                  @PathVariable("attrType") String type) {
        PageUtils page = attrService.queryPage(params, catalogId, type);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @GetMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId) {
        AttrResqVo attrResqVo = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", attrResqVo);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    public R save(@RequestBody AttrVo attr) {
        attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    public R update(@RequestBody AttrVo attrVo) {
        attrService.updateAttr(attrVo);

        return R.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeRelationAttr(Arrays.asList(attrIds));

        return R.ok();
    }

}
