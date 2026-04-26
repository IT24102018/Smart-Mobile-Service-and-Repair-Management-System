package com.mobix.controller;

import com.mobix.model.Order;
import com.mobix.model.Product;
import com.mobix.model.User;
import com.mobix.model.WarrantyRequest;
import com.mobix.service.OrderService;
import com.mobix.service.ProductService;
import com.mobix.service.UserService;
import com.mobix.service.WarrantyRequestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/warranty")
public class WarrantyRequestController {
    @Autowired
    private WarrantyRequestService warrantyRequestService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    // Show warranty request form for an order
    @GetMapping("/request/{orderId}")
    public String showRequestForm(@PathVariable Long orderId, HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            return "redirect:/users/login";
        }

        User user = userService.getUserByEmail(userEmail);
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return "redirect:/users/dashboard";
        }

        Product product = productService.getProductById(order.getProductId());
        
        if (user == null || order == null || product == null) {
            return "redirect:/users/dashboard";
        }

        if (!isOutOfWarranty(order, product)) {
            model.addAttribute("order", order);
            model.addAttribute("product", product);
            model.addAttribute("user", user);
            model.addAttribute("warrantyAlert", "Warranty Available");
            model.addAttribute("blockRequest", true);
            return "warranty-request";
        }

        model.addAttribute("order", order);
        model.addAttribute("product", product);
        model.addAttribute("user", user);
        
        // Calculate extension fee: $10 per additional month
        model.addAttribute("feePerMonth", new BigDecimal("10.00"));
        
        return "warranty-request";
    }

    // Submit warranty extension request
    @PostMapping("/request/submit")
    public String submitRequest(@RequestParam Long orderId,
                               @RequestParam Integer extensionMonths,
                               @RequestParam String paymentMethod,
                               HttpSession session,
                               Model model) {
        try {
            String userEmail = (String) session.getAttribute("userEmail");
            if (userEmail == null) {
                return "redirect:/users/login";
            }

            User user = userService.getUserByEmail(userEmail);
            Order order = orderService.getOrderById(orderId);
            
            // Check if order exists before accessing its properties
            if (order == null) {
                return "redirect:/users/dashboard";
            }
            
            Product product = productService.getProductById(order.getProductId());

            if (user == null || product == null) {
                return "redirect:/users/dashboard";
            }

            if (!isOutOfWarranty(order, product)) {
                model.addAttribute("order", order);
                model.addAttribute("product", product);
                model.addAttribute("user", user);
                model.addAttribute("warrantyAlert", "Warranty Available");
                model.addAttribute("blockRequest", true);
                return "warranty-request";
            }

            // Calculate fee: $10 per month
            BigDecimal extensionFee = new BigDecimal("10.00").multiply(new BigDecimal(extensionMonths));
            
            // Get current warranty months (default to 12 if null)
            Integer currentWarranty = product.getWarrantyMonths();
            if (currentWarranty == null) {
                currentWarranty = 12; // Default 1 year warranty
            }

            WarrantyRequest request = new WarrantyRequest();
            request.setUserId(user.getId());
            request.setOrderId(orderId);
            request.setCustomerName(user.getName());
            request.setCustomerEmail(user.getEmail());
            request.setProductName(product.getName()); // Set product name
            request.setCurrentWarrantyMonths(currentWarranty);
            request.setRequestedExtensionMonths(extensionMonths);
            request.setTotalWarrantyMonths(currentWarranty + extensionMonths);
            request.setExtensionFee(extensionFee);
            request.setPaymentMethod(paymentMethod);

            warrantyRequestService.createRequest(request);

            return "redirect:/users/dashboard?warranty=requested";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/users/dashboard";
        }
    }

    // Admin: View all warranty requests
    @GetMapping("/admin/requests")
    public String viewAllRequests(HttpSession session, Model model) {
        Boolean adminLoggedIn = (Boolean) session.getAttribute("adminLoggedIn");
        if (adminLoggedIn == null || !adminLoggedIn) {
            return "redirect:/admin/login";
        }

        model.addAttribute("requests", warrantyRequestService.getAllRequests());
        return "admin-warranty-requests";
    }

    // Admin: Approve warranty request
    @GetMapping("/admin/approve/{id}")
    public String approveRequest(@PathVariable Long id,
                                @RequestParam(required = false) String decision,
                                HttpSession session) {
        Boolean adminLoggedIn = (Boolean) session.getAttribute("adminLoggedIn");
        if (adminLoggedIn == null || !adminLoggedIn) {
            return "redirect:/admin/login";
        }

        warrantyRequestService.approveRequest(id, decision != null ? decision : "Approved by admin");
        return "redirect:/warranty/admin/requests";
    }

    // Admin: Reject warranty request
    @GetMapping("/admin/reject/{id}")
    public String rejectRequest(@PathVariable Long id,
                               @RequestParam(required = false) String decision,
                               HttpSession session) {
        Boolean adminLoggedIn = (Boolean) session.getAttribute("adminLoggedIn");
        if (adminLoggedIn == null || !adminLoggedIn) {
            return "redirect:/admin/login";
        }

        warrantyRequestService.rejectRequest(id, decision != null ? decision : "Rejected by admin");
        return "redirect:/warranty/admin/requests";
    }

    private boolean isOutOfWarranty(Order order, Product product) {
        Integer baseWarrantyMonths = product.getWarrantyMonths() != null ? product.getWarrantyMonths() : 12;
        int approvedExtensionMonths = warrantyRequestService.getRequestsByOrderId(order.getId()).stream()
                .filter(request -> "APPROVED".equalsIgnoreCase(request.getRequestStatus()))
                .map(WarrantyRequest::getRequestedExtensionMonths)
                .filter(months -> months != null && months > 0)
                .mapToInt(Integer::intValue)
                .sum();

        int totalWarrantyMonths = baseWarrantyMonths + approvedExtensionMonths;
        LocalDateTime warrantyEnd = order.getOrderDate().plusMonths(totalWarrantyMonths);
        return LocalDateTime.now().isAfter(warrantyEnd);
    }
}
