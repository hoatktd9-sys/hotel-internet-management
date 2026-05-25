package com.example.demo.controller;

import com.example.demo.enumtype.RoomStatus;
import com.example.demo.model.Room;
import com.example.demo.model.RoomType;
import com.example.demo.service.RoomService;
import com.example.demo.service.RoomTypeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
public class RoomController {

    private final RoomService roomService;
    private final RoomTypeService roomTypeService;

    public RoomController(
            RoomService roomService,
            RoomTypeService roomTypeService
    ) {
        this.roomService = roomService;
        this.roomTypeService = roomTypeService;
    }

    // ===== HOME =====
    @PreAuthorize("hasAuthority('View_Room')")
    @GetMapping("/")
    public String home() {
        return "redirect:/rooms";
    }

    // ===== DANH SÁCH PHÒNG =====
    @GetMapping("/rooms")
    public String list(Model model) {
        model.addAttribute("list", roomService.findAll());
        return "list";
    }

    // ===== FORM THÊM PHÒNG =====
    @PreAuthorize("hasAuthority('Create_Room')")
    @GetMapping("/create")
    public String create(Model model) {
        Room room = new Room();
        model.addAttribute("room", room);
        model.addAttribute("roomTypes", roomTypeService.findAll());
        model.addAttribute("isEdit", false);
        return "create";
    }

