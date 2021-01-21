package com.dadazhang.gulimall.elasticsearch.service;

import com.dadazhang.gulimall.elasticsearch.vo.SearchParamVo;
import com.dadazhang.gulimall.elasticsearch.vo.SearchResultVo;

public interface MallSearchService {
    SearchResultVo search(SearchParamVo param);
}
