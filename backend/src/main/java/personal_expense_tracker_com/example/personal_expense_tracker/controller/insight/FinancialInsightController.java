package personal_expense_tracker_com.example.personal_expense_tracker.controller.insight;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.FinancialInsight;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.service.insight.FinancialInsightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/financial-insights")
public class FinancialInsightController {

    @Autowired
    private FinancialInsightService financialInsightService;

    @GetMapping
    public ResponseEntity<List<FinancialInsight>> getLatestInsights(
            @RequestParam(defaultValue = "10") int limit, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<FinancialInsight> insights = financialInsightService.getLatestInsights(limit, currentUser);
        return ResponseEntity.ok(insights);
    }

    @GetMapping("/type/{insightType}")
    public ResponseEntity<List<FinancialInsight>> getInsightsByType(@PathVariable String insightType, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<FinancialInsight> insights = financialInsightService.getInsightsByType(insightType, currentUser);
        return ResponseEntity.ok(insights);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<FinancialInsight>> getInsightsByCategory(@PathVariable Long categoryId, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<FinancialInsight> insights = financialInsightService.getInsightsByCategory(categoryId, currentUser);
        return ResponseEntity.ok(insights);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<FinancialInsight>> getRecentInsights(
            @RequestParam(defaultValue = "24") int hours, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<FinancialInsight> insights = financialInsightService.getRecentInsights(since, currentUser);
        return ResponseEntity.ok(insights);
    }

    @PostMapping("/generate/all")
    public ResponseEntity<Void> generateAllInsights(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        financialInsightService.generateAllInsights(currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate/weekend-weekday")
    public ResponseEntity<Void> generateWeekendVsWeekdayInsight(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        financialInsightService.generateWeekendVsWeekdayInsight(currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate/budget-utilization")
    public ResponseEntity<Void> generateBudgetUtilizationInsights(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        financialInsightService.generateBudgetUtilizationInsights(currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate/spending-trend")
    public ResponseEntity<Void> generateSpendingTrendInsights(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        financialInsightService.generateSpendingTrendInsights(currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate/category-comparison")
    public ResponseEntity<Void> generateCategoryComparisonInsights(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        financialInsightService.generateCategoryComparisonInsights(currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate/daily-average")
    public ResponseEntity<Void> generateDailyAverageInsight(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        financialInsightService.generateDailyAverageInsight(currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate/ai")
    public ResponseEntity<List<FinancialInsight>> generateAIInsights(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<FinancialInsight> insights = financialInsightService.generateAIInsights(currentUser);
        return ResponseEntity.ok(insights);
    }

    @GetMapping("/ai")
    public ResponseEntity<List<FinancialInsight>> getAIInsights(
            @RequestParam(defaultValue = "10") int limit, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<FinancialInsight> insights = financialInsightService.generateAIInsights(currentUser);
        return ResponseEntity.ok(insights.stream().limit(limit).collect(java.util.stream.Collectors.toList()));
    }
} 
