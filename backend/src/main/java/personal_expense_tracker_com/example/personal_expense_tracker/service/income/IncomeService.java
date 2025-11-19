package personal_expense_tracker_com.example.personal_expense_tracker.service.income;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.Category;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.Income;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import personal_expense_tracker_com.example.personal_expense_tracker.repository.category.CategoryRepository;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.income.IncomeRepository;

@Service
public class IncomeService {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Income> findAll(){
        return incomeRepository.findAll();
    }

    public List<Income> findAllByUser(User user) {
        return incomeRepository.findByUserOrderByDateDesc(user);
    }
    
    public Income addIncome(Income income) {
        return incomeRepository.save(income);
    }
    
    public List<Income> getAllIncomes() {
        return incomeRepository.findAll(); 
    }
    
    public Income getIncomeById(Long id) {
        return incomeRepository.findById(id).orElse(null);
    }

    public Income getIncomeByIdAndUser(Long id, User user) {
        return incomeRepository.findByIdAndUser(id, user).orElse(null);
    }
    
    public Income updateIncome(Long id, Income updatedIncome) {
        Income existingIncome = incomeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Income not found with id: " + id));

        existingIncome.setDescription(updatedIncome.getDescription());
        existingIncome.setAmount(updatedIncome.getAmount());
        existingIncome.setDate(updatedIncome.getDate());

        if (updatedIncome.getCategory() != null && updatedIncome.getCategory().getId() != null) {
            Category category = categoryRepository.findById(updatedIncome.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + updatedIncome.getCategory().getId()));
            existingIncome.setCategory(category);
        }

        return incomeRepository.save(existingIncome);
    }

    public Income updateIncome(Long id, Income updatedIncome, User user) {
        Optional<Income> existingIncomeOpt = incomeRepository.findByIdAndUser(id, user);
        if (existingIncomeOpt.isEmpty()) {
            return null;
        }

        Income existingIncome = existingIncomeOpt.get();
        existingIncome.setDescription(updatedIncome.getDescription());
        existingIncome.setAmount(updatedIncome.getAmount());
        existingIncome.setDate(updatedIncome.getDate());

        if (updatedIncome.getCategory() != null && updatedIncome.getCategory().getId() != null) {
            Category category = categoryRepository.findByIdAndUser(updatedIncome.getCategory().getId(), user)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + updatedIncome.getCategory().getId()));
            existingIncome.setCategory(category);
        }

        return incomeRepository.save(existingIncome);
    }
    
    public void removeIncome(Long incomeId) {
        if (incomeRepository.existsById(incomeId)) {
            incomeRepository.deleteById(incomeId); 
        } else {
            throw new IllegalArgumentException("Income not found with id: " + incomeId);
        }
    }

    public void removeIncome(Long incomeId, User user) {
        Optional<Income> income = incomeRepository.findByIdAndUser(incomeId, user);
        if (income.isPresent()) {
            incomeRepository.deleteById(incomeId);
        } else {
            throw new IllegalArgumentException("Income not found with id: " + incomeId + " for user");
        }
    }
    
    public List<Income> findByCategoryName(String categoryName){
        return incomeRepository.findIncomesByCategoryName(categoryName);
    }

    public List<Income> findByCategoryNameAndUser(String categoryName, User user) {
        return incomeRepository.findByCategoryCategoryNameAndUser(categoryName, user);
    }
    
    public List<Income> findByDateBetween(LocalDate start, LocalDate end){
        return incomeRepository.findByDateBetween(start, end);
    }
    
    public Double getTotalIncomes(){
        return incomeRepository.getTotalIncome();
    }

    public Double getTotalIncomes(User user) {
        Double total = incomeRepository.getTotalIncomeForUser(user);
        return total != null ? total : 0.0;
    }

    public List<Income> findTop5ByOrderByDateDesc() {
        return incomeRepository.findTop5ByOrderByDateDesc();
    }

    public List<Income> findTop5ByUserOrderByDateDesc(User user) {
        return incomeRepository.findTop5ByUserOrderByDateDesc(user);
    }
}
    
