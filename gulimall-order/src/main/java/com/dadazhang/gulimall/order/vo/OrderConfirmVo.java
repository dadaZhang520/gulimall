package com.dadazhang.gulimall.order.vo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class OrderConfirmVo {

    //地址信息
    private List<MemberAddressVo> address;
    //下单的商品信息
    private List<OrderItemVo> items;
    //优惠信息（积分信息）
    private Integer integration;
    //商品总金额
    private BigDecimal total;
    //应付价格
    private BigDecimal payPrice;
    //总件数
    private Integer count;
    //库存信息
    private Map<Long,Boolean> hasStock;
    //防重令牌
    private String orderToken;

    public List<MemberAddressVo> getAddress() {
        return address;
    }

    public void setAddress(List<MemberAddressVo> address) {
        this.address = address;
    }

    public List<OrderItemVo> getItems() {
        return items;
    }

    public void setItems(List<OrderItemVo> items) {
        this.items = items;
    }

    public Integer getIntegration() {
        return integration;
    }

    public void setIntegration(Integer integration) {
        this.integration = integration;
    }

    public BigDecimal getTotal() {
        BigDecimal total = new BigDecimal("0");
        if (items != null && items.size() > 0) {
            for (OrderItemVo item : items) {
                total = total.add(item.getPrice().multiply(new BigDecimal(item.getCount())));
            }
        }
        return total;
    }

    public BigDecimal getPayPrice() {

        return getTotal();
    }

    public String getOrderToken() {
        return orderToken;
    }

    public void setOrderToken(String orderToken) {
        this.orderToken = orderToken;
    }

    public Integer getCount() {
        Integer count = 0;
        if (items != null && items.size() > 0) {
            for (OrderItemVo item : items) {
              count += item.getCount();
            }
        }
        return count;
    }

    public Map<Long, Boolean> getHasStock() {
        return hasStock;
    }

    public void setHasStock(Map<Long, Boolean> hasStock) {
        this.hasStock = hasStock;
    }
}


