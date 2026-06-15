package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudStorageService {

    private final Cloudinary cloudinary;

    public CloudStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Đẩy ảnh lên Cloudinary và trả về URL tuyệt đối của ảnh
     */
    public String uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            return null;
        }
        try {
            // Thực hiện upload lên thư mục đặt tên là "hotel_rooms" trên Cloud để dễ quản lý
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", "hotel_rooms"));

            // Lấy ra đường dẫn URL an toàn (https)
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Tải ảnh lên Cloud thất bại: " + e.getMessage());
        }
    }
}