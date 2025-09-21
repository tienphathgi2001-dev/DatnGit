package com.asm5.service;

import com.asm5.config.Config;
import com.asm5.model.Account;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {

    public String createPaymentUrlForAccount(Account account, String ipAddr, String bankCode) {
        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
        String orderType = "billpayment";
        String vnp_Amount = String.valueOf(100000 * 100); // Test: 100000 VNĐ nhân 100 (đơn vị VND nhỏ nhất)

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0"); // gọi trực tiếp chuỗi
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", Config.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", vnp_Amount);
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan cho tai khoan: " + account.getUserName());
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", Config.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", ipAddr);
        vnp_Params.put("vnp_SecureHashType", "HmacSHA512");

        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }

        // Tạo thời gian tạo và hết hạn theo timezone VN
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Sắp xếp các key theo thứ tự tăng dần a-z
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        // Build chuỗi dữ liệu để tạo hash và chuỗi query url
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                if (hashData.length() > 0) hashData.append('&');
                hashData.append(fieldName).append('=').append(fieldValue);

                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8))
                     .append('=')
                     .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8))
                     .append('&');
            }
        }

        // Tạo chữ ký bảo mật
        String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hashData.toString());
        query.append("vnp_SecureHash=").append(vnp_SecureHash);

        // Trả về url hoàn chỉnh để redirect
        return Config.vnp_PayUrl + "?" + query.toString();
    }

    public boolean verifyResponse(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        // Bỏ 2 tham số không dùng khi tạo hash
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if (hashData.length() > 0) hashData.append('&');
            hashData.append(fieldName).append('=').append(fieldValue);
        }

        String calculatedHash = Config.hmacSHA512(Config.secretKey, hashData.toString());

        return calculatedHash.equalsIgnoreCase(receivedHash);
    }
}
