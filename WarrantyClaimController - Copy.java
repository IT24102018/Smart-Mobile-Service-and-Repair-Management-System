package com.mobix.controller;

import com.mobix.model.Order;
import com.mobix.model.Product;
import com.mobix.model.User;
import com.mobix.model.WarrantyRequest;
import com.mobix.model.WarrantyClaim;
import com.mobix.service.OrderService;
import com.mobix.service.ProductService;
import com.mobix.service.UserService;
import com.mobix.service.WarrantyClaimService;
import com.mobix.service.WarrantyRequestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/warranty-claim")
public class WarrantyClaimController {

    @Autowired
    private WarrantyClaimService warrantyClaimService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private WarrantyRequestService warrantyRequestService;

    // Show warranty claim form for a specific order
    @GetMapping("/{orderId}")
    public String showClaimForm(@PathVariable Long orderId, HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            return "redirect:/users/login";
        }

        User user = userService.getUserByEmail(userEmail);
        Order order = orderService.getOrderById(orderId);

        if (user == null || order == null || !order.getUserId().equals(user.getId())) {
            return "redirect:/users/dashboard";
        }

        Product product = productService.getProductById(order.getProductId());

        if (!isWarrantyAvailable(order, product)) {
            return "redirect:/users/dashboard?claim=expired";
        }

        model.addAttribute("order", order);
        model.addAttribute("product", product);
        model.addAttribute("user", user);

        return "warranty-claim-form";
    }

    // Submit the warranty claim
    @PostMapping("/submit")
    public String submitClaim(@RequestParam Long orderId,
            @RequestParam String issueDescription,
            HttpSession session,
            Model model) {
        try {
            String userEmail = (String) session.getAttribute("userEmail");
            if (userEmail == null) {
                return "redirect:/users/login";
            }

            User user = userService.getUserByEmail(userEmail);
            Order order = orderService.getOrderById(orderId);

            if (user == null || order == null || !order.getUserId().equals(user.getId())) {
                return "redirect:/users/dashboard";
            }

            Product product = productService.getProductById(order.getProductId());

            if (!isWarrantyAvailable(order, product)) {
                return "redirect:/users/dashboard?claim=expired";
            }

            WarrantyClaim claim = new WarrantyClaim();
            claim.setOrderId(orderId);
            claim.setUserId(user.getId());
            claim.setCustomerName(user.getName());
            claim.setCustomerEmail(user.getEmail());
            claim.setProductName(product != null ? product.getName() : order.getProductName());
            claim.setIssueDescription(issueDescription);

            warrantyClaimService.submitClaim(claim);

            return "redirect:/users/dashboard?claim=submitted";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/users/dashboard?error=claim";
        }
    }

    private boolean isWarrantyAvailable(Order order, Product product) {
        if (order == null || order.getOrderDate() == null) {
            return false;
        }

        int baseWarrantyMonths = (product != null && product.getWarrantyMonths() != null)
                ? product.getWarrantyMonths()
                : 12;

        int approvedExtensionMonths = warrantyRequestService.getRequestsByOrderId(order.getId()).stream()
                .filter(request -> "APPROVED".equalsIgnoreCase(request.getRequestStatus()))
                .map(WarrantyRequest::getRequestedExtensionMonths)
                .filter(months -> months != null && months > 0)
                .mapToInt(Integer::intValue)
                .sum();

        LocalDateTime warrantyEnd = order.getOrderDate().plusMonths(baseWarrantyMonths + approvedExtensionMonths);
        return !LocalDateTime.now().isAfter(warrantyEnd);
    }

    // Admin: View all warranty claims
    @GetMapping("/admin/claims")
    public String viewAllClaims(HttpSession session, Model model) {
        Boolean adminLoggedIn = (Boolean) session.getAttribute("adminLoggedIn");
        if (adminLoggedIn == null || !adminLoggedIn) {
            return "redirect:/admin/login";
        }

        model.addAttribute("claims", warrantyClaimService.getAllClaims());
        model.addAttribute("adminEmail", session.getAttribute("adminEmail"));
        return "admin-warranty-claims";
    }

    // Admin: Update warranty claim status
    @PostMapping("/admin/update")
    public String updateClaimStatus(@RequestParam Long claimId,
            @RequestParam String status,
            @RequestParam(required = false) String adminNotes,
            HttpSession session) {
        Boolean adminLoggedIn = (Boolean) session.getAttribute("adminLoggedIn");
        if (adminLoggedIn == null || !adminLoggedIn) {
            return "redirect:/admin/login";
        }

        warrantyClaimService.updateClaimStatus(claimId, status, adminNotes);
        return "redirect:/warranty-claim/admin/claims";
    }
}
