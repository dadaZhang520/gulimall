package com.dadazhang.gulimall.ware.feign;

import com.dadazhang.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @GetMapping("/member/memberreceiveaddress/info/{id}")
    R info(@PathVariable("id") Long id);
}
