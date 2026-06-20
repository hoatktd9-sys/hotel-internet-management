package com.example.demo.controller;

import com.example.demo.enumtype.RoomStatus;
import com.example.demo.model.CheckIn; // <-- ĐÃ FIX: Thêm dòng import thực thể CheckIn
import com.example.demo.model.Customer; // Thêm dòng import thực thể Customer để dùng cho xử lý lưu
import com.example.demo.model.Room;
import com.example.demo.model.RoomType;
import com.example.demo.service.CloudStorageService;
import com.example.demo.service.ProductService;
import com.example.demo.service.RoomService;
import com.example.demo.service.RoomTypeService;
import com.example.demo.service.CustomerService; // Thêm service quản lý khách hàng
import com.example.demo.service.CheckInService;   // Thêm service quản lý checkin/đặt phòng
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
    private final CustomerService customerService; // Khai báo thêm
    private final CheckInService checkInService;   // Khai báo thêm

    // Đã cập nhật Constructor để Spring tự động Injection (Tiêm) các Service mới vào
    public RoomController(
            RoomService roomService,
            RoomTypeService roomTypeService,
            ProductService productService,
            CloudStorageService cloudStorageService,
            CustomerService customerService,
            CheckInService checkInService) {
        this.roomService = roomService;
        this.roomTypeService = roomTypeService;
        this.productService = productService;
        this.cloudStorageService = cloudStorageService;
        this.customerService = customerService;
        this.checkInService = checkInService;
    }

    // ==========================================
    // 1. DANH SÁCH PHÒNG MÁY
    // ==========================================
    @GetMapping(value = {"", "/"})
    public String list(Model model) {
        List<Room> roomList = roomService.findAll();
        model.addAttribute("list", roomList);

        List<Room> availableRooms = roomList.stream()
                .filter(r -> r.getStatus() == RoomStatus.AVAILABLE)
                .collect(Collectors.toList());
        model.addAttribute("availableRooms", availableRooms);

        return "list";
    }

    // ==========================================
    // 2. THÊM MỚI PHÒNG MÁY (DÙNG CLOUD STORAGE)
    // ==========================================
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
            redirectAttributes.addFlashAttribute("success", "Thêm phòng máy mới thành open thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi khi thêm phòng: " + e.getMessage());
            model.addAttribute("roomTypes", roomTypeService.findAll());
            model.addAttribute("isEdit", false);
            return "create";
        }

        return "redirect:/rooms";
    }

    // ==========================================
    // 3. CẬP NHẬT PHÒNG MÁY (GIỮ/ĐỔI ẢNH CLOUD)
    // ==========================================
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

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateRoom(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("room") Room room,
                             BindingResult result,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             Model model,
                             RedirectAttributes redirectAttributes) {

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

            if (room.getRoomType() != null && room.getRoomType().getId() != null) {
                RoomType validRoomType = roomTypeService.findById(room.getRoomType().getId());
                existingRoom.setRoomType(validRoomType);
            }

            if (imageFile != null && !imageFile.isEmpty()) {
                String cloudImageUrl = cloudStorageService.uploadImage(imageFile);
                existingRoom.setImage(cloudImageUrl);
            }

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

    // ==========================================
    // 4. XÓA PHÒNG MÁY
    // ==========================================
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

    // ==========================================
    // 5. XEM CHI TIẾT PHÒNG
    // ==========================================
    @GetMapping("/room/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("room", roomService.findById(id));
        return "detail";
    }

    // ==========================================
    // 6. CÁC HÀM TÌM KIẾM & LỌC DỮ LIỆU
    // ==========================================
    @GetMapping("/search")
    public String search(@RequestParam(required = false) Double price, Model model) {
        model.addAttribute("list", roomService.search(price));
        model.addAttribute("price", price);
        return "list";
    }

    @GetMapping("/status/{status}")
    public String filterByStatus(@PathVariable RoomStatus status, Model model) {
        model.addAttribute("list", roomService.findByStatus(status));
        model.addAttribute("selectedStatus", status);
        return "list";
    }

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

    // ==========================================
    // 7. THAY ĐỔI TRẠNG THÁI & ĐẶT TRƯỚC
    // ==========================================
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

    // A. HIỂN THỊ FORM ĐẶT TRƯỚC PHÒNG (SỬA LẠI ĐỂ TRẢ VỀ FORM HTML)
    @GetMapping("/reserve")
    @PreAuthorize("hasRole('ADMIN')")
    public String showReserveForm(@RequestParam("roomId") Long roomId, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Nạp dữ liệu khách hàng và phòng để đưa vào giao diện revese.html
            model.addAttribute("rooms", roomService.findAll());
            model.addAttribute("customers", customerService.findAll());
            model.addAttribute("selectedRoomId", roomId);

            // Trả về chính xác đường dẫn file template: templates/checkin/reserve.html
            return "checkin/reserve";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể mở form đặt phòng: " + e.getMessage());
            return "redirect:/rooms";
        }
    }

    // B. TIẾP NHẬN DỮ LIỆU TỪ FORM ĐẶT PHÒNG VÀ LƯU VÀO CƠ SỞ DỮ LIỆU
    @PostMapping("/reserve/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveReservation(
            @RequestParam("customerId") Long customerId,
            @RequestParam("roomId") Long roomId,
            @RequestParam("checkInTimeStr") String checkInTimeStr,
            @RequestParam("expectedHours") Double expectedHours,
            RedirectAttributes redirectAttributes) {
        try {
            CheckIn booking = new CheckIn();

            Customer customer = customerService.findById(customerId);
            booking.setCustomer(customer);

            Room room = roomService.findById(roomId);
            booking.setRoom(room);

            // Chuyển đổi chuỗi String thời gian từ giao diện (datetime-local) sang LocalDateTime
            java.time.LocalDateTime checkInTime = java.time.LocalDateTime.parse(checkInTimeStr);
            booking.setCheckInTime(checkInTime);
            booking.setExpectedHours(expectedHours);
            booking.setStatus("RESERVED");

            // Lưu hóa đơn đặt phòng vào DB
            checkInService.save(booking);

            // Đổi trạng thái phòng sang RESERVED (Đặt trước)
            roomService.updateStatus(roomId, RoomStatus.RESERVED);

            redirectAttributes.addFlashAttribute("success", "Đã đặt trước phòng máy thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi xử lý lưu thông tin đặt phòng: " + e.getMessage());
        }
        return "redirect:/rooms";
    }

    // ==========================================
    // 8. DỮ LIỆU CHUNG (CẢNH BÁO KHO & HẾT GIỜ)
    // ==========================================
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        List<Room> allRooms = roomService.getAll();
        model.addAttribute("availableCount", allRooms.stream().filter(r -> r.getStatus() == RoomStatus.AVAILABLE).count());
        model.addAttribute("occupiedCount", allRooms.stream().filter(r -> r.getStatus() == RoomStatus.OCCUPIED).count());
        model.addAttribute("cleaningCount", allRooms.stream().filter(r -> r.getStatus() == RoomStatus.CLEANING).count());
        model.addAttribute("maintenanceCount", allRooms.stream().filter(r -> r.getStatus() == RoomStatus.MAINTENANCE).count());
        model.addAttribute("reservedCount", allRooms.stream().filter(r -> r.getStatus() == RoomStatus.RESERVED).count());
        model.addAttribute("allCount", allRooms.size());

        long lowStockCount = productService.getAllProducts().stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() <= 10)
                .count();
        model.addAttribute("lowStockCount", lowStockCount);

        List<Room> almostOvertimeRooms = allRooms.stream()
                .filter(r -> r.getStatus() == RoomStatus.OCCUPIED)
                .filter(r -> r.getActiveCheckIn() != null && r.getActiveCheckIn().isAlmostOvertime())
                .collect(Collectors.toList());

        model.addAttribute("almostOvertimeRooms", almostOvertimeRooms);
        model.addAttribute("almostOvertimeCount", almostOvertimeRooms.size());
    }
}