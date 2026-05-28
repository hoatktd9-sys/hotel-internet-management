package com.example.demo.controller;

import com.example.demo.model.ServiceCategory;
import com.example.demo.service.ServiceCategoryService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/service-categories")
public class ServiceCategoryController {

    private final ServiceCategoryService categoryService;

    public ServiceCategoryController(ServiceCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // Hiển thị danh sách và form thêm mới (tích hợp trên 1 trang cho tiện quản lý)
    @GetMapping
    @PreAuthorize("hasAuthority('Admin_Service')")
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        if (!model.containsAttribute("category")) {
            model.addAttribute("category", new ServiceCategory());
        }
        model.addAttribute("isEdit", false);
        return "service-category/list";
    }

    // Lưu thêm mới hoặc cập nhật
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('Admin_Service')")
    public String save(@Valid @ModelAttribute("category") ServiceCategory category,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {

        if (category.getId() == null && categoryService.existsByName(category.getName())) {
            result.rejectValue("name", "error.name", "Tên nhóm dịch vụ này đã tồn tại!");
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.category", result);
            redirectAttributes.addFlashAttribute("category", category);
            return "redirect:/service-categories";
        }

        categoryService.save(category);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin nhóm dịch vụ thành công!");
        return "redirect:/service-categories";
    }

    // Mở form chỉnh sửa
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('Admin_Service')")
    public String edit(@PathVariable Long id, Model model) {
        ServiceCategory category = categoryService.findById(id);
        if (category == null) {
            return "redirect:/service-categories";
        }
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("category", category);
        model.addAttribute("isEdit", true);
        return "service-category/list";
    }

    // Xóa danh mục
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('Admin_Service')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa nhóm dịch vụ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa nhóm dịch vụ này do đang có sản phẩm thuộc nhóm!");
        }
        return "redirect:/service-categories";
    }
}