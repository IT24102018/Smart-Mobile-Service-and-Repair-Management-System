package com.mobix.repository;

import com.mobix.model.WarrantyClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim, Long> {
    List<WarrantyClaim> findByUserId(Long userId);

    List<WarrantyClaim> findByUserIdOrderByClaimDateDesc(Long userId);

    List<WarrantyClaim> findByOrderId(Long orderId);
}
