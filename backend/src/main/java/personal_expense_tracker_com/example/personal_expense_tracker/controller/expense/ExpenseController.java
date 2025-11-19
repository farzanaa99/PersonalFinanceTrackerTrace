package personal_expense_tracker_com.example.personal_expense_tracker.controller.expense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.Category;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.Expense;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.service.category.CategoryService;
import personal_expense_tracker_com.example.personal_expense_tracker.service.expense.ExpenseService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);
    private final ExpenseService expenseService;
    private final CategoryService categoryService;

    @Autowired
    public ExpenseController(ExpenseService expenseService, CategoryService categoryService) {
        this.expenseService = expenseService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<Map<String, List<Expense>>> getExpensesGroupedByCategory(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<Expense> expenses = expenseService.findAllByUser(currentUser);
        
        Map<String, List<Expense>> groupedExpenses = new HashMap<>();

        for (Expense expense : expenses) {
            String categoryName = expense.getCategory() != null ? expense.getCategory().getCategoryName() : "Uncategorized";
            if (!groupedExpenses.containsKey(categoryName)) {
                groupedExpenses.put(categoryName, new ArrayList<>());
            }
            groupedExpenses.get(categoryName).add(expense);
        }

        return ResponseEntity.ok(groupedExpenses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(@PathVariable("id") Long id, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        Optional<Expense> expense = expenseService.getExpenseByIdAndUser(id, currentUser);
        if (expense.isPresent()) {
            return new ResponseEntity<>(expense.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<Expense> createExpense(@RequestBody Expense expense, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        
        logger.debug("Received Expense with category ID: {}", 
            expense.getCategory() != null ? expense.getCategory().getId() : "null");
        
        // 1. Validate category exists
        if (expense.getCategory() == null || expense.getCategory().getId() == null) {
            logger.error("Expense creation failed: No category provided");
            return ResponseEntity.badRequest().build();
        }
        
        // 2. Fetch and attach the full category
        Category category = categoryService.getCategoryByIdAndUser(
            expense.getCategory().getId(),
            currentUser
        );
        
        if (category == null) {
            logger.error("Category not found: {}", expense.getCategory().getId());
            return ResponseEntity.badRequest().build();
        }
        
        // 3. Reconstruct the Expense with proper relationships
        Expense newExpense = new Expense(
            expense.getAmount(),
            expense.getDescription(),
            expense.getDate(),
            category,  // Use the fetched category
            currentUser
        );
        
        Expense savedExpense = expenseService.addExpense(newExpense);
        logger.debug("Created expense with ID: {} and category: {}", 
            savedExpense.getId(), savedExpense.getCategory().getCategoryName());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedExpense);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@PathVariable("id") Long id, @RequestBody Expense updatedExpense, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        
        logger.debug("Updating Expense with ID: {} and category ID: {}", 
            id, updatedExpense.getCategory() != null ? updatedExpense.getCategory().getId() : "null");
        
        try {
            // If category is provided, validate it exists
            if (updatedExpense.getCategory() != null && updatedExpense.getCategory().getId() != null) {
                Category category = categoryService.getCategoryByIdAndUser(
                    updatedExpense.getCategory().getId(),
                    currentUser
                );
                
                if (category == null) {
                    logger.error("Category not found for update: {}", updatedExpense.getCategory().getId());
                    return ResponseEntity.badRequest().build();
                }
                
                updatedExpense.setCategory(category);
            }
            
            Expense expense = expenseService.updateExpense(id, updatedExpense, currentUser);
            if (expense != null) {
                return new ResponseEntity<>(expense, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Failed to update expense: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable("id") Long id, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (expenseService.getExpenseByIdAndUser(id, currentUser).isPresent()) {
            expenseService.removeExpense(id, currentUser);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);         
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/category/{categoryName}")
    public List<Expense> getExpensesByCategory(@PathVariable("categoryName") String categoryName, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        return expenseService.findByCategoryNameAndUser(categoryName, currentUser);
    }

    @GetMapping("/amount/{amount}")
    public List<Expense> getExpensesByAmountGreaterThan(@PathVariable("amount") Double amount, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        return expenseService.findByAmountGreaterThanAndUser(amount, currentUser);
    }

    @GetMapping("/date")
    public List<Expense> getExpensesByDateRange(@RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        return expenseService.findByDateBetweenAndUser(start, end, currentUser);
    }

    @GetMapping("/latest")
    public List<Expense> getTop5LatestExpenses(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        return expenseService.findTop5ByUserOrderByDateDesc(currentUser);
    }

    @GetMapping("/total")
    public Double getTotalExpenses(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        return expenseService.getTotalExpenses(currentUser);
    }
}
