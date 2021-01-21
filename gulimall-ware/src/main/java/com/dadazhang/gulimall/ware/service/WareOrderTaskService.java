package com.dadazhang.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.gulimall.ware.entity.WareOrderTaskEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-20 13:35:46
 */
public interface WareOrderTaskService extends IService<WareOrderTaskEntity> {

    PageUtils queryPage(Map<String, Object> params);

    WareOrderTaskEntity getWareOrderTaskByOrderSn(String orderSn);
}

