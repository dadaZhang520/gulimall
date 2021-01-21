package com.dadazhang.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.gulimall.ware.vo.PurchaseDoneVo;
import com.dadazhang.gulimall.ware.vo.PurchaseMergeVo;
import com.dadazhang.gulimall.ware.entity.PurchaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-20 13:35:46
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<PurchaseEntity> unReceiveList();

    void purchaseMerge (PurchaseMergeVo mergeVo);

    void receivePurchase(List<Long> ids);

    void purchaseDone (PurchaseDoneVo doneVo);
}

