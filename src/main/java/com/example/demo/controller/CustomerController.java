package com.example.demo.controller;

import com.example.demo.model.Customer;
import com.example.demo.service.CustomerService;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @PreAuthorize("hasAuthority('View_Customer')")
    @GetMapping
    public String list(Model model) {
        model.addAttribute("list", service.findAll());
        return "customer/list";
    }

    @PreAuthorize("hasAuthority('Create_Customer')")
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer/create";
    }

    @PreAuthorize("hasAuthority('Create_Customer')")
    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("customer") Customer customer,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return "customer/create";
        }
        service.save(customer);
        return "redirect:/customers";
    }

    @PreAuthorize("hasAuthority('Edit_Customer')")
    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model
    ) {
        model.addAttribute(
                "customer",
                service.findById(id)
        );
        model.addAttribute(
                "isEdit",
                true
        );
        return "customer/create";
    }

    @PreAuthorize("hasAuthority('Edit_Customer')")
    @PostMapping("/update")
    public String update(
            @Valid @ModelAttribute("customer") Customer customer,
            BindingResult result,
            Model model
    ) {
        if(result.hasErrors()){
            model.addAttribute(
                    "isEdit",
                    true
            );
            return "customer/create";
        }
        service.save(customer);
        return "redirect:/customers";
    }

    @PreAuthorize("hasAuthority('Delete_Customer')")
    @GetMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            Model model
    ) {
        try {
            service.delete(id);
        } catch (Exception e) {
            // Nếu có lỗi (thường là lỗi ràng buộc khóa ngoại trong DB)
            model.addAttribute(
                    "error",
                    "Không thể xóa khách hàng vì khách đã có lịch sử check-in!"
            );
            // Load lại danh sách để hiển thị cùng thông báo lỗi
            model.addAttribute(
                    "list",
                    service.findAll()
            );
            return "customer/list";
        }
        return "redirect:/customers";
    }

    // ===== TÌM KIẾM KHÁCH HÀNG (MỚI THÊM & TỐI ƯU PATH) =====
    @GetMapping("/search")
    public String searchCustomer(

            @RequestParam(required = false)
            String keyword,

            Model model
    ) {

        model.addAttribute(
                "list",
                service.search(keyword)
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        return "customer/list";
    }
}