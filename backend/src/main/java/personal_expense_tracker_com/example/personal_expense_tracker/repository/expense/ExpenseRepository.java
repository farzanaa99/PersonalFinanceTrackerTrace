package personal_expense_tracker_com.example.personal_expense_tracker.repository.expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.Expense;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUser(User user);
    
    List<Expense> findByUserOrderByDateDesc(User user);

    @Query("SELECT e FROM Expense e WHERE e.id = :id AND e.user = :user")
    Optional<Expense> findByIdAndUser(@Param("id") Long id, @Param("user") User user);

    List<Expense> findByCategoryCategoryNameAndUser(String categoryName, User user);

    @Query("SELECT e FROM Expense e WHERE e.category.categoryName = :categoryName")
    List<Expense> findExpensesByCategoryName(@Param("categoryName") String categoryName);

    List<Expense> findByDescriptionContainingAndUser(String keyword, User user);

    List<Expense> findByUserOrderByDateAsc(User user);

    List<Expense> findByDateBetweenAndUser(LocalDate start, LocalDate end, User user);

    List<Expense> findByDateBetween(LocalDate start, LocalDate end);

    List<Expense> findByAmountGreaterThanAndUser(Double amount, User user);

    List<Expense> findTop5ByUserOrderByDateDesc(User user);

    Long countByCategoryCategoryNameAndUser(String name, User user);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user = :user")
    Double getTotalExpensesForUser(@Param("user") User user);
    
    @Query("SELECT SUM(e.amount) FROM Expense e")
    Double getTotalExpenses();
    
    @Query("SELECT e FROM Expense e WHERE e.category.id = :categoryId AND e.user = :user")
    List<Expense> findByCategoryIdAndUser(@Param("categoryId") Long categoryId, @Param("user") User user);
    
    @Query("SELECT e FROM Expense e WHERE e.category.id = :categoryId")
    List<Expense> findByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.category.id = :categoryId AND e.user = :user")
    Double getTotalExpensesByCategoryForUser(@Param("categoryId") Long categoryId, @Param("user") User user);
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.date BETWEEN :startDate AND :endDate AND e.user = :user")
    Double getTotalExpensesByDateRangeForUser(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("user") User user);

    List<Expense> findByAmountGreaterThan(Double amount);
    
    List<Expense> findTop5ByOrderByDateDesc();

    @Query("SELECT e FROM Expense e WHERE e.category.id = :categoryId AND e.date BETWEEN :startDate AND :endDate AND e.user = :user")
    List<Expense> findByCategoryIdAndDateBetweenAndUser(@Param("categoryId") Long categoryId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("user") User user);
}

