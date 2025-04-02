package com.nhhoang.e_commerce.config;

import org.apache.commons.codec.digest.HmacUtils;

import java.util.Map;
import java.util.TreeMap;

public class VNPayUtil {

    public static String hmacSHA512(String key, String data) {
        return HmacUtils.hmacSha512Hex(key, data);
    }

    public static String getQueryString(Map<String, String> params) {
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (query.length() > 0) {
                query.append("&");
            }
            query.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return query.toString();
    }
}