package personal_expense_tracker_com.example.personal_expense_tracker.repository.recurring;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.RecurringTransaction;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    List<RecurringTransaction> findByUserAndIsActiveTrue(User user);
    
    List<RecurringTransaction> findByIsActiveTrue();
    
    List<RecurringTransaction> findByTransactionTypeAndUserAndIsActiveTrue(RecurringTransaction.TransactionType transactionType, User user);
    
    List<RecurringTransaction> findByTransactionTypeAndIsActiveTrue(RecurringTransaction.TransactionType transactionType);
    
    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.nextDueDate <= :date AND rt.isActive = true AND rt.user = :user")
    List<RecurringTransaction> findDueTransactionsForUser(@Param("date") LocalDate date, @Param("user") User user);
    
    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.nextDueDate <= :date AND rt.isActive = true")
    List<RecurringTransaction> findDueTransactions(@Param("date") LocalDate date);
    
    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.category.id = :categoryId AND rt.isActive = true AND rt.user = :user")
    List<RecurringTransaction> findByCategoryIdAndUser(@Param("categoryId") Long categoryId, @Param("user") User user);
    
    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.category.id = :categoryId AND rt.isActive = true")
    List<RecurringTransaction> findByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.recurrencePattern = :pattern AND rt.isActive = true AND rt.user = :user")
    List<RecurringTransaction> findByRecurrencePatternAndUser(@Param("pattern") RecurringTransaction.RecurrencePattern pattern, @Param("user") User user);
    
    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.recurrencePattern = :pattern AND rt.isActive = true")
    List<RecurringTransaction> findByRecurrencePattern(@Param("pattern") RecurringTransaction.RecurrencePattern pattern);
} 
