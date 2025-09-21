package com.asm5.interceptor;

import com.asm5.model.Account;
import com.asm5.service.accountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Autowired
    private com.asm5.service.accountService accountService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false); // dùng false để tránh tạo session mới

        if (session == null || session.getAttribute("accountId") == null) {
            response.sendRedirect("/taikhoan"); // chưa đăng nhập
            return false;
        }

        Integer accountId = (Integer) session.getAttribute("accountId");
        Account account = accountService.findById(accountId);

        if (account == null || account.getAdmin() == null || !account.getAdmin()) {
            response.sendRedirect("/access-denied"); // không phải admin
            return false;
        }

        return true; // được phép truy cập
    }
}
