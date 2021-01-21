package com.dadazhang.gulimall.elasticsearch.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dadazhang.common.to.es.SkuEsModel;
import com.dadazhang.common.utils.R;
import com.dadazhang.gulimall.elasticsearch.config.GulimallElasticSearchConfig;
import com.dadazhang.gulimall.elasticsearch.constant.EsConstant;
import com.dadazhang.gulimall.elasticsearch.feign.ProductFeignService;
import com.dadazhang.gulimall.elasticsearch.service.MallSearchService;
import com.dadazhang.gulimall.elasticsearch.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public SearchResultVo search(SearchParamVo param) {

        SearchResultVo result = null;

        //获取构建好的 SearchRequest
        SearchRequest request = BuildSearchRequest(param);

        try {
            //发送请求
            SearchResponse response = restHighLevelClient.search(request, GulimallElasticSearchConfig.COMMON_OPTIONS);
            //分析响应数据
            result = BuildSearchResponse(response, param);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }


    /**
     * 构建请求DSL
     *
     * @param param 请求数据
     * @return SearchRequest
     */
    private SearchRequest BuildSearchRequest(SearchParamVo param) {

        //请求到product索引
        SearchRequest request = new SearchRequest(EsConstant.PRODUCT_INDEX);

        //构建请求数据
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //构建bool查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //1）、构建must

        //1.1）、构建skuTitle
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }

        //2）、构建filter

        //2.1）、构建catalogId
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }

        //2.2）、构建brandId
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        //2.3）、构建hasStock
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        //2.4）、构建skuPrice
        if (!StringUtils.isEmpty(param.getSkuPrice())) {

            //构建range
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");

            //分割price
            String skuPrice = param.getSkuPrice();

            String[] sp = skuPrice.split("_");

            if (sp.length == 2 && !StringUtils.isEmpty(sp[0])) {
                rangeQuery.gte(sp[0]);
                rangeQuery.lte(sp[1]);
            } else {
                if (StringUtils.isEmpty(sp[0])) {
                    rangeQuery.lte(sp[1]);
                } else {
                    rangeQuery.gte(sp[0]);
                }
            }

            boolQuery.filter(rangeQuery);
            if (StringUtils.isEmpty(param.getSort())) {
                sourceBuilder.sort(SortBuilders.fieldSort("skuPrice"));
            }
        }

        //2.5）、构建attr
        if (param.getAttr() != null && param.getAttr().size() > 0) {

            List<String> attrs = param.getAttr();
            for (String attr : attrs) {
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                String[] sp = attr.split("_");
                String attrId = sp[0];
                String[] attrValue;
                if (sp[1].split(":").length == 1) {
                    attrValue = new String[]{sp[1]};
                } else {
                    attrValue = sp[1].split(":");
                }
                boolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                boolQueryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValue));
                boolQuery.filter(QueryBuilders.nestedQuery("attrs", boolQueryBuilder, ScoreMode.None));
            }
        }

        //2.5）、构建sku_attr
        if (param.getSkuAttrs() != null && param.getSkuAttrs().size() > 0) {

            List<String> skuAttrs = param.getSkuAttrs();
            for (String skuAttr : skuAttrs) {
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                String[] sp = skuAttr.replace(" ", "+").split("_");
                String sku_attrId = sp[0];
                String[] attrValue;

                if (sp[1].split(":").length == 1) {
                    attrValue = new String[]{sp[1]};
                } else {
                    attrValue = sp[1].split(":");
                }
                boolQueryBuilder.must(QueryBuilders.termQuery("skuAttrs.attrId", sku_attrId));
                boolQueryBuilder.must(QueryBuilders.termsQuery("skuAttrs.attrValue", attrValue));
                boolQuery.filter(QueryBuilders.nestedQuery("skuAttrs", boolQueryBuilder, ScoreMode.None));
            }
        }


        //添加  query
        sourceBuilder.query(boolQuery);

        //3）、构建  from 、 size
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_SIZE);
        sourceBuilder.size(EsConstant.PRODUCT_SIZE);

        //4）、构建 highlight
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red;'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }

        //5）、构建  sort
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();

            String[] sp = sort.split("_");
            FieldSortBuilder sortBuilder = SortBuilders.fieldSort(sp[0]);
            if (sp[1].equalsIgnoreCase("desc")) {
                sortBuilder.order(SortOrder.DESC);
            } else {
                sortBuilder.order(SortOrder.ASC);
            }
            sourceBuilder.sort(sortBuilder);
        }

        //6）、构建 aggregations
        //6.1）、构建 brand agg
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg");
        brandAgg.field("brandId").size(50);
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brandAgg);

        //6.2）、构建 category agg
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg");
        catalogAgg.field("catalogId").size(50);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalogAgg);

        //6.3）、构建 attr agg
        NestedAggregationBuilder nested_attrAgg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(50);
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        nested_attrAgg.subAggregation(attrIdAgg);
        sourceBuilder.aggregation(nested_attrAgg);

        //6.4）、构建 sku attr agg
        NestedAggregationBuilder nested_SkuAttrAgg = AggregationBuilders.nested("sku_attr_agg", "skuAttrs");
        TermsAggregationBuilder attrId_agg = AggregationBuilders.terms("attr_id_agg").field("skuAttrs.attrId").size(10);
        attrId_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("skuAttrs.attrName").size(1));
        attrId_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("skuAttrs.attrValue").size(10));
        nested_SkuAttrAgg.subAggregation(attrId_agg);
        sourceBuilder.aggregation(nested_SkuAttrAgg);

        System.out.println("构建的DSL:" + sourceBuilder.toString());


        //添加到request中
        request.source(sourceBuilder);

        return request;
    }

    /**
     * 分析响应数据
     *
     * @param response
     * @param param
     * @return SearchResult
     */
    private SearchResultVo BuildSearchResponse(SearchResponse response, SearchParamVo param) throws UnsupportedEncodingException {

        SearchResultVo result = new SearchResultVo();

        SearchHits hits = response.getHits();

        List<SkuEsModel> skuEsModels = new ArrayList<>();

        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    String skuTitle = hit.getHighlightFields().get("skuTitle").fragments()[0].string();
                    skuEsModel.setSkuTitle(skuTitle);
                }
                skuEsModels.add(skuEsModel);
            }
        }

        result.setProducts(skuEsModels);

        //获取聚合出的数据
        ParsedNested attr_nested = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_agg = attr_nested.getAggregations().get("attr_id_agg");

        //获取聚合分析出的属性
        List<SearchResultVo.AttrVo> attrVos = new ArrayList<>();

        for (Terms.Bucket bucket : attr_agg.getBuckets()) {
            SearchResultVo.AttrVo attrVo = new SearchResultVo.AttrVo();
            //获取属性id attrId
            Long attrId = bucket.getKeyAsNumber().longValue();
            //获取属性名 attrName
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            //获取属性值 attrValue
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets()
                    .stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());

            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValues(attrValues);

            attrVos.add(attrVo);
        }

        //获取聚合分析出的分类的属性
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<SearchResultVo.CatalogVo> catalogVos = new ArrayList<>();
        for (Terms.Bucket bucket : catalog_agg.getBuckets()) {
            SearchResultVo.CatalogVo catalogVo = new SearchResultVo.CatalogVo();
            //获取分类id catalogId
            Long catalogId = bucket.getKeyAsNumber().longValue();
            //获取分类名称 catalogName
            String catalogName = ((ParsedStringTerms) bucket.getAggregations().get("catalog_name_agg")).getBuckets().get(0).getKeyAsString();

            catalogVo.setCatId(catalogId);
            catalogVo.setCatName(catalogName);

            catalogVos.add(catalogVo);
        }

        //获取聚合分析出的品牌属性
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        List<SearchResultVo.BrandVo> brandVos = new ArrayList<>();
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResultVo.BrandVo brandVo = new SearchResultVo.BrandVo();
            //获取品牌id brandId
            Long brandId = bucket.getKeyAsNumber().longValue();
            //获取品牌图片 brandImg
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            //获取品牌名称 brandName
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();

            brandVo.setBrandId(brandId);
            brandVo.setImg(brandImg);
            brandVo.setBrandName(brandName);

            brandVos.add(brandVo);
        }

        //获取聚合分析出的sku属性
        ParsedNested skuAttr_nested = response.getAggregations().get("sku_attr_agg");
        ParsedLongTerms sku_attr_agg = skuAttr_nested.getAggregations().get("attr_id_agg");
        List<SearchResultVo.SkuAttrVo> skuAttrVos = new ArrayList<>();
        for (Terms.Bucket bucket : sku_attr_agg.getBuckets()) {
            SearchResultVo.SkuAttrVo skuAttrVo = new SearchResultVo.SkuAttrVo();
            long skuAttrId = bucket.getKeyAsNumber().longValue();
            String skuName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets()
                    .stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());

            skuAttrVo.setAttrId(skuAttrId);
            skuAttrVo.setAttrName(skuName);
            skuAttrVo.setAttrValues(attrValues);

            skuAttrVos.add(skuAttrVo);
        }

        //设置分页信息
        //设置当前页
        result.setPageNum(param.getPageNum());
        //检索到的记录数
        long total = hits.getTotalHits().value;
        //设置总记录数
        result.setTotalCount(total);
        //设置总页数
        int totalPages = (int) total % EsConstant.PRODUCT_SIZE == 0 ? (int) total / EsConstant.PRODUCT_SIZE : ((int) total / EsConstant.PRODUCT_SIZE) + 1;
        result.setTotalPage(totalPages);

        result.setAttrs(attrVos);
        result.setCatalogs(catalogVos);
        result.setBrands(brandVos);
        result.setSkuAttrs(skuAttrVos);

        //设置 NasVo
        List<SearchResultVo.NavVo> navVos = new ArrayList<>();

        if (param.getAttr() != null && param.getAttr().size() > 0) {
            for (String attr : param.getAttr()) {
                SearchResultVo.NavVo navVo = new SearchResultVo.NavVo();
                String[] sp = attr.split("_");
                R r = productFeignService.attrInfo(Long.parseLong(sp[0]));
                result.getAttrNavIds().add(Long.parseLong(sp[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo attrResponseVo = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    //设置navName
                    navVo.setNavName(attrResponseVo.getAttrName());
                }

                //设置navValue
                navVo.setNavValue(sp[1]);

                String encode = URLEncoder.encode(attr, "UTF-8");

                encode = encode.replace("+", "%20");

                String replace = ("&" + param.getQueryString()).replace("&attr=" + encode, "");

                if (replace.startsWith("&")) {
                    replace = replace.substring(1);
                }

                navVo.setLink("http://search.gulimall.com/list.html?" + replace);

                navVos.add(navVo);
            }
        }

        if (param.getSkuAttrs() != null && param.getSkuAttrs().size() > 0) {
            for (String attr : param.getSkuAttrs()) {
                SearchResultVo.NavVo navVo = new SearchResultVo.NavVo();
                String[] sp = attr.split("_");
                R r = productFeignService.skuAttrInfo(Long.parseLong(sp[0]));
                result.getSkuAttrNavIds().add(Long.parseLong(sp[0]));
                if (r.getCode() == 0) {
                    SkuAttrResponse skuAttrResponse = r.getData("skuSaleAttrValue", new TypeReference<SkuAttrResponse>() {
                    });
                    //设置navName
                    navVo.setNavName(skuAttrResponse.getAttrName());
                }

                //设置navValue
                navVo.setNavValue(sp[1].replace(" ", "+"));

                String encode = URLEncoder.encode(attr, "UTF-8");

                String replace = ("&" + param.getQueryString()).replace("&skuAttrs=" + encode, "");

                if (replace.startsWith("&")) {
                    replace = replace.substring(1);
                }

                navVo.setLink("http://search.gulimall.com/list.html?" + replace);

                navVos.add(navVo);
            }
        }

        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            SearchResultVo.NavVo navVo = new SearchResultVo.NavVo();
            R r = productFeignService.brandInfo(param.getBrandId());
            if (r.getCode() == 0) {
                List<BrandResponseVo> brandVoList = r.getData(new TypeReference<List<BrandResponseVo>>() {
                });

                for (BrandResponseVo brandVo : brandVoList) {

                    navVo.setNavName("品牌：");
                    navVo.setNavValue(brandVo.getName());


                    String replace = ("&" + param.getQueryString()).replace("&brandId=" + brandVo.getBrandId(), "");

                    if (replace.startsWith("&")) {
                        replace = replace.substring(1);
                    }

                    navVo.setLink("http://search.gulimall.com/list.html?" + replace);

                    navVos.add(navVo);
                }
            }
        }


        result.setNavs(navVos);

        return result;
    }
}
