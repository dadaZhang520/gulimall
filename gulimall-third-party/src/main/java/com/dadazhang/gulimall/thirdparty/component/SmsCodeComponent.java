package com.dadazhang.gulimall.thirdparty.component;

import com.dadazhang.gulimall.thirdparty.utils.HttpUtils;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Data
@ConfigurationProperties("alibaba.cloud.sms")
public class SmsCodeComponent {

    private String host;
    private String path;
    //    private String method;
    private String appcode;
    private String channel;
    private String templateID;

    public void sendSmsCode(String phone, String code) {
        String method = "POST";
        Map<String, String> headers = new HashMap<>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
//        bodys.put("callbackUrl", "http://test.dev.esandcloud.com");
        bodys.put("channel", channel);
        bodys.put("mobile", phone);
        // 19128337914（登录）
        // 20201114191548（注册）
        // 20201114191306（通用）
       // System.out.println(templateID);
        bodys.put("templateID", templateID);
        bodys.put("templateParamSet", "[" + code + "]");


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */

            System.out.println(phone);
            System.out.println(code);
            System.out.println(host);
            System.out.println(path);
            System.out.println(method);
            System.out.println(appcode);
            System.out.println(channel);
            System.out.println(templateID);

            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);



            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
