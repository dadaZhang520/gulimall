package com.dadazhang.gulimall.ware.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.Query;

import com.dadazhang.gulimall.ware.dao.PurchaseDetailDao;
import com.dadazhang.gulimall.ware.entity.PurchaseDetailEntity;
import com.dadazhang.gulimall.ware.service.PurchaseDetailService;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();

        //获取检索的名称
        String key = (String) params.get("key");
        //获取采购仓库id
        String wareId = (String) params.get("wareId");
        //获取采购单状态status
        String status = (String) params.get("status");

        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq("purchase_id", key).or().like("sku_id", key);
            });
        }

        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }

        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("status", status);
        }

        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}