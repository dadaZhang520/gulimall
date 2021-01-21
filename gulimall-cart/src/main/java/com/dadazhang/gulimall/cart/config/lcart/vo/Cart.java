package com.dadazhang.gulimall.cart.config.lcart.vo;

import java.math.BigDecimal;
import java.util.List;

public class Cart {

    private Integer typeNum;
    private Integer checkNum;
    private BigDecimal amount;
    private BigDecimal reduce = new BigDecimal("0.00");
    private List<CartItem> items;

    public Integer getTypeNum() {
        int num = 0;
        if (items != null && items.size() > 0) {
            num = items.size();
        }
        return num;
    }

    public Integer getCheckNum() {
        int num = 0;
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                if (item.isCheck()) {
                    num += item.getCount();
                }
            }
        }
        return num;
    }

    public BigDecimal getAmount() {
        BigDecimal price = new BigDecimal("0.00");
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                if (item.isCheck()) {
                    price = price.add(item.getTotalPrice());
                }
            }
            price = price.subtract(reduce);
        }
        return price;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
}
