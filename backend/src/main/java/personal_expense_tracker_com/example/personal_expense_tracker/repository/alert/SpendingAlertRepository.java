package personal_expense_tracker_com.example.personal_expense_tracker.repository.alert;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.SpendingAlert;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SpendingAlertRepository extends JpaRepository<SpendingAlert, Long> {

    List<SpendingAlert> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    List<SpendingAlert> findByIsReadFalseOrderByCreatedAtDesc();
    
    List<SpendingAlert> findByAlertTypeAndUserOrderByCreatedAtDesc(String alertType, User user);
    
    List<SpendingAlert> findByAlertTypeOrderByCreatedAtDesc(String alertType);
    
    @Query("SELECT sa FROM SpendingAlert sa WHERE sa.category.id = :categoryId AND sa.user = :user ORDER BY sa.createdAt DESC")
    List<SpendingAlert> findByCategoryIdAndUser(@Param("categoryId") Long categoryId, @Param("user") User user);
    
    @Query("SELECT sa FROM SpendingAlert sa WHERE sa.category.id = :categoryId ORDER BY sa.createdAt DESC")
    List<SpendingAlert> findByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT sa FROM SpendingAlert sa WHERE sa.createdAt >= :since AND sa.user = :user ORDER BY sa.createdAt DESC")
    List<SpendingAlert> findRecentAlertsForUser(@Param("since") LocalDateTime since, @Param("user") User user);
    
    @Query("SELECT sa FROM SpendingAlert sa WHERE sa.createdAt >= :since ORDER BY sa.createdAt DESC")
    List<SpendingAlert> findRecentAlerts(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(sa) FROM SpendingAlert sa WHERE sa.isRead = false AND sa.user = :user")
    long countUnreadAlertsForUser(@Param("user") User user);
    
    @Query("SELECT COUNT(sa) FROM SpendingAlert sa WHERE sa.isRead = false")
    long countUnreadAlerts();
} 
