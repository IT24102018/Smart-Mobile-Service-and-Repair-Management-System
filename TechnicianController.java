package com.mobix.controller;

import com.mobix.model.RepairRequest;
import com.mobix.model.Technician;
import com.mobix.service.RepairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/technician")
public class TechnicianController {

    @Autowired
    private RepairService repairService;

    // Show technician login page
    @GetMapping("/login")
    public String showLogin(HttpSession session) {
        if (session.getAttribute("technicianLoggedIn") != null) {
            return "redirect:/technician/dashboard";
        }
        return "technician-login";
    }

    // Process technician login
    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        // Trim whitespace from inputs
        String trimmedEmail = email != null ? email.trim() : "";
        String trimmedPassword = password != null ? password.trim() : "";

        if (Technician.validateCredentials(trimmedEmail, trimmedPassword)) {
            session.setAttribute("technicianLoggedIn", true);
            session.setAttribute("technicianEmail", trimmedEmail);
            return "redirect:/technician/dashboard";
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "technician-login";
        }
    }

    // Show technician dashboard
    @GetMapping("/dashboard")
    public String showDashboard(
            @RequestParam(required = false) String type,
            HttpSession session, Model model) {
        if (session.getAttribute("technicianLoggedIn") == null) {
            return "redirect:/technician/login";
        }

        List<RepairRequest> allRequests = repairService.getAllRepairRequests();

        // Filter by technician type if requested
        if (type != null && !type.isEmpty()) {
            allRequests = allRequests.stream()
                    .filter(req -> type.equals(req.getAssignedTechnician()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("requests", allRequests);
        model.addAttribute("selectedType", type);
        model.addAttribute("technicianEmail", session.getAttribute("technicianEmail"));
        return "technician-dashboard";
    }

    // Update repair request status
    @PostMapping("/repair-requests/update")
    public String updateRepairRequestStatus(@RequestParam Long requestId,
            @RequestParam String status,
            HttpSession session) {
        if (session.getAttribute("technicianLoggedIn") == null) {
            return "redirect:/technician/login";
        }

        // Technically we might want to ensure the technician can only update requests
        // assigned to them
        // But since they all share the same login, we just update it
        RepairRequest request = repairService.getRepairRequestById(requestId);
        if (request != null) {
            repairService.updateRepairRequestStatus(requestId, status, request.getAssignedTechnician()); // retain the
                                                                                                         // assigned
                                                                                                         // tech
        }

        return "redirect:/technician/dashboard";
    }

    // Technician logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/technician/login";
    }
}
