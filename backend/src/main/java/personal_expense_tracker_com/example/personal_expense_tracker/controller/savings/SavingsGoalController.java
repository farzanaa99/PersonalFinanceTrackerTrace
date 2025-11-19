package personal_expense_tracker_com.example.personal_expense_tracker.controller.savings;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.Category;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.SavingsGoal;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.service.category.CategoryService;
import personal_expense_tracker_com.example.personal_expense_tracker.service.savings.SavingsGoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/savings-goals")
public class SavingsGoalController {

    private static final Logger logger = LoggerFactory.getLogger(SavingsGoalController.class);
    private final SavingsGoalService savingsGoalService;
    private final CategoryService categoryService;

    @Autowired
    public SavingsGoalController(SavingsGoalService savingsGoalService, CategoryService categoryService) {
        this.savingsGoalService = savingsGoalService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<SavingsGoal>> getAllActiveSavingsGoals(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<SavingsGoal> goals = savingsGoalService.getAllActiveSavingsGoals(currentUser);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<SavingsGoal>> getCompletedGoals(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<SavingsGoal> goals = savingsGoalService.getCompletedGoals(currentUser);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<SavingsGoal>> getOverdueGoals(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<SavingsGoal> goals = savingsGoalService.getOverdueGoals(currentUser);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<SavingsGoal>> getSavingsGoalsByCategory(@PathVariable Long categoryId, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<SavingsGoal> goals = savingsGoalService.getSavingsGoalsByCategory(categoryId, currentUser);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoal> getSavingsGoalById(@PathVariable Long id) {
        Optional<SavingsGoal> goal = savingsGoalService.getSavingsGoalById(id);
        return goal.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SavingsGoal> createSavingsGoal(@RequestBody SavingsGoal savingsGoal, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        
        logger.info("=== SAVINGS GOAL CREATION DEBUG ===");
        logger.info("Request body received: {}", savingsGoal);
        logger.info("Category object: {}", savingsGoal.getCategory());
        logger.info("Category ID: {}", savingsGoal.getCategory() != null ? savingsGoal.getCategory().getId() : "null");
        logger.info("User: {}", currentUser != null ? currentUser.getId() : "null");
        
        try {
            if (savingsGoal.getCategory() == null || savingsGoal.getCategory().getId() == null) {
                logger.error("SavingsGoal creation failed: No category provided");
                return ResponseEntity.badRequest().build();
            }
            
            Category category = categoryService.getCategoryByIdAndUser(
                savingsGoal.getCategory().getId(), 
                currentUser
            );
            
            logger.info("Fetched category: {}", category);
            
            if (category == null) {
                logger.error("Category not found: {}", savingsGoal.getCategory().getId());
                return ResponseEntity.badRequest().build();
            }
            
            SavingsGoal newSavingsGoal = new SavingsGoal(
                savingsGoal.getName(),
                savingsGoal.getDescription(),
                savingsGoal.getTargetAmount(),
                savingsGoal.getCurrentAmount(),
                savingsGoal.getTargetDate(),
                category,  
                currentUser
            );
            
            logger.info("Created new SavingsGoal object: {}", newSavingsGoal);
            
            SavingsGoal created = savingsGoalService.createSavingsGoal(newSavingsGoal);
            logger.info("Successfully created savings goal with ID: {} and category: {}", 
                created.getId(), created.getCategory().getCategoryName());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
            
        } catch (Exception e) {
            logger.error("Exception during savings goal creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoal> updateSavingsGoal(@PathVariable Long id, @RequestBody SavingsGoal updatedGoal, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        
        logger.debug("Updating SavingsGoal with ID: {} and category ID: {}", 
            id, updatedGoal.getCategory() != null ? updatedGoal.getCategory().getId() : "null");
        
        try {
            if (updatedGoal.getCategory() != null && updatedGoal.getCategory().getId() != null) {
                Category category = categoryService.getCategoryByIdAndUser(
                    updatedGoal.getCategory().getId(), 
                    currentUser
                );
                
                if (category == null) {
                    logger.error("Category not found for update: {}", updatedGoal.getCategory().getId());
                    return ResponseEntity.badRequest().build();
                }
                
                updatedGoal.setCategory(category);
            }
            
            SavingsGoal updated = savingsGoalService.updateSavingsGoal(id, updatedGoal);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Failed to update savings goal: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateSavingsGoal(@PathVariable Long id) {
        savingsGoalService.deactivateSavingsGoal(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<SavingsGoal> updateProgress(@PathVariable Long id, @RequestBody ProgressUpdateRequest request) {
        SavingsGoal updated = savingsGoalService.updateProgress(id, request.getAmount());
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/due-between")
    public ResponseEntity<List<SavingsGoal>> getGoalsDueBetween(
            @RequestParam LocalDate start, @RequestParam LocalDate end, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<SavingsGoal> goals = savingsGoalService.getGoalsDueBetween(start, end, currentUser);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/min-progress")
    public ResponseEntity<List<SavingsGoal>> getGoalsWithMinProgress(@RequestParam double minProgress, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<SavingsGoal> goals = savingsGoalService.getGoalsWithMinProgress(minProgress, currentUser);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/count/completed")
    public ResponseEntity<Long> getCompletedGoalsCount(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        long count = savingsGoalService.getCompletedGoalsCount(currentUser);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/active")
    public ResponseEntity<Long> getActiveGoalsCount(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        long count = savingsGoalService.getActiveGoalsCount(currentUser);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/generate-alerts")
    public ResponseEntity<Void> generateSavingsGoalAlerts(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        savingsGoalService.generateSavingsGoalAlerts(currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check-completion")
    public ResponseEntity<Void> checkGoalCompletion(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        savingsGoalService.checkGoalCompletion(currentUser);
        return ResponseEntity.ok().build();
    }

    public static class ProgressUpdateRequest {
        private double amount;

        public ProgressUpdateRequest() {}

        public ProgressUpdateRequest(double amount) {
            this.amount = amount;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }
} 
