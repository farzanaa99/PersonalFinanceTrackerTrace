package personal_expense_tracker_com.example.personal_expense_tracker.service.ai;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.*;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.transaction.TransactionRepository;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.insight.FinancialInsightRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIFinancialInsightService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private FinancialInsightRepository financialInsightRepository;

    public List<FinancialInsight> generateAIInsights(User user) {
        List<Transaction> transactions = transactionRepository.findByUser(user);
        
        if (transactions.isEmpty()) {
            return generateWelcomeInsights(user);
        }

        List<FinancialInsight> insights = new ArrayList<>();
        
        // Generate different types of insights
        insights.addAll(generateSpendingPatternInsights(transactions, user));
        insights.addAll(generateCategoryInsights(transactions, user));
        insights.addAll(generateTrendInsights(transactions, user));
        insights.addAll(generateBudgetInsights(transactions, user));
        insights.addAll(generateSavingsInsights(transactions, user));
        
        // Save insights to database
        financialInsightRepository.saveAll(insights);
        
        return insights;
    }

    private List<FinancialInsight> generateWelcomeInsights(User user) {
        List<FinancialInsight> insights = new ArrayList<>();
        
        insights.add(new FinancialInsight(
            "WELCOME",
            "Welcome to Your Financial Journey!",
            "Start tracking your expenses to get personalized insights and improve your financial health.",
            0.0,
            0.0,
            "current",
            null,
            user
        ));
        
        insights.add(new FinancialInsight(
            "TIP",
            "Pro Tip: Categorize Your Expenses",
            "Categorizing your expenses helps identify spending patterns and areas for improvement.",
            0.0,
            0.0,
            "current",
            null,
            user
        ));
        
        return insights;
    }

    private List<FinancialInsight> generateSpendingPatternInsights(List<Transaction> transactions, User user) {
        List<FinancialInsight> insights = new ArrayList<>();
        
        // Analyze spending by day of week
        Map<String, Double> dayOfWeekSpending = transactions.stream()
            .filter(t -> t.getAmount() < 0) // Only expenses
            .collect(Collectors.groupingBy(
                t -> t.getDate().getDayOfWeek().toString(),
                Collectors.summingDouble(t -> Math.abs(t.getAmount()))
            ));
        
        if (!dayOfWeekSpending.isEmpty()) {
            String highestSpendingDay = dayOfWeekSpending.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
            
            double highestAmount = dayOfWeekSpending.getOrDefault(highestSpendingDay, 0.0);
            
            insights.add(new FinancialInsight(
                "SPENDING_PATTERN",
                "Your Highest Spending Day",
                String.format("You spend the most on %s, averaging $%.2f per transaction.", 
                    highestSpendingDay, highestAmount),
                highestAmount,
                0.0,
                "weekly",
                null,
                user
            ));
        }
        
        return insights;
    }

    private List<FinancialInsight> generateCategoryInsights(List<Transaction> transactions, User user) {
        List<FinancialInsight> insights = new ArrayList<>();
        
        // Analyze spending by category
        Map<Category, Double> categorySpending = transactions.stream()
            .filter(t -> t.getAmount() < 0 && t.getCategory() != null)
            .collect(Collectors.groupingBy(
                Transaction::getCategory,
                Collectors.summingDouble(t -> Math.abs(t.getAmount()))
            ));
        
        if (!categorySpending.isEmpty()) {
            // Find top spending category
            Map.Entry<Category, Double> topCategory = categorySpending.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
            
            if (topCategory != null) {
                double totalSpending = categorySpending.values().stream().mapToDouble(Double::doubleValue).sum();
                double percentage = (topCategory.getValue() / totalSpending) * 100;
                
                insights.add(new FinancialInsight(
                    "CATEGORY_ANALYSIS",
                    "Your Biggest Expense Category",
                    String.format("%s accounts for %.1f%% of your total spending at $%.2f", 
                        topCategory.getKey().getCategoryName(), percentage, topCategory.getValue()),
                    topCategory.getValue(),
                    percentage,
                    "current",
                    topCategory.getKey(),
                    user
                ));
            }
            
            // Find categories with budget overruns
            categorySpending.forEach((category, spent) -> {
                if (category.getBudget() > 0) {
                    double budgetUtilization = (spent / category.getBudget()) * 100;
                    if (budgetUtilization > 100) {
                        insights.add(new FinancialInsight(
                            "BUDGET_ALERT",
                            "Budget Exceeded",
                            String.format("You've exceeded your %s budget by %.1f%%", 
                                category.getCategoryName(), budgetUtilization - 100),
                            spent,
                            budgetUtilization,
                            "current",
                            category,
                            user
                        ));
                    }
                }
            });
        }
        
        return insights;
    }

    private List<FinancialInsight> generateTrendInsights(List<Transaction> transactions, User user) {
        List<FinancialInsight> insights = new ArrayList<>();
        
        // Compare current month vs previous month
        LocalDateTime now = LocalDateTime.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();
        
        double currentMonthSpending = transactions.stream()
            .filter(t -> t.getAmount() < 0 && 
                        t.getDate().getMonthValue() == currentMonth && 
                        t.getDate().getYear() == currentYear)
            .mapToDouble(t -> Math.abs(t.getAmount()))
            .sum();
        
        double previousMonthSpending = transactions.stream()
            .filter(t -> t.getAmount() < 0 && 
                        t.getDate().getMonthValue() == (currentMonth == 1 ? 12 : currentMonth - 1) && 
                        t.getDate().getYear() == (currentMonth == 1 ? currentYear - 1 : currentYear))
            .mapToDouble(t -> Math.abs(t.getAmount()))
            .sum();
        
        if (previousMonthSpending > 0) {
            double changePercentage = ((currentMonthSpending - previousMonthSpending) / previousMonthSpending) * 100;
            String trend = changePercentage > 0 ? "increased" : "decreased";
            
            insights.add(new FinancialInsight(
                "TREND_ANALYSIS",
                "Monthly Spending Trend",
                String.format("Your spending has %s by %.1f%% compared to last month", 
                    trend, Math.abs(changePercentage)),
                currentMonthSpending,
                changePercentage,
                "monthly",
                null,
                user
            ));
        }
        
        return insights;
    }

    private List<FinancialInsight> generateBudgetInsights(List<Transaction> transactions, User user) {
        List<FinancialInsight> insights = new ArrayList<>();
        
        // Calculate total budget vs actual spending
        double totalBudget = transactions.stream()
            .filter(t -> t.getCategory() != null && t.getCategory().getBudget() > 0)
            .map(t -> t.getCategory().getBudget())
            .distinct()
            .mapToDouble(Double::doubleValue)
            .sum();
        
        double totalSpending = transactions.stream()
            .filter(t -> t.getAmount() < 0)
            .mapToDouble(t -> Math.abs(t.getAmount()))
            .sum();
        
        if (totalBudget > 0) {
            double budgetUtilization = (totalSpending / totalBudget) * 100;
            
            insights.add(new FinancialInsight(
                "BUDGET_UTILIZATION",
                "Overall Budget Status",
                String.format("You've used %.1f%% of your total budget", budgetUtilization),
                totalSpending,
                budgetUtilization,
                "current",
                null,
                user
            ));
        }
        
        return insights;
    }

    private List<FinancialInsight> generateSavingsInsights(List<Transaction> transactions, User user) {
        List<FinancialInsight> insights = new ArrayList<>();
        
        // Calculate savings rate
        double totalIncome = transactions.stream()
            .filter(t -> t.getAmount() > 0)
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        double totalExpenses = transactions.stream()
            .filter(t -> t.getAmount() < 0)
            .mapToDouble(t -> Math.abs(t.getAmount()))
            .sum();
        
        if (totalIncome > 0) {
            double savingsRate = ((totalIncome - totalExpenses) / totalIncome) * 100;
            
            if (savingsRate > 0) {
                insights.add(new FinancialInsight(
                    "SAVINGS_RATE",
                    "Your Savings Rate",
                    String.format("You're saving %.1f%% of your income - great job!", savingsRate),
                    totalIncome - totalExpenses,
                    savingsRate,
                    "current",
                    null,
                    user
                ));
            } else {
                insights.add(new FinancialInsight(
                    "SAVINGS_ALERT",
                    "Spending More Than Income",
                    "You're spending more than you earn. Consider reviewing your expenses.",
                    Math.abs(totalIncome - totalExpenses),
                    Math.abs(savingsRate),
                    "current",
                    null,
                    user
                ));
            }
        }
        
        return insights;
    }
} 