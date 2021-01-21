package com.dadazhang.gulimall.product.app;

import java.util.Arrays;
import java.util.Map;

import com.dadazhang.gulimall.product.vo.SpuSaveVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.dadazhang.gulimall.product.entity.SpuInfoEntity;
import com.dadazhang.gulimall.product.service.SpuInfoService;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.R;


/**
 * spu信息
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:28:42
 */
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {

    @Autowired
    private SpuInfoService spuInfoService;

    /**
     * 通过skuid查询spu信息
     *
     * @param skuId
     * @return
     */
    @GetMapping("/info/sku/id")
    public R getInfoBySkuId(@RequestParam Long skuId) {

        SpuInfoEntity entity = spuInfoService.getInfoBySkuId(skuId);

        return R.ok().setData(entity);
    }


    /**
     * 通过属性检索获取spu列表
     */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = spuInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 上架商品
     */
    @PostMapping("/{id}/up")
    public R productUp(@PathVariable("id") Long id) {
        spuInfoService.productUp(id);

        return R.ok();
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SpuSaveVo vo) {
        spuInfoService.saveSpuInfo(vo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SpuInfoEntity spuInfo) {
        spuInfoService.updateById(spuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
