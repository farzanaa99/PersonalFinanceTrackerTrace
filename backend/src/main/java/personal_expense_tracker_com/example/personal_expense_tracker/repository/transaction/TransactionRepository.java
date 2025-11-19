package personal_expense_tracker_com.example.personal_expense_tracker.repository.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.Transaction;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findAll(Pageable pageable); 

    List<Transaction> findByDateBetween(LocalDate start, LocalDate end);
    List<Transaction> findByDateBetweenAndUser(LocalDate start, LocalDate end, User user);
    
    List<Transaction> findByAmountGreaterThan(Double amount);
    List<Transaction> findByAmountGreaterThanAndUser(Double amount, User user);
    
    List<Transaction> findByAmountLessThan(Double amount);
    List<Transaction> findByAmountLessThanAndUser(Double amount, User user);

    List<Transaction> findByCategoryCategoryName(String name);
    List<Transaction> findByCategoryCategoryNameAndUser(String name, User user);

    List<Transaction> findByDescriptionContainingIgnoreCase(String keyword);
    List<Transaction> findByDescriptionContainingIgnoreCaseAndUser(String keyword, User user);

    List<Transaction> findAllByOrderByDateDesc();
    List<Transaction> findAllByOrderByAmountDesc();
    List<Transaction> findAllByUserOrderByAmountDesc(User user);

    List<Transaction> findByUser(User user);

    Optional<Transaction> findByIdAndUser(Long id, User user);

    @Query("SELECT SUM(t.amount) FROM Transaction t")
    Double getTotalAmount();

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user")
    Double getTotalAmountForUser(@Param("user") User user);

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE FUNCTION('YEAR', t.date) = FUNCTION('YEAR', CURRENT_DATE)
          AND FUNCTION('MONTH', t.date) = FUNCTION('MONTH', CURRENT_DATE)
    """)
    BigDecimal getTotalIncomeThisMonth();

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE FUNCTION('YEAR', t.date) = FUNCTION('YEAR', CURRENT_DATE)
          AND FUNCTION('MONTH', t.date) = FUNCTION('MONTH', CURRENT_DATE)
          AND t.user = :user
    """)
    BigDecimal getTotalIncomeThisMonthForUser(@Param("user") User user);

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE FUNCTION('YEAR', t.date) = FUNCTION('YEAR', CURRENT_DATE)
          AND FUNCTION('MONTH', t.date) = FUNCTION('MONTH', CURRENT_DATE)
          AND t.user = :user
    """)
    BigDecimal getTotalExpensesThisMonthForUser(@Param("user") User user);

    @Query("SELECT t.category.categoryName, t FROM Transaction t ORDER BY t.category.categoryName")
    List<Object[]> findAllTransactionsGroupedByCategory();

    @Query("""
        SELECT FUNCTION('YEAR', t.date) as year,
               FUNCTION('MONTH', t.date) as month,
               SUM(t.amount) as balance
        FROM Transaction t
        WHERE FUNCTION('YEAR', t.date) = :year
        GROUP BY FUNCTION('YEAR', t.date), FUNCTION('MONTH', t.date)
        ORDER BY FUNCTION('YEAR', t.date), FUNCTION('MONTH', t.date)
    """)
    List<Object[]> getMonthlyNetBalances(@Param("year") int year);

    @Query("""
        SELECT FUNCTION('YEAR', t.date) as year,
               FUNCTION('MONTH', t.date) as month,
               SUM(t.amount) as balance
        FROM Transaction t
        WHERE FUNCTION('YEAR', t.date) = :year
          AND t.user = :user
        GROUP BY FUNCTION('YEAR', t.date), FUNCTION('MONTH', t.date)
        ORDER BY FUNCTION('YEAR', t.date), FUNCTION('MONTH', t.date)
    """)
    List<Object[]> getMonthlyNetBalancesForUser(@Param("year") int year, @Param("user") User user);
}
