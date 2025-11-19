package personal_expense_tracker_com.example.personal_expense_tracker.repository.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.Category;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUser(User user);
    
    List<Category> findByUserOrderByCategoryNameAsc(User user);

    @Query("SELECT c FROM Category c WHERE c.id = :id AND c.user = :user")
    Optional<Category> findByIdAndUser(@Param("id") Long id, @Param("user") User user);

    List<Category> findByCategoryNameAndUser(String categoryName, User user);

    @Query("SELECT c FROM Category c WHERE c.categoryName = :categoryName")
    Optional<Category> findByCategoryName(@Param("categoryName") String categoryName);

    boolean existsByCategoryNameAndUser(String categoryName, User user);

    boolean existsByCategoryName(String categoryName);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.category.categoryName = :name AND e.user = :user")
    Long countExpensesInCategoryForUser(@Param("name") String name, @Param("user") User user);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.category.categoryName = :name")
    Long countExpensesInCategory(@Param("name") String name);
}
