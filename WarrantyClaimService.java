package com.mobix.service;

import com.mobix.model.WarrantyClaim;
import com.mobix.repository.WarrantyClaimRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WarrantyClaimService {

    @Autowired
    private WarrantyClaimRepository warrantyClaimRepository;

    public WarrantyClaim submitClaim(WarrantyClaim claim) {
        claim.setClaimDate(LocalDateTime.now());
        claim.setUpdatedDate(LocalDateTime.now());
        claim.setStatus("PENDING");
        return warrantyClaimRepository.save(claim);
    }

    public List<WarrantyClaim> getAllClaims() {
        Sort sortByNewest = Sort.by(Sort.Order.desc("claimDate"), Sort.Order.desc("id"));
        return warrantyClaimRepository.findAll(sortByNewest);
    }

    public List<WarrantyClaim> getClaimsByUserId(Long userId) {
        return warrantyClaimRepository.findByUserIdOrderByClaimDateDesc(userId);
    }

    public WarrantyClaim getClaimById(Long id) {
        return warrantyClaimRepository.findById(id).orElse(null);
    }

    public WarrantyClaim updateClaimStatus(Long id, String status, String adminNotes) {
        Optional<WarrantyClaim> optionalClaim = warrantyClaimRepository.findById(id);
        if (optionalClaim.isPresent()) {
            WarrantyClaim claim = optionalClaim.get();
            claim.setStatus(status);
            if (adminNotes != null && !adminNotes.trim().isEmpty()) {
                claim.setAdminNotes(adminNotes);
            }
            claim.setUpdatedDate(LocalDateTime.now());
            return warrantyClaimRepository.save(claim);
        }
        return null;
    }
}
