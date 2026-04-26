package com.mobix.repository;

import com.mobix.model.RepairRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RepairRequestRepository extends JpaRepository<RepairRequest, Long> {
    List<RepairRequest> findByCustomerEmail(String customerEmail);
    List<RepairRequest> findByStatus(String status);
}
