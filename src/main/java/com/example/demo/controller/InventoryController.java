package com.example.demo.controller;

import com.example.demo.model.InventoryTransaction;
import com.example.demo.model.Product;
import com.example.demo.model.Supplier;
import com.example.demo.repository.InventoryTransactionRepository;
import com.example.demo.repository.SupplierRepository;
import com.example.demo.service.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/inventory")
public class InventoryController {

    private final ProductService productService;
    private final SupplierRepository supplierRepository;
    private final InventoryTransactionRepository transactionRepository;

    public InventoryController(ProductService productService,
                               SupplierRepository supplierRepository,
                               InventoryTransactionRepository transactionRepository) {
        this.productService = productService;
        this.supplierRepository = supplierRepository;
        this.transactionRepository = transactionRepository;
    }

    // FEATURE 41: Xem tồn kho hiện tại + Đổ dữ liệu phục vụ nút Nhập kho
    @GetMapping("/stock")
    @PreAuthorize("hasAnyAuthority('Admin_Service', 'Staff')")
    public String viewStock(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        // Lấy danh sách nhà cung cấp đang hoạt động để chọn khi nhập kho
        model.addAttribute("suppliers", supplierRepository.findByActiveTrue());
        return "admin/inventory/stock";
    }

    // FEATURE 42: Xử lý Nhập kho
    @PostMapping("/import")
    @PreAuthorize("hasAnyAuthority('Admin_Service', 'Staff')")
    public String importStock(@RequestParam("productId") Long productId,
                              @RequestParam("supplierId") Long supplierId,
                              @RequestParam("quantity") Integer quantity,
                              @RequestParam("price") Double price,
                              @RequestParam(value = "note", required = false) String note,
                              RedirectAttributes redirectAttributes) {
        try {
            // 1. Lấy thông tin sản phẩm và nhà cung cấp
            Product product = productService.getProductById(productId);
            Supplier supplier = supplierRepository.findById(supplierId)
                    .orElseThrow(() -> new RuntimeException("Nhà cung cấp không tồn tại"));

            if (quantity <= 0) {
                throw new RuntimeException("Số lượng nhập kho phải lớn hơn 0");
            }

            // 2. Cập nhật số lượng tồn kho của sản phẩm (Tăng kho)
            product.setStockQuantity(product.getStockQuantity() + quantity);
            productService.saveProduct(product); // Lưu lại thay đổi vào database

            // 3. Ghi log lịch sử giao dịch kho
            InventoryTransaction tx = new InventoryTransaction();
            tx.setProduct(product);
            tx.setSupplier(supplier);
            tx.setTransactionType("IMPORT");
            tx.setQuantity(quantity);
            tx.setPrice(price);
            tx.setNote(note);

            // Lấy tên tài khoản nhân viên đang đăng nhập để lưu vết
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            tx.setOperator(currentUsername);

            transactionRepository.save(tx);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Nhập kho thành công sản phẩm: " + product.getName() + " (+" + quantity + ")");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi nhập kho: " + e.getMessage());
        }
        return "redirect:/admin/inventory/stock";
    }

    // =========================================================================
    // [THÊM MỚI] FEATURE 45: ĐIỀU HƯỚNG XEM LỊCH SỬ GIAO DỊCH NHẬP XUẤT KHO
    // =========================================================================
    @GetMapping("/transactions")
    @PreAuthorize("hasAnyAuthority('Admin_Service', 'Staff')")
    public String viewTransactions(Model model) {
        // Đổ toàn bộ lịch sử giao dịch mới nhất lên đầu bảng
        model.addAttribute("transactions", transactionRepository.findAllByOrderByIdDesc());
        return "admin/inventory/transactions";
    }
}