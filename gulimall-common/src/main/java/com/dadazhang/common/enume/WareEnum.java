package com.dadazhang.common.enume;

public class WareEnum {

    public enum WareTaskStatus {

        LOCK_STOCK(0, "锁定库存"),
        RELEASE_STOCK(1, "释放库存"),
        SUBTRACT_STOCK(2, "减去库存");

        private final Integer code;

        private final String msg;

        WareTaskStatus(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    public enum PurchaseEnum {
        CREATE(0, "新建"),
        ASSIGN(1, "已分配"),
        RECEIVE(2, "采购中"),
        FINISH(3, "采购完成"),
        ERROR(4, "采购异常");

        private final Integer code;

        private final String msg;

        PurchaseEnum(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    public enum PurchaseDetailEnum {
        CREATE(0, "新建"),
        ASSIGN(1, "已分配"),
        RECEIVE(2, "正在采购"),
        FINISH(3, "采购完成"),
        FAILED(4, "采购失败");

        private final Integer code;

        private final String msg;

        PurchaseDetailEnum(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
