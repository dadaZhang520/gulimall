package com.dadazhang.gulimall.elasticsearch.app;

import com.dadazhang.common.exception.BizCodeEnum;
import com.dadazhang.common.to.es.SkuEsModel;
import com.dadazhang.common.utils.R;
import com.dadazhang.gulimall.elasticsearch.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequestMapping("search/save")
@RestController
public class SearchSaveController {

    @Autowired
    ProductSaveService productSaveService;

    //上架商品
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModel) {

        boolean b = false;
        try {
            b = productSaveService.saveProduct(skuEsModel);
        } catch (IOException e) {
            log.error("SearchSaveController ： {}", e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }

        if (b) {
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }
        return R.ok();
    }

}
