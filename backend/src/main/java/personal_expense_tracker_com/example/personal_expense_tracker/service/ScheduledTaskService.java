package personal_expense_tracker_com.example.personal_expense_tracker.service;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.service.recurring.RecurringTransactionService;
import personal_expense_tracker_com.example.personal_expense_tracker.service.alert.SpendingAlertService;
import personal_expense_tracker_com.example.personal_expense_tracker.service.insight.FinancialInsightService;
import personal_expense_tracker_com.example.personal_expense_tracker.service.savings.SavingsGoalService;
import personal_expense_tracker_com.example.personal_expense_tracker.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.user.UserRepository;

import java.util.List;

@Service
public class ScheduledTaskService {

    @Autowired
    private RecurringTransactionService recurringTransactionService;
    
    @Autowired
    private SpendingAlertService spendingAlertService;
    
    @Autowired
    private FinancialInsightService financialInsightService;
    
    @Autowired
    private SavingsGoalService savingsGoalService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Scheduled(cron = "0 0 6 * * ?")
    public void processRecurringTransactions() {
        recurringTransactionService.processDueRecurringTransactions();
    }

    // Generate spending alerts daily at 8 AM
    @Scheduled(cron = "0 0 8 * * ?")
    public void generateSpendingAlerts() {
        List<User> users = userService.getAllUsers();
        for (User user : users) {
            spendingAlertService.generateBudgetAlerts(user);
            spendingAlertService.generateWeekendSpendingAlert(user);
            spendingAlertService.generateUnusualSpendingAlert(user);
        }
    }

    // Generate financial insights daily at 9 AM
    @Scheduled(cron = "0 0 9 * * ?")
    public void generateFinancialInsights() {
        List<User> users = userService.getAllUsers();
        for (User user : users) {
            financialInsightService.generateAllInsights(user);
        }
    }

    // Generate savings goal alerts daily at 7 AM
    @Scheduled(cron = "0 0 7 * * ?")
    public void generateSavingsGoalAlerts() {
        List<User> users = userService.getAllUsers();
        for (User user : users) {
            savingsGoalService.generateSavingsGoalAlerts(user);
            savingsGoalService.checkGoalCompletion(user);
        }
    }

    // Weekly cleanup of old insights (every Sunday at 2 AM)
    @Scheduled(cron = "0 0 2 ? * SUN")
    public void cleanupOldInsights() {
        // This could be implemented to remove insights older than a certain period
        // For now, we'll keep all insights
    }

    // Generate AI insights for all active users daily at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateDailyAIInsights() {
        List<User> activeUsers = userRepository.findByIsActiveTrue();
        
        for (User user : activeUsers) {
            try {
                financialInsightService.generateAIInsights(user);
            } catch (Exception e) {
                System.err.println("Failed to generate AI insights for user: " + user.getId() + " - " + e.getMessage());
            }
        }
    }

    // Generate AI insights every 6 hours for active users
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 6 hours in milliseconds
    public void generatePeriodicAIInsights() {
        List<User> activeUsers = userRepository.findByIsActiveTrue();
        
        for (User user : activeUsers) {
            try {
                financialInsightService.generateAIInsights(user);
            } catch (Exception e) {
                System.err.println("Failed to generate periodic AI insights for user: " + user.getId() + " - " + e.getMessage());
            }
        }
    }
} 
