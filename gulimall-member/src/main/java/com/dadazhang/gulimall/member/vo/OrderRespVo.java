package com.dadazhang.gulimall.member.vo;
import lombok.Data;

import java.util.List;

@Data
public class OrderRespVo {

    private int totalCount;
    private int pageSize;
    private int totalPage;
    private int currPage;
    private List<OrderVo> list;

}