    // ===== LƯU PHÒNG =====
    @PreAuthorize("hasAuthority('Create_Room')")
    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("room") Room room,
            BindingResult result,
            @RequestParam(value = "roomType", required = false) String roomTypeParam,
            @RequestParam(value = "roomType.id", required = false) Long roomTypeIdParam,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model
    ) throws IOException {

        Long typeId = null;
        if (room.getRoomType() != null && room.getRoomType().getId() != null) {
            typeId = room.getRoomType().getId();
        } else if (roomTypeIdParam != null) {
            typeId = roomTypeIdParam;
        } else if (roomTypeParam != null && !roomTypeParam.trim().isEmpty()) {
            try {
                typeId = Long.parseLong(roomTypeParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        if (typeId == null) {
            result.rejectValue("roomType", "error.roomType", "Vui lòng chọn loại phòng hợp lệ.");
        }

        if (result.hasErrors()) {
            System.out.println("===== VALIDATION ERROR (SAVE) =====");
            result.getAllErrors().forEach(error -> System.out.println(error.getDefaultMessage()));

            model.addAttribute("roomTypes", roomTypeService.findAll());
            model.addAttribute("isEdit", false);
            return "create";
        }

        RoomType managedRoomType = roomTypeService.findById(typeId);
        if (managedRoomType == null) {
            result.rejectValue("roomType", "error.roomType", "Loại phòng không tồn tại trên hệ thống.");
            model.addAttribute("roomTypes", roomTypeService.findAll());
            model.addAttribute("isEdit", false);
            return "create";
        }
        room.setRoomType(managedRoomType);

        if (!imageFile.isEmpty()) {
            String uploadDir = new File("uploads").getAbsolutePath();
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            File saveFile = new File(dir, fileName);
            imageFile.transferTo(saveFile);
            room.setImage(fileName);
        }

        room.setStatus(RoomStatus.AVAILABLE);
        roomService.save(room);

        return "redirect:/rooms";
    }

    // ===== FORM UPDATE =====
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("room", roomService.findById(id));
        model.addAttribute("roomTypes", roomTypeService.findAll());
        model.addAttribute("isEdit", true);
        return "create";
    }

    // ===== CẬP NHẬT PHÒNG =====
    @PreAuthorize("hasAuthority('Edit_Room')")
    @PostMapping("/update/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("room") Room room,
            BindingResult result,
            @RequestParam(value = "roomType", required = false) String roomTypeParam,
            @RequestParam(value = "roomType.id", required = false) Long roomTypeIdParam,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model
    ) throws IOException {

        Long typeId = null;
        if (room.getRoomType() != null && room.getRoomType().getId() != null) {
            typeId = room.getRoomType().getId();
        } else if (roomTypeIdParam != null) {
            typeId = roomTypeIdParam;
        } else if (roomTypeParam != null && !roomTypeParam.trim().isEmpty()) {
            try {
                typeId = Long.parseLong(roomTypeParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        if (typeId == null) {
            result.rejectValue("roomType", "error.roomType", "Vui lòng chọn loại phòng.");
        }

        if (result.hasErrors()) {
            System.out.println("===== VALIDATION ERROR (UPDATE) =====");
            result.getAllErrors().forEach(error -> System.out.println(error.getDefaultMessage()));

            model.addAttribute("roomTypes", roomTypeService.findAll());
            model.addAttribute("isEdit", true);
            room.setId(id);
            return "create";
        }

        RoomType managedRoomType = roomTypeService.findById(typeId);
        if (managedRoomType == null) {
            result.rejectValue("roomType", "error.roomType", "Loại phòng không tồn tại trên hệ thống.");
            model.addAttribute("roomTypes", roomTypeService.findAll());
            model.addAttribute("isEdit", true);
            return "create";
        }
        room.setRoomType(managedRoomType);

        room.setId(id);
        Room oldRoom = roomService.findById(id);
        room.setStatus(oldRoom.getStatus());

        // CHUẨN HÓA LOGIC UPLOAD ẢNH: Xóa bỏ đoạn Files.copy bị lỗi cú pháp, dùng cơ chế transferTo đồng bộ ổn định
        if (!imageFile.isEmpty()) {
            String uploadDir = new File("uploads").getAbsolutePath();
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            File saveFile = new File(dir, fileName);
            imageFile.transferTo(saveFile);
            room.setImage(fileName);
        } else {
            room.setImage(oldRoom.getImage());
        }

        roomService.save(room);
        return "redirect:/rooms";
    }

    // ===== XÓA =====
    @PreAuthorize("hasAuthority('Delete_Room')")
    @GetMapping("/room/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa phòng thành công!");
        } catch (Exception e) {
            System.err.println("===== LỖI LOGIC HOẶC RÀNG BUỘC KHI XÓA PHÒNG =====");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/rooms";
    }

    // ===== CHI TIẾT PHÒNG =====
    @GetMapping("/room/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("room", roomService.findById(id));
        return "detail";
    }

    // ===== SEARCH THEO GIÁ =====
    @GetMapping("/search")
    public String search(@RequestParam(required = false) Double price, Model model) {
        model.addAttribute("list", roomService.search(price));
        model.addAttribute("price", price);
        return "list";
    }

    // ===== LỌC THEO TRẠNG THÁI =====
    @GetMapping("/status/{status}")
    public String filterByStatus(@PathVariable RoomStatus status, Model model) {
        model.addAttribute("list", roomService.findByStatus(status));
        model.addAttribute("selectedStatus", status);
        return "list";
    }

    // ===== CHUYỂN TRẠNG THÁI =====
    @GetMapping("/change-status/{id}/{status}")
    public String changeStatus(@PathVariable Long id, @PathVariable RoomStatus status) {
        roomService.updateStatus(id, status);
        return "redirect:/rooms";
    }

    // ===== HOÀN TẤT DỌN DẸP =====
    @GetMapping("/room/available/{id}")
    public String makeRoomAvailable(@PathVariable Long id) {
        roomService.updateStatus(id, RoomStatus.AVAILABLE);
        return "redirect:/rooms";
    }

    // ===== SEARCH NÂNG CAO =====
    @GetMapping("/rooms/search")
    public String searchRooms(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(required = false) String roomType,
            Model model
    ) {
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }
        if (roomType != null && roomType.trim().isEmpty()) {
            roomType = null;
        }

        model.addAttribute("list", roomService.searchRooms(keyword, status, roomType));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedRoomType", roomType);

        return "list";
    }
}