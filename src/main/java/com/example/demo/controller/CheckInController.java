package com.example.demo.controller;

import com.example.demo.model.CheckIn;
import com.example.demo.service.CheckInService;
import com.example.demo.service.CustomerService;
import com.example.demo.service.RoomService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private RoomService roomService;

    // HIỂN THỊ FORM CHECK-IN
    @GetMapping("/checkin")
    public String checkInPage(Model model){

        model.addAttribute("checkin", new CheckIn());

        model.addAttribute("customers",
                customerService.getAll());

        model.addAttribute("rooms",
                roomService.getAll());

        return "checkin/create";
    }

    // LƯU CHECK-IN
    @PostMapping("/checkin/save")
    public String saveCheckIn(@ModelAttribute CheckIn checkIn){

        checkInService.save(checkIn);

        return "redirect:/rooms";
    }

}