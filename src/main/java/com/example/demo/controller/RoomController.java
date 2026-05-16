package com.example.demo.controller;

import com.example.demo.enumtype.RoomStatus;
import com.example.demo.model.Room;
import com.example.demo.service.RoomService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

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

    // ===== LƯU PHÒNG (ĐH CẬP NHẬT LOGIC UPLOAD) =====
    @PostMapping("/save")
    public String save(

            @Valid @ModelAttribute("room")
            Room room,

            BindingResult result,

            @RequestParam("imageFile")
            MultipartFile imageFile,

            Model model
    ) throws IOException {

        if (result.hasErrors()) {
            return "create";
        }

        // ===== UPLOAD ẢNH =====

        if (!imageFile.isEmpty()) {

            // thư mục uploads
            String uploadDir =
                    new File("uploads").getAbsolutePath();

            File dir = new File(uploadDir);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            // đổi tên file tránh trùng
            String fileName =
                    UUID.randomUUID()
                            + "_"
                            + imageFile.getOriginalFilename();

            // lưu file
            File saveFile =
                    new File(uploadDir, fileName);

            imageFile.transferTo(saveFile);

            // lưu tên file vào DB
            room.setImage(fileName);
        }

        // mặc định AVAILABLE
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

    // ===== UPDATE (ĐH CẬP NHẬT LOGIC UPLOAD) =====
    @PostMapping("/update")
    public String update(

            @Valid @ModelAttribute("room")
            Room room,

            BindingResult result,

            @RequestParam("imageFile")
            MultipartFile imageFile,

            Model model
    ) throws IOException {

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

        // ===== UPDATE ẢNH =====

        if (!imageFile.isEmpty()) {

            String uploadDir =
                    new File("uploads").getAbsolutePath();

            File dir = new File(uploadDir);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName =
                    UUID.randomUUID()
                            + "_"
                            + imageFile.getOriginalFilename();

            File saveFile =
                    new File(uploadDir, fileName);

            imageFile.transferTo(saveFile);

            room.setImage(fileName);

        } else {

            // giữ ảnh cũ
            room.setImage(oldRoom.getImage());
        }

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

    // ===== SEARCH NÂNG CAO =====
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