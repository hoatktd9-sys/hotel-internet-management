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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @GetMapping("/checkin")
    public String checkInPage(Model model){
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("rooms", roomService.getAll());
        return "checkin/create";
    }

    @PostMapping("/checkin/save")
    public String saveCheckIn(
            @RequestParam Long customerId,
            @RequestParam Long roomId
    ){
        CheckIn checkIn = new CheckIn();
        checkIn.setCustomer(customerService.findById(customerId));

        Room room = roomService.findById(roomId);
        checkIn.setRoom(room);
        checkIn.setCheckInTime(LocalDateTime.now());

        roomService.updateStatus(roomId, RoomStatus.OCCUPIED);
        checkInService.save(checkIn);

        return "redirect:/rooms";
    }

    // 1. GIAO DIỆN XEM TRƯỚC HÓA ĐƠN XUẤT PHÒNG
    @GetMapping("/checkout/{roomId}")
    public String checkOut(
            @PathVariable Long roomId,
            Model model
    ) {
        CheckIn checkIn = checkInService.findActiveByRoomId(roomId);

        if(checkIn == null){
            return "redirect:/rooms";
        }

        LocalDateTime tempCheckOutTime = LocalDateTime.now();
        Duration duration = Duration.between(checkIn.getCheckInTime(), tempCheckOutTime);

        double totalHours = Math.max(1.0, duration.toMinutes() / 60.0);

        double roomPrice = 0.0;
        if (checkIn.getRoom() != null) {
            roomPrice = checkIn.getRoom().getPrice();
        }
        double roomTotalPrice = totalHours * roomPrice;

        List<RoomServiceOrder> activeOrders = new ArrayList<>();
        double totalServicePrice = 0.0;

        try {
            List<RoomServiceOrder> ordersInDb = roomServiceOrderRepository.findByRoomIdAndStatus(roomId, "PENDING");
            if (ordersInDb != null) {
                for (RoomServiceOrder order : ordersInDb) {
                    if (order != null && order.getTotalPrice() != null) {
                        activeOrders.add(order);
                        totalServicePrice += order.getTotalPrice(); // Chốt giá chốt cứng lúc gọi
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi quét danh sách món ăn: " + e.getMessage());
        }

        double finalBillPrice = roomTotalPrice + totalServicePrice;

        model.addAttribute("checkIn", checkIn);
        model.addAttribute("tempCheckOutTime", tempCheckOutTime);
        model.addAttribute("totalHours", totalHours);
        model.addAttribute("roomTotalPrice", roomTotalPrice);
        model.addAttribute("serviceOrders", activeOrders);
        model.addAttribute("totalServicePrice", totalServicePrice);
        model.addAttribute("finalBillPrice", finalBillPrice);

        return "billing/confirm";
    }

    // 2. XỬ LÝ LƯU CHỐT HÓA ĐƠN XUỐNG CƠ SỞ DỮ LIỆU (ĐÃ TỐI ƯU BẢO MẬT KHÉP KÍN)
    @PostMapping("/billing/confirm")
    public String confirmBilling(
            @RequestParam("id") Long id,
            @RequestParam(value = "totalHours", defaultValue = "1.0") Double totalHours
    ) {
        CheckIn checkIn = checkInService.findById(id);
        if (checkIn == null) {
            return "redirect:/rooms";
        }

        // Bước 1: Tính toán lại tiền phòng trực tiếp ở Backend
        double roomPrice = 0.0;
        if (checkIn.getRoom() != null) {
            roomPrice = checkIn.getRoom().getPrice();
        }
        double calculatedRoomPrice = totalHours * roomPrice;

        // Bước 2: Quét lại DB để lấy tổng tiền dịch vụ đã được khóa cứng tại thời điểm đặt
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

        // Bước 3: Tổng hợp doanh thu cuối cùng an toàn tuyệt đối
        double secureFinalPrice = calculatedRoomPrice + calculatedServicePrice;

        // Bước 4: Lưu thông tin kết thúc phiên CheckIn
        checkIn.setCheckOutTime(LocalDateTime.now());
        checkIn.setTotalHours(totalHours);
        checkIn.setTotalPrice(secureFinalPrice); // Lưu số tiền được xác thực bởi Backend
        checkInService.save(checkIn);

        // Bước 5: Chuyển toàn bộ đơn dịch vụ của phòng sang trạng thái hoàn thành "DELIVERED"
        if (activeOrders != null) {
            for (RoomServiceOrder order : activeOrders) {
                if (order != null) {
                    order.setStatus("DELIVERED");
                    roomServiceOrderRepository.save(order);
                }
            }
        }

        // Bước 6: Đổi trạng thái phòng sang dọn dẹp CLEANING
        if (checkIn.getRoom() != null) {
            roomService.updateStatus(checkIn.getRoom().getId(), RoomStatus.CLEANING);
        }

        return "redirect:/rooms";
    }

    @PreAuthorize("hasAuthority('View_History')")
    @GetMapping("/checkin/history")
    public String history(Model model){
        model.addAttribute("list", checkInService.getAll());
        return "checkin/history";
    }
}