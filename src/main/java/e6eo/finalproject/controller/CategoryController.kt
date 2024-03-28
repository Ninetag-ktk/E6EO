package e6eo.finalproject.controller

import e6eo.finalproject.dao.CategoryDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories")
class CategoryController {
    @Autowired
    private lateinit var categoryDAO: CategoryDAO

    @PostMapping("") // 프론트에서 카테고리 목록 요청 시 데이터 리턴
    // 옵저브 토큰 => 카테고리 목록
    fun getCategories(@RequestBody observe: String): ResponseEntity<*> {
        val categories = categoryDAO.categoriesGet(observe)
        return ResponseEntity.ok(categories)
    }

    @PatchMapping("/category")
    fun updateCategory(@RequestBody request: Map<String, String>) {
        categoryDAO.updateCategory(request)
    }

    @DeleteMapping("/category")
    fun deleteCategory(@RequestBody request: Map<String, String>) {
        println("체크")
        categoryDAO.deleteCategory(request)
    }
}
