package com.example.demo.controller;

import com.example.demo.enumtype.RoomStatus;
import com.example.demo.model.Room;
import java.util.stream.Collectors;
import com.example.demo.model.RoomType;
import com.example.demo.service.RoomService;
import com.example.demo.service.RoomTypeService;
import com.example.demo.service.ProductService;
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
    private final ProductService productService;

    public RoomController(
            RoomService roomService,
            RoomTypeService roomTypeService,
            ProductService productService) {
        this.roomService = roomService;
        this.roomTypeService = roomTypeService;
        this.productService = productService;
    }

    @PreAuthorize("hasAuthority('View_Room')")
    @GetMapping("/")
    public String home() {
        return "redirect:/rooms";
    }

    @GetMapping("/rooms")
    public String list(Model model) {
        java.util.List<Room> roomList = roomService.findAll();
        model.addAttribute("list", roomList);

        // LOGIC CẢNH BÁO KHO
        long lowStockCount = productService.getAllProducts().stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() <= 10)
                .count();
        model.addAttribute("lowStockCount", lowStockCount);

        // LOGIC CẢNH BÁO PHÒNG SẮP HẾT GIỜ
        java.util.List<Room> almostOvertimeRooms = roomList.stream()
                .filter(r -> r.getStatus() == RoomStatus.OCCUPIED)
                .filter(r -> r.getActiveCheckIn() != null && r.getActiveCheckIn().isAlmostOvertime())
                .collect(Collectors.toList());

        model.addAttribute("almostOvertimeRooms", almostOvertimeRooms);
        model.addAttribute("almostOvertimeCount", almostOvertimeRooms.size());

        return "list";
    }

    // ... (Các phương thức khác giữ nguyên)

    @GetMapping("/rooms/search")
    public String searchRooms(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(required = false) String roomType,
            Model model) {

        model.addAttribute("list", roomService.searchRooms(keyword, status, roomType));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedRoomType", roomType);

        long lowStockCount = productService.getAllProducts().stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() <= 10)
                .count();
        model.addAttribute("lowStockCount", lowStockCount);

        return "list";
    }

    @ModelAttribute
    public void addCommonAttributes(Model model) {
        java.util.List<Room> allRooms = roomService.getAll();
        model.addAttribute("availableRooms", allRooms.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == RoomStatus.AVAILABLE)
                .collect(Collectors.toList()));

        model.addAttribute("availableCount", allRooms.stream().filter(r -> r.getStatus() == RoomStatus.AVAILABLE).count());
        model.addAttribute("occupiedCount", allRooms.stream().filter(r -> r.getStatus() == RoomStatus.OCCUPIED).count());
        model.addAttribute("cleaningCount", allRooms.stream().filter(r -> r.getStatus() == RoomStatus.CLEANING).count());
        model.addAttribute("maintenanceCount", allRooms.stream().filter(r -> r.getStatus() == RoomStatus.MAINTENANCE).count());
        model.addAttribute("reservedCount", allRooms.stream().filter(r -> r.getStatus() == RoomStatus.RESERVED).count());
        model.addAttribute("allCount", allRooms.size());
    }

    // Lưu ý: Hãy đảm bảo bạn giữ lại các phương thức create, save, edit, update, delete, v.v.
    // từ code gốc của bạn vào trong file này nhé.
}