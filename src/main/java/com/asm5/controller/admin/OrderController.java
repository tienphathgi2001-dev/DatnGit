package com.asm5.controller.admin;

import com.asm5.model.Order;
import com.asm5.model.OrderDetail;
import com.asm5.repository.NewsRepository;
import com.asm5.repository.OrderDetailRepository;
import com.asm5.repository.OrderRepository;
import com.asm5.service.StatisticsService;

import java.util.List;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.propertyeditors.CustomDateEditor;


@Controller("adminOrderController") // Specify a unique bean name
@RequestMapping("/admin")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;
   

    @Autowired
    private OrderDetailRepository orderDetailRepository;
    
    @Autowired
    private  NewsRepository newsRepository;
    
    @Autowired
    private StatisticsService statisticsService;
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

@RequestMapping("/order")
public String listOrders(Model model,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "5") int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
    Page<Order> orderPage = orderRepository.findAll(pageable);


    model.addAttribute("list", orderPage.getContent()); // ✅ gửi đúng tên
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", orderPage.getTotalPages());
    model.addAttribute("totalItems", orderPage.getTotalElements());

    return "admin/Order/qlhoadon";
}
    @RequestMapping("/editOrder/{id}")
    public String editOrder(@PathVariable("id") Integer id, Model model) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + id));
        
        // Lấy chi tiết hóa đơn
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(id); // Gọi thông qua biến đã tiêm

        System.out.println("Order details size: " + orderDetails.size());

        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails); // Gửi sang view
        return "admin/Order/editOrder";
    }
@PostMapping("/updateOrder")
public String updateOrder(@ModelAttribute("order") Order order, RedirectAttributes redirectAttrs) {
    if (order.getId() == null) {
        throw new IllegalArgumentException("Order ID must not be null");
    }

    Order existingOrder = orderRepository.findById(order.getId())
            .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + order.getId()));

    int oldStatus = existingOrder.getStatus();
    int newStatus = order.getStatus();

    // Không cho lùi trạng thái
    if (newStatus < oldStatus) {
        redirectAttrs.addFlashAttribute("error", "Không thể quay lại trạng thái trước đó!");
        return "redirect:/admin/order/editOrder/" + order.getId();
    }

    // Nếu đã "Đang giao" (3) hoặc "Hoàn thành" (4) thì không cho hủy
    if ((oldStatus == 3 || oldStatus == 4) && newStatus == 0) {
        redirectAttrs.addFlashAttribute("error", "Đơn hàng đã giao/hoàn thành, không thể hủy!");
        return "redirect:/admin/order/editOrder/" + order.getId();
    }

    // Nếu pass validation thì cập nhật
    existingOrder.setStatus(newStatus);
    existingOrder.setPaymentStatus(order.getPaymentStatus());
    existingOrder.setDiscount(order.getDiscount());
    existingOrder.setDiscountCode(order.getDiscountCode());
    existingOrder.setFeeship(order.getFeeship());
    existingOrder.setTotal(order.getTotal());
    existingOrder.setPaymentMethod(order.getPaymentMethod());

    orderRepository.save(existingOrder);

    redirectAttrs.addFlashAttribute("success", "Cập nhật trạng thái đơn hàng thành công!");
    return "redirect:/admin/order";
}



    @PostMapping("/deleteOrder/{id}")
    public String deleteOrder(@PathVariable("id") Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + id));
        orderRepository.delete(order);
        return "redirect:/admin/order";
    }

    @RequestMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order Id: " + id));
        orderRepository.delete(order);
        return "redirect:/admin/order";
    }
    @GetMapping("/index")
    public String dashboard(Model model) {

        // Tổng sản phẩm
        model.addAttribute("totalProducts", statisticsService.getTotalProducts());

        // Tổng đơn hàng
        model.addAttribute("totalOrders", statisticsService.getTotalOrders());

        // Tổng doanh thu
        Double totalRevenue = statisticsService.getTotalRevenue();
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);

        long totalNews = newsRepository.countAllNews();

        // Thêm vào model
        model.addAttribute("totalNews", totalNews);
        
     // Biểu đồ doanh thu
        List<Object[]> monthlyRevenue = statisticsService.getMonthlyRevenue(LocalDate.now().getYear());

        List<String> revenueLabels = new ArrayList<>();
        List<Double> revenueData = new ArrayList<>();
        if (monthlyRevenue != null) {
            for (Object[] r : monthlyRevenue) {
                revenueLabels.add("Tháng " + r[0]);
                Number value = (Number) r[1];      // r[1] có thể là Long hoặc Double
                revenueData.add(value != null ? value.doubleValue() : 0.0);
            }
        }
        model.addAttribute("revenueLabels", revenueLabels);
        model.addAttribute("revenueData", revenueData);
;

        return "admin/index"; // file Thymeleaf dashboard
    }
    
}