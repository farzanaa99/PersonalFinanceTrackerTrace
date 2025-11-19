package personal_expense_tracker_com.example.personal_expense_tracker.service.alert;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.SpendingAlert;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.Category;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.Expense;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.alert.SpendingAlertRepository;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.expense.ExpenseRepository;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.category.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Service
public class SpendingAlertService {

    @Autowired
    private SpendingAlertRepository spendingAlertRepository;
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;

    public List<SpendingAlert> getUnreadAlerts(User user) {
        return spendingAlertRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    public List<SpendingAlert> getAlertsByType(String alertType, User user) {
        return spendingAlertRepository.findByAlertTypeAndUserOrderByCreatedAtDesc(alertType, user);
    }

    public List<SpendingAlert> getAlertsByCategory(Long categoryId, User user) {
        return spendingAlertRepository.findByCategoryIdAndUser(categoryId, user);
    }

    public List<SpendingAlert> getRecentAlerts(LocalDateTime since, User user) {
        return spendingAlertRepository.findRecentAlertsForUser(since, user);
    }

    public long getUnreadAlertCount(User user) {
        return spendingAlertRepository.countUnreadAlertsForUser(user);
    }

    public void markAlertAsRead(Long alertId) {
        Optional<SpendingAlert> alert = spendingAlertRepository.findById(alertId);
        if (alert.isPresent()) {
            SpendingAlert spendingAlert = alert.get();
            spendingAlert.setRead(true);
            spendingAlertRepository.save(spendingAlert);
        }
    }

    public void markAllAlertsAsRead(User user) {
        List<SpendingAlert> unreadAlerts = getUnreadAlerts(user);
        for (SpendingAlert alert : unreadAlerts) {
            alert.setRead(true);
            spendingAlertRepository.save(alert);
        }
    }

    public void generateBudgetAlerts(User user) {
        List<Category> categories = categoryRepository.findByUser(user);
        
        for (Category category : categories) {
            if (category.getBudget() > 0) {
                double totalSpent = calculateCategorySpending(category.getId(), user);
                double budgetUtilization = (totalSpent / category.getBudget()) * 100;
                
                if (budgetUtilization >= 100) {
                    createAlert(
                        SpendingAlert.AlertType.BUDGET_EXCEEDED.toString(),
                        String.format("You've exceeded your %s budget by %.2f%%", 
                                    category.getCategoryName(), budgetUtilization - 100),
                        category.getBudget(),
                        totalSpent,
                        category,
                        user
                    );
                } else if (budgetUtilization >= 80) {
                    createAlert(
                        SpendingAlert.AlertType.BUDGET_WARNING.toString(),
                        String.format("You've used %.1f%% of your %s budget", 
                                    budgetUtilization, category.getCategoryName()),
                        category.getBudget(),
                        totalSpent,
                        category,
                        user
                    );
                }
            }
        }
    }

    public void generateWeekendSpendingAlert(User user) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);
        
        double weekdaySpending = calculateSpendingForPeriod(weekStart, today.with(DayOfWeek.FRIDAY), user);
        double weekendSpending = calculateSpendingForPeriod(today.with(DayOfWeek.SATURDAY), weekEnd, user);
        
        if (weekendSpending > 0 && weekdaySpending > 0) {
            double weekendPercentage = (weekendSpending / (weekdaySpending + weekendSpending)) * 100;
            
            if (weekendPercentage > 50) {
                createAlert(
                    SpendingAlert.AlertType.WEEKEND_SPENDING.toString(),
                    String.format("Your weekend spending is %.1f%% of your weekly spending", weekendPercentage),
                    weekdaySpending,
                    weekendSpending,
                    null,
                    user
                );
            }
        }
    }

    public void generateUnusualSpendingAlert(User user) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        double todaySpending = calculateSpendingForPeriod(today, today, user);
        double yesterdaySpending = calculateSpendingForPeriod(yesterday, yesterday, user);
        
        if (yesterdaySpending > 0 && todaySpending > (yesterdaySpending * 2)) {
            createAlert(
                SpendingAlert.AlertType.UNUSUAL_SPENDING.toString(),
                String.format("Your spending today (%.2f) is %.1fx higher than yesterday (%.2f)", 
                            todaySpending, todaySpending / yesterdaySpending, yesterdaySpending),
                yesterdaySpending,
                todaySpending,
                null,
                user
            );
        }
    }

    private double calculateCategorySpending(Long categoryId, User user) {
        List<Expense> expenses = expenseRepository.findByCategoryIdAndUser(categoryId, user);
        return expenses.stream()
                      .mapToDouble(Expense::getAmount)
                      .sum();
    }

    private double calculateSpendingForPeriod(LocalDate startDate, LocalDate endDate, User user) {
        List<Expense> expenses = expenseRepository.findByDateBetweenAndUser(startDate, endDate, user);
        return expenses.stream()
                      .mapToDouble(Expense::getAmount)
                      .sum();
    }

    public void createAlert(String alertType, String message, double threshold, 
                           double currentValue, Category category, User user) {
        SpendingAlert alert = new SpendingAlert(alertType, message, threshold, currentValue, category, user);
        spendingAlertRepository.save(alert);
    }
} 
