package com.example.demo.controller;

import com.example.demo.enumtype.RoomStatus;
import com.example.demo.model.CheckIn;
import com.example.demo.model.Room;
import com.example.demo.model.RoomServiceOrder;
import com.example.demo.repository.RoomServiceOrderRepository;
import com.example.demo.service.CheckInService;
import com.example.demo.service.CustomerService;
import com.example.demo.service.RoomService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CheckInController {

    private final CheckInService checkInService;
    private final CustomerService customerService;
    private final RoomService roomService;
    private final RoomServiceOrderRepository roomServiceOrderRepository;

    public CheckInController(
            CheckInService checkInService,
            CustomerService customerService,
            RoomService roomService,
            RoomServiceOrderRepository roomServiceOrderRepository
    ) {
        this.checkInService = checkInService;
        this.customerService = customerService;
        this.roomService = roomService;
        this.roomServiceOrderRepository = roomServiceOrderRepository;
    }

    // ===== HIỂN THỊ FORM CHECK-IN (TẠO PHIẾU THUÊ TRỰC TIẾP) =====
    @GetMapping("/checkin")
    public String checkInPage(
            @RequestParam(required = false) Long roomId,
            Model model
    ){
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("rooms", roomService.getAll().stream()
                .filter(r -> r.getStatus() == RoomStatus.AVAILABLE)
                .collect(Collectors.toList()));
        model.addAttribute("selectedRoomId", roomId);
        return "checkin/create";
    }

    // ===== LƯU PHIẾU THUÊ (CHECK-IN TRỰC TIẾP) =====
    @PostMapping("/checkin/save")
    public String saveCheckIn(
            @RequestParam Long customerId,
            @RequestParam Long roomId,
            @RequestParam(defaultValue = "1.0") Double expectedHours,
            RedirectAttributes redirectAttributes
    ){
        Room room = roomService.findById(roomId);
        if (room == null || room.getStatus() != RoomStatus.AVAILABLE) {
            redirectAttributes.addFlashAttribute("error", "Phòng không khả dụng để check-in!");
            return "redirect:/rooms";
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setCustomer(customerService.findById(customerId));
        checkIn.setRoom(room);
        checkIn.setCheckInTime(LocalDateTime.now());
        checkIn.setExpectedHours(expectedHours);
        checkIn.setStatus("ACTIVE");

        // Đổi trạng thái phòng sang OCCUPIED
        roomService.updateStatus(roomId, RoomStatus.OCCUPIED);
        checkInService.save(checkIn);

        redirectAttributes.addFlashAttribute("successMessage", "Tạo phiếu thuê và nhận phòng thành công!");
        return "redirect:/rooms";
    }

    // ===== HIỂN THỊ FORM ĐẶT TRƯỚC PHÒNG (RESERVATION) =====
    @GetMapping("/reserve")
    public String reservePage(
            @RequestParam(required = false) Long roomId,
            Model model
    ) {
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("rooms", roomService.getAll().stream()
                .filter(r -> r.getStatus() == RoomStatus.AVAILABLE)
                .collect(Collectors.toList()));
        model.addAttribute("selectedRoomId", roomId);
        return "checkin/reserve";
    }

    // ===== LƯU ĐẶT TRƯỚC PHÒNG =====
    @PostMapping("/reserve/save")
    public String saveReservation(
            @RequestParam Long customerId,
            @RequestParam Long roomId,
            @RequestParam String checkInTimeStr,
            @RequestParam(defaultValue = "1.0") Double expectedHours,
            RedirectAttributes redirectAttributes
    ) {
        Room room = roomService.findById(roomId);
        if (room == null || room.getStatus() != RoomStatus.AVAILABLE) {
            redirectAttributes.addFlashAttribute("error", "Phòng không khả dụng để đặt trước!");
            return "redirect:/rooms";
        }

        LocalDateTime checkInTime = LocalDateTime.parse(checkInTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        CheckIn checkIn = new CheckIn();
        checkIn.setCustomer(customerService.findById(customerId));
        checkIn.setRoom(room);
        checkIn.setCheckInTime(checkInTime); // Dự kiến nhận phòng
        checkIn.setExpectedHours(expectedHours);
        checkIn.setStatus("RESERVED");

        // Đổi trạng thái phòng sang RESERVED
        roomService.updateStatus(roomId, RoomStatus.RESERVED);
        checkInService.save(checkIn);

        redirectAttributes.addFlashAttribute("successMessage", "Đặt trước phòng thành công!");
        return "redirect:/rooms";
    }

    // ===== CHECK-IN KHÁCH ĐÃ ĐẶT TRƯỚC (ACTIVATE RESERVATION) =====
    @GetMapping("/checkin/activate/{id}")
    public String activateReservation(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        CheckIn checkIn = checkInService.findById(id);
        if (checkIn == null || !"RESERVED".equals(checkIn.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin đặt trước hợp lệ!");
            return "redirect:/rooms";
        }

        Room room = checkIn.getRoom();
        checkIn.setStatus("ACTIVE");
        checkIn.setCheckInTime(LocalDateTime.now()); // Cập nhật thời gian check-in thực tế

        roomService.updateStatus(room.getId(), RoomStatus.OCCUPIED);
        checkInService.save(checkIn);

        redirectAttributes.addFlashAttribute("successMessage", "Check-in khách thành công!");
        return "redirect:/rooms";
    }

    // ===== HỦY ĐẶT PHÒNG =====
    @GetMapping("/booking/cancel/{id}")
    public String cancelReservation(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        CheckIn checkIn = checkInService.findById(id);
        if (checkIn == null || !"RESERVED".equals(checkIn.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin đặt trước để hủy!");
            return "redirect:/rooms";
        }

        Room room = checkIn.getRoom();
        checkIn.setStatus("CANCELLED");
        checkIn.setCheckOutTime(LocalDateTime.now());

        roomService.updateStatus(room.getId(), RoomStatus.AVAILABLE);
        checkInService.save(checkIn);

        redirectAttributes.addFlashAttribute("successMessage", "Hủy đặt phòng thành công!");
        return "redirect:/rooms";
    }

    // ===== CHUYỂN PHÒNG =====
    @PostMapping("/checkin/transfer")
    public String transferRoom(
            @RequestParam Long checkInId,
            @RequestParam Long newRoomId,
            RedirectAttributes redirectAttributes
    ) {
        CheckIn checkIn = checkInService.findById(checkInId);
        Room newRoom = roomService.findById(newRoomId);

        if (checkIn == null || newRoom == null || newRoom.getStatus() != RoomStatus.AVAILABLE) {
            redirectAttributes.addFlashAttribute("error", "Chuyển phòng thất bại! Phòng mới không khả dụng.");
            return "redirect:/rooms";
        }

        Room oldRoom = checkIn.getRoom();

        // Cập nhật trạng thái phòng cũ
        roomService.updateStatus(oldRoom.getId(), RoomStatus.AVAILABLE);

        // Cập nhật phòng mới theo trạng thái của check-in
        if ("ACTIVE".equals(checkIn.getStatus())) {
            roomService.updateStatus(newRoom.getId(), RoomStatus.OCCUPIED);
        } else if ("RESERVED".equals(checkIn.getStatus())) {
            roomService.updateStatus(newRoom.getId(), RoomStatus.RESERVED);
        }

        // Cập nhật thông tin phiếu thuê
        checkIn.setRoom(newRoom);
        checkInService.save(checkIn);

        redirectAttributes.addFlashAttribute("successMessage", "Chuyển phòng từ " + oldRoom.getRoomName() + " sang " + newRoom.getRoomName() + " thành công!");
        return "redirect:/rooms";
    }

    // ===== GIA HẠN THỜI GIAN THUÊ =====
    @PostMapping("/checkin/extend")
    public String extendCheckIn(
            @RequestParam Long checkInId,
            @RequestParam Double additionalHours,
            RedirectAttributes redirectAttributes
    ) {
        CheckIn checkIn = checkInService.findById(checkInId);
        if (checkIn == null || !"ACTIVE".equals(checkIn.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Gia hạn thất bại! Phiếu thuê không hoạt động.");
            return "redirect:/rooms";
        }

        checkIn.setExpectedHours(checkIn.getExpectedHours() + additionalHours);
        checkInService.save(checkIn);

        redirectAttributes.addFlashAttribute("successMessage", "Gia hạn thời gian thuê thêm " + additionalHours + " giờ thành công!");
        return "redirect:/rooms";
    }

    // ===== 1. GIAO DIỆN XEM TRƯỚC HÓA ĐƠN XUẤT PHÒNG =====
    @GetMapping("/checkout/{roomId}")
    public String checkOut(
            @PathVariable Long roomId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        CheckIn checkIn = checkInService.findActiveByRoomId(roomId);
        if (checkIn == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy phiên sử dụng phòng đang hoạt động!");
            return "redirect:/rooms";
        }

        LocalDateTime tempCheckOutTime = LocalDateTime.now();
        Duration duration = Duration.between(checkIn.getCheckInTime(), tempCheckOutTime);
        double totalHours = Math.max(1.0, duration.toMinutes() / 60.0);

        double roomPrice = 0.0;
        if (checkIn.getRoom() != null) {
            roomPrice = checkIn.getRoom().getPrice();
        }

        double expectedHours = checkIn.getExpectedHours() != null ? checkIn.getExpectedHours() : 0.0;
        double roomTotalPrice = 0.0;
        double overtimeHours = 0.0;
        double overtimeCharge = 0.0;

        if (expectedHours > 0.0 && totalHours > expectedHours) {
            roomTotalPrice = expectedHours * roomPrice;
            overtimeHours = totalHours - expectedHours;
            overtimeCharge = overtimeHours * roomPrice * 1.5; // Hệ số phụ phí quá giờ 1.5x
        } else {
            roomTotalPrice = totalHours * roomPrice;
        }

        // Quét đơn gọi dịch vụ của bạn
        List<RoomServiceOrder> activeOrders = new ArrayList<>();
        double totalServicePrice = 0.0;

        try {
            List<RoomServiceOrder> ordersInDb = roomServiceOrderRepository.findByRoomIdAndStatus(roomId, "PENDING");
            if (ordersInDb != null) {
                for (RoomServiceOrder order : ordersInDb) {
                    if (order != null && order.getTotalPrice() != null) {
                        activeOrders.add(order);
                        totalServicePrice += order.getTotalPrice();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi quét danh sách món ăn: " + e.getMessage());
        }

        double finalBillPrice = roomTotalPrice + overtimeCharge + totalServicePrice;

        checkIn.setCheckOutTime(tempCheckOutTime);
        checkIn.setTotalHours(totalHours);
        checkIn.setOvertimeHours(overtimeHours);
        checkIn.setOvertimeCharge(overtimeCharge);

        model.addAttribute("checkIn", checkIn);
        model.addAttribute("tempCheckOutTime", tempCheckOutTime);
        model.addAttribute("totalHours", totalHours);
        model.addAttribute("roomTotalPrice", roomTotalPrice);
        model.addAttribute("serviceOrders", activeOrders);
        model.addAttribute("totalServicePrice", totalServicePrice);
        model.addAttribute("finalBillPrice", finalBillPrice);

        return "billing/confirm";
    }

    // ===== 2. XỬ LÝ LƯU CHỐT HÓA ĐƠN XUỐNG DB (BẢO MẬT KHÉP KÍN) =====
    @PostMapping("/billing/confirm")
    public String confirmBilling(
            @RequestParam Long id,
            @RequestParam(value = "totalHours", defaultValue = "1.0") Double totalHours,
            @RequestParam(defaultValue = "0.0") Double surcharge,
            RedirectAttributes redirectAttributes
    ) {
        CheckIn checkIn = checkInService.findById(id);
        if (checkIn == null) {
            redirectAttributes.addFlashAttribute("error", "Thanh toán thất bại! Không tìm thấy phiếu thuê.");
            return "redirect:/rooms";
        }

        double roomPrice = 0.0;
        if (checkIn.getRoom() != null) {
            roomPrice = checkIn.getRoom().getPrice();
        }

        double expectedHours = checkIn.getExpectedHours() != null ? checkIn.getExpectedHours() : 0.0;
        double calculatedRoomPrice = 0.0;
        double overtimeHours = 0.0;
        double overtimeCharge = 0.0;

        if (expectedHours > 0.0 && totalHours > expectedHours) {
            calculatedRoomPrice = expectedHours * roomPrice;
            overtimeHours = totalHours - expectedHours;
            overtimeCharge = overtimeHours * roomPrice * 1.5;
        } else {
            calculatedRoomPrice = totalHours * roomPrice;
        }

        double calculatedServicePrice = 0.0;
        List<RoomServiceOrder> activeOrders = null;

        if (checkIn.getRoom() != null) {
            Long roomId = checkIn.getRoom().getId();
            activeOrders = roomServiceOrderRepository.findByRoomIdAndStatus(roomId, "PENDING");
            if (activeOrders != null) {
                for (RoomServiceOrder order : activeOrders) {
                    if (order != null && order.getTotalPrice() != null) {
                        calculatedServicePrice += order.getTotalPrice();
                    }
                }
            }
        }

        double secureFinalPrice = calculatedRoomPrice + overtimeCharge + calculatedServicePrice + surcharge;

        checkIn.setCheckOutTime(LocalDateTime.now());
        checkIn.setTotalHours(totalHours);
        checkIn.setOvertimeHours(overtimeHours);
        checkIn.setOvertimeCharge(overtimeCharge);
        checkIn.setSurcharge(surcharge);
        checkIn.setTotalPrice(secureFinalPrice);
        checkIn.setStatus("COMPLETED");
        checkInService.save(checkIn);

        if (activeOrders != null) {
            for (RoomServiceOrder order : activeOrders) {
                if (order != null) {
                    order.setStatus("DELIVERED");
                    roomServiceOrderRepository.save(order);
                }
            }
        }

        if (checkIn.getRoom() != null) {
            roomService.updateStatus(checkIn.getRoom().getId(), RoomStatus.CLEANING);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Thanh toán thành công cho phòng " + checkIn.getRoom().getRoomName() + "!");
        return "redirect:/rooms";
    }

    @PreAuthorize("hasAuthority('View_History')")
    @GetMapping("/checkin/history")
    public String history(Model model){
        model.addAttribute("list", checkInService.getAll());
        return "checkin/history";
    }
}