package com.example.demo.controller;

import com.example.demo.model.RoomType;
import com.example.demo.service.RoomTypeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/room-types")
public class RoomTypeController {

    private final RoomTypeService service;

    public RoomTypeController(RoomTypeService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("list", service.findAll());
        return "room-type/list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("roomType", new RoomType());
        return "room-type/create";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("roomType") RoomType roomType, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "room-type/create";
        }
        service.save(roomType);
        return "redirect:/room-types";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("roomType", service.findById(id));
        model.addAttribute("isEdit", true);
        return "room-type/create";
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("roomType") RoomType roomType, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "room-type/create";
        }
        service.save(roomType);
        return "redirect:/room-types";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/room-types";
    }
}
