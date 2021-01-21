package com.dadazhang.gulimall.ware.service.impl;

import com.dadazhang.gulimall.ware.service.WareOrderTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.Query;

import com.dadazhang.gulimall.ware.dao.WareOrderTaskDetailDao;
import com.dadazhang.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.dadazhang.gulimall.ware.service.WareOrderTaskDetailService;


@Service("wareOrderTaskDetailService")
public class WareOrderTaskDetailServiceImpl extends ServiceImpl<WareOrderTaskDetailDao, WareOrderTaskDetailEntity> implements WareOrderTaskDetailService {

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareOrderTaskDetailEntity> page = this.page(
                new Query<WareOrderTaskDetailEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<WareOrderTaskDetailEntity> getWareOrderTaskDetailByTaskId(Long taskId) {

        return list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", taskId));
    }
}