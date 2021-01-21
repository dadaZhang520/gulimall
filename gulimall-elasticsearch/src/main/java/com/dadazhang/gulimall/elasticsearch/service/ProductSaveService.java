package com.dadazhang.gulimall.elasticsearch.service;

import com.dadazhang.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProductSaveService {

    boolean saveProduct(List<SkuEsModel> skuEsModel) throws IOException;
}
