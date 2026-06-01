package com.example.demo.controller;

import com.example.demo.model.Customer;
import com.example.demo.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    // ===== FEATURE: XEM DANH SÁCH KHÁCH HÀNG =====
    @PreAuthorize("hasAuthority('View_Customer')")
    @GetMapping
    public String list(Model model) {
        model.addAttribute("list", service.findAll());
        return "customer/list";
    }

    // ===== FEATURE: THÊM KHÁCH HÀNG (FORM) =====
    @PreAuthorize("hasAuthority('Create_Customer')")
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("customer", new Customer());
        model.addAttribute("isEdit", false);
        return "customer/create";
    }

    // ===== FEATURE: LƯU KHÁCH HÀNG MỚI =====
    @PreAuthorize("hasAuthority('Create_Customer')")
    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("customer") Customer customer,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "customer/create";
        }
        try {
            service.save(customer);
        } catch (Exception e) {
            model.addAttribute("error", "Lưu thất bại: CCCD đã tồn tại hoặc có lỗi xảy ra!");
            return "customer/create";
        }
        return "redirect:/customers";
    }

    // ===== FEATURE: CẬP NHẬT KHÁCH HÀNG (FORM) =====
    @PreAuthorize("hasAuthority('Edit_Customer')")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("customer", service.findById(id));
        model.addAttribute("isEdit", true);
        return "customer/create";
    }

    // ===== FEATURE: LƯU CẬP NHẬT KHÁCH HÀNG =====
    @PreAuthorize("hasAuthority('Edit_Customer')")
    @PostMapping("/update")
    public String update(
            @Valid @ModelAttribute("customer") Customer customer,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "customer/create";
        }
        try {
            service.save(customer);
        } catch (Exception e) {
            model.addAttribute("isEdit", true);
            model.addAttribute("error", "Cập nhật thất bại: CCCD đã tồn tại hoặc có lỗi xảy ra!");
            return "customer/create";
        }
        return "redirect:/customers";
    }

    // ===== FEATURE: XÓA KHÁCH HÀNG =====
    @PreAuthorize("hasAuthority('Delete_Customer')")
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa khách hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa khách hàng vì khách đã có lịch sử thuê phòng!");
        }
        return "redirect:/customers";
    }

    // ===== FEATURE: TÌM KIẾM KHÁCH HÀNG =====
    @PreAuthorize("hasAuthority('View_Customer')")
    @GetMapping("/search")
    public String searchCustomer(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("list", service.search(keyword));
        model.addAttribute("keyword", keyword);
        return "customer/list";
    }

    // ===== FEATURE: QUẢN LÝ KHÁCH VIP (ĐÃ FIX PHÂN QUYỀN) =====
    @PreAuthorize("hasAuthority('Edit_Customer')")
    @GetMapping("/toggle-vip/{id}")
    public String toggleVip(@PathVariable Long id) {
        service.toggleVip(id);
        return "redirect:/customers";
    }

    // ===== FEATURE: KHÓA KHÁCH HÀNG (ĐÃ FIX PHÂN QUYỀN) =====
    @PreAuthorize("hasAuthority('Edit_Customer')")
    @GetMapping("/toggle-active/{id}")
    public String toggleActive(@PathVariable Long id) {
        service.toggleActive(id);
        return "redirect:/customers";
    }
}