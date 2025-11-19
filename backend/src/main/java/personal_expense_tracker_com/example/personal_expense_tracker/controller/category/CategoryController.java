package personal_expense_tracker_com.example.personal_expense_tracker.controller.category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.Category;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.service.category.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<Category> getAllCategories(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        return categoryService.getAllCategoriesByUser(currentUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable("id") Long id, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        Category category = categoryService.getCategoryByIdAndUser(id, currentUser);
        if (category != null) {
            return new ResponseEntity<>(category, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");

        if (categoryService.existsByCategoryNameAndUser(category.getCategoryName(), currentUser)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        category.setUser(currentUser);

        Category createdCategory = categoryService.addCategory(category, currentUser);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable("id") Long id, @RequestBody Category updatedCategory, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        
        Category existingCategory = categoryService.getCategoryByIdAndUser(id, currentUser);
        if (existingCategory == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        if (!existingCategory.getCategoryName().equals(updatedCategory.getCategoryName())) {
            if (categoryService.existsByCategoryNameAndUser(updatedCategory.getCategoryName(), currentUser)) {
                return new ResponseEntity<>(HttpStatus.CONFLICT); 
            }
        }
        
        Category category = categoryService.updateCategory(id, updatedCategory, currentUser);
        if (category != null) {
            return new ResponseEntity<>(category, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long id, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        Category category = categoryService.getCategoryByIdAndUser(id, currentUser);
        if (category != null) {
            categoryService.deleteCategory(id, currentUser);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/exists/{categoryName}")
    public ResponseEntity<Boolean> doesCategoryExist(@PathVariable("categoryName") String categoryName, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        boolean exists = categoryService.existsByCategoryNameAndUser(categoryName, currentUser);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @GetMapping("/name/{categoryName}")
    public ResponseEntity<Category> getCategoryByName(@PathVariable("categoryName") String categoryName, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        Category category = categoryService.findByCategoryNameAndUser(categoryName, currentUser);
        if (category != null) {
            return new ResponseEntity<>(category, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/duplicates")
    public ResponseEntity<List<String>> findDuplicateCategories(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<String> duplicateNames = categoryService.findDuplicateCategoryNames(currentUser);
        return new ResponseEntity<>(duplicateNames, HttpStatus.OK);
    }

    @PostMapping("/cleanup-duplicates")
    public ResponseEntity<Map<String, Integer>> cleanupDuplicateCategories(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        int removedCount = categoryService.cleanupDuplicateCategories(currentUser);
        return new ResponseEntity<>(Map.of("removedCount", removedCount), HttpStatus.OK);
    }
}
