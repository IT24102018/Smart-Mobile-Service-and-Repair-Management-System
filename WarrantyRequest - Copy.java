package com.mobix.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "warranty_requests")
public class WarrantyRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long orderId;
    private String customerName;
    private String customerEmail;
    private String productName; // Product name for display
    
    private Integer currentWarrantyMonths;
    private Integer requestedExtensionMonths; // How many additional months requested
    private Integer totalWarrantyMonths; // Current + Extension
    
    private BigDecimal extensionFee; // Fee for extension
    private String paymentMethod; // "CREDIT_CARD", "DEBIT_CARD", "CASH_ON_DELIVERY"
    private String paymentStatus; // "PENDING", "PAID", "FAILED"
    
    private String requestStatus; // "PENDING", "APPROVED", "REJECTED"
    private String adminDecision;
    private LocalDateTime requestDate;
    private LocalDateTime decisionDate;

    // Constructors
    public WarrantyRequest() {
        this.requestDate = LocalDateTime.now();
        this.paymentStatus = "PENDING";
        this.requestStatus = "PENDING";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getCurrentWarrantyMonths() {
        return currentWarrantyMonths;
    }

    public void setCurrentWarrantyMonths(Integer currentWarrantyMonths) {
        this.currentWarrantyMonths = currentWarrantyMonths;
    }

    public Integer getRequestedExtensionMonths() {
        return requestedExtensionMonths;
    }

    public void setRequestedExtensionMonths(Integer requestedExtensionMonths) {
        this.requestedExtensionMonths = requestedExtensionMonths;
    }

    public Integer getTotalWarrantyMonths() {
        return totalWarrantyMonths;
    }

    public void setTotalWarrantyMonths(Integer totalWarrantyMonths) {
        this.totalWarrantyMonths = totalWarrantyMonths;
    }

    public BigDecimal getExtensionFee() {
        return extensionFee;
    }

    public void setExtensionFee(BigDecimal extensionFee) {
        this.extensionFee = extensionFee;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getAdminDecision() {
        return adminDecision;
    }

    public void setAdminDecision(String adminDecision) {
        this.adminDecision = adminDecision;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDateTime getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(LocalDateTime decisionDate) {
        this.decisionDate = decisionDate;
    }
}
