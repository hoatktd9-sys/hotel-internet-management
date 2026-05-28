package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.repository.ServiceCategoryRepository;
import com.example.demo.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
@PreAuthorize("hasAuthority('Admin_Service')")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    // 1. Hiển thị danh sách sản phẩm & Form thêm mới
    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", serviceCategoryRepository.findAll());
        if (!model.containsAttribute("product")) {
            model.addAttribute("product", new Product());
        }
        return "../templates/admin/product/manage-products";
    }

    // 2. Xử lý Thêm mới hoặc Cập nhật sản phẩm
    @PostMapping("/save")
    public String saveProduct(@Valid @ModelAttribute("product") Product product,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.product", result);
            redirectAttributes.addFlashAttribute("product", product);
            return "redirect:/admin/products";
        }

        try {
            if (product.getImage() == null || product.getImage().trim().isEmpty()) {
                product.setImage("default-product.png");
            }
            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin sản phẩm vào thực đơn thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("product", product);
        }
        return "redirect:/admin/products";
    }

    // 3. Chuẩn bị dữ liệu để sửa sản phẩm
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        try {
            Product product = productService.getProductById(id);
            model.addAttribute("product", product);
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("categories", serviceCategoryRepository.findAll());
            return "../templates/admin/product/manage-products";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/products";
        }
    }

    // 4. Xử lý Xóa sản phẩm
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa món ăn khỏi thực đơn thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/products";
    }
}