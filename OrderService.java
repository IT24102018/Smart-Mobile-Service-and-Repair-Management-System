package com.mobix.service;

import com.mobix.model.Order;
import com.mobix.model.Product;
import com.mobix.repository.OrderRepository;
import com.mobix.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    public Order createOrder(Long productId, String customerName, String customerEmail,
            String customerPhone, String shippingAddress, String paymentMethod, Long userId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        if (product.getStock() <= 0) {
            throw new RuntimeException("Product out of stock");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setProductName(product.getName());
        order.setPrice(product.getPrice());
        order.setQuantity(1);
        order.setTotalAmount(product.getPrice());
        order.setCustomerName(customerName);
        order.setCustomerEmail(customerEmail);
        order.setCustomerPhone(customerPhone);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod);
        order.setWarrantyMonths(product.getWarrantyMonths() != null ? product.getWarrantyMonths() : 12);

        // Reduce stock
        product.setStock(product.getStock() - 1);
        productRepository.save(product);

        return orderRepository.save(order);
    }

    public Order processPayment(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }

        // Simulate payment processing
        order.setPaymentStatus("PAID");
        order.setPaymentDate(LocalDateTime.now());
        order.setOrderStatus("CONFIRMED");
        if (order.getWarrantyMonths() != null && order.getWarrantyMonths() > 0) {
            order.setWarrantyExpiryDate(order.getPaymentDate().toLocalDate().plusMonths(order.getWarrantyMonths()));
        }

        return orderRepository.save(order);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }
}
