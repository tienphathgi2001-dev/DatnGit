package com.asm5.service;

import com.asm5.model.CartDetail;
import com.asm5.model.District;
import com.asm5.model.Unit;
import com.asm5.model.Ward;
import com.asm5.repository.DistrictRepository;
import com.asm5.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShippingService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final DistrictRepository districtRepository;
    private final WardRepository wardRepository; // Thêm repository cho Ward

    private static final String GHN_URL = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";
    private static final String TOKEN = "0faeca05-7f15-11f0-bdaf-ae7fa045a771"; // thay bằng token thật

    // Shop mặc định ở Quận 1 HCM
    private static final int FROM_DISTRICT = 1442;
    private static final String FROM_WARD = "21211";
    private static final int UNIT_WEIGHT = 200; // gram / sản phẩm

    public int getShippingFee(String wardCode,String districtCode, List<CartDetail> cartDetails) {
        try {
            // Lấy Ward từ wardCode
            Ward ward = wardRepository.findByMa(wardCode).orElse(null);
            if (ward == null || ward.getDistrict() == null || ward.getDistrict().getCode() == null) {
                return 0; // Không tìm thấy ward hoặc district hoặc code
            }
            String toDistrictCode = ward.getDistrict().getCode(); // Lấy code của district

            HttpHeaders headers = new HttpHeaders();
            headers.set("Token", TOKEN);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Tổng khối lượng (cộng dồn)
            double totalWeight = cartDetails.stream()
                .mapToDouble(cd -> {
                    Unit unit = cd.getProduct().getUnit();
                    return (unit != null && unit.getWeight() != null)
                        ? unit.getWeight() * cd.getQuantity()
                        : 1.0 * cd.getQuantity(); // fallback nếu thiếu
                })
                .sum();

            double maxLength = cartDetails.stream()
                .mapToDouble(cd -> {
                    Unit unit = cd.getProduct().getUnit();
                    return (unit != null && unit.getLength() != null)
                        ? unit.getLength()
                        : 20;
                })
                .max().orElse(20);

            double maxWidth = cartDetails.stream()
                .mapToDouble(cd -> {
                    Unit unit = cd.getProduct().getUnit();
                    return (unit != null && unit.getWidth() != null)
                        ? unit.getWidth()
                        : 15;
                })
                .max().orElse(15);

            double maxHeight = cartDetails.stream()
                .mapToDouble(cd -> {
                    Unit unit = cd.getProduct().getUnit();
                    return (unit != null && unit.getHeight() != null)
                        ? unit.getHeight()
                        : 10;
                })
                .max().orElse(10);

            // Chuyển sang đơn vị gram nếu cần
            int weightGram = Math.max(1, (int)(totalWeight * 1000));

            // Truyền vào GHN API
            Map<String, Object> body = new HashMap<>();
            body.put("from_district_id", FROM_DISTRICT);
            body.put("from_ward_code", FROM_WARD);    
            
            if (districtCode == null || districtCode.isEmpty() || !districtCode.matches("\\d+")) {
                throw new IllegalArgumentException("District code không hợp lệ: " + districtCode);
            }
            body.put("to_district_id", Integer.parseInt(districtCode)); // dùng districtCode truyền từ controller
            body.put("to_ward_code", wardCode);
            body.put("service_type_id", 2);
            body.put("weight", weightGram); // gram
            body.put("length", Math.max(1, (int)maxLength)); // cm
            body.put("width", Math.max(1, (int)maxWidth));   // cm
            body.put("height", Math.max(1, (int)maxHeight)); // cm

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(GHN_URL, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                return data != null ? (Integer) data.get("total") : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
