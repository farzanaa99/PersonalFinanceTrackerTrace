package personal_expense_tracker_com.example.personal_expense_tracker.service.insight;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.FinancialInsight;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.Category;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.Expense;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.insight.FinancialInsightRepository;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.expense.ExpenseRepository;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.category.CategoryRepository;
import personal_expense_tracker_com.example.personal_expense_tracker.service.ai.AIFinancialInsightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FinancialInsightService {

    @Autowired
    private FinancialInsightRepository financialInsightRepository;
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AIFinancialInsightService aiFinancialInsightService;

    public List<FinancialInsight> getLatestInsights(int limit, User user) {
        List<FinancialInsight> existingInsights = financialInsightRepository.findLatestInsightsForUser(limit, user);
        
        if (existingInsights.isEmpty() || shouldRegenerateInsights(existingInsights)) {
            return aiFinancialInsightService.generateAIInsights(user);
        }
        
        return existingInsights;
    }

    public List<FinancialInsight> generateAIInsights(User user) {
        return aiFinancialInsightService.generateAIInsights(user);
    }

    private boolean shouldRegenerateInsights(List<FinancialInsight> insights) {
        if (insights.isEmpty()) return true;
        
        // Regenerate if insights are older than 24 hours
        LocalDateTime oldestInsight = insights.stream()
            .map(FinancialInsight::getCalculatedAt)
            .min(LocalDateTime::compareTo)
            .orElse(LocalDateTime.MIN);
        
        return LocalDateTime.now().minusHours(24).isAfter(oldestInsight);
    }

    public List<FinancialInsight> getInsightsByType(String insightType, User user) {
        return financialInsightRepository.findByUserAndInsightTypeOrderByCalculatedAtDesc(user, insightType);
    }

    public List<FinancialInsight> getInsightsByCategory(Long categoryId, User user) {
        return financialInsightRepository.findByCategoryIdAndUser(categoryId, user);
    }

    public List<FinancialInsight> getRecentInsights(LocalDateTime since, User user) {
        return financialInsightRepository.findRecentInsightsForUser(since, user);
    }

    public void generateAllInsights(User user) {
        generateWeekendVsWeekdayInsight(user);
        generateBudgetUtilizationInsights(user);
        generateSpendingTrendInsights(user);
        generateCategoryComparisonInsights(user);
        generateDailyAverageInsight(user);
    }

    public void generateWeekendVsWeekdayInsight(User user) {
        LocalDate today = LocalDate.now();
        LocalDate previousWeekStart = today.minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate previousWeekEnd = today.minusWeeks(1).with(DayOfWeek.SUNDAY);
        
        double weekdaySpending = calculateSpendingForPeriod(previousWeekStart, previousWeekStart.plusDays(4), user);
        double weekendSpending = calculateSpendingForPeriod(previousWeekStart.plusDays(5), previousWeekEnd, user);
        
        if (weekdaySpending > 0 && weekendSpending > 0) {
            double weekendPercentage = (weekendSpending / (weekdaySpending + weekendSpending)) * 100;
            String title = "Weekend vs Weekday Spending";
            String description = String.format("Your weekend spending is %.1f%% of your weekly spending", weekendPercentage);
            
            FinancialInsight insight = new FinancialInsight(
                FinancialInsight.InsightType.WEEKEND_VS_WEEKDAY_SPENDING.toString(),
                title,
                description,
                weekendSpending,
                weekendPercentage,
                "Previous Week",
                null,
                user
            );
            financialInsightRepository.save(insight);
        }
    }

    public void generateBudgetUtilizationInsights(User user) {
        List<Category> categories = categoryRepository.findByUser(user);
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        
        for (Category category : categories) {
            if (category.getBudget() > 0) {
                double totalSpent = calculateCategorySpendingForPeriod(category.getId(), monthStart, today, user);
                double utilization = (totalSpent / category.getBudget()) * 100;
                
                String title = String.format("%s Budget Utilization", category.getCategoryName());
                String description = String.format("You've used %.1f%% of your %s budget", utilization, category.getCategoryName());
                
                FinancialInsight insight = new FinancialInsight(
                    FinancialInsight.InsightType.BUDGET_UTILIZATION.toString(),
                    title,
                    description,
                    totalSpent,
                    utilization,
                    "Current Month",
                    category,
                    user
                );
                financialInsightRepository.save(insight);
            }
        }
    }

    public void generateSpendingTrendInsights(User user) {
        LocalDate today = LocalDate.now();
        LocalDate week1End = today.minusDays(1);
        LocalDate week1Start = week1End.minusDays(6);
        LocalDate week2End = week1Start.minusDays(1);
        LocalDate week2Start = week2End.minusDays(6);
        
        double thisWeekSpending = calculateSpendingForPeriod(week1Start, week1End, user);
        double lastWeekSpending = calculateSpendingForPeriod(week2Start, week2End, user);
        
        if (lastWeekSpending > 0) {
            double change = ((thisWeekSpending - lastWeekSpending) / lastWeekSpending) * 100;
            String trend = change > 0 ? "increased" : "decreased";
            String title = "Weekly Spending Trend";
            String description = String.format("Your spending has %s by %.1f%% compared to last week", trend, Math.abs(change));
            
            FinancialInsight insight = new FinancialInsight(
                FinancialInsight.InsightType.SPENDING_TREND.toString(),
                title,
                description,
                thisWeekSpending,
                Math.abs(change),
                "Weekly Comparison",
                null,
                user
            );
            financialInsightRepository.save(insight);
        }
    }

    public void generateCategoryComparisonInsights(User user) {
        List<Category> categories = categoryRepository.findByUser(user);
        Map<Category, Double> categorySpending = categories.stream()
            .collect(Collectors.toMap(
                category -> category,
                category -> calculateCategorySpending(category.getId(), user)
            ));
        
        Category topCategory = categorySpending.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        if (topCategory != null) {
            double topSpending = categorySpending.get(topCategory);
            double totalSpending = categorySpending.values().stream().mapToDouble(Double::doubleValue).sum();
            
            if (totalSpending > 0) {
                double percentage = (topSpending / totalSpending) * 100;
                String title = "Top Spending Category";
                String description = String.format("%s is your highest spending category at %.1f%% of total expenses", 
                                                 topCategory.getCategoryName(), percentage);
                
                FinancialInsight insight = new FinancialInsight(
                    FinancialInsight.InsightType.CATEGORY_COMPARISON.toString(),
                    title,
                    description,
                    topSpending,
                    percentage,
                    "Current Month",
                    topCategory,
                    user
                );
                financialInsightRepository.save(insight);
            }
        }
    }

    public void generateDailyAverageInsight(User user) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        
        double monthlySpending = calculateSpendingForPeriod(monthStart, today, user);
        long daysInMonth = java.time.temporal.ChronoUnit.DAYS.between(monthStart, today) + 1;
        double dailyAverage = monthlySpending / daysInMonth;
        
        String title = "Daily Average Spending";
        String description = String.format("Your average daily spending this month is %.2f", dailyAverage);
        
        FinancialInsight insight = new FinancialInsight(
            FinancialInsight.InsightType.DAILY_AVERAGE.toString(),
            title,
            description,
            dailyAverage,
            0.0,
            "Current Month",
            null,
            user
        );
        financialInsightRepository.save(insight);
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

    private double calculateCategorySpendingForPeriod(Long categoryId, LocalDate startDate, LocalDate endDate, User user) {
        List<Expense> expenses = expenseRepository.findByCategoryIdAndDateBetweenAndUser(categoryId, startDate, endDate, user);
        return expenses.stream()
                      .mapToDouble(Expense::getAmount)
                      .sum();
    }
} 
