package com.example.demo.service;

import com.example.demo.model.InventoryTransaction;
import com.example.demo.model.Product;
import com.example.demo.repository.InventoryTransactionRepository;
import com.example.demo.repository.ProductRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryTransactionRepository transactionRepository;

    // Thay đổi dùng Constructor Injection thay cho @Autowired cũ để tối ưu hiệu năng và dễ mở rộng
    public ProductService(ProductRepository productRepository,
                          InventoryTransactionRepository transactionRepository) {
        this.productRepository = productRepository;
        this.transactionRepository = transactionRepository;
    }

    // Chỉ lấy các sản phẩm đang có active = true ra trang quản trị và thực đơn
    public List<Product> getAllProducts() {
        return productRepository.findByActiveTrue();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm có ID: " + id));
    }

    @Transactional
    public Product saveProduct(Product product) {
        // TỰ ĐỘNG CHUẨN HÓA: Cắt bỏ khoảng trắng thừa ở hai đầu tên sản phẩm trước khi xử lý
        if (product.getName() != null) {
            product.setName(product.getName().trim());
        }

        // TỐI ƯU LOGIC CHECK TRÙNG: Chỉ tìm các sản phẩm trùng tên mà ĐANG HOẠT ĐỘNG (active = true)
        Optional<Product> activeProductOpt = productRepository.findByNameAndActiveTrue(product.getName());

        if (activeProductOpt.isPresent()) {
            Product activeProduct = activeProductOpt.get();

            // Trường hợp 1: Thêm mới (id == null) nhưng tên này đã có một món khác đang bán
            if (product.getId() == null) {
                throw new RuntimeException("Tên sản phẩm này đang tồn tại và đang được bán trong menu!");
            }
            // Trường hợp 2: Cập nhật sửa đổi (id != null), kiểm tra xem có trùng với ID của chính nó không
            else if (!activeProduct.getId().equals(product.getId())) {
                throw new RuntimeException("Tên sản phẩm này đã bị trùng với một sản phẩm khác đang hoạt động!");
            }
        }

        return productRepository.save(product);
    }

    // Chuyển logic từ xóa cứng sang ẩn sản phẩm (Xóa mềm)
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        if (product != null) {
            product.setActive(false); // Đánh dấu ẩn món ăn khỏi hệ thống
            productRepository.save(product); // Cập nhật trạng thái xuống DB an toàn không lo xích khóa ngoại
        }
    }

    // NÂNG CẤP FEATURE 43: Tự động ghi nhận lịch sử giao dịch EXPORT khi trừ kho thành công
    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        Product product = getProductById(productId);

        if (product == null || !product.getActive()) {
            throw new RuntimeException("Sản phẩm không tồn tại hoặc đã ngừng kinh doanh!");
        }

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Sản phẩm '" + product.getName() + "' trong kho không đủ số lượng phục vụ (Hiện còn: " + product.getStockQuantity() + ")!");
        }

        // 1. Thực hiện trừ kho của sản phẩm
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        // 2. TỰ ĐỘNG XUẤT KHO: Tạo bản ghi log giao dịch xuất kho để lưu vết
        InventoryTransaction tx = new InventoryTransaction();
        tx.setProduct(product);
        tx.setTransactionType("EXPORT"); // Đánh dấu loại giao dịch là XUẤT KHO
        tx.setQuantity(quantity);
        tx.setPrice(product.getPrice()); // Lưu giá bán tại thời điểm xuất
        tx.setNote("Hệ thống tự động trừ kho do khách đặt món dịch vụ");

        // Lấy tên tài khoản nhân viên/khách hàng thực hiện thao tác
        try {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            tx.setOperator(currentUsername);
        } catch (Exception e) {
            tx.setOperator("System"); // Phòng hờ nếu hệ thống tự chạy không có context đăng nhập
        }

        transactionRepository.save(tx);
    }
}