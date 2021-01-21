package com.dadazhang.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.dadazhang.common.exception.BizCodeEnum;
import com.dadazhang.common.to.SkuHasStockTo;
import com.dadazhang.gulimall.ware.exception.NoStockException;
import com.dadazhang.gulimall.ware.vo.HasStockVo;
import com.dadazhang.gulimall.ware.vo.StockLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.dadazhang.gulimall.ware.entity.WareSkuEntity;
import com.dadazhang.gulimall.ware.service.WareSkuService;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.R;


/**
 * 商品库存
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-20 13:35:46
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {

    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 订单成功后减去实际库存，解锁锁定库存
     */
    @PostMapping("/order/finish/handler")
    public R orderFinishHandler(@RequestParam String orderSn) {
        wareSkuService.orderFinishHandler(orderSn);
        return R.ok();
    }

    @PostMapping("/lock/stock")
    public R lockStock(@RequestBody StockLockVo stockLockVo) {
        try {
            wareSkuService.lockStock(stockLockVo);
        } catch (NoStockException e) {
            return R.error(BizCodeEnum.NO_STOCK_EXCEPTION.getCode(), BizCodeEnum.NO_STOCK_EXCEPTION.getMessage());
        }
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 通过skuId获取信息
     */
    @PostMapping("/sku/has/stock")
    public boolean skuHasStockBySkuId(@RequestParam Long skuId) {

        return wareSkuService.skuHasStockBySkuId(skuId);
    }

    /**
     * 通过skuId获取信息
     */
    @PostMapping("/skus/has/stock")
    public R skuHasStockBySkuIds(@RequestParam Long[] skuIds) {

        return R.ok().setData(wareSkuService.skuHasStockBySkuIds(skuIds));
    }

    @PostMapping("/order/has/stock/")
    public R orderHasStock(@RequestBody List<HasStockVo> hasStockVos) {

        List<SkuHasStockTo> tos = wareSkuService.orderHasStock(hasStockVos);

        return R.ok().setData(tos);
    }

    /**
     * 信息
     */
    @GetMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
