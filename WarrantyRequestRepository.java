package com.mobix.repository;

import com.mobix.model.WarrantyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WarrantyRequestRepository extends JpaRepository<WarrantyRequest, Long> {
    List<WarrantyRequest> findByUserId(Long userId);
    List<WarrantyRequest> findByOrderId(Long orderId);
    List<WarrantyRequest> findByRequestStatus(String requestStatus);
}
