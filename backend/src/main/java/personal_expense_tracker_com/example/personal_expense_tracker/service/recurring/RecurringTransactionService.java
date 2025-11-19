package personal_expense_tracker_com.example.personal_expense_tracker.service.recurring;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.RecurringTransaction;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.Expense;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.Income;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.Category;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.recurring.RecurringTransactionRepository;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.expense.ExpenseRepository;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.income.IncomeRepository;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.category.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RecurringTransactionService {

    @Autowired
    private RecurringTransactionRepository recurringTransactionRepository;
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private IncomeRepository incomeRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;

    public List<RecurringTransaction> getAllActiveRecurringTransactions(User user) {
        return recurringTransactionRepository.findByUserAndIsActiveTrue(user);
    }

    public List<RecurringTransaction> getRecurringTransactionsByType(RecurringTransaction.TransactionType type, User user) {
        return recurringTransactionRepository.findByTransactionTypeAndUserAndIsActiveTrue(type, user);
    }

    public RecurringTransaction createRecurringTransaction(RecurringTransaction recurringTransaction) {
        return recurringTransactionRepository.save(recurringTransaction);
    }

    public Optional<RecurringTransaction> getRecurringTransactionById(Long id) {
        return recurringTransactionRepository.findById(id);
    }

    public RecurringTransaction updateRecurringTransaction(Long id, RecurringTransaction updatedTransaction) {
        Optional<RecurringTransaction> existing = recurringTransactionRepository.findById(id);
        if (existing.isPresent()) {
            RecurringTransaction transaction = existing.get();
            transaction.setDescription(updatedTransaction.getDescription());
            transaction.setAmount(updatedTransaction.getAmount());
            transaction.setNextDueDate(updatedTransaction.getNextDueDate());
            transaction.setRecurrencePattern(updatedTransaction.getRecurrencePattern());
            transaction.setCategory(updatedTransaction.getCategory());
            transaction.setTransactionType(updatedTransaction.getTransactionType());
            return recurringTransactionRepository.save(transaction);
        }
        return null;
    }

    public void deactivateRecurringTransaction(Long id) {
        Optional<RecurringTransaction> transaction = recurringTransactionRepository.findById(id);
        if (transaction.isPresent()) {
            RecurringTransaction rt = transaction.get();
            rt.setActive(false);
            recurringTransactionRepository.save(rt);
        }
    }

    @Transactional
    public void processDueRecurringTransactions() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> dueTransactions = recurringTransactionRepository.findDueTransactions(today);
        
        for (RecurringTransaction recurringTransaction : dueTransactions) {
            createTransactionFromRecurring(recurringTransaction);
            
            recurringTransaction.setNextDueDate(recurringTransaction.calculateNextDueDate());
            recurringTransactionRepository.save(recurringTransaction);
        }
    }

    private void createTransactionFromRecurring(RecurringTransaction recurringTransaction) {
        Category category = recurringTransaction.getCategory();
        User user = recurringTransaction.getUser();
        
        if (recurringTransaction.getTransactionType() == RecurringTransaction.TransactionType.EXPENSE) {
            Expense expense = new Expense(
                recurringTransaction.getAmount(),
                recurringTransaction.getDescription(),
                recurringTransaction.getNextDueDate(),
                category,
                user
            );
            expenseRepository.save(expense);
        } else {
            Income income = new Income(
                recurringTransaction.getAmount(),
                recurringTransaction.getDescription(),
                recurringTransaction.getNextDueDate(),
                category,
                user
            );
            incomeRepository.save(income);
        }
    }

    public List<RecurringTransaction> getRecurringTransactionsByCategory(Long categoryId, User user) {
        return recurringTransactionRepository.findByCategoryIdAndUser(categoryId, user);
    }

    public List<RecurringTransaction> getRecurringTransactionsByPattern(RecurringTransaction.RecurrencePattern pattern, User user) {
        return recurringTransactionRepository.findByRecurrencePatternAndUser(pattern, user);
    }
} 
