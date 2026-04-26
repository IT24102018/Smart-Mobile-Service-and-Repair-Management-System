package com.mobix.controller;

import com.mobix.model.Admin;
import com.mobix.model.Order;
import com.mobix.model.Product;
import com.mobix.model.RepairRequest;
import com.mobix.model.SoldItemDTO;
import com.mobix.model.User;
import com.mobix.model.WarrantyRequest;
import com.mobix.service.OrderService;
import com.mobix.service.ProductService;
import com.mobix.service.ReviewService;
import com.mobix.service.UserService;
import com.mobix.service.WarrantyRequestService;
import com.mobix.service.RepairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private WarrantyRequestService warrantyRequestService;

    @Autowired
    private RepairService repairService;

    @Autowired
    private ReviewService reviewService;

    // Show admin login page
    @GetMapping("/login")
    public String showLogin(HttpSession session) {
        if (session.getAttribute("adminLoggedIn") != null) {
            return "redirect:/admin/dashboard";
        }
        return "admin-login";
    }

    // Process admin login
    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        // Trim whitespace from inputs
        String trimmedEmail = email != null ? email.trim() : "";
        String trimmedPassword = password != null ? password.trim() : "";

        if (Admin.validateCredentials(trimmedEmail, trimmedPassword)) {
            session.setAttribute("adminLoggedIn", true);
            session.setAttribute("adminEmail", trimmedEmail);
            return "redirect:/admin/dashboard";
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "admin-login";
        }
    }

    // Show admin dashboard
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("adminEmail", session.getAttribute("adminEmail"));
        return "admin-dashboard";
    }

    // Show dedicated customer feedback page
    @GetMapping("/customer-feedback")
    public String showCustomerFeedback(HttpSession session, Model model) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("reviews", reviewService.getAllReviews());
        model.addAttribute("adminEmail", session.getAttribute("adminEmail"));
        return "admin-customer-feedback";
    }

    // Show add product form
    @GetMapping("/product/add")
    public String showAddProduct(HttpSession session, Model model) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }
        model.addAttribute("product", new Product());
        return "admin-product-form";
    }

    // Process add product
    @PostMapping("/product/add")
    public String addProduct(HttpSession session,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            @RequestParam String brand,
            @RequestParam String category,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam Integer stock,
            @RequestParam String stockStatus,
            @RequestParam(required = false) Integer warrantyMonths,
            Model model) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setBrand(brand);
        product.setCategory(category);
        if ("OUT_OF_STOCK".equalsIgnoreCase(stockStatus)) {
            product.setStock(0);
        } else {
            product.setStock(stock != null && stock > 0 ? stock : 1);
        }
        product.setWarrantyMonths(warrantyMonths != null ? warrantyMonths : 12);

        String warrantyValidationError = validateWarrantyMonths(category, product.getWarrantyMonths());
        if (warrantyValidationError != null) {
            model.addAttribute("error", warrantyValidationError);
            model.addAttribute("product", product);
            return "admin-product-form";
        }

        if (imageFile == null || imageFile.isEmpty()) {
            model.addAttribute("error", "Please upload a product image");
            model.addAttribute("product", product);
            return "admin-product-form";
        }

        product.setImageUrl(saveProductImage(imageFile));

        productService.saveProduct(product);
        return "redirect:/admin/dashboard";
    }

    // Show edit product form
    @GetMapping("/product/edit/{id}")
    public String showEditProduct(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("product", product);
        return "admin-product-form";
    }

    // Process edit product
    @PostMapping("/product/edit/{id}")
    public String updateProduct(@PathVariable Long id,
            HttpSession session,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            @RequestParam String brand,
            @RequestParam String category,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam Integer stock,
            @RequestParam String stockStatus,
            @RequestParam(required = false) Integer warrantyMonths,
            Model model) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        Product existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            return "redirect:/admin/dashboard";
        }

        Integer resolvedWarrantyMonths = warrantyMonths != null ? warrantyMonths : 12;
        String warrantyValidationError = validateWarrantyMonths(category, resolvedWarrantyMonths);
        if (warrantyValidationError != null) {
            Product productWithError = new Product();
            productWithError.setId(existingProduct.getId());
            productWithError.setName(name);
            productWithError.setDescription(description);
            productWithError.setPrice(price);
            productWithError.setBrand(brand);
            productWithError.setCategory(category);
            productWithError.setImageUrl(existingProduct.getImageUrl());
            productWithError.setStock("OUT_OF_STOCK".equalsIgnoreCase(stockStatus) ? 0 : (stock != null && stock > 0 ? stock : 1));
            productWithError.setWarrantyMonths(resolvedWarrantyMonths);
            model.addAttribute("error", warrantyValidationError);
            model.addAttribute("product", productWithError);
            return "admin-product-form";
        }

        Product productDetails = new Product();
        productDetails.setName(name);
        productDetails.setDescription(description);
        productDetails.setPrice(price);
        productDetails.setBrand(brand);
        productDetails.setCategory(category);
        if (imageFile != null && !imageFile.isEmpty()) {
            productDetails.setImageUrl(saveProductImage(imageFile));
        } else {
            productDetails.setImageUrl(existingProduct.getImageUrl());
        }
        if ("OUT_OF_STOCK".equalsIgnoreCase(stockStatus)) {
            productDetails.setStock(0);
        } else {
            productDetails.setStock(stock != null && stock > 0 ? stock : 1);
        }
        productDetails.setWarrantyMonths(resolvedWarrantyMonths);

        productService.updateProduct(id, productDetails);
        return "redirect:/admin/dashboard";
    }

    private String validateWarrantyMonths(String category, Integer warrantyMonths) {
        int months = warrantyMonths != null ? warrantyMonths : 0;
        String normalizedCategory = category != null ? category.trim().toUpperCase() : "";

        if ("PHONE".equals(normalizedCategory) && months <= 6) {
            return "Phone warranty must be greater than 6 months.";
        }
        if ("ACCESSORY".equals(normalizedCategory) && months <= 1) {
            return "Accessory warranty must be greater than 1 month.";
        }
        return null;
    }

    // Delete product
    @GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        productService.deleteProduct(id);
        return "redirect:/admin/dashboard";
    }

    // Delete customer feedback
    @GetMapping("/feedback/delete/{id}")
    public String deleteFeedback(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        reviewService.deleteReviewById(id);
        return "redirect:/admin/customer-feedback";
    }

    // Admin logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    // Show customer services - sold items history and warranty requests
    @GetMapping("/customer-services")
    public String showCustomerServices(HttpSession session, Model model) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        // Get all orders
        java.util.List<Order> allOrders = orderService.getAllOrders();

        // Create a list to store sold items with customer details
        java.util.List<SoldItemDTO> soldItems = new java.util.ArrayList<>();

        for (Order order : allOrders) {
            if (order.getOrderStatus() != null &&
                    (order.getOrderStatus().equals("CONFIRMED") ||
                            order.getOrderStatus().equals("DELIVERED") ||
                            order.getOrderStatus().equals("COMPLETED"))) {

                // Get user details (only if userId is not null)
                User user = null;
                if (order.getUserId() != null) {
                    user = userService.getUserById(order.getUserId());
                }

                SoldItemDTO dto = new SoldItemDTO();
                dto.setOrderId(order.getId());
                dto.setProductName(order.getProductName());
                dto.setQuantity(order.getQuantity());
                dto.setTotalAmount(order.getTotalAmount());
                dto.setOrderDate(order.getOrderDate());
                dto.setOrderStatus(order.getOrderStatus());

                if (user != null) {
                    dto.setCustomerName(user.getName());
                    dto.setCustomerEmail(user.getEmail());
                    dto.setCustomerPhone(user.getPhone());
                } else {
                    dto.setCustomerName(order.getCustomerName());
                    dto.setCustomerEmail(order.getCustomerEmail());
                    dto.setCustomerPhone(order.getCustomerPhone());
                }

                soldItems.add(dto);
            }
        }

        // Get all warranty requests
        java.util.List<WarrantyRequest> warrantyRequests = warrantyRequestService.getAllWarrantyRequests();

        model.addAttribute("soldItems", soldItems);
        model.addAttribute("warrantyRequests", warrantyRequests);
        model.addAttribute("adminEmail", session.getAttribute("adminEmail"));
        return "admin-customer-services";
    }

    // Backward-compatible route for the Customer Orders button
    @GetMapping("/customer-orders")
    public String showCustomerOrders(HttpSession session, Model model) {
        return showCustomerServices(session, model);
    }

    // Approve warranty request
    @GetMapping("/warranty-approve/{id}")
    public String approveWarrantyRequest(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        WarrantyRequest request = warrantyRequestService.getWarrantyRequestById(id);
        if (request != null) {
            warrantyRequestService.approveWarrantyRequest(id, "Approved by admin");
        }

        return "redirect:/admin/customer-services";
    }

    // Reject warranty request
    @GetMapping("/warranty-reject/{id}")
    public String rejectWarrantyRequest(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        WarrantyRequest request = warrantyRequestService.getWarrantyRequestById(id);
        if (request != null) {
            warrantyRequestService.rejectWarrantyRequest(id, "Rejected by admin");
        }

        return "redirect:/admin/customer-services";
    }

    // Approve repair request
    @GetMapping("/repair-approve/{id}")
    public String approveRepairRequest(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }
        repairService.updateRepairRequestStatus(id, "APPROVED", null);
        return "redirect:/admin/repair-requests";
    }

    // Reject repair request
    @GetMapping("/repair-reject/{id}")
    public String rejectRepairRequest(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }
        repairService.updateRepairRequestStatus(id, "REJECTED", null);
        return "redirect:/admin/repair-requests";
    }

    // Show repair requests
    @GetMapping("/repair-requests")
    public String showRepairRequests(HttpSession session, Model model) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        java.util.List<RepairRequest> requests = repairService.getAllRepairRequests();
        model.addAttribute("requests", requests);
        model.addAttribute("repairRequestCount", requests.size());
        model.addAttribute("pendingRepairCount", requests.stream().filter(request -> "PENDING".equalsIgnoreCase(request.getStatus())).count());
        model.addAttribute("activeRepairCount", requests.stream().filter(request -> "APPROVED".equalsIgnoreCase(request.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(request.getStatus())).count());
        model.addAttribute("completedRepairCount", requests.stream().filter(request -> "COMPLETED".equalsIgnoreCase(request.getStatus())).count());
        model.addAttribute("adminEmail", session.getAttribute("adminEmail"));
        return "admin-repair-requests";
    }

    // Update repair request status
    @PostMapping("/repair-requests/update")
    public String updateRepairRequestStatus(@RequestParam Long requestId,
            @RequestParam String status,
            @RequestParam(required = false) String assignedTechnician,
            HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        repairService.updateRepairRequestStatus(requestId, status, assignedTechnician);
        return "redirect:/admin/repair-requests";
    }

    // Show all users
    @GetMapping("/users")
    public String showAllUsers(HttpSession session, Model model) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        java.util.List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("adminEmail", session.getAttribute("adminEmail"));
        return "admin-users";
    }

    // Delete a user
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/admin/login";
        }

        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    private String saveProductImage(MultipartFile imageFile) {
        try {
            Path uploadPath = resolveUploadDirectory("product-images");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = imageFile.getOriginalFilename();
            String extension = ".jpg";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }

            String filename = UUID.randomUUID() + extension;
            Path filePath = uploadPath.resolve(filename);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/product-images/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload product image");
        }
    }

    private Path resolveUploadDirectory(String folderName) {
        Path cwd = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path projectRoot = cwd;

        if (!Files.exists(projectRoot.resolve("pom.xml"))) {
            Path nestedModule = cwd.resolve("MobixWeb");
            if (Files.exists(nestedModule.resolve("pom.xml"))) {
                projectRoot = nestedModule;
            }
        }

        return projectRoot.resolve("uploads").resolve(folderName).normalize();
    }
}
