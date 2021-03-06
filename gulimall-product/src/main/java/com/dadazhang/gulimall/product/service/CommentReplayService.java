package com.dadazhang.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.gulimall.product.entity.CommentReplayEntity;

import java.util.Map;

/**
 * 商品评价回复关系
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:28:42
 */
public interface CommentReplayService extends IService<CommentReplayEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

