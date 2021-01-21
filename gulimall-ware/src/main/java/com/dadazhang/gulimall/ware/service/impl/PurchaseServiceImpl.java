package com.dadazhang.gulimall.ware.service.impl;

import com.dadazhang.common.enume.WareEnum;
import com.dadazhang.gulimall.ware.service.WareSkuService;
import com.dadazhang.gulimall.ware.vo.PurchaseDoneItemVo;
import com.dadazhang.gulimall.ware.vo.PurchaseDoneVo;
import com.dadazhang.gulimall.ware.vo.PurchaseMergeVo;
import com.dadazhang.gulimall.ware.entity.PurchaseDetailEntity;
import com.dadazhang.gulimall.ware.service.PurchaseDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.Query;

import com.dadazhang.gulimall.ware.dao.PurchaseDao;
import com.dadazhang.gulimall.ware.entity.PurchaseEntity;
import com.dadazhang.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseEntity> unReceiveList() {
        Integer[] status = new Integer[]{WareEnum.PurchaseEnum.CREATE.getCode(), WareEnum.PurchaseEnum.ASSIGN.getCode()};
        return list(new QueryWrapper<PurchaseEntity>().in("status", status));
    }

    @Transactional
    @Override
    public void purchaseMerge(PurchaseMergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        //1）如果采购单id为空就自动创建要给采购单
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareEnum.PurchaseEnum.CREATE.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        //2）设置采购详情的信息
        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> purchaseDetailEntities = items.stream().map(detailId -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setStatus(WareEnum.PurchaseDetailEnum.ASSIGN.getCode());
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setId(detailId);
            return purchaseDetailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(purchaseDetailEntities);

        //3）每次合并完采购单信息，对时间进行修改
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setUpdateTime(new Date());
        purchaseEntity.setId(purchaseId);
        this.updateById(purchaseEntity);
    }

    @Transactional
    @Override
    public void receivePurchase(List<Long> ids) {
        //1）获取可以领取的采购单
        List<PurchaseEntity> purchaseEntities = ids.stream().map(this::getById)
                .filter(purchase -> purchase.getStatus().
                        equals(WareEnum.PurchaseEnum.CREATE.getCode()) || purchase.getStatus().
                        equals(WareEnum.PurchaseEnum.ASSIGN.getCode()))
                .peek(purchase -> {
                    purchase.setStatus(WareEnum.PurchaseEnum.RECEIVE.getCode());
                    purchase.setUpdateTime(new Date());
                }).collect(Collectors.toList());

        this.updateBatchById(purchaseEntities);

        //2）修改采购单详情的状态
        if (purchaseEntities.size() > 0) {
            purchaseEntities.forEach(purchase -> {
                List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", purchase.getId()));
                if (purchaseDetailEntities.size() > 0) {
                    List<PurchaseDetailEntity> purchaseDetailEntityList = purchaseDetailEntities.stream().peek(detail -> {
                        detail.setStatus(WareEnum.PurchaseDetailEnum.RECEIVE.getCode());
                    }).collect(Collectors.toList());
                    purchaseDetailService.updateBatchById(purchaseDetailEntityList);
                }
            });
        }
    }

    @Transactional
    @Override
    public void purchaseDone(PurchaseDoneVo doneVo) {

        //1）采购单的状态是采购选项决定的
        List<PurchaseDoneItemVo> items = doneVo.getItems();
        List<PurchaseDetailEntity> updatePurchaseEntity = new ArrayList<>();
        boolean flag = true;
        for (PurchaseDoneItemVo item : items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if (item.getStatus().equals(WareEnum.PurchaseDetailEnum.FAILED.getCode())) {
                flag = false;
                purchaseDetailEntity.setStatus(WareEnum.PurchaseDetailEnum.FAILED.getCode());
            } else {
                // 1.1）修改商品库存
                PurchaseDetailEntity detail = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum());
            }
            purchaseDetailEntity.setStatus(WareEnum.PurchaseDetailEnum.FINISH.getCode());
            purchaseDetailEntity.setId(item.getItemId());
            updatePurchaseEntity.add(purchaseDetailEntity);
        }
        purchaseDetailService.updateBatchById(updatePurchaseEntity);

        //2）修改采购单状态
        Long purchaseId = doneVo.getId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        if (!flag) {
            purchaseEntity.setStatus(WareEnum.PurchaseEnum.ERROR.getCode());
        } else {
            purchaseEntity.setStatus(WareEnum.PurchaseEnum.FINISH.getCode());
        }
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }
}