package com.dadazhang.gulimall.cart.config.lcart.vo;

import java.math.BigDecimal;
import java.util.List;

public class CartItem {

    private Long skuId;
    private String title;
    private String img;
    private boolean check = true;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;
    private List<String> saleAttr;

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getTotalPrice() {
        return this.price.multiply(new BigDecimal(count));
    }

    public List<String> getSaleAttr() {
        return saleAttr;
    }

    public void setSaleAttr(List<String> saleAttr) {
        this.saleAttr = saleAttr;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
