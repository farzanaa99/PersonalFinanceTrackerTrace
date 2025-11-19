package personal_expense_tracker_com.example.personal_expense_tracker.service.category;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.Category;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.category.CategoryRepository;

@Service
public class CategoryService {
    
    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);
    private CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    public Category addCategory(Category category, User user) {
        // Ensure user is set
        if (category.getUser() == null) {
            category.setUser(user);
        }
        
        return categoryRepository.save(category);
    }
    

    public List<Category> getAllCategoriesByUser(User user) {
        return categoryRepository.findByUser(user);
    }

    public Category getCategoryByIdAndUser(Long id, User user) {
        return categoryRepository.findByIdAndUser(id, user).orElse(null);
    }

    public Category updateCategory(Long id, Category updatedCategory, User user) {
        Optional<Category> optionalCategory = categoryRepository.findByIdAndUser(id, user);
        if (optionalCategory.isPresent()) {
            Category existingCategory = optionalCategory.get();
            existingCategory.setCategoryName(updatedCategory.getCategoryName());
            existingCategory.setBudget(updatedCategory.getBudget()); 
            return categoryRepository.save(existingCategory);
        } else {
            return null;
        }
    }

    public void deleteCategory(Long id, User user) {
        Optional<Category> optionalCategory = categoryRepository.findByIdAndUser(id, user);
        if (optionalCategory.isPresent()) {
            categoryRepository.deleteById(id);
        } else {
            throw new NoSuchElementException("Category not found with ID: " + id + " for user");  
        }
    }

    public boolean existsByCategoryNameAndUser(String categoryName, User user){
        return categoryRepository.existsByCategoryNameAndUser(categoryName, user);
    }

    public Category findByCategoryNameAndUser(String categoryName, User user) {
        List<Category> categories = categoryRepository.findByCategoryNameAndUser(categoryName, user);
        
        if (categories.isEmpty()) {
            return null;
        }
        
        // Handle duplicates: log warning and return first match
        if (categories.size() > 1) {
            log.warn("Multiple categories found for name '{}' and user {}", categoryName, user.getId());
        }
        
        return categories.get(0);
    }

    /**
     * Find duplicate categories for a user
     * @param user The user to check for duplicates
     * @return List of category names that have duplicates
     */
    public List<String> findDuplicateCategoryNames(User user) {
        List<Category> allCategories = categoryRepository.findByUser(user);
        return allCategories.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                Category::getCategoryName,
                java.util.stream.Collectors.counting()
            ))
            .entrySet().stream()
            .filter(entry -> entry.getValue() > 1)
            .map(java.util.Map.Entry::getKey)
            .toList();
    }

    /**
     * Clean up duplicate categories for a user (keep the first one)
     * @param user The user whose duplicates should be cleaned
     * @return Number of duplicates removed
     */
    public int cleanupDuplicateCategories(User user) {
        List<String> duplicateNames = findDuplicateCategoryNames(user);
        int removedCount = 0;
        
        for (String categoryName : duplicateNames) {
            List<Category> duplicates = categoryRepository.findByCategoryNameAndUser(categoryName, user);
            if (duplicates.size() > 1) {
                // Keep the first one, delete the rest
                for (int i = 1; i < duplicates.size(); i++) {
                    categoryRepository.delete(duplicates.get(i));
                    removedCount++;
                }
                log.info("Removed {} duplicate categories for name '{}' and user {}", 
                    duplicates.size() - 1, categoryName, user.getId());
            }
        }
        
        return removedCount;
    }
}
