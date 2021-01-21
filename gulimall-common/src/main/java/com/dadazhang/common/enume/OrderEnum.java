package com.dadazhang.common.enume;

public class OrderEnum {

    public static enum OrderStatusEnum {
        CREATE_NEW(0, "待付款"),
        PAYED(1, "已付款"),
        SENDED(2, "已发货"),
        RECIEVED(3, "已完成"),
        CANCLED(4, "已取消"),
        SERVICING(5, "售后中"),
        SERVICED(6, "售后完成");
        private Integer code;
        private String msg;

        OrderStatusEnum(Integer code, String msg) {
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

    public static enum OrderDelStatusEnum {
        NOT_DELETE(0, "未删除"),
        DELETE(1, "已删除");
        private Integer code;
        private String msg;

        OrderDelStatusEnum(Integer code, String msg) {
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
