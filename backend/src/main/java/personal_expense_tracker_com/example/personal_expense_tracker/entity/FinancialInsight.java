package personal_expense_tracker_com.example.personal_expense_tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "financial_insights")
public class FinancialInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String insightType;
    private String title;
    private String description;
    private double value;
    private double percentage;
    private String period;
    private LocalDateTime calculatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    public enum InsightType {
        WEEKEND_VS_WEEKDAY_SPENDING,
        BUDGET_UTILIZATION,
        SPENDING_TREND,
        CATEGORY_COMPARISON,
        MONTHLY_AVERAGE,
        DAILY_AVERAGE
    }

    public FinancialInsight() {}

    public FinancialInsight(String insightType, String title, String description, 
                           double value, double percentage, String period, Category category, User user) {
        this.insightType = insightType;
        this.title = title;
        this.description = description;
        this.value = value;
        this.percentage = percentage;
        this.period = period;
        this.category = category;
        this.user = user;
        this.calculatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getInsightType() {
        return insightType;
    }

    public void setInsightType(String insightType) {
        this.insightType = insightType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
} 
