package personal_expense_tracker_com.example.personal_expense_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PersonalExpenseTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PersonalExpenseTrackerApplication.class, args);
	}

}
