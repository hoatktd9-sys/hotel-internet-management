package com.example.demo.controller;

import com.example.demo.model.Room;
import com.example.demo.enumtype.RoomStatus;
import com.example.demo.service.RoomService;
import com.example.demo.service.RoomTypeService;
import com.example.demo.model.RoomType;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

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
    private final RoomTypeService roomTypeService;

    public RoomController(RoomService service, RoomTypeService roomTypeService) {
        this.service = service;
        this.roomTypeService = roomTypeService;
    }

    // ===== HOME =====
    @GetMapping("/")
    public String home() {
        return "redirect:/rooms";
    }

    // ===== LIST =====
    @GetMapping("/rooms")
    public String list(Model model) {

        model.addAttribute(
                "list",
                service.findAll()
        );

        return "list";
    }

    // ===== CREATE FORM =====
    @GetMapping("/create")
    public String create(Model model) {

        model.addAttribute(
                "room",
                new Room()
        );
        model.addAttribute("roomTypes", roomTypeService.findAll());

        return "create";
    }

    // ===== SAVE =====
    // ===== LƯU PHÒNG (ĐH CẬP NHẬT LOGIC UPLOAD) =====
    @PostMapping("/save")
    public String save(

            @Valid @ModelAttribute("room")
            Room room,

            BindingResult result,

            @RequestParam("imageFile")
            MultipartFile imageFile,

            @RequestParam("imageFile") MultipartFile imageFile,
            Model model
    ) throws IOException {

        if (service.existsByRoomName(room.getRoomName())) {
            result.rejectValue("roomName", "error.room", "Tên phòng đã tồn tại");
        }

        if (result.hasErrors()) {
            System.out.println("Validation errors: " + result.getAllErrors());
            model.addAttribute("roomTypes", roomTypeService.findAll());
            return "create";
        }

        try {
            if (!imageFile.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                Path uploadPath = Paths.get("src/main/resources/static/images");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Files.copy(imageFile.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                room.setImage("/images/" + fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        // mặc định phòng mới là AVAILABLE
        room.setStatus(RoomStatus.AVAILABLE);

        service.save(room);

        model.addAttribute(
                "success",
                "Thêm phòng thành công!"
        );

        model.addAttribute(
                "room",
                new Room()
        );
        model.addAttribute("roomTypes", roomTypeService.findAll());

        return "create";
    }

    // ===== EDIT =====
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
        model.addAttribute("roomTypes", roomTypeService.findAll());

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

            @RequestParam("imageFile") MultipartFile imageFile,
            Model model
    ) throws IOException {

        Room oldRoom = service.findById(room.getId());
        if (!oldRoom.getRoomName().equals(room.getRoomName()) && service.existsByRoomName(room.getRoomName())) {
            result.rejectValue("roomName", "error.room", "Tên phòng đã tồn tại");
        }

        if (result.hasErrors()) {

            model.addAttribute(
                    "isEdit",
                    true
            );
            model.addAttribute("roomTypes", roomTypeService.findAll());

            return "create";
        }

        room.setStatus(oldRoom.getStatus());

        try {
            if (!imageFile.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                Path uploadPath = Paths.get("src/main/resources/static/images");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Files.copy(imageFile.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                room.setImage("/images/" + fileName);
            } else {
                room.setImage(oldRoom.getImage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    // ===== DELETE =====
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {

        service.delete(id);

        return "redirect:/rooms";
    }

    // ===== SEARCH =====
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

    // ===== HOÀN TẤT DỌN DẸP =====
    @GetMapping("/room/complete-cleaning/{id}")
    public String completeCleaning(
            @PathVariable Long id
    ) {

        // tìm phòng
        Room room = service.findById(id);

        // đổi trạng thái
        room.setStatus(RoomStatus.AVAILABLE);

        // lưu
        service.save(room);

        return "redirect:/rooms";
    }

    // ===== HOÀN TẤT DỌN DẸP (API MỚI) =====
    @GetMapping("/room/available/{id}")
    public String makeRoomAvailable(
            @PathVariable Long id
    ) {

        // tìm phòng
        Room room = service.findById(id);

        // chuyển sang AVAILABLE
        room.setStatus(RoomStatus.AVAILABLE);

        // lưu
        service.save(room);

        return "redirect:/rooms";
    }

    // ===== XÓA PHÒNG (API MỚI) =====
    @GetMapping("/room/delete/{id}")
    public String deleteRoom(
            @PathVariable Long id
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

        service.delete(id);
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        return "redirect:/rooms";
    }

}