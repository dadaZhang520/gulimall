package com.dadazhang.gulimall.ware.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.dadazhang.gulimall.ware.vo.PurchaseDoneVo;
import com.dadazhang.gulimall.ware.vo.PurchaseMergeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.dadazhang.gulimall.ware.entity.PurchaseEntity;
import com.dadazhang.gulimall.ware.service.PurchaseService;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.R;


/**
 * 采购信息
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-20 13:35:46
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 获取没有领取的采购单列表
     */
    @GetMapping("/unreceive/list")
    public R unReceiveList() {
        List<PurchaseEntity> purchaseEntities = purchaseService.unReceiveList();
        return R.ok().put("data", purchaseEntities);
    }

    /**
     * 领取采购单
     */
    @PostMapping("/received")
    public R received(@RequestBody List<Long> ids) {

        purchaseService.receivePurchase(ids);

        return R.ok();
    }

    /**
     * 采购单完成
     */
    @PostMapping("/done")
    public R received(@RequestBody PurchaseDoneVo doneVo) {

        purchaseService.purchaseDone(doneVo);

        return R.ok();
    }


    /**
     * 合并采购单信息
     */
    @PostMapping("/merge")
    public R merge(@RequestBody PurchaseMergeVo mergeVo) {

        purchaseService.purchaseMerge(mergeVo);

        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody PurchaseEntity purchase) {
        purchase.setCreateTime(new Date());
        purchase.setUpdateTime(new Date());
        purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody PurchaseEntity purchase) {
        purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
