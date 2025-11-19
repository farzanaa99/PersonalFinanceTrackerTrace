package personal_expense_tracker_com.example.personal_expense_tracker.repository.income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.Income;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findByUser(User user);
    
    List<Income> findByUserOrderByDateDesc(User user);

    @Query("SELECT i FROM Income i WHERE i.id = :id AND i.user = :user")
    Optional<Income> findByIdAndUser(@Param("id") Long id, @Param("user") User user);

    List<Income> findByCategoryCategoryNameAndUser(String categoryName, User user);

    @Query("SELECT i FROM Income i WHERE i.category.categoryName = :categoryName")
    List<Income> findIncomesByCategoryName(@Param("categoryName") String categoryName);

    List<Income> findByDescriptionContainingAndUser(String keyword, User user);

    List<Income> findByUserOrderByDateAsc(User user);

    List<Income> findByDateBetweenAndUser(LocalDate start, LocalDate end, User user);

    List<Income> findByDateBetween(LocalDate start, LocalDate end);

    List<Income> findByAmountGreaterThanAndUser(Double amount, User user);

    List<Income> findTop5ByUserOrderByDateDesc(User user);

    Long countByCategoryCategoryNameAndUser(String name, User user);

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.user = :user")
    Double getTotalIncomeForUser(@Param("user") User user);
    
    @Query("SELECT SUM(i.amount) FROM Income i")
    Double getTotalIncome();

    void deleteIncomeById(Long id);
    
    @Query("SELECT i FROM Income i WHERE i.category.id = :categoryId AND i.user = :user")
    List<Income> findByCategoryIdAndUser(@Param("categoryId") Long categoryId, @Param("user") User user);
    
    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.category.id = :categoryId AND i.user = :user")
    Double getTotalIncomeByCategoryForUser(@Param("categoryId") Long categoryId, @Param("user") User user);
    
    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.date BETWEEN :startDate AND :endDate AND i.user = :user")
    Double getTotalIncomeByDateRangeForUser(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("user") User user);

    List<Income> findTop5ByOrderByDateDesc();

}
