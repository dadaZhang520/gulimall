package com.dadazhang.gulimall.order.feign;

import com.dadazhang.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @GetMapping("member/memberreceiveaddress/address/member/id")
    R getAddressByMemberId(@RequestParam Long memberId);

}
