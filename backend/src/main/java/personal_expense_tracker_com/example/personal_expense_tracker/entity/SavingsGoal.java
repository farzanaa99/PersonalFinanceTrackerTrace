package personal_expense_tracker_com.example.personal_expense_tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "savings_goals")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Add at class level
public class SavingsGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "goal_name")
    private String name;
    private String description;
    private double targetAmount;
    private double currentAmount;
    private LocalDate targetDate;
    private LocalDateTime createdAt;
    private boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    public SavingsGoal() {
        this.createdAt = LocalDateTime.now();
    }

    public SavingsGoal(String name, String description, double targetAmount, 
                      double currentAmount, LocalDate targetDate, Category category, User user) {
        this.name = name;
        this.description = description;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate;
        this.category = category;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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

    // Helper methods
    public double getProgressPercentage() {
        if (targetAmount <= 0) return 0.0;
        return (currentAmount / targetAmount) * 100;
    }

    public double getRemainingAmount() {
        return Math.max(0, targetAmount - currentAmount);
    }

    public boolean isCompleted() {
        return currentAmount >= targetAmount;
    }

    public boolean isOverdue() {
        return LocalDate.now().isAfter(targetDate) && !isCompleted();
    }

    public long getDaysRemaining() {
        LocalDate today = LocalDate.now();
        if (today.isAfter(targetDate)) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(today, targetDate);
    }

    public double getDailyRequiredAmount() {
        long daysRemaining = getDaysRemaining();
        if (daysRemaining <= 0) return 0.0;
        return getRemainingAmount() / daysRemaining;
    }

    @Override
    public String toString() {
        return "SavingsGoal{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", targetAmount=" + targetAmount +
                ", currentAmount=" + currentAmount +
                ", targetDate=" + targetDate +
                ", createdAt=" + createdAt +
                ", isActive=" + isActive +
                ", category=" + (category != null ? "Category{id=" + category.getId() + ", name='" + category.getCategoryName() + "'}" : "null") +
                ", user=" + (user != null ? "User{id=" + user.getId() + "}" : "null") +
                '}';
    }
} 
