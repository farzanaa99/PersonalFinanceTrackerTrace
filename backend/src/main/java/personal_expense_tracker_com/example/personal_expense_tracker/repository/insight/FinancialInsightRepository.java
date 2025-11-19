package personal_expense_tracker_com.example.personal_expense_tracker.repository.insight;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.FinancialInsight;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FinancialInsightRepository extends JpaRepository<FinancialInsight, Long> {

    List<FinancialInsight> findByUserAndInsightTypeOrderByCalculatedAtDesc(User user, String insightType);
    
    List<FinancialInsight> findByInsightTypeOrderByCalculatedAtDesc(String insightType);
    
    @Query("SELECT fi FROM FinancialInsight fi WHERE fi.category.id = :categoryId AND fi.user = :user ORDER BY fi.calculatedAt DESC")
    List<FinancialInsight> findByCategoryIdAndUser(@Param("categoryId") Long categoryId, @Param("user") User user);
    
    @Query("SELECT fi FROM FinancialInsight fi WHERE fi.category.id = :categoryId ORDER BY fi.calculatedAt DESC")
    List<FinancialInsight> findByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT fi FROM FinancialInsight fi WHERE fi.calculatedAt >= :since AND fi.user = :user ORDER BY fi.calculatedAt DESC")
    List<FinancialInsight> findRecentInsightsForUser(@Param("since") LocalDateTime since, @Param("user") User user);
    
    @Query("SELECT fi FROM FinancialInsight fi WHERE fi.calculatedAt >= :since ORDER BY fi.calculatedAt DESC")
    List<FinancialInsight> findRecentInsights(@Param("since") LocalDateTime since);
    
    @Query("SELECT fi FROM FinancialInsight fi WHERE fi.period = :period AND fi.user = :user ORDER BY fi.calculatedAt DESC")
    List<FinancialInsight> findByPeriodAndUser(@Param("period") String period, @Param("user") User user);
    
    @Query("SELECT fi FROM FinancialInsight fi WHERE fi.period = :period ORDER BY fi.calculatedAt DESC")
    List<FinancialInsight> findByPeriod(@Param("period") String period);
    
    @Query("SELECT fi FROM FinancialInsight fi WHERE fi.user = :user ORDER BY fi.calculatedAt DESC LIMIT :limit")
    List<FinancialInsight> findLatestInsightsForUser(@Param("limit") int limit, @Param("user") User user);
    
    @Query("SELECT fi FROM FinancialInsight fi ORDER BY fi.calculatedAt DESC LIMIT :limit")
    List<FinancialInsight> findLatestInsights(@Param("limit") int limit);
} 
