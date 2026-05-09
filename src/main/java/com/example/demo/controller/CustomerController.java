package com.example.demo.controller;

import com.example.demo.model.Customer;
import com.example.demo.service.CustomerService;
import jakarta.validation.Valid;
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

    // ===== DANH SÁCH =====

    @GetMapping
    public String list(Model model) {

        model.addAttribute("list", service.findAll());

        return "customer/list";
    }

    // ===== FORM THÊM =====

    @GetMapping("/create")
    public String create(Model model) {

        model.addAttribute("customer", new Customer());

        return "customer/create";
    }

    // ===== LƯU =====

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
}