package com.example.demo.controller;

import com.example.demo.enumtype.RoomStatus;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private CheckInRepository checkInRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomServiceOrderRepository roomServiceOrderRepository;

    @Autowired
    private ProductRepository productRepository;

    // ==========================================
    // 1. DASHBOARD TỔNG QUAN
    // ==========================================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Bill> allBills = billRepository.findAll();
        List<Room> allRooms = roomRepository.findAll();
        List<RoomServiceOrder> allOrders = roomServiceOrderRepository.findAll();

        // 1.1 Doanh thu
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);

        double todayRevenue = allBills.stream()
                .filter(b -> "PAID".equalsIgnoreCase(b.getStatus()) && b.getPaymentTime() != null 
                        && !b.getPaymentTime().isBefore(startOfToday) && !b.getPaymentTime().isAfter(endOfToday))
                .mapToDouble(Bill::getFinalAmount)
                .sum();

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        double monthRevenue = allBills.stream()
                .filter(b -> "PAID".equalsIgnoreCase(b.getStatus()) && b.getPaymentTime() != null 
                        && !b.getPaymentTime().isBefore(startOfMonth) && !b.getPaymentTime().isAfter(endOfToday))
                .mapToDouble(Bill::getFinalAmount)
                .sum();

        // 1.2 Trạng thái phòng
        long totalRooms = allRooms.size();
        long availableRooms = allRooms.stream().filter(r -> r.getStatus() == RoomStatus.AVAILABLE).count();
        long occupiedRooms = allRooms.stream().filter(r -> r.getStatus() == RoomStatus.OCCUPIED).count();
        long cleaningRooms = allRooms.stream().filter(r -> r.getStatus() == RoomStatus.CLEANING).count();
        long maintenanceRooms = allRooms.stream().filter(r -> r.getStatus() == RoomStatus.MAINTENANCE).count();
        long reservedRooms = allRooms.stream().filter(r -> r.getStatus() == RoomStatus.RESERVED).count();

        // 1.3 Phòng sử dụng nhiều nhất (Top 5)
        List<RoomReportRow> roomReport = calculateRoomPopularity(allRooms, allBills);
        List<RoomReportRow> topRooms = roomReport.stream()
                .sorted(Comparator.comparingLong(RoomReportRow::getCheckInCount).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // 1.4 Sản phẩm bán chạy nhất (Top 5)
        List<ProductReportRow> productReport = calculateProductSales(allOrders);
        List<ProductReportRow> topProducts = productReport.stream()
                .sorted(Comparator.comparingLong(ProductReportRow::getQuantitySold).reversed())
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("todayRevenue", todayRevenue);
        model.addAttribute("monthRevenue", monthRevenue);
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("occupiedRooms", occupiedRooms);
        model.addAttribute("cleaningRooms", cleaningRooms);
        model.addAttribute("maintenanceRooms", maintenanceRooms);
        model.addAttribute("reservedRooms", reservedRooms);
        model.addAttribute("topRooms", topRooms);
        model.addAttribute("topProducts", topProducts);

        return "admin/reports/dashboard";
    }

    // ==========================================
    // 2. BÁO CÁO DOANH THU (NGÀY/THÁNG)
    // ==========================================
    @GetMapping("/revenue")
    public String revenue(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {

        List<Bill> allBills = billRepository.findAll();
        
        LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : LocalDate.now();

        LocalDateTime startLDT = start.atStartOfDay();
        LocalDateTime endLDT = end.atTime(LocalTime.MAX);

        List<Bill> filteredBills = allBills.stream()
                .filter(b -> "PAID".equalsIgnoreCase(b.getStatus()) && b.getPaymentTime() != null 
                        && !b.getPaymentTime().isBefore(startLDT) && !b.getPaymentTime().isAfter(endLDT))
                .sorted(Comparator.comparing(Bill::getPaymentTime).reversed())
                .collect(Collectors.toList());

        double totalRevenueRange = filteredBills.stream().mapToDouble(Bill::getFinalAmount).sum();
        double roomRevenue = filteredBills.stream().mapToDouble(b -> b.getRoomPriceReal() != null ? b.getRoomPriceReal() + (b.getOvertimePriceReal() != null ? b.getOvertimePriceReal() : 0.0) : 0.0).sum();
        double serviceRevenue = filteredBills.stream().mapToDouble(b -> b.getServicePriceReal() != null ? b.getServicePriceReal() : 0.0).sum();
        double surchargeRevenue = filteredBills.stream().mapToDouble(b -> b.getSurchargeReal() != null ? b.getSurchargeReal() : 0.0).sum();

        // Group revenue by date for chart
        Map<LocalDate, Double> revenueByDate = filteredBills.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getPaymentTime().toLocalDate(),
                        TreeMap::new,
                        Collectors.summingDouble(Bill::getFinalAmount)
                ));

        // Group revenue by month for last 12 months list
        Map<YearMonth, Double> revenueByMonth = allBills.stream()
                .filter(b -> "PAID".equalsIgnoreCase(b.getStatus()) && b.getPaymentTime() != null)
                .collect(Collectors.groupingBy(
                        b -> YearMonth.from(b.getPaymentTime()),
                        TreeMap::new,
                        Collectors.summingDouble(Bill::getFinalAmount)
                ));

        List<Map.Entry<String, Double>> monthlyList = revenueByMonth.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey().toString(), entry.getValue()))
                .sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey()))
                .collect(Collectors.toList());

        model.addAttribute("bills", filteredBills);
        model.addAttribute("totalRevenueRange", totalRevenueRange);
        model.addAttribute("roomRevenue", roomRevenue);
        model.addAttribute("serviceRevenue", serviceRevenue);
        model.addAttribute("surchargeRevenue", surchargeRevenue);
        model.addAttribute("startDate", start.toString());
        model.addAttribute("endDate", end.toString());
        model.addAttribute("revenueByDate", revenueByDate);
        model.addAttribute("monthlyList", monthlyList);

        return "admin/reports/revenue";
    }

    // ==========================================
    // 3. THỐNG KÊ TẦN SUẤT SỬ DỤNG PHÒNG
    // ==========================================
    @GetMapping("/rooms")
    public String rooms(Model model) {
        List<Bill> allBills = billRepository.findAll();
        List<Room> allRooms = roomRepository.findAll();

        List<RoomReportRow> roomReport = calculateRoomPopularity(allRooms, allBills);
        roomReport.sort(Comparator.comparingLong(RoomReportRow::getCheckInCount).reversed());

        model.addAttribute("roomReport", roomReport);
        return "admin/reports/rooms";
    }

    // ==========================================
    // 4. THỐNG KÊ SẢN PHẨM BÁN CHẠY
    // ==========================================
    @GetMapping("/products")
    public String products(Model model) {
        List<RoomServiceOrder> allOrders = roomServiceOrderRepository.findAll();

        List<ProductReportRow> productReport = calculateProductSales(allOrders);
        productReport.sort(Comparator.comparingLong(ProductReportRow::getQuantitySold).reversed());

        model.addAttribute("productReport", productReport);
        return "admin/reports/products";
    }

    // ==========================================
    // 5. XUẤT BÁO CÁO RA FILE EXCEL (CSV UTF-8 BOM)
    // ==========================================
    @GetMapping("/export")
    public void exportCsv(
            @RequestParam String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) throws IOException {

        response.setContentType("text/csv; charset=UTF-8");
        
        PrintWriter writer = response.getWriter();
        // Ghi UTF-8 BOM để Excel hiển thị đúng tiếng Việt có dấu
        writer.write('\ufeff');

        if ("revenue".equalsIgnoreCase(type)) {
            response.setHeader("Content-Disposition", "attachment; filename=\"Bao_Cao_Doanh_Thu_" + LocalDate.now() + ".csv\"");
            writer.println("Mã Hóa Đơn,Thời Gian Thanh Toán,Phương Thức,Tiền Phòng,Tiền Dịch Vụ,Phụ Thu,Tổng Tiền");
            
            List<Bill> allBills = billRepository.findAll();
            LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
            LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : LocalDate.now();
            LocalDateTime startLDT = start.atStartOfDay();
            LocalDateTime endLDT = end.atTime(LocalTime.MAX);

            List<Bill> filteredBills = allBills.stream()
                    .filter(b -> "PAID".equalsIgnoreCase(b.getStatus()) && b.getPaymentTime() != null 
                            && !b.getPaymentTime().isBefore(startLDT) && !b.getPaymentTime().isAfter(endLDT))
                    .sorted(Comparator.comparing(Bill::getPaymentTime).reversed())
                    .collect(Collectors.toList());

            for (Bill b : filteredBills) {
                double roomReal = b.getRoomPriceReal() != null ? b.getRoomPriceReal() + (b.getOvertimePriceReal() != null ? b.getOvertimePriceReal() : 0.0) : 0.0;
                double serviceReal = b.getServicePriceReal() != null ? b.getServicePriceReal() : 0.0;
                double surchargeReal = b.getSurchargeReal() != null ? b.getSurchargeReal() : 0.0;
                
                writer.println(String.format("%s,%s,%s,%.0f,%.0f,%.0f,%.0f",
                        b.getBillCode(),
                        b.getPaymentTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        b.getPaymentMethod(),
                        roomReal,
                        serviceReal,
                        surchargeReal,
                        b.getFinalAmount()
                ));
            }
        } 
        else if ("rooms".equalsIgnoreCase(type)) {
            response.setHeader("Content-Disposition", "attachment; filename=\"Bao_Cao_Tan_Suat_Phong_" + LocalDate.now() + ".csv\"");
            writer.println("Tên Phòng,Loại Phòng,Số Lượt Thuê,Tổng Số Giờ Sử Dụng,Doanh Thu Đóng Góp (VNĐ)");

            List<Bill> allBills = billRepository.findAll();
            List<Room> allRooms = roomRepository.findAll();
            List<RoomReportRow> rows = calculateRoomPopularity(allRooms, allBills);
            rows.sort(Comparator.comparingLong(RoomReportRow::getCheckInCount).reversed());

            for (RoomReportRow r : rows) {
                writer.println(String.format("%s,%s,%d,%.2f,%.0f",
                        r.getRoomName(),
                        r.getRoomType(),
                        r.getCheckInCount(),
                        r.getTotalHours(),
                        r.getTotalRevenue()
                ));
            }
        } 
        else if ("products".equalsIgnoreCase(type)) {
            response.setHeader("Content-Disposition", "attachment; filename=\"Bao_Cao_San_Pham_" + LocalDate.now() + ".csv\"");
            writer.println("Tên Sản Phẩm,Danh Mục Dịch Vụ,Số Lượng Bán,Tổng Doanh Số (VNĐ)");

            List<RoomServiceOrder> allOrders = roomServiceOrderRepository.findAll();
            List<ProductReportRow> rows = calculateProductSales(allOrders);
            rows.sort(Comparator.comparingLong(ProductReportRow::getQuantitySold).reversed());

            for (ProductReportRow p : rows) {
                writer.println(String.format("%s,%s,%d,%.0f",
                        p.getProductName(),
                        p.getCategoryName(),
                        p.getQuantitySold(),
                        p.getTotalRevenue()
                ));
            }
        }

        writer.flush();
        writer.close();
    }

    // ==========================================
    // CÁC HÀM TÍNH TOÁN PHỤ
    // ==========================================

    private List<RoomReportRow> calculateRoomPopularity(List<Room> allRooms, List<Bill> allBills) {
        Map<Long, RoomReportRow> reportMap = new HashMap<>();
        
        // Khởi tạo tất cả các phòng để đảm bảo các phòng có 0 lượt thuê vẫn hiển thị
        for (Room r : allRooms) {
            String typeName = r.getRoomType() != null ? r.getRoomType().getName() : "Không xác định";
            reportMap.put(r.getId(), new RoomReportRow(r.getRoomName(), typeName, 0, 0.0, 0.0));
        }

        for (Bill b : allBills) {
            if ("PAID".equalsIgnoreCase(b.getStatus()) && b.getCheckIn() != null && b.getCheckIn().getRoom() != null) {
                Room room = b.getCheckIn().getRoom();
                RoomReportRow row = reportMap.get(room.getId());
                if (row != null) {
                    row.setCheckInCount(row.getCheckInCount() + 1);
                    double hours = b.getCheckIn().getTotalHours() != null ? b.getCheckIn().getTotalHours() : 0.0;
                    row.setTotalHours(row.getTotalHours() + hours);
                    row.setTotalRevenue(row.getTotalRevenue() + b.getFinalAmount());
                }
            }
        }

        return new ArrayList<>(reportMap.values());
    }

    private List<ProductReportRow> calculateProductSales(List<RoomServiceOrder> allOrders) {
        Map<Long, ProductReportRow> reportMap = new HashMap<>();

        for (RoomServiceOrder o : allOrders) {
            if ("DELIVERED".equalsIgnoreCase(o.getStatus()) && o.getProduct() != null) {
                Product p = o.getProduct();
                ProductReportRow row = reportMap.get(p.getId());
                String catName = p.getCategory() != null ? p.getCategory().getName() : "Không xác định";
                if (row == null) {
                    row = new ProductReportRow(p.getName(), catName, 0, 0.0);
                    reportMap.put(p.getId(), row);
                }
                row.setQuantitySold(row.getQuantitySold() + o.getQuantity());
                row.setTotalRevenue(row.getTotalRevenue() + (o.getTotalPrice() != null ? o.getTotalPrice() : 0.0));
            }
        }

        return new ArrayList<>(reportMap.values());
    }

    // ==========================================
    // CÁC LỚP BÁO CÁO PHỤ (ROWS DTO)
    // ==========================================

    public static class RoomReportRow {
        private String roomName;
        private String roomType;
        private long checkInCount;
        private double totalHours;
        private double totalRevenue;

        public RoomReportRow(String roomName, String roomType, long checkInCount, double totalHours, double totalRevenue) {
            this.roomName = roomName;
            this.roomType = roomType;
            this.checkInCount = checkInCount;
            this.totalHours = totalHours;
            this.totalRevenue = totalRevenue;
        }

        public String getRoomName() { return roomName; }
        public void setRoomName(String roomName) { this.roomName = roomName; }
        public String getRoomType() { return roomType; }
        public void setRoomType(String roomType) { this.roomType = roomType; }
        public long getCheckInCount() { return checkInCount; }
        public void setCheckInCount(long checkInCount) { this.checkInCount = checkInCount; }
        public double getTotalHours() { return totalHours; }
        public void setTotalHours(double totalHours) { this.totalHours = totalHours; }
        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    }

    public static class ProductReportRow {
        private String productName;
        private String categoryName;
        private long quantitySold;
        private double totalRevenue;

        public ProductReportRow(String productName, String categoryName, long quantitySold, double totalRevenue) {
            this.productName = productName;
            this.categoryName = categoryName;
            this.quantitySold = quantitySold;
            this.totalRevenue = totalRevenue;
        }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public long getQuantitySold() { return quantitySold; }
        public void setQuantitySold(long quantitySold) { this.quantitySold = quantitySold; }
        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    }
}
