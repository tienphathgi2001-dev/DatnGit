package com.asm5.service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.asm5.repository.StatisticsRepository;

@Service
public class StatisticsService {

	@Autowired
	private StatisticsRepository statisticsRepository;

	public long getTotalProducts() {
		return statisticsRepository.countTotalProducts();
	}

	public long getTotalOrders() {
		return statisticsRepository.countTotalOrders();
	}

	public Double getTotalRevenue() {
		return statisticsRepository.getTotalRevenue();
	}

	public Long getTotalStock() {
		return statisticsRepository.getTotalStock();
	}

	public List<Object[]> getBestSellingProducts() {
		return statisticsRepository.getBestSellingProducts();
	}

	public List<Object[]> getMonthlyRevenue(int year) {
		return statisticsRepository.getMonthlyRevenue(year);
	}

	// ✅ Chuyển LocalDate sang Date để tương thích với repository
	public Double getRevenueBetweenDates(LocalDate startDate, LocalDate endDate) {
		Date start = java.sql.Date.valueOf(startDate);
		Date end = java.sql.Date.valueOf(endDate);
		return statisticsRepository.getRevenueBetweenDates(start, end);
	}

	public List<Object[]> getOrderSummaries() {
		return statisticsRepository.getOrderSummaries();
	}

	public long getOrderCountBetweenDates(LocalDate startDate, LocalDate endDate) {
		Date start = java.sql.Date.valueOf(startDate);
		Date end = java.sql.Date.valueOf(endDate);
		return statisticsRepository.countOrdersBetweenDates(start, end);
	}

	public List<Object[]> getOrdersPerDay(LocalDate startDate, LocalDate endDate) {
		Date start = java.sql.Date.valueOf(startDate);
		Date end = java.sql.Date.valueOf(endDate);
		return statisticsRepository.countOrdersPerDayBetweenDates(start, end);
	}

	public List<Object[]> getProductsSoldBetweenDates(Date startDate, Date endDate) {
		return statisticsRepository.getProductsSoldBetweenDates(startDate, endDate);
	}

	// thống kê đơn hàng theo trạng thái
	public List<Object[]> getOrdersByStatus() {
		return statisticsRepository.getOrdersByStatus();
	}

	// 4. Doanh thu theo sản phẩm
	public List<Object[]> getRevenueByProduct() {
		return statisticsRepository.getRevenueByProduct();
	}

	// 5. Sản phẩm tồn kho thấp
	public List<Object[]> getLowStockProducts(int threshold) {
		return statisticsRepository.getLowStockProducts(threshold)
				.stream()
				.map(p -> new Object[] {
						p.getId(), // ID sản phẩm
						p.getName(), // Tên sản phẩm
						p.getQuantity() // Số lượng tồn
				})
				.toList();
	}
	 // 1️⃣
    public Double getTodayRevenue() {
        return statisticsRepository.getTodayRevenue();
    }

    // 2️⃣
    public Double getThisWeekRevenue() {
        return statisticsRepository.getThisWeekRevenue();
    }

    // 3️⃣
    public Double getThisMonthRevenue() {
        return statisticsRepository.getThisMonthRevenue();
    }

    // 4️⃣
    public List<Object[]> getTopCustomers() {
        return statisticsRepository.getTopCustomers();
    }

	 // Doanh thu theo tất cả khách hàng
    public List<Object[]> getRevenueByCustomer() {
        return statisticsRepository.getRevenueByCustomer();
    }

}
