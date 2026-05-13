package com.example.demo.controller;

import com.example.demo.enumtype.RoomStatus;
import com.example.demo.model.CheckIn;
import com.example.demo.model.Room;
import com.example.demo.service.CheckInService;
import com.example.demo.service.CustomerService;
import com.example.demo.service.RoomService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Controller
public class CheckInController {

    private final CheckInService checkInService;
    private final CustomerService customerService;
    private final RoomService roomService;

    public CheckInController(
            CheckInService checkInService,
            CustomerService customerService,
            RoomService roomService
    ) {
        this.checkInService = checkInService;
        this.customerService = customerService;
        this.roomService = roomService;
    }

    // ===== HIỂN THỊ FORM CHECK-IN =====
    @GetMapping("/checkin")
    public String checkInPage(Model model){

        model.addAttribute(
                "customers",
                customerService.findAll()
        );

        model.addAttribute(
                "rooms",
                roomService.getAll()
        );

        return "checkin/create";
    }

    // ===== LƯU CHECK-IN =====
    @PostMapping("/checkin/save")
    public String saveCheckIn(
            @RequestParam Long customerId,
            @RequestParam Long roomId
    ){

        CheckIn checkIn = new CheckIn();

        // lấy khách hàng
        checkIn.setCustomer(
                customerService.findById(customerId)
        );

        // lấy phòng
        Room room = roomService.findById(roomId);

        checkIn.setRoom(room);

        // thời gian check-in
        checkIn.setCheckInTime(LocalDateTime.now());

        // đổi trạng thái phòng
        room.setStatus(RoomStatus.OCCUPIED);

        // lưu phòng
        roomService.save(room);

        // lưu check-in
        checkInService.save(checkIn);

        return "redirect:/rooms";
    }

    // ===== CHECK-OUT =====
    @GetMapping("/checkout/{roomId}")
    public String checkOut(
            @PathVariable Long roomId,
            Model model
    ) {

        // tìm phiên check-in đang hoạt động
        CheckIn checkIn =
                checkInService.findActiveByRoomId(roomId);

        // nếu không tồn tại
        if(checkIn == null){
            return "redirect:/rooms";
        }

        // thời gian checkout
        checkIn.setCheckOutTime(LocalDateTime.now());

        // tính thời gian sử dụng
        Duration duration = Duration.between(
                checkIn.getCheckInTime(),
                checkIn.getCheckOutTime()
        );

        // đổi sang giờ
        double totalHours =
                duration.toMinutes() / 60.0;

        checkIn.setTotalHours(totalHours);

        // lấy giá phòng
        double roomPrice =
                checkIn.getRoom().getPrice();

        // tính tổng tiền
        double totalPrice =
                totalHours * roomPrice;

        checkIn.setTotalPrice(totalPrice);

        // lưu tạm vào DB để confirmBilling có thể tìm thấy dữ liệu đã tính toán
        checkInService.save(checkIn);

        // truyền sang giao diện billing
        model.addAttribute(
                "checkIn",
                checkIn
        );

        return "billing/confirm";
    }

    // ===== XÁC NHẬN THANH TOÁN (ĐÃ THAY THẾ) =====
    @PostMapping("/billing/confirm")
    public String confirmBilling(
            @RequestParam Long id
    ) {

        // lấy checkin từ DB
        CheckIn checkIn =
                checkInService.findById(id);

        // lấy phòng
        Room room =
                checkIn.getRoom();

        // chuyển trạng thái
        room.setStatus(RoomStatus.CLEANING);

        // lưu phòng
        roomService.save(room);

        // lưu checkin
        checkInService.save(checkIn);

        return "redirect:/rooms";
    }

    // ===== LỊCH SỬ CHECK-IN =====
    @GetMapping("/checkin/history")
    public String history(Model model){

        model.addAttribute(
                "list",
                checkInService.getAll()
        );

        return "checkin/history";
    }
}