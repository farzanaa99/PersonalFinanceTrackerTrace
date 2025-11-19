package personal_expense_tracker_com.example.personal_expense_tracker.repository.savings;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.SavingsGoal;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    List<SavingsGoal> findByUserAndIsActiveTrue(User user);
    
    List<SavingsGoal> findByIsActiveTrue();
    
    List<SavingsGoal> findByUserAndIsActiveTrueOrderByTargetDateAsc(User user);
    
    List<SavingsGoal> findByIsActiveTrueOrderByTargetDateAsc();
    
    @Query("SELECT sg FROM SavingsGoal sg WHERE sg.isActive = true AND sg.currentAmount >= sg.targetAmount AND sg.user = :user")
    List<SavingsGoal> findCompletedGoalsForUser(@Param("user") User user);
    
    @Query("SELECT sg FROM SavingsGoal sg WHERE sg.isActive = true AND sg.currentAmount >= sg.targetAmount")
    List<SavingsGoal> findCompletedGoals();
    
    @Query("SELECT sg FROM SavingsGoal sg WHERE sg.isActive = true AND sg.currentAmount < sg.targetAmount AND sg.targetDate < :today AND sg.user = :user")
    List<SavingsGoal> findOverdueGoalsForUser(@Param("today") LocalDate today, @Param("user") User user);
    
    @Query("SELECT sg FROM SavingsGoal sg WHERE sg.isActive = true AND sg.currentAmount < sg.targetAmount AND sg.targetDate < :today")
    List<SavingsGoal> findOverdueGoals(@Param("today") LocalDate today);
    
    @Query("SELECT sg FROM SavingsGoal sg WHERE sg.isActive = true AND sg.targetDate BETWEEN :startDate AND :endDate AND sg.user = :user")
    List<SavingsGoal> findGoalsDueBetweenForUser(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("user") User user);
    
    @Query("SELECT sg FROM SavingsGoal sg WHERE sg.isActive = true AND sg.targetDate BETWEEN :startDate AND :endDate")
    List<SavingsGoal> findGoalsDueBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT sg FROM SavingsGoal sg WHERE sg.category.id = :categoryId AND sg.isActive = true AND sg.user = :user")
    List<SavingsGoal> findByCategoryIdAndUser(@Param("categoryId") Long categoryId, @Param("user") User user);
    
    @Query("SELECT sg FROM SavingsGoal sg WHERE sg.category.id = :categoryId AND sg.isActive = true")
    List<SavingsGoal> findByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT sg FROM SavingsGoal sg WHERE sg.isActive = true AND sg.user = :user AND (:categoryId IS NULL OR sg.category.id = :categoryId)")
    List<SavingsGoal> findByUserAndOptionalCategory(@Param("user") User user, @Param("categoryId") Long categoryId);
    
    @Query("SELECT sg FROM SavingsGoal sg WHERE sg.isActive = true AND sg.targetAmount > 0 AND (sg.currentAmount / sg.targetAmount * 100) >= :minProgress AND sg.user = :user ORDER BY sg.targetDate ASC")
    List<SavingsGoal> findGoalsWithMinProgressForUser(@Param("minProgress") double minProgress, @Param("user") User user);
    
    @Query("SELECT sg FROM SavingsGoal sg WHERE sg.isActive = true AND sg.targetAmount > 0 AND (sg.currentAmount / sg.targetAmount * 100) >= :minProgress ORDER BY sg.targetDate ASC")
    List<SavingsGoal> findGoalsWithMinProgress(@Param("minProgress") double minProgress);
    
    @Query("SELECT COUNT(sg) FROM SavingsGoal sg WHERE sg.isActive = true AND sg.currentAmount >= sg.targetAmount AND sg.user = :user")
    long countCompletedGoalsForUser(@Param("user") User user);
    
    @Query("SELECT COUNT(sg) FROM SavingsGoal sg WHERE sg.isActive = true AND sg.currentAmount >= sg.targetAmount")
    long countCompletedGoals();
    
    @Query("SELECT COUNT(sg) FROM SavingsGoal sg WHERE sg.isActive = true AND sg.currentAmount < sg.targetAmount AND sg.user = :user")
    long countActiveGoalsForUser(@Param("user") User user);
    
    @Query("SELECT COUNT(sg) FROM SavingsGoal sg WHERE sg.isActive = true AND sg.currentAmount < sg.targetAmount")
    long countActiveGoals();
} 
