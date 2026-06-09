package com.example.demo.controller;

import com.example.demo.enumtype.RoomStatus;
import com.example.demo.model.Bill;
import com.example.demo.model.CheckIn;
import com.example.demo.model.Room;
import com.example.demo.model.RoomServiceOrder;
import com.example.demo.repository.BillRepository;
import com.example.demo.repository.RoomServiceOrderRepository;
import com.example.demo.service.CheckInService;
import com.example.demo.service.CustomerService;
import com.example.demo.service.RoomService;
import com.example.demo.service.ActivityLogService; // Thêm Service ghi Log

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class CheckInController {

    private final CheckInService checkInService;
    private final CustomerService customerService;
    private final RoomService roomService;
    private final RoomServiceOrderRepository roomServiceOrderRepository;
    private final BillRepository billRepository;
    private final ActivityLogService activityLogService; // Khai báo Service ghi Log

    public CheckInController(
            CheckInService checkInService,
            CustomerService customerService,
            RoomService roomService,
            RoomServiceOrderRepository roomServiceOrderRepository,
            BillRepository billRepository,
            ActivityLogService activityLogService) { // Inject vào Constructor
        this.checkInService = checkInService;
        this.customerService = customerService;
        this.roomService = roomService;
        this.roomServiceOrderRepository = roomServiceOrderRepository;
        this.billRepository = billRepository;
        this.activityLogService = activityLogService;
    }

    // ===== HÀM ĐIỀU HƯỚNG NÚT TẠO PHIẾU THUÊ =====
    @GetMapping("/checkin")
    public String redirectCheckIn(@RequestParam(required = false) Long roomId) {
        if (roomId != null) {
            return "redirect:/checkin/create?roomId=" + roomId;
        }
        return "redirect:/checkin/create";
    }

    // ===== HIỂN THỊ FORM CHECK-IN TRỰC TIẾP =====
    @GetMapping("/checkin/create")
    public String checkInPage(Model model, @RequestParam(required = false) Long roomId) {
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("rooms", roomService.getAll().stream()
                .filter(r -> r.getStatus() == RoomStatus.AVAILABLE)
                .collect(Collectors.toList()));
        model.addAttribute("selectedRoomId", roomId);
        return "checkin/create";
    }

    // ===== LƯU PHIẾU THUÊ TRỰC TIẾP =====
    @PostMapping("/checkin/save")
    public String saveCheckIn(
            @RequestParam Long customerId,
            @RequestParam Long roomId,
            @RequestParam(defaultValue = "1.0") Double expectedHours,
            RedirectAttributes redirectAttributes) {
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

        roomService.updateStatus(roomId, RoomStatus.OCCUPIED);
        checkInService.save(checkIn);

        // [LOG] Ghi nhận hành động Tạo phiếu thuê trực tiếp
        activityLogService.log("CHECKIN_DIRECT", "Tạo phiếu mở máy trực tiếp tại phòng "
                + room.getRoomName() + " cho khách hàng ID: " + customerId + " (" + expectedHours + " giờ)");

        redirectAttributes.addFlashAttribute("successMessage", "Tạo phiếu thuê và nhận phòng thành công!");
        return "redirect:/checkin/history";
    }

    // ===== HIỂN THỊ FORM ĐẶT TRƯỚC PHÒNG (PRE-BOOK) =====
    @GetMapping("/reserve")
    public String reservePage(@RequestParam(required = false) Long roomId, Model model) {
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
            @RequestParam(value = "checkInTime", required = false) String checkInTime,
            @RequestParam(value = "checkInTimeStr", required = false) String checkInTimeStr,
            @RequestParam(defaultValue = "1.0") Double expectedHours,
            RedirectAttributes redirectAttributes) {

        Room room = roomService.findById(roomId);
        if (room == null || room.getStatus() != RoomStatus.AVAILABLE) {
            redirectAttributes.addFlashAttribute("error", "Phòng không khả dụng để đặt trước!");
            return "redirect:/rooms";
        }

        String rawDateTimeStr = (checkInTime != null && !checkInTime.trim().isEmpty()) ? checkInTime : checkInTimeStr;
        LocalDateTime finalCheckInTime = null;
        if (rawDateTimeStr != null && !rawDateTimeStr.trim().isEmpty()) {
            try {
                if (rawDateTimeStr.contains("T")) {
                    finalCheckInTime = LocalDateTime.parse(rawDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } else {
                    String cleanedStr = rawDateTimeStr.replace("CH", "PM").replace("SA", "AM").trim();
                    DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a",
                            java.util.Locale.ENGLISH);
                    finalCheckInTime = LocalDateTime.parse(cleanedStr, customFormatter);
                }
            } catch (Exception e) {
                finalCheckInTime = LocalDateTime.now();
            }
        } else {
            finalCheckInTime = LocalDateTime.now();
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setCustomer(customerService.findById(customerId));
        checkIn.setRoom(room);
        checkIn.setCheckInTime(finalCheckInTime);
        checkIn.setExpectedHours(expectedHours);
        checkIn.setStatus("RESERVED");

        roomService.updateStatus(roomId, RoomStatus.AVAILABLE);
        checkInService.save(checkIn);

        // [LOG] Ghi nhận hành động Đặt trước phòng máy
        activityLogService.log("RESERVE_ROOM", "Đặt lịch trước phòng "
                + room.getRoomName() + " cho khách hàng ID: " + customerId + ", thời gian dự kiến: " + finalCheckInTime);

        redirectAttributes.addFlashAttribute("successMessage", "Đặt trước phòng thành công!");
        return "redirect:/rooms";
    }

    // ===== KÍCH HOẠT NHẬN PHÒNG ĐÃ ĐẶT =====
    @GetMapping("/checkin/activate/{id}")
    public String activateReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        CheckIn checkIn = checkInService.findById(id);
        if (checkIn == null || !"RESERVED".equals(checkIn.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin đặt trước hợp lệ!");
            return "redirect:/rooms";
        }
        checkIn.setStatus("ACTIVE");
        checkIn.setCheckInTime(LocalDateTime.now());
        roomService.updateStatus(checkIn.getRoom().getId(), RoomStatus.OCCUPIED);
        checkInService.save(checkIn);

        // [LOG] Kích hoạt nhận máy đã booking trước đó
        activityLogService.log("ACTIVATE_RESERVATION", "Khách nhận máy đặt trước tại phòng "
                + checkIn.getRoom().getRoomName() + " (Mã phiếu thuê: " + id + ")");

        redirectAttributes.addFlashAttribute("successMessage", "Check-in khách thành công!");
        return "redirect:/rooms";
    }

    // ===== HỦY ĐẶT PHÒNG =====
    @GetMapping("/booking/cancel/{id}")
    public String cancelReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        CheckIn checkIn = checkInService.findById(id);
        if (checkIn == null || !"RESERVED".equals(checkIn.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin đặt trước để hủy!");
            return "redirect:/rooms";
        }
        checkIn.setStatus("CANCELLED");
        checkIn.setCheckOutTime(LocalDateTime.now());
        roomService.updateStatus(checkIn.getRoom().getId(), RoomStatus.AVAILABLE);
        checkInService.save(checkIn);

        // [LOG] Hủy lịch đặt trước máy
        activityLogService.log("CANCEL_RESERVATION", "Hủy phiếu đặt lịch trước phòng máy "
                + checkIn.getRoom().getRoomName() + " (Mã phiếu thuê: " + id + ")");

        redirectAttributes.addFlashAttribute("successMessage", "Hủy đặt phòng thành công!");
        return "redirect:/rooms";
    }

    // ===== CHUYỂN PHÒNG =====
    @PostMapping("/checkin/transfer")
    public String transferRoom(@RequestParam Long checkInId, @RequestParam Long newRoomId,
                               RedirectAttributes redirectAttributes) {
        CheckIn checkIn = checkInService.findById(checkInId);
        Room newRoom = roomService.findById(newRoomId);
        if (checkIn == null || newRoom == null || newRoom.getStatus() != RoomStatus.AVAILABLE) {
            redirectAttributes.addFlashAttribute("error", "Chuyển phòng thất bại!");
            return "redirect:/rooms";
        }
        String oldRoomName = checkIn.getRoom().getRoomName();
        roomService.updateStatus(checkIn.getRoom().getId(), RoomStatus.AVAILABLE);
        if ("ACTIVE".equals(checkIn.getStatus()))
            roomService.updateStatus(newRoom.getId(), RoomStatus.OCCUPIED);
        else if ("RESERVED".equals(checkIn.getStatus()))
            roomService.updateStatus(newRoom.getId(), RoomStatus.RESERVED);
        checkIn.setRoom(newRoom);
        checkInService.save(checkIn);

        // [LOG] Chuyển đổi phòng/máy tính
        activityLogService.log("TRANSFER_ROOM", "Chuyển khách hàng từ phòng "
                + oldRoomName + " sang phòng trống mới " + newRoom.getRoomName() + " (Phiếu: " + checkInId + ")");

        redirectAttributes.addFlashAttribute("successMessage", "Chuyển phòng thành công!");
        return "redirect:/rooms";
    }

    // ===== GIA HẠN THỜI GIAN =====
    @PostMapping("/checkin/extend")
    public String extendCheckIn(@RequestParam Long checkInId, @RequestParam Double additionalHours,
                                RedirectAttributes redirectAttributes) {
        CheckIn checkIn = checkInService.findById(checkInId);
        if (checkIn == null || !"ACTIVE".equals(checkIn.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Gia hạn thất bại!");
            return "redirect:/rooms";
        }
        checkIn.setExpectedHours(checkIn.getExpectedHours() + additionalHours);
        checkInService.save(checkIn);

        // [LOG] Gia hạn phiên chơi máy
        activityLogService.log("EXTEND_ROOM", "Gia hạn thêm +" + additionalHours
                + " giờ cho phòng " + checkIn.getRoom().getRoomName() + " (Tổng giờ mới: " + checkIn.getExpectedHours() + "h)");

        redirectAttributes.addFlashAttribute("successMessage", "Gia hạn thành công!");
        return "redirect:/rooms";
    }

    // ===== CHUẨN BỊ TRẢ PHÒNG & TÍNH TOÁN TIỀN PHÒNG (Feature 54) =====
    @GetMapping("/checkout/{roomId}")
    public String checkOut(@PathVariable Long roomId, Model model, RedirectAttributes redirectAttributes) {
        CheckIn checkIn = checkInService.findActiveByRoomId(roomId);
        if (checkIn == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy phiên sử dụng phòng!");
            return "redirect:/rooms";
        }
        LocalDateTime now = LocalDateTime.now();
        double totalHours = Math.max(1.0, Duration.between(checkIn.getCheckInTime(), now).toMinutes() / 60.0);
        double roomPrice = checkIn.getRoom().getPrice();
        double expectedHours = checkIn.getExpectedHours() != null ? checkIn.getExpectedHours() : 0.0;
        double roomTotalPrice, overtimeHours = 0.0, overtimeCharge = 0.0;

        if (expectedHours > 0.0 && totalHours > expectedHours) {
            roomTotalPrice = expectedHours * roomPrice;
            overtimeHours = totalHours - expectedHours;
            overtimeCharge = overtimeHours * roomPrice * 1.5;
        } else {
            roomTotalPrice = totalHours * roomPrice;
        }

        List<RoomServiceOrder> orders = roomServiceOrderRepository.findByRoomIdAndStatus(roomId, "PENDING");
        double totalServicePrice = orders.stream().mapToDouble(RoomServiceOrder::getTotalPrice).sum();

        model.addAttribute("checkIn", checkIn);
        model.addAttribute("totalHours", totalHours);
        model.addAttribute("roomTotalPrice", roomTotalPrice);
        model.addAttribute("serviceOrders", orders);
        model.addAttribute("totalServicePrice", totalServicePrice);
        model.addAttribute("finalBillPrice", roomTotalPrice + overtimeCharge + totalServicePrice);
        return "billing/confirm";
    }

    // ===== XÁC NHẬN THANH TOÁN (Feature 54, 55, 56) =====
    @PostMapping("/billing/confirm")
    public String confirmBilling(@RequestParam Long id, @RequestParam(defaultValue = "1.0") Double totalHours,
                                 @RequestParam(defaultValue = "0.0") Double surcharge,
                                 @RequestParam(defaultValue = "CASH") String paymentMethod,
                                 RedirectAttributes redirectAttributes) {
        CheckIn checkIn = checkInService.findById(id);
        if (checkIn == null)
            return "redirect:/rooms";

        double roomPrice = checkIn.getRoom().getPrice();
        double expected = checkIn.getExpectedHours() != null ? checkIn.getExpectedHours() : 0.0;
        double roomTotal = (totalHours > expected) ? (expected * roomPrice) : (totalHours * roomPrice);
        double overtime = (totalHours > expected) ? ((totalHours - expected) * roomPrice * 1.5) : 0.0;

        List<RoomServiceOrder> orders = roomServiceOrderRepository.findByRoomIdAndStatus(checkIn.getRoom().getId(),
                "PENDING");
        double serviceTotal = orders.stream().mapToDouble(RoomServiceOrder::getTotalPrice).sum();

        checkIn.setCheckOutTime(LocalDateTime.now());
        checkIn.setTotalHours(totalHours);
        checkIn.setOvertimeHours((totalHours > expected) ? (totalHours - expected) : 0.0);
        checkIn.setOvertimeCharge(overtime);
        checkIn.setSurcharge(surcharge);
        checkIn.setTotalPrice(roomTotal + overtime + serviceTotal + surcharge);
        checkIn.setStatus("COMPLETED");
        checkInService.save(checkIn);

        Bill bill = new Bill();
        bill.setBillCode("HD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-"
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        bill.setCheckIn(checkIn);
        bill.setRoomPriceReal(roomTotal);
        bill.setOvertimePriceReal(overtime);
        bill.setServicePriceReal(serviceTotal);
        bill.setSurchargeReal(surcharge);
        bill.setFinalAmount(checkIn.getTotalPrice());
        bill.setPaymentMethod(paymentMethod);
        bill.setPaymentStatus("PAID");
        bill.setPaymentTime(LocalDateTime.now());
        bill.setStatus("PAID");
        billRepository.save(bill);

        orders.forEach(o -> {
            o.setStatus("DELIVERED");
            roomServiceOrderRepository.save(o);
        });
        roomService.updateStatus(checkIn.getRoom().getId(), RoomStatus.CLEANING);

        // [LOG] Xác nhận thanh toán & In hóa đơn hóa đơn
        activityLogService.log("CHECKOUT_BILLING", "Hoàn tất thanh toán phòng "
                + checkIn.getRoom().getRoomName() + ", Xuất hóa đơn: " + bill.getBillCode()
                + " (Tổng số tiền: " + String.format("%,.0f", bill.getFinalAmount()) + " VNĐ)");

        return "redirect:/billing/invoice/" + bill.getId();
    }

    // ===== XEM VÀ IN HÓA ĐƠN CHI TIẾT (Feature 57) =====
    @GetMapping("/billing/invoice/{id}")
    public String viewInvoice(@PathVariable Long id, Model model) {
        model.addAttribute("bill", billRepository.findById(id).orElseThrow());
        return "billing/invoice";
    }

    @GetMapping("/billing/invoice/by-checkin/{checkInId}")
    public String viewInvoiceByCheckIn(@PathVariable Long checkInId, RedirectAttributes redirectAttributes) {
        return billRepository.findByCheckInId(checkInId)
                .map(b -> "redirect:/billing/invoice/" + b.getId())
                .orElse("redirect:/checkin/history");
    }

    // ===== XEM LỊCH SỬ THUÊ PHÒNG =====
    @PreAuthorize("hasAuthority('View_History')")
    @GetMapping("/checkin/history")
    public String history(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by("id").descending());
        org.springframework.data.domain.Page<CheckIn> checkInPage = checkInService.getAll(pageable);
        model.addAttribute("list", checkInPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", checkInPage.getTotalPages());
        return "checkin/history";
    }

    // ===== FEATURE 58: XEM LỊCH SỬ THANH TOÁN (ADMIN) =====
    @PreAuthorize("hasAuthority('View_History') or hasRole('ADMIN')")
    @GetMapping("/billing/history")
    public String viewBillingHistory(Model model) {
        List<Bill> bills = billRepository.findAll();
        double totalRevenue = bills.stream()
                .filter(b -> !"REFUNDED".equals(b.getStatus()))
                .mapToDouble(Bill::getFinalAmount)
                .sum();
        model.addAttribute("bills", bills);
        model.addAttribute("totalRevenue", totalRevenue);
        return "billing/history";
    }

    // ===== FEATURE 59: ADMIN XỬ LÝ HOÀN TIỀN KHI CÓ SỰ CỐ =====
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/billing/refund/{id}")
    public String refundBill(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Bill> billOpt = billRepository.findById(id);
        if (billOpt.isPresent()) {
            Bill bill = billOpt.get();
            if ("REFUNDED".equals(bill.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "Hóa đơn này đã được hoàn tiền trước đó!");
                return "redirect:/billing/history";
            }
            // 1. Cập nhật trạng thái hóa đơn
            bill.setStatus("REFUNDED");
            billRepository.save(bill);

            // 2. Đồng bộ đưa trạng thái phòng về CLEANING qua roomService bảo mật hơn
            if (bill.getCheckIn() != null && bill.getCheckIn().getRoom() != null) {
                roomService.updateStatus(bill.getCheckIn().getRoom().getId(), RoomStatus.CLEANING);
            }

            // [LOG] Admin phê duyệt hoàn tiền hóa đơn
            activityLogService.log("REFUND_BILL", "ADMIN đã duyệt hoàn tiền thành công cho mã hóa đơn: "
                    + bill.getBillCode() + " (Số tiền hoàn lại: " + String.format("%,.0f", bill.getFinalAmount()) + " VNĐ)");

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã hoàn tiền thành công cho hóa đơn " + bill.getBillCode());
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy hóa đơn yêu cầu hoàn tiền.");
        }
        return "redirect:/billing/history";
    }

    // ===== TẠO ĐIỂM ĐẦU CUỐI XEM DANH SÁCH LOG DÀNH CHO ADMIN =====
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/logs")
    public String viewSystemLogs(Model model) {
        model.addAttribute("logs", activityLogService.getAllLogs());
        return "admin/log-list";
    }
}