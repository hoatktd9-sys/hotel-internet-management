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
    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("room") Room room,
            BindingResult result,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model
    ) {

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

    // ===== UPDATE =====
    @PostMapping("/update")
    public String update(
            @Valid @ModelAttribute("room") Room room,
            BindingResult result,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model
    ) {

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
    ) {

        service.delete(id);

        return "redirect:/rooms";
    }

}