package com.dadazhang.gulimall.coupon.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.dadazhang.gulimall.coupon.entity.SeckillSessionEntity;
import com.dadazhang.gulimall.coupon.service.SeckillSessionService;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.R;


/**
 * 秒杀活动场次
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:22:10
 */
@RestController
@RequestMapping("/coupon/seckillsession")
public class SeckillSessionController {

    @Autowired
    private SeckillSessionService seckillSessionService;

    @GetMapping("/threeDay/seckill/session")
    public R threeDaysSeckillSession() {

        List<SeckillSessionEntity> seckillSessionEntities = seckillSessionService.threeDaysSeckillSession();

        return R.ok().setData(seckillSessionEntities);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = seckillSessionService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        SeckillSessionEntity seckillSession = seckillSessionService.getById(id);

        return R.ok().put("seckillSession", seckillSession);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SeckillSessionEntity seckillSession) {
        seckillSessionService.save(seckillSession);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SeckillSessionEntity seckillSession) {
        seckillSessionService.updateById(seckillSession);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        seckillSessionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
