package com.dadazhang.gulimall.product.constant;

public class ProductConstant {

    //三级分类缓存Key
    public static final String REDIS_CATALOG_JSON_DATA = "catalogJsonData";

    //分布式锁Key
    public static final String REDIS_CATALOG_LOCK = "catalog_lock";

    public enum AttrEnum {
        ATTR_TYPE_BASE(1, "基本属性"), ATTR_TYPE_SALE(0, "销售属性");

        private final int code;

        private final String msg;

        AttrEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    public enum SpuInfoEnum {
        NEW_SPU(0, "商品新建"), UP_SPU(1, "商品上架"), DOWN_SPU(2, "商品下架");

        private final int code;

        private final String msg;

        SpuInfoEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
