package com.mobix.controller;

import com.mobix.model.Product;
import com.mobix.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/shop")
public class ShopController {
    @Autowired
    private ProductService productService;

    // Show main shop page with all products
    @GetMapping
    public String showShop(Model model) {
        model.addAttribute("phones", productService.getPhones());
        model.addAttribute("accessories", productService.getAccessories());
        return "shop";
    }

    // Show phones category
    @GetMapping("/phones")
    public String showPhones(Model model) {
        model.addAttribute("phones", productService.getPhones());
        model.addAttribute("category", "Brand New Phones");
        return "category";
    }

    // Show accessories category
    @GetMapping("/accessories")
    public String showAccessories(Model model) {
        model.addAttribute("accessories", productService.getAccessories());
        model.addAttribute("category", "Accessories");
        return "category";
    }

    // Show product details
    @GetMapping("/product/{id}")
    public String showProductDetails(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/shop";
        }
        model.addAttribute("product", product);
        return "product-details";
    }
}
