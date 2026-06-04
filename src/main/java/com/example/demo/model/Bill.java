package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mã hóa đơn tự động định dạng (Ví dụ: HD-20260602-0001) để phục vụ việc in ấn
    private String billCode;

    // Kết nối 1-1 với phiên thuê phòng
    @OneToOne
    @JoinColumn(name = "checkin_id", unique = true)
    private CheckIn checkIn;

    private Double roomPriceReal;      // Tiền phòng thực tế tính toán
    private Double servicePriceReal;   // Tổng tiền dịch vụ thực tế
    private Double overtimePriceReal;  // Tiền quá giờ thực tế
    private Double surchargeReal;      // Phụ thu nhập thêm lúc check-out
    private Double finalAmount;        // Tổng số tiền cuối cùng khách phải trả

    // Phương thức thanh toán: CASH (Tiền mặt), TRANSFER (Chuyển khoản)
    private String paymentMethod;

    // Trạng thái thanh toán của hóa đơn
    private String paymentStatus = "PAID";

    // Trạng thái hóa đơn (VD: ACTIVE, VOIDED, v.v.)
    private String status = "PAID";

    private LocalDateTime paymentTime;

    // ===== GETTER SETTER =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBillCode() {
        return billCode;
    }

    public void setBillCode(String billCode) {
        this.billCode = billCode;
    }

    public CheckIn getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(CheckIn checkIn) {
        this.checkIn = checkIn;
    }

    public Double getRoomPriceReal() {
        return roomPriceReal;
    }

    public void setRoomPriceReal(Double roomPriceReal) {
        this.roomPriceReal = roomPriceReal;
    }

    public Double getServicePriceReal() {
        return servicePriceReal;
    }

    public void setServicePriceReal(Double servicePriceReal) {
        this.servicePriceReal = servicePriceReal;
    }

    public Double getOvertimePriceReal() {
        return overtimePriceReal;
    }

    public void setOvertimePriceReal(Double overtimePriceReal) {
        this.overtimePriceReal = overtimePriceReal;
    }

    public Double getSurchargeReal() {
        return surchargeReal;
    }

    public void setSurchargeReal(Double surchargeReal) {
        this.surchargeReal = surchargeReal;
    }

    public Double getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(Double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }
}