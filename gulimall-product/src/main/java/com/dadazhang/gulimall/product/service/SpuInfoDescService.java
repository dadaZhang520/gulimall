package com.dadazhang.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.gulimall.product.entity.SpuInfoDescEntity;

import java.util.Map;

/**
 * spu信息介绍
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:28:42
 */
public interface SpuInfoDescService extends IService<SpuInfoDescEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfoDesc(SpuInfoDescEntity spuInfoDescEntity);

}

