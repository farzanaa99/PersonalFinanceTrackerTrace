package personal_expense_tracker_com.example.personal_expense_tracker.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("EXPENSE")
public class Expense extends Transaction {

    public Expense() {}

    public Expense(double amount, String description, LocalDate date, Category category, User user) {
        super(amount, description, date, category, user);
    }

    @Override
    public String getType() {
        return "EXPENSE"; 
    }
}
