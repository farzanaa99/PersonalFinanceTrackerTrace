package personal_expense_tracker_com.example.personal_expense_tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "recurring_transactions")
public class RecurringTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private double amount;
    private LocalDate nextDueDate;
    
    @Enumerated(EnumType.STRING)
    private RecurrencePattern recurrencePattern;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private Category category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    
    private boolean isActive = true;
    private LocalDateTime createdAt;

    public enum RecurrencePattern {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }

    public enum TransactionType {
        INCOME, EXPENSE
    }

    public RecurringTransaction() {}

    public RecurringTransaction(String description, double amount, LocalDate nextDueDate, 
                               RecurrencePattern recurrencePattern, Category category, 
                               TransactionType transactionType, User user) {
        this.description = description;
        this.amount = amount;
        this.nextDueDate = nextDueDate;
        this.recurrencePattern = recurrencePattern;
        this.category = category;
        this.transactionType = transactionType;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public RecurrencePattern getRecurrencePattern() {
        return recurrencePattern;
    }

    public void setRecurrencePattern(RecurrencePattern recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
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

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // Method to calculate next due date based on recurrence pattern
    public LocalDate calculateNextDueDate() {
        LocalDate current = this.nextDueDate;
        switch (this.recurrencePattern) {
            case DAILY:
                return current.plusDays(1);
            case WEEKLY:
                return current.plusWeeks(1);
            case MONTHLY:
                return current.plusMonths(1);
            case YEARLY:
                return current.plusYears(1);
            default:
                return current;
        }
    }
} 
