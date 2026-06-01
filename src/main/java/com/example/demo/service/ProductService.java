package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

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

    // ĐIỂM 2: Hàm xử lý kiểm tra tồn kho và trừ kho khi khách đặt món (Feature 38)
    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        Product product = getProductById(productId);

        if (product == null || !product.getActive()) {
            throw new RuntimeException("Sản phẩm không tồn tại hoặc đã ngừng kinh doanh!");
        }

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Sản phẩm '" + product.getName() + "' trong kho không đủ số lượng phục vụ (Hiện còn: " + product.getStockQuantity() + ")!");
        }

        // Thực hiện trừ kho
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
    }
}