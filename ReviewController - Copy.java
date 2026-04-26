package com.mobix.controller;

import com.mobix.model.Review;
import com.mobix.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private static final String REVIEW_IMAGE_UPLOAD_DIR = "src/main/resources/static/uploads/review-images/";

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/add")
    public String showReviewForm(Model model, HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        // Pre-fill email if user is logged in
        model.addAttribute("userEmail", userEmail);
        model.addAttribute("technicians", getTechnicianOptions());
        return "review-form";
    }

    @PostMapping("/add")
    public String submitReview(@RequestParam(required = false) String userEmail,
            @RequestParam(defaultValue = "GENERAL") String feedbackType,
            @RequestParam(required = false) String technicianName,
            @RequestParam Integer rating,
            @RequestParam String comment,
            @RequestParam(required = false) MultipartFile image,
            Model model,
            RedirectAttributes redirectAttributes) {

        if ("TECHNICIAN".equalsIgnoreCase(feedbackType) && (technicianName == null || technicianName.trim().isEmpty())) {
            model.addAttribute("errorMessage", "Please select a technician for technician feedback.");
            model.addAttribute("userEmail", userEmail);
            model.addAttribute("feedbackType", feedbackType);
            model.addAttribute("comment", comment);
            model.addAttribute("rating", rating);
            model.addAttribute("technicians", getTechnicianOptions());
            return "review-form";
        }

        Review review = new Review();
        review.setUserEmail(userEmail != null && !userEmail.isEmpty() ? userEmail : "Anonymous");
        review.setRating(rating);
        review.setComment(comment);
        review.setFeedbackType(feedbackType);
        review.setTechnicianName("TECHNICIAN".equalsIgnoreCase(feedbackType) ? technicianName : null);

        if (image != null && !image.isEmpty()) {
            review.setImagePath(saveReviewImage(image));
        }

        reviewService.saveReview(review);

        redirectAttributes.addFlashAttribute("successMessage", "Thank you for your feedback!");
        return "redirect:/"; // Redirect back to home
    }

    private String saveReviewImage(MultipartFile imageFile) {
        try {
            Path uploadPath = Paths.get(REVIEW_IMAGE_UPLOAD_DIR);
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

            return "/uploads/review-images/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload feedback image");
        }
    }

    private List<String> getTechnicianOptions() {
        return List.of("Screen Technician", "Battery Technician", "Software Technician", "Hardware Technician");
    }
}
