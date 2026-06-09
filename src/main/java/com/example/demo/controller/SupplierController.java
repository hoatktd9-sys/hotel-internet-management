package com.example.demo.controller;

import com.example.demo.model.Supplier;
import com.example.demo.repository.SupplierRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/suppliers")
public class SupplierController {

    private final SupplierRepository supplierRepository;

    public SupplierController(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    // Hiển thị danh sách và khởi tạo đối tượng form
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String listSuppliers(Model model) {
        model.addAttribute("suppliers", supplierRepository.findByActiveTrue());
        if (!model.containsAttribute("supplier")) {
            model.addAttribute("supplier", new Supplier());
        }
        return "admin/inventory/manage-suppliers";
    }

    // Xử lý lưu dữ liệu (Thêm mới / Cập nhật) kèm Validation nâng cao
    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveSupplier(@Valid @ModelAttribute("supplier") Supplier supplier,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.supplier", result);
            redirectAttributes.addFlashAttribute("supplier", supplier);
            return "redirect:/admin/suppliers";
        }

        try {
            if (supplier.getName() != null) {
                supplier.setName(supplier.getName().trim());
            }

            // Xử lý logic chống trùng tên nhà cung cấp
            Optional<Supplier> existing = supplierRepository.findByName(supplier.getName());
            if (existing.isPresent()) {
                // Nếu là thêm mới HOẶC sửa nhưng trùng tên của một NCC khác ID
                if (supplier.getId() == null || !existing.get().getId().equals(supplier.getId())) {
                    // Nếu NCC đó bị xóa mềm trước đây, khôi phục lại thay vì báo lỗi
                    if (!existing.get().getActive()) {
                        Supplier reactiveSup = existing.get();
                        reactiveSup.setContactName(supplier.getContactName());
                        reactiveSup.setPhone(supplier.getPhone());
                        reactiveSup.setEmail(supplier.getEmail());
                        reactiveSup.setAddress(supplier.getAddress());
                        reactiveSup.setActive(true);
                        supplierRepository.save(reactiveSup);
                        redirectAttributes.addFlashAttribute("successMessage", "Khôi phục và cập nhật đối tác thành công!");
                        return "redirect:/admin/suppliers";
                    }
                    throw new RuntimeException("Tên nhà cung cấp này đã tồn tại trên hệ thống!");
                }
            }

            supplierRepository.save(supplier);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin nhà cung cấp thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("supplier", supplier);
        }
        return "redirect:/admin/suppliers";
    }

    // Đẩy dữ liệu vào form để tiến hành chỉnh sửa
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Supplier supplier = supplierRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đối tác yêu cầu."));
            model.addAttribute("supplier", supplier);
            model.addAttribute("suppliers", supplierRepository.findByActiveTrue());
            return "admin/inventory/manage-suppliers";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/suppliers";
        }
    }

    // Xóa mềm nhà cung cấp (chuyển active = false để tránh lỗi khóa ngoại integrity với bảng chứa phiếu nhập)
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteSupplier(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Supplier supplier = supplierRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Nhà cung cấp không tồn tại hoặc đã bị xóa trước đó."));
            supplier.setActive(false);
            supplierRepository.save(supplier);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa đối tác cung cấp thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/suppliers";
    }
}