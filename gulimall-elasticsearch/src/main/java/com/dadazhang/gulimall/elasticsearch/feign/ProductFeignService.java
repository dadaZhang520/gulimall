package com.dadazhang.gulimall.elasticsearch.feign;

import com.dadazhang.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("/product/attr/info/{attrId}")
    R attrInfo(@PathVariable("attrId") Long attrId);

    @GetMapping("/product/skusaleattrvalue/info/{id}")
    R skuAttrInfo(@PathVariable("id") Long id);

    @GetMapping("/product/brand/infos")
    R brandInfo(@RequestParam List<Long> brandIds);

}
