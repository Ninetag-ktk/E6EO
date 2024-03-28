package e6eo.finalproject.controller;

import e6eo.finalproject.dao.CategoryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryDAO categoryDAO;

    @PostMapping("") // 프론트에서 카테고리 목록 요청 시 데이터 리턴
    // 옵저브 토큰 => 카테고리 목록
    public ResponseEntity<?> getCategories(@RequestBody String observe) {
        Map<String, String> categories = categoryDAO.categoriesGet(observe);
        return ResponseEntity.ok(categories);
    }

    @PatchMapping("/category")
    public void updateCategory(@RequestBody Map<String, String> request) {
        categoryDAO.updateCategory(request);
    }

    @DeleteMapping("/category")
    public void deleteCategory(@RequestBody Map<String, String> request) {
        System.out.println("체크");
        categoryDAO.deleteCategory(request);
    }
}
