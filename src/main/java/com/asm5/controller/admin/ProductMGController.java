package com.asm5.controller.admin;

import java.io.IOException;
import java.nio.file.*;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.asm5.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.asm5.model.Product;
import com.asm5.repository.ProductRepository;
import com.asm5.repository.UnitRepository;

@Controller
@RequestMapping("/admin/product")
public class ProductMGController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private UnitRepository unitRepository;


    @Value("${upload.dir:src/main/resources/static/img/}")
    private String UPLOAD_DIR;

    // ✅ THÊM INITBINDER Ở ĐÂY
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("image");
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products); // 👈 Đặt là "products"
        return "index";
    }



    @GetMapping("/list/{pageNo}")
public String list(Model model,
                   @PathVariable("pageNo") Integer pageNo,
                   @RequestParam(value = "keyword", required = false) String keyword) {

    Sort sort = Sort.by(Sort.Direction.DESC, "id");
    Pageable pageable = PageRequest.of(Math.max(pageNo - 1, 0), 10, sort);

    Page<Product> page;
 if (keyword != null && !keyword.trim().isEmpty()) {
    page = productRepository.findByNameContainingIgnoreCaseAndQuantityGreaterThanAndActivedTrue(
            keyword.trim(), 0, pageable);
    model.addAttribute("keyword", keyword);
} else {
    page = productRepository.findAll(pageable);
}


    model.addAttribute("currentPage", pageNo);
    model.addAttribute("totalPages", page.getTotalPages());
    model.addAttribute("list", page.getContent());

    return "admin/product/list";
}



    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("units", unitRepository.findAll()); // thêm dòng này
        return "admin/product/add";
    }


    @PostMapping("/insert")
    public String insert(@Validated @ModelAttribute("product") Product product,
                         BindingResult result,
                         @RequestParam("image") MultipartFile imageFile,
                         Model model,
                         RedirectAttributes redirectAttributes) {
    	if (result.hasErrors()) {
    	    System.out.println("Có lỗi xảy ra:");
    	    result.getAllErrors().forEach(e -> System.out.println(" - " + e.getDefaultMessage()));
    	    model.addAttribute("categories", categoryRepository.findAll());
    	    model.addAttribute("units", unitRepository.findAll());
    	    return "admin/product/add";
    	}


        try {
            if (!imageFile.isEmpty()) {
                String fileName = saveImage(imageFile);
                product.setImage(fileName);
            } else {
                product.setImage("default.png");
            }

            if (product.getCreatedDate() == null) {
                product.setCreatedDate(new Date());
            }

            productRepository.save(product);
            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công!");
            return "redirect:/admin/product/list/1";

        } catch (IOException e) {
            model.addAttribute("error", "Lỗi khi lưu ảnh: " + e.getMessage());
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("units", unitRepository.findAll());
            return "admin/product/add";
        }
    }
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("units", unitRepository.findAll());
            return "admin/product/edit"; // 🟢 trỏ tới view edit.html
        } else {
            return "redirect:/admin/product/list/1";
        }
    }




    @PostMapping("/update")
    public String update(@ModelAttribute("product") Product product,
                         @RequestParam("image") MultipartFile imageFile,
                         Model model) {
        try {
            Optional<Product> opt = productRepository.findById(product.getId());
            if (opt.isEmpty()) {
                model.addAttribute("error", "Sản phẩm không tồn tại");
                return "admin/product/edit";
            }

            Product existing = opt.get();
            existing.setName(product.getName());
            existing.setPrice(product.getPrice());
            existing.setQuantity(product.getQuantity());
            existing.setCategory(product.getCategory());
            existing.setActived(product.getActived());

            if (!imageFile.isEmpty()) {
                deleteImage(existing.getImage()); // xóa ảnh cũ
                String fileName = saveImage(imageFile);
                existing.setImage(fileName);
            }

            productRepository.save(existing);
            return "redirect:/admin/product/list/1";
        } catch (IOException e) {
            model.addAttribute("error", "Cập nhật thất bại: " + e.getMessage());
            return "admin/product/edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        Optional<Product> opt = productRepository.findById(id);
        if (opt.isPresent()) {
            Product p = opt.get();
            deleteImage(p.getImage()); // xóa ảnh nếu có
            productRepository.delete(p);
        }
        return "redirect:/admin/product/list/1";
    }

    private String saveImage(MultipartFile imageFile) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    private void deleteImage(String fileName) {
        if (fileName == null || fileName.isEmpty()) return;
        try {
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Không thể xóa ảnh: " + e.getMessage());
        }
    }
}
