package com.example.demo.controller;

import com.example.demo.model.Room;
import com.example.demo.enumtype.RoomStatus;
import com.example.demo.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class RoomController {

    private final RoomService service;

    public RoomController(RoomService service) {
        this.service = service;
    }

    // ===== HOME =====
    @GetMapping("/")
    public String home() {
        return "redirect:/rooms";
    }

    // ===== LIST =====
    @GetMapping("/rooms")
    public String list(Model model) {
        model.addAttribute("list", service.findAll());
        return "list";
    }

    // ===== CREATE FORM =====
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("room", new Room());
        return "create";
    }

    // ===== SAVE =====
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("room") Room room,
                       BindingResult result,
                       Model model) {

        if (result.hasErrors()) {
            return "create";
        }
        room.setStatus(RoomStatus.AVAILABLE);

        service.save(room);

        model.addAttribute("success", "Thêm phòng thành công!");
        model.addAttribute("room", new Room());

        return "create";
    }

    // ===== EDIT =====
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {

        model.addAttribute("room", service.findById(id));
        model.addAttribute("isEdit", true);

        return "create";
    }

    // ===== UPDATE =====
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("room") Room room,
                         BindingResult result,
                         Model model) {

        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "create";
        }

        Room oldRoom = service.findById(room.getId());

        room.setStatus(oldRoom.getStatus());

        service.save(room);

        return "redirect:/rooms";
    }

    // ===== DELETE =====
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {

        service.delete(id);

        return "redirect:/rooms";
    }

    // ===== SEARCH =====
    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) Double price,
            Model model) {

        model.addAttribute("list", service.search(price));
        model.addAttribute("price", price);

        return "list";
    }
}