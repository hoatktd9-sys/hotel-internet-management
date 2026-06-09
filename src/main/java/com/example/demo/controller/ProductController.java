package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.model.Room;
import com.example.demo.model.RoomServiceOrder;
import com.example.demo.repository.RoomServiceOrderRepository;
import com.example.demo.repository.RoomRepository;
import com.example.demo.repository.ServiceCategoryRepository;
import com.example.demo.service.ProductService;
import com.example.demo.service.ActivityLogService; // IMPORT SERVICE MỚI
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
public class ProductController {

    private final ProductService productService;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final RoomServiceOrderRepository roomServiceOrderRepository;
    private final RoomRepository roomRepository;
    private final ActivityLogService activityLogService; // INJECT SERVICE MỚI

    public ProductController(
            ProductService productService,
            ServiceCategoryRepository serviceCategoryRepository,
            RoomServiceOrderRepository roomServiceOrderRepository,
            RoomRepository roomRepository,
            ActivityLogService activityLogService // CẬP NHẬT CONSTRUCTOR
    ) {
        this.productService = productService;
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.roomServiceOrderRepository = roomServiceOrderRepository;
        this.roomRepository = roomRepository;
        this.activityLogService = activityLogService;
    }

    // 1. Hiển thị danh sách sản phẩm & Form thêm mới
    @GetMapping
    @PreAuthorize("hasAuthority('Admin_Service')")
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", serviceCategoryRepository.findAll());
        if (!model.containsAttribute("product")) {
            model.addAttribute("product", new Product());
        }
        return "../templates/admin/product/manage-products";
    }

    // 2. Xử lý Thêm mới hoặc Cập nhật sản phẩm
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('Admin_Service')")
    public String saveProduct(@Valid @ModelAttribute("product") Product product,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.product", result);
            redirectAttributes.addFlashAttribute("product", product);
            return "redirect:/admin/products";
        }

        try {
            if (product.getImage() == null || product.getImage().trim().isEmpty()) {
                product.setImage("default-product.png");
            }
            product.setActive(true);
            productService.saveProduct(product);

            // Ghi log hành động
            activityLogService.log("PRODUCT_SAVE", "Cập nhật/Thêm sản phẩm: " + product.getName());

            redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin sản phẩm vào thực đơn thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("product", product);
        }
        return "redirect:/admin/products";
    }

    // 3. Chuẩn bị dữ liệu để sửa sản phẩm
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('Admin_Service')")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        try {
            Product product = productService.getProductById(id);
            model.addAttribute("product", product);
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("categories", serviceCategoryRepository.findAll());
            return "../templates/admin/product/manage-products";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/products";
        }
    }

    // 4. Xử lý Xóa sản phẩm
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('Admin_Service')")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            activityLogService.log("PRODUCT_DELETE", "Đã xóa sản phẩm ID: " + id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa món ăn khỏi thực đơn thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // 5. Xem menu dịch vụ công khai
    @GetMapping("/menu")
    @PreAuthorize("hasAnyAuthority('Admin_Service', 'Staff', 'ROLE_USER')")
    public String showPublicMenu(@RequestParam(value = "roomId", required = false) Long roomId, Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", serviceCategoryRepository.findAll());
        model.addAttribute("currentRoomId", roomId);
        return "../templates/admin/product/view-menu";
    }

    // 6. Xử lý lưu thông tin khách hàng gọi món từ Menu
    @PostMapping("/order")
    @PreAuthorize("hasAnyAuthority('Admin_Service', 'Staff', 'ROLE_USER')")
    public String processRoomOrder(
            @RequestParam("productId") Long productId,
            @RequestParam("roomId") Long roomId,
            @RequestParam("quantity") Integer quantity,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Room room = roomRepository.findById(roomId).orElse(null);
            if (room == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Phòng không tồn tại!");
                return "redirect:/rooms";
            }

            Product product = productService.getProductById(productId);

            // Kiểm tra kho và trừ kho
            productService.decreaseStock(productId, quantity);

            // Lưu đơn đặt dịch vụ
            RoomServiceOrder order = new RoomServiceOrder();
            order.setRoom(room);
            order.setProduct(product);
            order.setQuantity(quantity);
            order.setTotalPrice(product.getPrice() * quantity);
            order.setStatus("PENDING");

            roomServiceOrderRepository.save(order);

            // [LOG CHÈN THÊM]: Ghi nhận hành động gọi món dịch vụ
            activityLogService.log("ORDER_SERVICE", "Đặt " + quantity + " x " + product.getName() + " cho phòng " + room.getRoomName());

            redirectAttributes.addFlashAttribute("successMessage", "Gọi món cho phòng " + room.getRoomName() + " thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/products/menu?roomId=" + roomId;
        }

        return "redirect:/rooms";
    }
}