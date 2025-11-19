package personal_expense_tracker_com.example.personal_expense_tracker.controller.income;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.Category;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.Income;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.service.category.CategoryService;
import personal_expense_tracker_com.example.personal_expense_tracker.service.income.IncomeService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/income")
public class IncomeController {

    private static final Logger logger = LoggerFactory.getLogger(IncomeController.class);
    private final IncomeService incomeService;
    private final CategoryService categoryService;

    @Autowired
    public IncomeController(IncomeService incomeService, CategoryService categoryService) {
        this.incomeService = incomeService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<Map<String, List<Income>>> getIncomesGroupedByCategory(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<Income> incomes = incomeService.findAllByUser(currentUser);
        
        Map<String, List<Income>> groupedIncomes = new HashMap<>();

        for (Income income : incomes) {
            String categoryName = income.getCategory() != null ? income.getCategory().getCategoryName() : "Uncategorized";
            if (!groupedIncomes.containsKey(categoryName)) {
                groupedIncomes.put(categoryName, new ArrayList<>());
            }
            groupedIncomes.get(categoryName).add(income);
        }

        return ResponseEntity.ok(groupedIncomes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Income> getIncomeById(@PathVariable("id") Long id, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        Income income = incomeService.getIncomeByIdAndUser(id, currentUser);
        if (income != null) {
            return new ResponseEntity<>(income, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<Income> createIncome(@RequestBody Income income, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        
        logger.debug("Received Income with category ID: {}", 
            income.getCategory() != null ? income.getCategory().getId() : "null");
        
        // 1. Validate category exists
        if (income.getCategory() == null || income.getCategory().getId() == null) {
            logger.error("Income creation failed: No category provided");
            return ResponseEntity.badRequest().build();
        }
        
        // 2. Fetch and attach the full category
        Category category = categoryService.getCategoryByIdAndUser(
            income.getCategory().getId(), 
            currentUser
        );
        
        if (category == null) {
            logger.error("Category not found: {}", income.getCategory().getId());
            return ResponseEntity.badRequest().build();
        }
        
        // 3. Reconstruct the Income with proper relationships
        Income newIncome = new Income(
            income.getAmount(),
            income.getDescription(),
            income.getDate(),
            category,  // Use the fetched category
            currentUser
        );
        
        Income savedIncome = incomeService.addIncome(newIncome);
        logger.debug("Created income with ID: {} and category: {}", 
            savedIncome.getId(), savedIncome.getCategory().getCategoryName());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedIncome);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Income> updateIncome(@PathVariable("id") Long id, @RequestBody Income updatedIncome, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        
        logger.debug("Updating Income with ID: {} and category ID: {}", 
            id, updatedIncome.getCategory() != null ? updatedIncome.getCategory().getId() : "null");
        
        try {
            // If category is provided, validate it exists
            if (updatedIncome.getCategory() != null && updatedIncome.getCategory().getId() != null) {
                Category category = categoryService.getCategoryByIdAndUser(
                    updatedIncome.getCategory().getId(), 
                    currentUser
                );
                
                if (category == null) {
                    logger.error("Category not found for update: {}", updatedIncome.getCategory().getId());
                    return ResponseEntity.badRequest().build();
                }
                
                updatedIncome.setCategory(category);
            }
            
            Income income = incomeService.updateIncome(id, updatedIncome, currentUser);
            return new ResponseEntity<>(income, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update income: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable("id") Long id, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        try {
            incomeService.removeIncome(id, currentUser);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/category/{name}")
    public List<Income> getIncomesByCategory(@PathVariable("name") String categoryName, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        return incomeService.findByCategoryNameAndUser(categoryName, currentUser);
    }

    @GetMapping("/total")
    public Double getTotalIncomes(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        return incomeService.getTotalIncomes(currentUser);
    }

    @GetMapping("/top-5-recent")
    public List<Income> getTop5RecentIncomes(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        return incomeService.findTop5ByUserOrderByDateDesc(currentUser);
    }
}
