package personal_expense_tracker_com.example.personal_expense_tracker.service.savings;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.SavingsGoal;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.savings.SavingsGoalRepository;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.category.CategoryRepository;
import personal_expense_tracker_com.example.personal_expense_tracker.service.category.CategoryService;
import personal_expense_tracker_com.example.personal_expense_tracker.service.alert.SpendingAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class SavingsGoalService {

    private static final Logger logger = LoggerFactory.getLogger(SavingsGoalService.class);
    
    @Autowired
    private SavingsGoalRepository savingsGoalRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private SpendingAlertService spendingAlertService;

    public List<SavingsGoal> getAllActiveSavingsGoals(User user) {
        return savingsGoalRepository.findByUserAndIsActiveTrueOrderByTargetDateAsc(user);
    }

    public List<SavingsGoal> getCompletedGoals(User user) {
        return savingsGoalRepository.findCompletedGoalsForUser(user);
    }

    public List<SavingsGoal> getOverdueGoals(User user) {
        return savingsGoalRepository.findOverdueGoalsForUser(LocalDate.now(), user);
    }

    public List<SavingsGoal> getGoalsDueBetween(LocalDate startDate, LocalDate endDate, User user) {
        return savingsGoalRepository.findGoalsDueBetweenForUser(startDate, endDate, user);
    }

    public List<SavingsGoal> getSavingsGoalsByCategory(Long categoryId, User user) {
        logger.debug("Getting savings goals for category ID: {} and user: {}", categoryId, user.getId());
        
        if (categoryId == null) {
            logger.warn("Category ID is null, returning empty list");
            return new ArrayList<>();
        }
        
        try {
            List<SavingsGoal> goals = savingsGoalRepository.findByCategoryIdAndUser(categoryId, user);
            logger.debug("Found {} savings goals for category ID: {}", goals.size(), categoryId);
            return goals;
        } catch (Exception e) {
            logger.error("Error getting savings goals by category: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<SavingsGoal> getGoalsWithMinProgress(double minProgress, User user) {
        return savingsGoalRepository.findGoalsWithMinProgressForUser(minProgress, user);
    }

    public long getCompletedGoalsCount(User user) {
        return savingsGoalRepository.countCompletedGoalsForUser(user);
    }

    public long getActiveGoalsCount(User user) {
        return savingsGoalRepository.countActiveGoalsForUser(user);
    }

    public SavingsGoal createSavingsGoal(SavingsGoal savingsGoal) {
        logger.debug("Creating savings goal: name={}, targetAmount={}, category={}", 
            savingsGoal.getName(), savingsGoal.getTargetAmount(),
            savingsGoal.getCategory() != null ? savingsGoal.getCategory().getCategoryName() : "null");
        
        SavingsGoal saved = savingsGoalRepository.save(savingsGoal);
        logger.debug("Created savings goal with ID: {}", saved.getId());
        return saved;
    }

    public Optional<SavingsGoal> getSavingsGoalById(Long id) {
        return savingsGoalRepository.findById(id);
    }

    public SavingsGoal updateSavingsGoal(Long id, SavingsGoal updatedGoal) {
        logger.debug("Updating savings goal with ID: {} and category: {}", 
            id, updatedGoal.getCategory() != null ? updatedGoal.getCategory().getCategoryName() : "null");
        
        Optional<SavingsGoal> existing = savingsGoalRepository.findById(id);
        if (existing.isPresent()) {
            SavingsGoal goal = existing.get();
            goal.setName(updatedGoal.getName());
            goal.setDescription(updatedGoal.getDescription());
            goal.setTargetAmount(updatedGoal.getTargetAmount());
            goal.setCurrentAmount(updatedGoal.getCurrentAmount());
            goal.setTargetDate(updatedGoal.getTargetDate());
            goal.setCategory(updatedGoal.getCategory());
            
            SavingsGoal saved = savingsGoalRepository.save(goal);
            logger.debug("Updated savings goal with ID: {}", saved.getId());
            return saved;
        }
        logger.warn("Savings goal not found for update: {}", id);
        return null;
    }

    public void deactivateSavingsGoal(Long id) {
        Optional<SavingsGoal> goal = savingsGoalRepository.findById(id);
        if (goal.isPresent()) {
            SavingsGoal savingsGoal = goal.get();
            savingsGoal.setActive(false);
            savingsGoalRepository.save(savingsGoal);
        }
    }

    public SavingsGoal updateProgress(Long goalId, double additionalAmount) {
        Optional<SavingsGoal> goal = savingsGoalRepository.findById(goalId);
        if (goal.isPresent()) {
            SavingsGoal savingsGoal = goal.get();
            double newAmount = savingsGoal.getCurrentAmount() + additionalAmount;
            savingsGoal.setCurrentAmount(newAmount);
            return savingsGoalRepository.save(savingsGoal);
        }
        return null;
    }

    public void generateSavingsGoalAlerts(User user) {
        List<SavingsGoal> overdueGoals = getOverdueGoals(user);
        
        for (SavingsGoal goal : overdueGoals) {
            String message = String.format("Your savings goal '%s' is overdue! You need %.2f more to reach your target of %.2f", 
                                         goal.getName(), goal.getRemainingAmount(), goal.getTargetAmount());
            
            spendingAlertService.createAlert(
                "SAVINGS_GOAL_OVERDUE",
                message,
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getCategory(),
                user
            );
        }

        List<SavingsGoal> activeGoals = getAllActiveSavingsGoals(user);
        LocalDate today = LocalDate.now();
        
        for (SavingsGoal goal : activeGoals) {
            if (!goal.isCompleted() && goal.getDaysRemaining() > 0) {
                double dailyRequired = goal.getDailyRequiredAmount();
                double currentDailyRate = goal.getCurrentAmount() / 
                    Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(goal.getCreatedAt().toLocalDate(), today));
                
                if (dailyRequired > currentDailyRate * 1.5) { // 50% behind schedule
                    String message = String.format("Your savings goal '%s' is behind schedule. You need to save %.2f daily to reach your target", 
                                                 goal.getName(), dailyRequired);
                    
                    spendingAlertService.createAlert(
                        "SAVINGS_GOAL_BEHIND_SCHEDULE",
                        message,
                        dailyRequired,
                        currentDailyRate,
                        goal.getCategory(),
                        user
                    );
                }
            }
        }
    }

    public void checkGoalCompletion(User user) {
        List<SavingsGoal> activeGoals = getAllActiveSavingsGoals(user);
        
        for (SavingsGoal goal : activeGoals) {
            if (goal.isCompleted()) {
                String message = String.format("Congratulations! You've reached your savings goal '%s' of %.2f!", 
                                             goal.getName(), goal.getTargetAmount());
                
                spendingAlertService.createAlert(
                    "SAVINGS_GOAL_COMPLETED",
                    message,
                    goal.getTargetAmount(),
                    goal.getCurrentAmount(),
                    goal.getCategory(),
                    user
                );
            }
        }
    }

    public double getTotalSavingsProgress(User user) {
        List<SavingsGoal> activeGoals = getAllActiveSavingsGoals(user);
        if (activeGoals.isEmpty()) return 0.0;
        
        double totalTarget = activeGoals.stream().mapToDouble(SavingsGoal::getTargetAmount).sum();
        double totalCurrent = activeGoals.stream().mapToDouble(SavingsGoal::getCurrentAmount).sum();
        
        return totalTarget > 0 ? (totalCurrent / totalTarget) * 100 : 0.0;
    }

    public double getTotalSavingsAmount(User user) {
        return getAllActiveSavingsGoals(user).stream()
                .mapToDouble(SavingsGoal::getCurrentAmount)
                .sum();
    }

    public double getTotalTargetAmount(User user) {
        return getAllActiveSavingsGoals(user).stream()
                .mapToDouble(SavingsGoal::getTargetAmount)
                .sum();
    }
} 
