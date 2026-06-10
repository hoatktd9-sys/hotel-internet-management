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

    // ===== FORM THÊM PHÒNG =====
    @PreAuthorize("hasAuthority('Create_Room')")
    @GetMapping("/rooms/create")
    public String create(Model model) {
        Room room = new Room();
        model.addAttribute("room", room);
        model.addAttribute("roomTypes", roomTypeService.findAll());
        model.addAttribute("isEdit", false);
        return "create";
    }

    // ===== LƯU PHÒNG (TẠO MỚI) =====
    @PreAuthorize("hasAuthority('Create_Room')")
    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("room") Room room,
            BindingResult result,
            @RequestParam(value = "roomType", required = false) String roomTypeParam,
            @RequestParam(value = "roomType.id", required = false) Long roomTypeIdParam,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model) throws IOException {

        Long typeId = null;
        if (room.getRoomType() != null && room.getRoomType().getId() != null) {
            typeId = room.getRoomType().getId();
        } else if (roomTypeIdParam != null) {
            typeId = roomTypeIdParam;
        } else if (roomTypeParam != null && !roomTypeParam.trim().isEmpty()) {
            try {
                typeId = Long.parseLong(roomTypeParam.trim());
            } catch (NumberFormatException ignored) {
            }
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
            String uploadDir = new File("uploads/images").getAbsolutePath();
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
            Model model) throws IOException {

        Long typeId = null;
        if (room.getRoomType() != null && room.getRoomType().getId() != null) {
            typeId = room.getRoomType().getId();
        } else if (roomTypeIdParam != null) {
            typeId = roomTypeIdParam;
        } else if (roomTypeParam != null && !roomTypeParam.trim().isEmpty()) {
            try {
                typeId = Long.parseLong(roomTypeParam.trim());
            } catch (NumberFormatException ignored) {
            }
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

        if (!imageFile.isEmpty()) {
            String uploadDir = new File("uploads/images").getAbsolutePath();
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
        roomService.refreshRoomStatus(id);
        return "redirect:/rooms";
    }

    // ===== SEARCH NÂNG CAO =====
    @GetMapping("/rooms/search")
    public String searchRooms(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(required = false) String roomType,
            Model model) {
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

        // LOGIC CẢNH BÁO KHO TRÊN TOÀN BỘ CÁC TRANG CỦA ROOM CONTROLLER
        long lowStockCount = productService.getAllProducts().stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() <= 10)
                .count();
        model.addAttribute("lowStockCount", lowStockCount);

        // LOGIC CẢNH BÁO PHÒNG SẮP HẾT GIỜ TRÊN TOÀN BỘ CÁC TRANG CỦA ROOM CONTROLLER
        java.util.List<Room> almostOvertimeRooms = allRooms.stream()
                .filter(r -> r.getStatus() == RoomStatus.OCCUPIED)
                .filter(r -> r.getActiveCheckIn() != null && r.getActiveCheckIn().isAlmostOvertime())
                .collect(Collectors.toList());

        model.addAttribute("almostOvertimeRooms", almostOvertimeRooms);
        model.addAttribute("almostOvertimeCount", almostOvertimeRooms.size());
    }
}