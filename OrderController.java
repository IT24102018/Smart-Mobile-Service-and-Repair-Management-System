package com.mobix.controller;

import com.mobix.model.Order;
import com.mobix.model.Product;
import com.mobix.model.User;
import com.mobix.service.OrderService;
import com.mobix.service.ProductService;
import com.mobix.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    // Show checkout page for a product
    @GetMapping("/checkout/{productId}")
    public String showCheckout(@PathVariable Long productId, Model model, HttpSession session) {
        try {
            Product product = productService.getProductById(productId);
            if (product == null) {
                return "redirect:/shop";
            }
            model.addAttribute("product", product);

            // Get logged-in user info and pre-fill form
            String userEmail = (String) session.getAttribute("userEmail");
            if (userEmail != null) {
                User user = userService.getUserByEmail(userEmail);
                if (user != null) {
                    model.addAttribute("customerName", user.getName());
                    model.addAttribute("customerEmail", user.getEmail());
                    model.addAttribute("customerPhone", user.getPhone());
                    model.addAttribute("shippingAddress", user.getAddress());
                }
            }

            return "checkout";
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error loading checkout: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/shop";
        }
    }

    // Process checkout and create order
    @PostMapping("/checkout")
    public String processCheckout(@RequestParam Long productId,
            @RequestParam String customerName,
            @RequestParam String customerEmail,
            @RequestParam String customerPhone,
            @RequestParam(required = false) String shippingAddress,
            @RequestParam String paymentMethod,
            HttpSession session,
            Model model) {
        try {
            // Get logged-in user from session
            String userEmail = (String) session.getAttribute("userEmail");
            User user = userService.getUserByEmail(userEmail);

            if (user == null) {
                model.addAttribute("error", "Please login to continue");
                return "redirect:/users/login";
            }

            // Use typed checkout address when provided, otherwise fallback to profile address.
            String finalShippingAddress = shippingAddress;
            if (finalShippingAddress == null || finalShippingAddress.trim().isEmpty()) {
                finalShippingAddress = user.getAddress();
            }
            if (finalShippingAddress == null || finalShippingAddress.trim().isEmpty()) {
                model.addAttribute("error", "Shipping address not found. Please update your account address.");
                model.addAttribute("product", productService.getProductById(productId));
                model.addAttribute("customerName", customerName);
                model.addAttribute("customerEmail", customerEmail);
                model.addAttribute("customerPhone", customerPhone);
                return "checkout";
            }

            Order order = orderService.createOrder(productId, customerName, customerEmail,
                    customerPhone, finalShippingAddress, paymentMethod, user.getId());
            return "redirect:/order/payment/" + order.getId();
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("product", productService.getProductById(productId));
            model.addAttribute("customerName", customerName);
            model.addAttribute("customerEmail", customerEmail);
            model.addAttribute("customerPhone", customerPhone);
            model.addAttribute("shippingAddress", shippingAddress);
            return "checkout";
        }
    }

    // Show payment page
    @GetMapping("/payment/{orderId}")
    public String showPayment(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return "redirect:/shop";
        }
        model.addAttribute("order", order);
        return "payment";
    }

    // Process payment
    @PostMapping("/payment/{orderId}")
    public String processPayment(@PathVariable Long orderId,
            @RequestParam String cardNumber,
            @RequestParam String cardHolder,
            @RequestParam String expiryDate,
            @RequestParam String cvv,
            Model model) {
        try {
            // In a real application, you would integrate with a payment gateway here
            // For demo purposes, we'll simulate a successful payment
            Order order = orderService.processPayment(orderId);
            return "redirect:/order/confirmation/" + order.getId();
        } catch (RuntimeException e) {
            model.addAttribute("error", "Payment failed: " + e.getMessage());
            model.addAttribute("order", orderService.getOrderById(orderId));
            return "payment";
        }
    }

    // Show order confirmation
    @GetMapping("/confirmation/{orderId}")
    public String showConfirmation(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return "redirect:/shop";
        }
        model.addAttribute("order", order);
        return "confirmation";
    }

    // Show receipt page
    @GetMapping("/receipt/{orderId}")
    public String showReceipt(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return "redirect:/shop";
        }
        model.addAttribute("order", order);
        return "receipt";
    }

    // Remove order from dashboard
    @PostMapping("/remove/{orderId}")
    public String removeOrder(@PathVariable Long orderId, HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail != null) {
            orderService.deleteOrder(orderId);
            return "redirect:/users/dashboard";
        }
        return "redirect:/users/login";
    }
}
