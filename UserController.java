package com.mobix.controller;

import com.mobix.model.Admin;
import com.mobix.model.Order;
import com.mobix.model.Product;
import com.mobix.model.User;
import com.mobix.model.WarrantyClaim;
import com.mobix.service.OrderService;
import com.mobix.service.ProductService;
import com.mobix.service.UserService;
import com.mobix.service.WarrantyClaimService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private com.mobix.service.RepairService repairService;

    @Autowired
    private WarrantyClaimService warrantyClaimService;

    // Show registration form
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // Handle registration
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        try {
            userService.registerUser(user);
            model.addAttribute("success", "Registration successful! Please login.");
            return "redirect:/users/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user); // Preserve form data
            return "register";
        }
    }

    // Show login form
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    // Handle login
    @PostMapping("/login")
    public String loginUser(@RequestParam String email,
            @RequestParam String password,
            Model model,
            HttpSession session) {

        String trimmedEmail = email != null ? email.trim() : "";
        String trimmedPassword = password != null ? password.trim() : "";

        // Intercept Technician Login
        if (com.mobix.model.Technician.validateCredentials(trimmedEmail, trimmedPassword)) {
            session.setAttribute("technicianLoggedIn", true);
            session.setAttribute("technicianEmail", trimmedEmail);
            return "redirect:/technician/dashboard";
        }

        User user = userService.login(trimmedEmail, trimmedPassword);
        if (user != null) {
            // Store user email in session
            session.setAttribute("userEmail", user.getEmail());
            // Redirect to customer dashboard after successful login
            return "redirect:/users/dashboard";
        } else {
            model.addAttribute("error", "Invalid email or password");
            model.addAttribute("email", trimmedEmail);
            return "login";
        }
    }

    // Show customer dashboard
    @GetMapping("/dashboard")
    public String showDashboard(@RequestParam(required = false) String claim,
            HttpSession session,
            Model model) {
        String userEmail = (String) session.getAttribute("userEmail");

        if ("submitted".equalsIgnoreCase(claim)) {
            model.addAttribute("claimAlertMessage", "Warranty claim submitted successfully.");
        } else if ("expired".equalsIgnoreCase(claim)) {
            model.addAttribute("claimAlertMessage", "Warranty is expired. Claim cannot be submitted.");
        }

        if (userEmail != null) {
            User user = userService.getUserByEmail(userEmail);
            if (user != null) {
                // Fetch user's orders
                java.util.List<Order> userOrders = orderService.getOrdersByUserId(user.getId());

                // Create a map of order ID to warranty months
                java.util.Map<Long, Integer> warrantyMap = new java.util.HashMap<>();
                for (Order order : userOrders) {
                    if (order.getProductId() != null) {
                        Product product = productService.getProductById(order.getProductId());
                        if (product != null) {
                            Integer warranty = product.getWarrantyMonths();
                            warrantyMap.put(order.getId(), warranty != null ? warranty : 12);
                        } else {
                            warrantyMap.put(order.getId(), 12);
                        }
                    } else {
                        warrantyMap.put(order.getId(), 12);
                    }
                }

                model.addAttribute("userOrders", userOrders);
                model.addAttribute("warrantyMap", warrantyMap);

                // Fetch user's repair requests
                java.util.List<com.mobix.model.RepairRequest> repairRequests = repairService
                        .getRepairRequestsByEmail(user.getEmail());
                model.addAttribute("repairRequests", repairRequests);

                // Fetch user's warranty claims
                java.util.List<WarrantyClaim> warrantyClaims = warrantyClaimService.getClaimsByUserId(user.getId());
                model.addAttribute("warrantyClaims", warrantyClaims);
            }
        }

        // Check if the logged-in user is the admin
        boolean isAdmin = userEmail != null && Admin.getAdminEmail().equals(userEmail);
        model.addAttribute("isAdmin", isAdmin);
        return "dashboard";
    }

    // Handle logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/users/login";
    }

    // List all users (for admin)
    // Show home page
    @GetMapping({ "/", "/home" })
    public String home() {
        return "index";
    }

    @GetMapping("/list")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "user-list";
    }
}