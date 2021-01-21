package com.dadazhang.gulimall.elasticsearch.service.impl;

import com.alibaba.fastjson.JSON;
import com.dadazhang.common.to.es.SkuEsModel;
import com.dadazhang.gulimall.elasticsearch.config.GulimallElasticSearchConfig;
import com.dadazhang.gulimall.elasticsearch.constant.EsConstant;
import com.dadazhang.gulimall.elasticsearch.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public boolean saveProduct(List<SkuEsModel> skuEsModel) throws IOException {

        //通过BulkRequest进行批量添加
        BulkRequest bulkRequest = new BulkRequest();

        for (SkuEsModel esModel : skuEsModel) {

            //创建IndexRequest 并设置index
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);

            //设置IndexRequest的id
            indexRequest.id(esModel.getSkuId().toString());

            //转换IndexRequest所需要的source资源
            String s = JSON.toJSONString(esModel);

            // //设置IndexRequest的source 并设置资源类型
            indexRequest.source(s, XContentType.JSON);

            bulkRequest.add(indexRequest);
        }

        //进行新增到elasticsearch中,返回响应结果
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        //处理响应结果
        boolean b = bulkResponse.hasFailures();

        for (BulkItemResponse bulkItemResponse : bulkResponse) {
            if (bulkItemResponse.isFailed()) {
                log.info("索引失败: {}", bulkItemResponse.getFailure());
                log.info("索引失败的message: {}", bulkItemResponse.getFailureMessage());
            }
        }

        return b;
    }
}
