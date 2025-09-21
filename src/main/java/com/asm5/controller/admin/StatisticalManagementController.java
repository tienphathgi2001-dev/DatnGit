package com.asm5.controller.admin;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.asm5.service.StatisticsService;

@Controller
public class StatisticalManagementController {

	@Autowired
	private StatisticsService statisticsService;

	@GetMapping("/admin/sanpham")
	public String productStatistics(Model model,
			@RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

			@RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		// ✅ 2. Sản phẩm bán chạy & doanh thu theo tháng
		model.addAttribute("bestSellingProducts", statisticsService.getBestSellingProducts());

		// ✅ Thống kê sản phẩm đã bán theo khoảng ngày
		if (startDate != null && endDate != null) {
			if (endDate.isBefore(startDate)) {
				model.addAttribute("error", "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.");
			} else {
				// Gọi service → repo
				List<Object[]> products = statisticsService.getProductsSoldBetweenDates(
						java.sql.Date.valueOf(startDate),
						java.sql.Date.valueOf(endDate));
				model.addAttribute("products", products);
				model.addAttribute("startDate", startDate);
				model.addAttribute("endDate", endDate);
			}
		}
		// doanh thu theo từng sản phẩm
		model.addAttribute("data", statisticsService.getRevenueByProduct());

		// ✅ sản phẩm sắp hết hàng
		model.addAttribute("lowStockProducts", statisticsService.getLowStockProducts(5));

		return "admin/sanpham";
	}

	@GetMapping("/admin/donhang")
	public String oderStatistics(Model model,
			@RequestParam(name = "year", required = false, defaultValue = "2024") int year,
			@RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		// 4. Danh sách đơn hàng (từ query bạn viết ở repository)
		model.addAttribute("orderSummaries", statisticsService.getOrderSummaries());

		// ✅ 5. Thống kê đơn hàng trong khoảng ngày
		if (startDate != null && endDate != null) {
			if (endDate.isBefore(startDate)) {
				model.addAttribute("error", "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.");
			} else {
				Double revenueBetween = statisticsService.getRevenueBetweenDates(startDate, endDate);
				model.addAttribute("revenueBetween", revenueBetween != null ? revenueBetween : 0.0);

				long orderCountBetween = statisticsService.getOrderCountBetweenDates(startDate, endDate);
				model.addAttribute("orderCountBetween", orderCountBetween);

				model.addAttribute("ordersPerDay", statisticsService.getOrdersPerDay(startDate, endDate));

				model.addAttribute("startDate", startDate);
				model.addAttribute("endDate", endDate);
			}
		}
		// đơn hàng theo trạng thái
		model.addAttribute("data", statisticsService.getOrdersByStatus());

		return "admin/donhang";
	}

	// Doanh thu
	@GetMapping("/admin/doanhthu")
	public String showRevenue(Model model,
			@RequestParam(name = "year", required = false, defaultValue = "2024") int year,
			@RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		// ✅ 1. Thống kê tổng quát
		model.addAttribute("totalProducts", statisticsService.getTotalProducts());
		model.addAttribute("totalOrders", statisticsService.getTotalOrders());
		model.addAttribute("totalRevenue", statisticsService.getTotalRevenue());
		model.addAttribute("totalStock", statisticsService.getTotalStock());

		model.addAttribute("monthlyRevenue", statisticsService.getMonthlyRevenue(year));
		model.addAttribute("year", year); // để gán lại lên ô input nếu cần

		// ✅ 3. Doanh thu theo khoảng ngày
		if (startDate != null && endDate != null) {
			// Luôn gắn lại để giữ giá trị trên form
			model.addAttribute("startDate", startDate);
			model.addAttribute("endDate", endDate);

			if (endDate.isBefore(startDate)) {
				model.addAttribute("error", "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.");
			} else {
				Double revenueBetween = statisticsService.getRevenueBetweenDates(startDate, endDate);
				model.addAttribute("revenueBetween", revenueBetween != null ? revenueBetween : 0.0);
			}
		}
		// ✅ 4. Doanh thu hôm nay / tuần này / tháng này
		model.addAttribute("todayRevenue", statisticsService.getTodayRevenue());
		model.addAttribute("thisWeekRevenue", statisticsService.getThisWeekRevenue());
		model.addAttribute("thisMonthRevenue", statisticsService.getThisMonthRevenue());

		// 4️⃣ Top khách hàng
		model.addAttribute("topCustomers", statisticsService.getTopCustomers());

		// 5️⃣ Doanh thu theo tất cả khách hàng (bảng & biểu đồ)
		model.addAttribute("revenueByCustomer", statisticsService.getRevenueByCustomer());

		return "admin/doanhthu"; //
	}

}
