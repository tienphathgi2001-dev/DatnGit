package com.asm5.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.asm5.model.Account;
import com.asm5.model.Order;
import com.asm5.repository.OrderRepository;

@Controller
public class index {
	@Autowired
	private OrderRepository orderRepository;

	@RequestMapping("lienhe")
	public String index0() {
		return "lienhe";
	}

	@RequestMapping("dangnhap")
	public String index1() {
		return "login";
	}

	@RequestMapping("taikhoan")
	public String index2() {
		return "login";
	}

	@RequestMapping("dangky")
	public String index3() {
		return "register";
	}

	@RequestMapping("doimatkhau")
	public String index5() {
		return "doimatkhau";
	}

	@RequestMapping("gioithieu")
	public String index6() {
		return "gioithieu";
	}

	public String index7() {
		return "dangxuat";
	}

	@RequestMapping("/admin/index")
	public String indexAdmin(Model model) {
		Account account = new Account();
		model.addAttribute("account", account);
		return "admin/index";
	}

	@GetMapping("/faqs")
	public String index10() {
		return "qa";
	}

	@GetMapping("/order/track")
	public String trackOrder(@RequestParam(required = false) String code, Model model) {
		if (code != null && !code.isEmpty()) {
			Optional<Order> optionalOrder = orderRepository.findByCode(code);
			if (optionalOrder.isPresent()) {
				Order order = optionalOrder.get();
				model.addAttribute("order", order);
			} else {
				model.addAttribute("error", "Không tìm thấy đơn hàng");
			}
		} else {
			model.addAttribute("error", "Vui lòng cung cấp mã đơn hàng");
		}
		return "order-track"; // Trả về file order-track.html
	}

}
