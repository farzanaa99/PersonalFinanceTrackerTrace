package personal_expense_tracker_com.example.personal_expense_tracker.controller.budget;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.*;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.Category;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.category.CategoryRepository;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class BudgetController {

    private final CategoryRepository categoryRepository;

    public BudgetController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/budgets")
    public Map<String, Double> getBudgets(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<Category> categories = categoryRepository.findByUser(currentUser);

        return categories.stream()
            .collect(Collectors.toMap(Category::getCategoryName, Category::getBudget));
    }
}
