package com.mobix.service;

import com.mobix.model.RepairRequest;
import com.mobix.repository.RepairRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class RepairService {
    @Autowired
    private RepairRequestRepository repairRequestRepository;

    public RepairRequest createRepairRequest(String customerName, String customerEmail,
            String contactNumber, String mobileBrand,
            String mobileModel, String year,
            String problemDescription, MultipartFile image) {
        RepairRequest request = new RepairRequest();
        request.setCustomerName(customerName);
        request.setCustomerEmail(customerEmail);
        request.setContactNumber(contactNumber);
        request.setMobileBrand(mobileBrand);
        request.setMobileModel(mobileModel);
        request.setYear(year);
        request.setProblemDescription(problemDescription);

        // Handle image upload
        if (image != null && !image.isEmpty()) {
            String imagePath = saveImage(image);
            request.setImagePath(imagePath);
        }

        return repairRequestRepository.save(request);
    }

    private String saveImage(MultipartFile image) {
        try {
            // Create directory if it doesn't exist
            Path uploadPath = resolveUploadDirectory("repair-images");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = image.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(image.getInputStream(), filePath);

            return "/uploads/repair-images/" + newFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + e.getMessage());
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

    public RepairRequest getRepairRequestById(Long id) {
        return repairRequestRepository.findById(id).orElse(null);
    }

    public List<RepairRequest> getAllRepairRequests() {
        Sort sortByNewest = Sort.by(Sort.Order.desc("requestDate"), Sort.Order.desc("id"));
        return repairRequestRepository.findAll(sortByNewest);
    }

    public List<RepairRequest> getRepairRequestsByEmail(String email) {
        return repairRequestRepository.findByCustomerEmail(email);
    }

    public RepairRequest updateRepairRequestStatus(Long id, String status, String assignedTechnician) {
        RepairRequest request = getRepairRequestById(id);
        if (request != null) {
            request.setStatus(status);
            if (assignedTechnician != null && !assignedTechnician.trim().isEmpty()) {
                request.setAssignedTechnician(assignedTechnician);
            }
            request.setUpdatedDate(java.time.LocalDateTime.now());
            return repairRequestRepository.save(request);
        }
        return null;
    }

    public void deleteRepairRequest(Long id) {
        repairRequestRepository.deleteById(id);
    }
}
