package com.dadazhang.gulimall.product.app;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.dadazhang.gulimall.product.entity.SpuInfoDescEntity;
import com.dadazhang.gulimall.product.service.SpuInfoDescService;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.R;



/**
 * spu信息介绍
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:28:42
 */
@RestController
@RequestMapping("product/spuinfodesc")
public class SpuInfoDescController {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    /**
     * 列表
     */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params){

        PageUtils page = spuInfoDescService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @GetMapping("/info/{spuId}")
    public R info(@PathVariable("spuId") Long spuId){
		SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getById(spuId);

        return R.ok().setData(spuInfoDesc);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    public R save(@RequestBody SpuInfoDescEntity spuInfoDesc){
		spuInfoDescService.save(spuInfoDesc);

        return R.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    public R update(@RequestBody SpuInfoDescEntity spuInfoDesc){
		spuInfoDescService.updateById(spuInfoDesc);

        return R.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    public R delete(@RequestBody Long[] spuIds){
		spuInfoDescService.removeByIds(Arrays.asList(spuIds));

        return R.ok();
    }

}
