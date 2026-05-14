package com.example.demo.controller;

import com.example.demo.enumtype.RoomStatus;
import com.example.demo.model.Room;
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

    // ===== DANH SÁCH PHÒNG =====
    @GetMapping("/rooms")
    public String list(Model model) {

        model.addAttribute(
                "list",
                service.findAll()
        );

        return "list";
    }

    // ===== FORM THÊM PHÒNG =====
    @GetMapping("/create")
    public String create(Model model) {

        model.addAttribute(
                "room",
                new Room()
        );

        return "create";
    }

    // ===== LƯU PHÒNG =====
    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("room") Room room,
            BindingResult result,
            Model model
    ) {

        if (result.hasErrors()) {
            return "create";
        }

        // mặc định phòng mới là AVAILABLE
        if (room.getStatus() == null) {
            room.setStatus(RoomStatus.AVAILABLE);
        }

        service.save(room);

        return "redirect:/rooms";
    }

    // ===== FORM UPDATE =====
    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model
    ) {

        model.addAttribute(
                "room",
                service.findById(id)
        );

        model.addAttribute(
                "isEdit",
                true
        );

        return "create";
    }

    // ===== UPDATE =====
    @PostMapping("/update")
    public String update(
            @Valid @ModelAttribute("room") Room room,
            BindingResult result,
            Model model
    ) {

        if (result.hasErrors()) {

            model.addAttribute(
                    "isEdit",
                    true
            );

            return "create";
        }

        // giữ trạng thái cũ
        Room oldRoom =
                service.findById(room.getId());

        room.setStatus(oldRoom.getStatus());

        service.save(room);

        return "redirect:/rooms";
    }

    // ===== XÓA =====
    @GetMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id
    ) {

        service.delete(id);

        return "redirect:/rooms";
    }

    // ===== CHI TIẾT PHÒNG =====
    @GetMapping("/room/detail/{id}")
    public String detail(
            @PathVariable Long id,
            Model model
    ) {

        model.addAttribute(
                "room",
                service.findById(id)
        );

        return "detail";
    }

    // ===== SEARCH THEO GIÁ =====
    @GetMapping("/search")
    public String search(

            @RequestParam(required = false)
            Double price,

            Model model
    ) {

        model.addAttribute(
                "list",
                service.search(price)
        );

        model.addAttribute(
                "price",
                price
        );

        return "list";
    }

    // ===== LỌC THEO TRẠNG THÁI =====
    @GetMapping("/status/{status}")
    public String filterByStatus(
            @PathVariable RoomStatus status,
            Model model
    ) {

        model.addAttribute(
                "list",
                service.findByStatus(status)
        );

        model.addAttribute(
                "selectedStatus",
                status
        );

        return "list";
    }

    // ===== CHUYỂN TRẠNG THÁI =====
    @GetMapping("/change-status/{id}/{status}")
    public String changeStatus(
            @PathVariable Long id,
            @PathVariable RoomStatus status
    ) {

        service.updateStatus(id, status);

        return "redirect:/rooms";
    }

    // ===== HOÀN TẤT DỌN DẸP =====
    @GetMapping("/room/available/{id}")
    public String makeRoomAvailable(
            @PathVariable Long id
    ) {

        service.updateStatus(
                id,
                RoomStatus.AVAILABLE
        );

        return "redirect:/rooms";
    }

    // ===== SEARCH NÂNG CAO (ĐÃ CẬP NHẬT ĐA THAM SỐ) =====
    @GetMapping("/rooms/search")
    public String searchRooms(

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            RoomStatus status,

            @RequestParam(required = false)
            String roomType,

            Model model
    ) {

        // convert chuỗi rỗng -> null để Repository xử lý đúng logic IS NULL
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        if (roomType != null && roomType.trim().isEmpty()) {
            roomType = null;
        }

        model.addAttribute(
                "list",
                service.searchRooms(
                        keyword,
                        status,
                        roomType
                )
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "selectedStatus",
                status
        );

        model.addAttribute(
                "selectedRoomType",
                roomType
        );

        return "list";
    }

}