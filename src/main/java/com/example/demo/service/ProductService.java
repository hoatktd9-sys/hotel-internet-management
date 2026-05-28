package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm có ID: " + id));
    }

    public Product saveProduct(Product product) {
        // Kiểm tra trùng tên: Nếu là thêm mới (id == null) hoặc đổi tên trùng với sản phẩm khác
        if (productRepository.existsByName(product.getName())) {
            if (product.getId() == null) {
                throw new RuntimeException("Tên sản phẩm này đã tồn tại trong menu!");
            } else {
                // Trường hợp cập nhật: Kiểm tra xem tên đó có phải của sản phẩm khác không
                Product existingProduct = productRepository.findById(product.getId()).orElse(null);
                if (existingProduct != null && !existingProduct.getName().equals(product.getName())) {
                    throw new RuntimeException("Tên sản phẩm này đã bị trùng với một sản phẩm khác!");
                }
            }
        }
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Sản phẩm không tồn tại hoặc đã bị xóa trước đó!");
        }
        productRepository.deleteById(id);
    }
}