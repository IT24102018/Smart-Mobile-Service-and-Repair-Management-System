package com.mobix.service;

import com.mobix.model.Product;
import com.mobix.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getPhones() {
        return productRepository.findByCategory("PHONE");
    }

    public List<Product> getAccessories() {
        return productRepository.findByCategory("ACCESSORY");
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);
        if (product != null) {
            product.setName(productDetails.getName());
            product.setDescription(productDetails.getDescription());
            product.setPrice(productDetails.getPrice());
            product.setBrand(productDetails.getBrand());
            product.setCategory(productDetails.getCategory());
            product.setImageUrl(productDetails.getImageUrl());
            product.setStock(productDetails.getStock());
            product.setWarrantyMonths(productDetails.getWarrantyMonths());
            return productRepository.save(product);
        }
        return null;
    }

    // Initialize sample data
    @PostConstruct
    public void initSampleData() {
        if (productRepository.count() == 0) {
            // Brand New Phones
            productRepository.save(new Product(
                "iPhone 15 Pro Max",
                "Latest Apple iPhone with A17 Pro chip, 256GB storage, Titanium design",
                new BigDecimal("1199.00"),
                "Apple",
                "PHONE",
                "https://images.unsplash.com/photo-1696446701796-da61225697cc?w=400",
                50
            ));

            productRepository.save(new Product(
                "Samsung Galaxy S24 Ultra",
                "Premium Android phone with S Pen, 200MP camera, 512GB storage",
                new BigDecimal("1299.00"),
                "Samsung",
                "PHONE",
                "https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=400",
                40
            ));

            productRepository.save(new Product(
                "Google Pixel 8 Pro",
                "Google's flagship with AI features, 128GB, Amazing camera",
                new BigDecimal("999.00"),
                "Google",
                "PHONE",
                "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=400",
                30
            ));

            productRepository.save(new Product(
                "OnePlus 12",
                "Flagship killer with Snapdragon 8 Gen 3, 256GB, 100W charging",
                new BigDecimal("799.00"),
                "OnePlus",
                "PHONE",
                "https://images.unsplash.com/photo-1660463976263-85c296956a59?w=400",
                25
            ));

            productRepository.save(new Product(
                "Xiaomi 14 Pro",
                "High-performance phone with Leica cameras, 256GB storage",
                new BigDecimal("899.00"),
                "Xiaomi",
                "PHONE",
                "https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=400",
                35
            ));

            productRepository.save(new Product(
                "Nothing Phone 2",
                "Unique design with Glyph interface, 256GB, Snapdragon 8+ Gen 1",
                new BigDecimal("699.00"),
                "Nothing",
                "PHONE",
                "https://images.unsplash.com/photo-1678911820864-e2c567c655d7?w=400",
                20
            ));

            // Accessories
            productRepository.save(new Product(
                "AirPods Pro 2",
                "Wireless noise-canceling earbuds with Spatial Audio",
                new BigDecimal("249.00"),
                "Apple",
                "ACCESSORY",
                "https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=400",
                100
            ));

            productRepository.save(new Product(
                "Samsung Galaxy Buds3 Pro",
                "Premium wireless earbuds with ANC and 360 Audio",
                new BigDecimal("229.00"),
                "Samsung",
                "ACCESSORY",
                "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=400",
                80
            ));

            productRepository.save(new Product(
                "Anker 737 Power Bank",
                "24000mAh power bank with 140W fast charging",
                new BigDecimal("149.00"),
                "Anker",
                "ACCESSORY",
                "https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5?w=400",
                60
            ));

            productRepository.save(new Product(
                "MagSafe Charger",
                "Apple official wireless charger for iPhone",
                new BigDecimal("39.00"),
                "Apple",
                "ACCESSORY",
                "https://images.unsplash.com/photo-1622445275576-721325763afe?w=400",
                120
            ));

            productRepository.save(new Product(
                "Spigen Tough Armor Case",
                "Military-grade protection case for iPhone 15",
                new BigDecimal("29.99"),
                "Spigen",
                "ACCESSORY",
                "https://images.unsplash.com/photo-1603313011101-320f26a4f6f6?w=400",
                200
            ));

            productRepository.save(new Product(
                "USB-C to Lightning Cable",
                "Fast charging cable, 2m length, MFi certified",
                new BigDecimal("19.99"),
                "Apple",
                "ACCESSORY",
                "https://images.unsplash.com/photo-1625153669622-870bdb2170d6?w=400",
                150
            ));
        }
    }
}
