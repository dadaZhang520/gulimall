package com.dadazhang.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.dadazhang.common.valid.AddGroup;
import com.dadazhang.common.valid.UpdateGroup;
import com.dadazhang.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.dadazhang.gulimall.product.entity.BrandEntity;
import com.dadazhang.gulimall.product.service.BrandService;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.R;


/**
 * 品牌
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:28:42
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @GetMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().setData(brand);
    }

    @GetMapping("/infos")
    public R infos(@RequestParam List<Long> brandIds) {
        List<BrandEntity> brandList = brandService.getBrandList(brandIds);

        return new R().setData(brandList);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    public R save(@Validated(AddGroup.class)  @RequestBody BrandEntity brand) {

        brandService.save(brand);

        return R.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand) {

        brandService.updateDetail(brand);

        return R.ok();
    }
    /**
     * 修改状态
     */
    @PostMapping("/update/status")
    public R updateStatus(@Validated(UpdateStatusGroup.class) @RequestBody BrandEntity brand) {

        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
