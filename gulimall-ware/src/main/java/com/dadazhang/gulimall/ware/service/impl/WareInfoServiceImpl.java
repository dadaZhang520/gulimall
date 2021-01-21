package com.dadazhang.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.dadazhang.common.utils.R;
import com.dadazhang.gulimall.ware.feign.MemberFeignService;
import com.dadazhang.gulimall.ware.vo.FareVo;
import com.dadazhang.gulimall.ware.vo.MemberAddressVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.Query;

import com.dadazhang.gulimall.ware.dao.WareInfoDao;
import com.dadazhang.gulimall.ware.entity.WareInfoEntity;
import com.dadazhang.gulimall.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");

        if (!StringUtils.isEmpty(key)) {
            wrapper.eq("id", key)
                    .or().like("name", key)
                    .or().like("address", key)
                    .or().like("areacode", key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        R r = memberFeignService.info(addrId);
        if (r.getCode() == 0) {
            MemberAddressVo addressVo = r.getData(new TypeReference<MemberAddressVo>() {
            });
            fareVo.setMemberAddressVo(addressVo);
            fareVo.setFarePrice(new BigDecimal(addressVo.getPhone().substring(addressVo.getPhone().length() - 1)));
            return fareVo;
        }
        return null;
    }

}