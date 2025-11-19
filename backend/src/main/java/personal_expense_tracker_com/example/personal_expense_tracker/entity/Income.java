package personal_expense_tracker_com.example.personal_expense_tracker.entity;

import java.time.LocalDate;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;


@Entity
@DiscriminatorValue("INCOME") 
public class Income extends Transaction {


    public Income() {}

    public Income(double amount, String description, LocalDate date, Category category, User user) {
        super(amount, description, date, category, user);
    }

    @Override
    public String getType() {
        return "INCOME";  // Explicitly return "INCOME"
    }
}
