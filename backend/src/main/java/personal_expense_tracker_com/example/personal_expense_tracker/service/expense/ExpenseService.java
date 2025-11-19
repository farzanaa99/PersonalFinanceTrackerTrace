package personal_expense_tracker_com.example.personal_expense_tracker.service.expense;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.Category;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.Expense;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.category.CategoryRepository;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.expense.ExpenseRepository;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Expense> getAllExpenses(User user) {
        return expenseRepository.findByUserOrderByDateDesc(user);
    }

    public List<Expense> findAllByUser(User user) {
        return expenseRepository.findByUserOrderByDateDesc(user);
    }

    public Expense addExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    public Optional<Expense> getExpenseById(Long id) {
        return expenseRepository.findById(id); 
    }

    public Optional<Expense> getExpenseByIdAndUser(Long id, User user) {
        return expenseRepository.findByIdAndUser(id, user);
    }

    public Expense updateExpense(Long id, Expense updatedExpense) {
        Expense existingExpense = expenseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        existingExpense.setDescription(updatedExpense.getDescription());
        existingExpense.setAmount(updatedExpense.getAmount());
        existingExpense.setDate(updatedExpense.getDate());

        if (updatedExpense.getCategory() != null && updatedExpense.getCategory().getId() != null) {
            Category category = categoryRepository.findById(updatedExpense.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + updatedExpense.getCategory().getId()));
            existingExpense.setCategory(category);
        }

        return expenseRepository.save(existingExpense);
    }

    public Expense updateExpense(Long id, Expense updatedExpense, User user) {
        Optional<Expense> existingExpenseOpt = expenseRepository.findByIdAndUser(id, user);
        if (existingExpenseOpt.isEmpty()) {
            return null;
        }

        Expense existingExpense = existingExpenseOpt.get();
        existingExpense.setDescription(updatedExpense.getDescription());
        existingExpense.setAmount(updatedExpense.getAmount());
        existingExpense.setDate(updatedExpense.getDate());

        if (updatedExpense.getCategory() != null && updatedExpense.getCategory().getId() != null) {
            Category category = categoryRepository.findByIdAndUser(updatedExpense.getCategory().getId(), user)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + updatedExpense.getCategory().getId()));
            existingExpense.setCategory(category);
        }

        return expenseRepository.save(existingExpense);
    }

    public void removeExpense(Long expenseId) {
        if (expenseRepository.existsById(expenseId)) {
            expenseRepository.deleteById(expenseId); 
        } else {
            throw new IllegalArgumentException("Expense not found with id: " + expenseId);
        }
    }

    public void removeExpense(Long expenseId, User user) {
        Optional<Expense> expense = expenseRepository.findByIdAndUser(expenseId, user);
        if (expense.isPresent()) {
            expenseRepository.deleteById(expenseId);
        } else {
            throw new IllegalArgumentException("Expense not found with id: " + expenseId + " for user");
        }
    }

    public List<Expense> getExpensesByDateRange(LocalDate startDate, LocalDate endDate, User user) {
        return expenseRepository.findByDateBetweenAndUser(startDate, endDate, user);
    }

    public List<Expense> findByDateBetweenAndUser(LocalDate start, LocalDate end, User user) {
        return expenseRepository.findByDateBetweenAndUser(start, end, user);
    }

    public List<Expense> getExpensesByCategory(Long categoryId, User user) {
        return expenseRepository.findByCategoryIdAndUser(categoryId, user);
    }

    public List<Expense> getExpensesByCategoryName(String categoryName, User user) {
        return expenseRepository.findByCategoryCategoryNameAndUser(categoryName, user);
    }

    public List<Expense> findByCategoryNameAndUser(String categoryName, User user) {
        return expenseRepository.findByCategoryCategoryNameAndUser(categoryName, user);
    }

    public List<Expense> getExpensesByDescription(String description, User user) {
        return expenseRepository.findByDescriptionContainingAndUser(description, user);
    }

    public List<Expense> getExpensesOrderedByDate(User user) {
        return expenseRepository.findByUserOrderByDateAsc(user);
    }

    public double getTotalExpenses(User user) {
        Double total = expenseRepository.getTotalExpensesForUser(user);
        return total != null ? total : 0.0;
    }

    public List<Expense> getExpensesAboveAmount(double amount, User user) {
        return expenseRepository.findByAmountGreaterThanAndUser(amount, user);
    }

    public List<Expense> findByAmountGreaterThanAndUser(Double amount, User user) {
        return expenseRepository.findByAmountGreaterThanAndUser(amount, user);
    }

    public List<Expense> getRecentExpenses(int limit, User user) {
        return expenseRepository.findTop5ByUserOrderByDateDesc(user);
    }

    public List<Expense> findTop5ByUserOrderByDateDesc(User user) {
        return expenseRepository.findTop5ByUserOrderByDateDesc(user);
    }

    public long getExpenseCountByCategory(String categoryName, User user) {
        return expenseRepository.countByCategoryCategoryNameAndUser(categoryName, user);
    }

    public Expense createExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    public double getTotalExpensesByCategory(Long categoryId, User user) {
        Double total = expenseRepository.getTotalExpensesByCategoryForUser(categoryId, user);
        return total != null ? total : 0.0;
    }

    public double getTotalExpensesByDateRange(LocalDate startDate, LocalDate endDate, User user) {
        Double total = expenseRepository.getTotalExpensesByDateRangeForUser(startDate, endDate, user);
        return total != null ? total : 0.0;
    }

    // Legacy methods for backward compatibility
    public List<Expense> findByCategoryName(String categoryName) {
        return expenseRepository.findExpensesByCategoryName(categoryName);
    }

    public List<Expense> findByAmountGreaterThan(Double amount) {
        return expenseRepository.findByAmountGreaterThan(amount);
    }

    public List<Expense> findByDateBetween(LocalDate start, LocalDate end) {
        return expenseRepository.findByDateBetween(start, end);
    }

    public List<Expense> findTop5ByOrderByDateDesc() {
        return expenseRepository.findTop5ByOrderByDateDesc();
    }

    public List<Expense> findAll() {
        return expenseRepository.findAll();
    }
}


