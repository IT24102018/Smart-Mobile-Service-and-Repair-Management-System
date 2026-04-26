package com.mobix.service;

import com.mobix.model.WarrantyRequest;
import com.mobix.repository.WarrantyRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WarrantyRequestService {
    @Autowired
    private WarrantyRequestRepository warrantyRequestRepository;

    public WarrantyRequest createRequest(WarrantyRequest request) {
        return warrantyRequestRepository.save(request);
    }

    public List<WarrantyRequest> getRequestsByUserId(Long userId) {
        return warrantyRequestRepository.findByUserId(userId);
    }

    public List<WarrantyRequest> getRequestsByOrderId(Long orderId) {
        return warrantyRequestRepository.findByOrderId(orderId);
    }

    public List<WarrantyRequest> getAllRequests() {
        return warrantyRequestRepository.findAll();
    }
    
    // Alias method for backward compatibility
    public List<WarrantyRequest> getAllWarrantyRequests() {
        return getAllRequests();
    }

    public List<WarrantyRequest> getPendingRequests() {
        return warrantyRequestRepository.findByRequestStatus("PENDING");
    }

    public WarrantyRequest getRequestById(Long id) {
        return warrantyRequestRepository.findById(id).orElse(null);
    }
    
    // Alias method for backward compatibility
    public WarrantyRequest getWarrantyRequestById(Long id) {
        return getRequestById(id);
    }

    public WarrantyRequest approveRequest(Long requestId, String adminDecision) {
        WarrantyRequest request = getRequestById(requestId);
        if (request != null) {
            request.setRequestStatus("APPROVED");
            request.setAdminDecision(adminDecision);
            request.setDecisionDate(LocalDateTime.now());
            request.setPaymentStatus("PAID");
            return warrantyRequestRepository.save(request);
        }
        return null;
    }

    public WarrantyRequest rejectRequest(Long requestId, String adminDecision) {
        WarrantyRequest request = getRequestById(requestId);
        if (request != null) {
            request.setRequestStatus("REJECTED");
            request.setAdminDecision(adminDecision);
            request.setDecisionDate(LocalDateTime.now());
            return warrantyRequestRepository.save(request);
        }
        return null;
    }
    
    // Alias methods for approve/reject with different naming
    public WarrantyRequest approveWarrantyRequest(Long requestId, String adminDecision) {
        return approveRequest(requestId, adminDecision);
    }
    
    public WarrantyRequest rejectWarrantyRequest(Long requestId, String adminDecision) {
        return rejectRequest(requestId, adminDecision);
    }
}
