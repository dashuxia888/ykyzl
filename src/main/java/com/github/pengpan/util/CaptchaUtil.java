package com.github.pengpan.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.kevinsawicki.http.HttpRequest;
import com.github.pengpan.service.CoreService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class CaptchaUtil {

    private final static String URL = "http://pred.fateadm.com/api/capreg";
    private final static String APP_ID = "325342";
    private final static String APP_KEY = "4uPwXX4SFbDIaB6bCorCtOnfxuq4xLFU";
    private final static String PD_ID = "125342";
    private final static String PD_KEY = "L00OwFsU01WvP2kdaWpNC3TPmizU8xk1";
    private final static String PREDICT_TYPE = "50100";
    private final static String SUCCESS_CODE = "0";

    public static String decrypt(String base64Str) {
        if (StringUtil.isEmpty(base64Str)) {
            return StringUtil.EMPTY;
        }

        String stm = DateUtil.getTimestamp(false);
        String sign = calcSign(PD_ID, PD_KEY, stm);
        String asign = calcSign(APP_ID, APP_KEY, stm);

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", PD_ID);
        params.put("timestamp", stm);
        params.put("sign", sign);
        params.put("asign", asign);
        params.put("predict_type", PREDICT_TYPE);
        params.put("img_data", base64Str);

        try {
            String result = HttpRequest.post(URL).readTimeout(3000).form(params).body();
            ResponseResult responseResult = JSON.parseObject(result, ResponseResult.class);
            if (!SUCCESS_CODE.equals(responseResult.getRetCode())) {
                log.error("解析图形验证码失败：{}", JSON.toJSONString(responseResult));
                return StringUtil.EMPTY;
            }
            String rspData = responseResult.getRspData();
            return JSONObject.parseObject(rspData).getString("result");
        } catch (Exception e) {
            log.error("解析图形验证码发生异常：{}", e.getMessage());
            return StringUtil.EMPTY;
        }
    }

    public static String calcSign(String id, String key, String tm) {
        return calcMd5(id + tm + calcMd5(tm + key));
    }

    public static String calcMd5(String src) {
        String md5str = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] input = src.getBytes();
            byte[] buff = md.digest(input);
            md5str = toHex(buff);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5str;
    }

    public static String toHex(byte[] arr) {
        StringBuilder md5str = new StringBuilder();
        int digital;
        for (byte b : arr) {
            digital = b;
            if (digital < 0) {
                digital += 256;
            }
            if (digital < 16) {
                md5str.append("0");
            }
            md5str.append(Integer.toHexString(digital));
        }
        return md5str.toString().toLowerCase();
    }

    @Data
    public static class ResponseResult {
        private String RetCode;
        private String ErrMsg;
        private String RequestId;
        private String RspData;
    }

    public static void main(String[] args) {
        String base64Str = "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";
        String result = decrypt(base64Str);
        System.out.println(result);

//        batchGetCaptcha();
//        batchInvokeCaptcha();
    }

    public static void batchGetCaptcha() {
        AtomicLong aLong = new AtomicLong(1480);
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            try {
                Result<String> captcha = new CoreService().captcha();
                String data = captcha.getData();
                if (StringUtil.isNotEmpty(data)) {
                    byte[] bytes = Base64.getDecoder().decode(data);
                    File f = new File("/Users/pengpan/Downloads/images/captcha-" + aLong.incrementAndGet() + ".png");
                    try (OutputStream os = new FileOutputStream(f)) {
                        os.write(bytes);
                        os.flush();
                    }
                }
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    public static void batchInvokeCaptcha() {
        for (int i = 1; i <= 1480; i++) {
            try {
                File f = new File("/Users/pengpan/Downloads/images/captcha-" + i + ".png");
                try (FileInputStream is = new FileInputStream(f)) {
                    byte[] data = new byte[is.available()];
                    if (is.read(data) > 0) {
                        String base64Str = Base64.getEncoder().encodeToString(data);
                        String decrypt = decrypt(base64Str);
                        System.out.println(i + "--->" + decrypt);
                    }
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

}
