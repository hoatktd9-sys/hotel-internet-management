package com.example.demo.controller;

import com.example.demo.enumtype.RoomStatus;
import com.example.demo.model.Room;
import com.example.demo.model.RoomType;
import com.example.demo.service.CloudStorageService;
import com.example.demo.service.ProductService;
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

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;
    private final RoomTypeService roomTypeService;
    private final ProductService productService;
    private final CloudStorageService cloudStorageService;

    public RoomController(
            RoomService roomService,
            RoomTypeService roomTypeService,
            ProductService productService,
            CloudStorageService cloudStorageService) {
        this.roomService = roomService;
        this.roomTypeService = roomTypeService;
        this.productService = productService;
        this.cloudStorageService = cloudStorageService;
    }

    @GetMapping(value = {"", "/"})
    public String list(Model model) {
        List<Room> roomList = roomService.findAll();
        model.addAttribute("list", roomList);

        List<Room> availableRooms = roomList.stream()
                .filter(r -> r.getStatus() == RoomStatus.AVAILABLE)
                .collect(Collectors.toList());
        model.addAttribute("availableRooms", availableRooms);

        long lowStockCount = productService.getAllProducts().stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() <= 10)
                .count();
        model.addAttribute("lowStockCount", lowStockCount);

        List<Room> almostOvertimeRooms = roomList.stream()
                .filter(r -> r.getStatus() == RoomStatus.OCCUPIED)
                .filter(r -> r.getActiveCheckIn() != null && r.getActiveCheckIn().isAlmostOvertime())
                .collect(Collectors.toList());

        model.addAttribute("almostOvertimeRooms", almostOvertimeRooms);
        model.addAttribute("almostOvertimeCount", almostOvertimeRooms.size());

        return "list";
    }

    @GetMapping("/search")
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

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createForm(Model model) {
        model.addAttribute("room", new Room());
        model.addAttribute("roomTypes", roomTypeService.findAll());
        model.addAttribute("isEdit", false);
        return "create";
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveRoom(@Valid @ModelAttribute("room") Room room,
                           BindingResult result,
                           @RequestParam("imageFile") MultipartFile imageFile,
                           @RequestParam(value = "roomType.id", required = false) Long roomTypeId,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("roomTypes", roomTypeService.findAll());
            model.addAttribute("isEdit", false);
            return "create";
        }

        try {
            if (roomTypeId != null) {
                RoomType validRoomType = roomTypeService.findById(roomTypeId);
                room.setRoomType(validRoomType);
            } else {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn loại cấu hình phòng!");
                return "redirect:/rooms/create";
            }

            if (imageFile != null && !imageFile.isEmpty()) {
                String cloudImageUrl = cloudStorageService.uploadImage(imageFile);
                room.setImage(cloudImageUrl);
            }

            if (room.getStatus() == null) {
                room.setStatus(RoomStatus.AVAILABLE);
            }

            roomService.save(room);
            redirectAttributes.addFlashAttribute("success", "Thêm phòng máy mới thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi khi thêm phòng: " + e.getMessage());
            model.addAttribute("roomTypes", roomTypeService.findAll());
            model.addAttribute("isEdit", false);
            return "create";
        }

        return "redirect:/rooms";
    }

    // ĐỒNG BỘ ĐƯỜNG DẪN SỬA: /rooms/edit/{id}
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Room room = roomService.findById(id);
        if (room == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin phòng máy cần sửa!");
            return "redirect:/rooms";
        }
        model.addAttribute("room", room);
        model.addAttribute("roomTypes", roomTypeService.findAll());
        model.addAttribute("isEdit", true);
        return "create";
    }

    // FIX LỖI 1 & LỖI 3: Xử lý cập nhật thông tin phòng máy hoàn chỉnh
    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateRoom(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("room") Room room,
                             BindingResult result,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        // Nếu có lỗi validate các trường cơ bản của Room, trả về view sửa luôn
        if (result.hasErrors()) {
            model.addAttribute("roomTypes", roomTypeService.findAll());
            model.addAttribute("isEdit", true);
            return "create";
        }

        try {
            Room existingRoom = roomService.findById(id);
            if (existingRoom == null) {
                redirectAttributes.addFlashAttribute("error", "Phòng không tồn tại!");
                return "redirect:/rooms";
            }

            // GIẢI QUYẾT LỖI 1: Lấy RoomType xịn từ DB để tránh lỗi Validation trường Name bị trống
            if (room.getRoomType() != null && room.getRoomType().getId() != null) {
                RoomType validRoomType = roomTypeService.findById(room.getRoomType().getId());
                existingRoom.setRoomType(validRoomType);
            }

            // GIẢI QUYẾT LỖI 3: Đồng bộ logic lưu ảnh đại diện mới hoặc giữ nguyên ảnh Cloudinary cũ
            if (imageFile != null && !imageFile.isEmpty()) {
                String cloudImageUrl = cloudStorageService.uploadImage(imageFile);
                existingRoom.setImage(cloudImageUrl);
            }
            // Nếu không chọn file mới, existingRoom vẫn giữ nguyên link ảnh cũ sẵn có trong DB

            // Cập nhật toàn bộ các trường thông tin thay đổi từ form vào dữ liệu gốc DB
            existingRoom.setRoomName(room.getRoomName());
            existingRoom.setPrice(room.getPrice());
            existingRoom.setComputerCount(room.getComputerCount());
            existingRoom.setDescription(room.getDescription());
            existingRoom.setCpu(room.getCpu());
            existingRoom.setRam(room.getRam());
            existingRoom.setVga(room.getVga());
            existingRoom.setSsd(room.getSsd());
            existingRoom.setMonitor(room.getMonitor());

            roomService.save(existingRoom);
            redirectAttributes.addFlashAttribute("success", "Cập nhật cấu hình phòng máy thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Cập nhật thất bại: " + e.getMessage());
        }

        return "redirect:/rooms";
    }

    // FIX LỖI 4: Định vị lại đường dẫn xóa chuẩn hóa ngắn gọn là /rooms/delete/{id}
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteRoom(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            roomService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa phòng máy thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Không thể xóa phòng: " + e.getMessage());
        }
        return "redirect:/rooms";
    }

    @GetMapping("/change-status/{id}/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeStatus(@PathVariable("id") Long id,
                               @PathVariable("status") RoomStatus status,
                               RedirectAttributes redirectAttributes) {
        try {
            roomService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái phòng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/rooms";
    }

    @GetMapping("/room/available/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String setRoomAvailable(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            roomService.updateStatus(id, RoomStatus.AVAILABLE);
            redirectAttributes.addFlashAttribute("success", "Phòng đã sẵn sàng hoạt động!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể cập nhật trạng thái: " + e.getMessage());
        }
        return "redirect:/rooms";
    }

    // CHỨC NĂNG ĐẶT TRƯỚC PHÒNG
    @GetMapping("/reserve")
    @PreAuthorize("hasRole('ADMIN')")
    public String reserveRoom(@RequestParam("roomId") Long roomId, RedirectAttributes redirectAttributes) {
        try {
            roomService.updateStatus(roomId, RoomStatus.RESERVED);
            redirectAttributes.addFlashAttribute("success", "Đã đặt trước phòng máy thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể đặt trước phòng: " + e.getMessage());
        }
        return "redirect:/rooms";
    }

    @ModelAttribute
    public void addCommonAttributes(Model model) {
        List<Room> allRooms = roomService.getAll();
        model.addAttribute("availableCount", allRooms.stream().filter(r -> r.getStatus() == RoomStatus.AVAILABLE).count());
        model.addAttribute("occupiedCount", allRooms.stream().filter(r -> r.getStatus() == RoomStatus.OCCUPIED).count());
        model.addAttribute("cleaningCount", allRooms.stream().filter(r -> r.getStatus() == RoomStatus.CLEANING).count());
        model.addAttribute("maintenanceCount", allRooms.stream().filter(r -> r.getStatus() == RoomStatus.MAINTENANCE).count());
        model.addAttribute("reservedCount", allRooms.stream().filter(r -> r.getStatus() == RoomStatus.RESERVED).count());
        model.addAttribute("allCount", allRooms.size());
    }
}