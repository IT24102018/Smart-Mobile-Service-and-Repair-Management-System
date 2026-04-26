package com.mobix.controller;

import com.mobix.model.Order;
import com.mobix.model.Product;
import com.mobix.model.RepairRequest;
import com.mobix.model.User;
import com.mobix.service.OrderService;
import com.mobix.service.RepairAiService;
import com.mobix.service.ProductService;
import com.mobix.service.RepairService;
import com.mobix.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/repair")
public class RepairController {
    @Autowired
    private RepairService repairService;

    @Autowired
    private RepairAiService repairAiService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    private void populateRepairFormData(HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            model.addAttribute("isCustomerInfoLocked", false);
            return;
        }

        User user = userService.getUserByEmail(userEmail);
        if (user == null) {
            model.addAttribute("isCustomerInfoLocked", false);
            return;
        }

        // Auto-fill customer info from registered profile.
        model.addAttribute("customerName", user.getName());
        model.addAttribute("customerEmail", user.getEmail());
        model.addAttribute("contactNumber", user.getPhone());
        model.addAttribute("isCustomerInfoLocked", true);

        List<Order> userOrders = orderService.getOrdersByUserId(user.getId());
        Map<Long, Integer> warrantyMap = new HashMap<>();
        for (Order order : userOrders) {
            if (order.getProductId() != null) {
                Product product = productService.getProductById(order.getProductId());
                if (product != null && product.getWarrantyMonths() != null) {
                    warrantyMap.put(order.getId(), product.getWarrantyMonths());
                } else {
                    warrantyMap.put(order.getId(), 12);
                }
            } else {
                warrantyMap.put(order.getId(), 12);
            }
        }

        model.addAttribute("userOrders", userOrders);
        model.addAttribute("warrantyMap", warrantyMap);
    }

    // Show repair request form
    @GetMapping
    public String showRepairForm(HttpSession session, Model model) {
        populateRepairFormData(session, model);
        model.addAttribute("currentYear", Year.now().getValue());

        return "repair-form";
    }

    @PostMapping(value = "/predict", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> predictRepair(@RequestBody Map<String, Object> payload) {
        try {
            return ResponseEntity.ok(repairAiService.predict(payload));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // Process repair request submission
    @PostMapping("/submit")
    public String submitRepairRequest(@RequestParam String customerName,
            @RequestParam String customerEmail,
            @RequestParam String contactNumber,
            @RequestParam String mobileBrand,
            @RequestParam String mobileModel,
            @RequestParam String year,
            @RequestParam String problemDescription,
            @RequestParam(required = false) MultipartFile image,
            HttpSession session,
            Model model) {
        try {
            // Validate word count (approximately 300 words = 2000 characters)
            if (problemDescription.length() > 2000) {
                model.addAttribute("error", "Description is too long. Please limit to 300 words.");
                populateRepairFormData(session, model);
                model.addAttribute("currentYear", Year.now().getValue());
                return "repair-form";
            }

            int minYear = 2000;
            int currentYear = Year.now().getValue();
            int manufactureYear;

            try {
                manufactureYear = Integer.parseInt(year);
            } catch (NumberFormatException ex) {
                model.addAttribute("error", "Year of manufacture must be a valid number.");
                populateRepairFormData(session, model);
                model.addAttribute("currentYear", currentYear);
                return "repair-form";
            }

            if (manufactureYear < minYear || manufactureYear > currentYear) {
                model.addAttribute("error",
                        "Year of manufacture must be between " + minYear + " and " + currentYear + ".");
                populateRepairFormData(session, model);
                model.addAttribute("currentYear", currentYear);
                return "repair-form";
            }

            RepairRequest request = repairService.createRepairRequest(
                    customerName, customerEmail, contactNumber,
                    mobileBrand, mobileModel, year,
                    problemDescription, image);

            try {
                Map<String, Object> predictionPayload = new HashMap<>();
                predictionPayload.put("problemDescription", problemDescription);
                predictionPayload.put("mobileBrand", mobileBrand);
                predictionPayload.put("mobileModel", mobileModel);
                predictionPayload.put("year", Integer.parseInt(year));
                predictionPayload.put("service_mode", "walk_in");

                Map<String, Object> prediction = repairAiService.predict(predictionPayload);
                Map<String, Object> ingestPayload = new HashMap<>(predictionPayload);
                ingestPayload.clear();
                ingestPayload.put("problem_description", problemDescription);
                ingestPayload.put("mobile_brand", mobileBrand);
                ingestPayload.put("mobile_model", mobileModel);
                ingestPayload.put("manufacture_year", Integer.parseInt(year));
                ingestPayload.put("service_mode", "walk_in");
                ingestPayload.put("device_segment", "lower_mid");
                ingestPayload.put("approx_device_price_lkr", null);
                ingestPayload.put("issue_label", prediction.get("issueLabel"));
                ingestPayload.put("severity", prediction.get("severity"));
                ingestPayload.put("predicted_cost_lkr", prediction.get("predictedCostLkr"));
                ingestPayload.put("predicted_cost_range", prediction.get("predictedCostRange"));
                ingestPayload.put("confidence", prediction.get("confidence"));
                repairAiService.ingest(ingestPayload);
            } catch (Exception aiException) {
                System.err.println("AI training ingest skipped: " + aiException.getMessage());
            }

            return "redirect:/repair/confirmation/" + request.getId();
        } catch (RuntimeException e) {
            model.addAttribute("error", "Failed to submit request: " + e.getMessage());
            populateRepairFormData(session, model);
            model.addAttribute("currentYear", Year.now().getValue());
            return "repair-form";
        }
    }

    // Show repair confirmation page
    @GetMapping("/confirmation/{requestId}")
    public String showConfirmation(@PathVariable Long requestId, Model model) {
        RepairRequest request = repairService.getRepairRequestById(requestId);
        if (request == null) {
            return "redirect:/repair";
        }
        model.addAttribute("request", request);
        return "repair-confirmation";
    }

    // Remove repair request from dashboard
    @PostMapping("/remove/{requestId}")
    public String removeRepairRequest(@PathVariable Long requestId, jakarta.servlet.http.HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail != null) {
            repairService.deleteRepairRequest(requestId);
            return "redirect:/users/dashboard";
        }
        return "redirect:/users/login";
    }
}
