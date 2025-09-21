package com.asm5.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Component
public class Config {
    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static String vnp_ReturnUrl = "http://localhost:8080/vnpay/return";
    public static String vnp_TmnCode = "HRPVGH39";
    public static String secretKey = "DJXWIBR43YC20SA07UQFVMWDSOMQR09L";
    public static String vnp_ApiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";

    public static String md5(String message) {
        String digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            digest = sb.toString();
        } catch (UnsupportedEncodingException ex) {
            digest = "";
        } catch (NoSuchAlgorithmException ex) {
            digest = "";
        }
        return digest;
    }

    public static String Sha256(String message) {
        String digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(message.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            digest = sb.toString();
        } catch (UnsupportedEncodingException ex) {
            digest = "";
        } catch (NoSuchAlgorithmException ex) {
            digest = "";
        }
        return digest;
    }

    //Util for VNPAY
  public static String hashAllFields(Map<String, String> fields) {
    List<String> fieldNames = new ArrayList<>(fields.keySet());
    Collections.sort(fieldNames);
    StringBuilder sb = new StringBuilder();
    Iterator<String> itr = fieldNames.iterator();
    while (itr.hasNext()) {
        String fieldName = itr.next();
        String fieldValue = fields.get(fieldName);
        if (fieldValue != null && fieldValue.length() > 0) {
            sb.append(fieldName);
            sb.append("=");
            sb.append(fieldValue);
        }
        if (itr.hasNext()) {
            sb.append("&");
        }
    }
    return hmacSHA512(secretKey, sb.toString());
}

    
    public static String hmacSHA512(final String key, final String data) {
        try {

            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            return "";
        }
    }
    
    public static String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null) {
                ipAdress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAdress = "Invalid IP:" + e.getMessage();
        }
        return ipAdress;
    }

    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static boolean validateSignature(Map<String, String> params) {
    if (!params.containsKey("vnp_SecureHash")) {
        return false;
    }
    String receivedHash = params.get("vnp_SecureHash");
    // Bỏ tham số chữ ký ra khỏi map trước khi tính lại chữ ký
    params.remove("vnp_SecureHash");
    params.remove("vnp_SecureHashType"); // Nếu có

    // Sắp xếp tham số theo thứ tự tăng dần key
    Map<String, String> sortedParams = new TreeMap<>(params);

    // Tạo chuỗi dữ liệu đã encode đúng chuẩn để hash
    StringBuilder hashData = new StringBuilder();
    for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
        if (hashData.length() > 0) {
            hashData.append('&');
        }
        String key = entry.getKey();
        String value = entry.getValue();
        // Encode giá trị theo US_ASCII (hoặc UTF-8 nếu VNPAY yêu cầu)
        String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
        hashData.append(key).append('=').append(encodedValue);
    }

    // Tính chữ ký HMAC SHA512
    String calculatedHash = hmacSHA512(secretKey, hashData.toString());

    // In log debug (bạn có thể comment dòng này khi chạy thật)
    System.out.println("Hash data for signature validation: " + hashData.toString());
    System.out.println("Received hash: " + receivedHash);
    System.out.println("Calculated hash: " + calculatedHash);

    // So sánh chữ ký trả về từ VNPAY và chữ ký tính được
    return receivedHash.equalsIgnoreCase(calculatedHash);
}

}
