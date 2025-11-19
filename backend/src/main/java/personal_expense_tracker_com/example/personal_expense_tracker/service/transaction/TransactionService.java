package personal_expense_tracker_com.example.personal_expense_tracker.service.transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.Transaction;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.transaction.TransactionRepository;

@Service
public class TransactionService {
 
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> findAll(){
        return transactionRepository.findAll();
    }

    public List<Transaction> findAllByUser(User user) {
        logger.debug("Finding all transactions for user: {}", user.getFirebaseUid());
        List<Transaction> transactions = transactionRepository.findByUser(user);
        logger.debug("Found {} transactions for user: {}", transactions.size(), user.getFirebaseUid());
        
        for (Transaction transaction : transactions) {
            if (transaction.getCategory() != null) {
                logger.debug("Transaction ID: {}, Category ID: {}, Category Name: {}", 
                    transaction.getId(), 
                    transaction.getCategory().getId(), 
                    transaction.getCategory().getCategoryName());
            } else {
                logger.warn("Transaction ID: {} has NULL category", transaction.getId());
            }
        }
        
        return transactions;
    }

    public BigDecimal getNetBalanceThisMonth(User user) {
        BigDecimal income = transactionRepository.getTotalIncomeThisMonthForUser(user);
        BigDecimal expenses = transactionRepository.getTotalExpensesThisMonthForUser(user);
        return income.subtract(expenses);
    }

    public List<Transaction> findByDateBetween(LocalDate start, LocalDate end) {
        return transactionRepository.findByDateBetween(start, end);
    }

    public List<Transaction> findByDateBetweenAndUser(LocalDate start, LocalDate end, User user) {
        return transactionRepository.findByDateBetweenAndUser(start, end, user);
    }
    
    public List<Transaction> findByAmountGreaterThan(Double amount){
        return transactionRepository.findByAmountGreaterThan(amount);
    }

    public List<Transaction> findByAmountGreaterThanAndUser(Double amount, User user){
        return transactionRepository.findByAmountGreaterThanAndUser(amount, user);
    }

    public List<Transaction> findByAmountLessThan(Double amount){
        return transactionRepository.findByAmountLessThan(amount);
    }

    public List<Transaction> findByAmountLessThanAndUser(Double amount, User user){
        return transactionRepository.findByAmountLessThanAndUser(amount, user);
    }

    public List<Transaction> findByCategoryCategoryName(String name){
        return transactionRepository.findByCategoryCategoryName(name);
    }

    public List<Transaction> findByCategoryCategoryNameAndUser(String name, User user){
        return transactionRepository.findByCategoryCategoryNameAndUser(name, user);
    }

    public List<Transaction> findByDescriptionContainingIgnoreCase(String keyword){
        return transactionRepository.findByDescriptionContainingIgnoreCase(keyword);
    }

    public List<Transaction> findByDescriptionContainingIgnoreCaseAndUser(String keyword, User user){
        return transactionRepository.findByDescriptionContainingIgnoreCaseAndUser(keyword, user);
    }

    public List<Transaction> findAllByOrderByDateDesc(){
        return transactionRepository.findAllByOrderByDateDesc();
    }

    public List<Transaction> findAllByOrderByAmountDesc(){
        return transactionRepository.findAllByOrderByAmountDesc();
    }

    public List<Transaction> findAllByUserOrderByAmountDesc(User user){
        return transactionRepository.findAllByUserOrderByAmountDesc(user);
    }

    public Double getTotalAmount(){
        return transactionRepository.getTotalAmount();
    }

    public Double getTotalAmount(User user){
        return transactionRepository.getTotalAmountForUser(user);
    }

    public BigDecimal getTotalExpensesThisMonth(User user){
        return transactionRepository.getTotalExpensesThisMonthForUser(user);
    }

    public BigDecimal getTotalIncomeThisMonth(){
        return transactionRepository.getTotalIncomeThisMonth();
    }

    public BigDecimal getTotalIncomeThisMonth(User user){
        return transactionRepository.getTotalIncomeThisMonthForUser(user);
    }

    public List<Object[]> findAllTransactionsGroupedByCategory(){
        return transactionRepository.findAllTransactionsGroupedByCategory();
    }

    public List<Map<String, Object>> getMonthlyNetBalances(int year) {
        List<Object[]> results = transactionRepository.getMonthlyNetBalances(year);
        
        List<Map<String, Object>> balances = new ArrayList<>();
        
        for (int month = 1; month <= 12; month++) {
            final int currentMonth = month;  // Create final copy
            String monthKey = String.format("%d-%02d", year, currentMonth);
            double balance = results.stream()
                .filter(r -> (int)r[1] == currentMonth)  // Use the final copy
                .findFirst()
                .map(r -> (double)r[2])
                .orElse(0.0);

            balances.add(Map.of(
                "month", monthKey,
                "balance", balance
            ));
        }
        
        return balances;
    }

    public List<Map<String, Object>> getMonthlyNetBalances(int year, User user) {
        List<Object[]> results = transactionRepository.getMonthlyNetBalancesForUser(year, user);
        
        List<Map<String, Object>> balances = new ArrayList<>();
        
        for (int month = 1; month <= 12; month++) {
            final int currentMonth = month;  
            String monthKey = String.format("%d-%02d", year, currentMonth);
            double balance = results.stream()
                .filter(r -> (int)r[1] == currentMonth)  
                .findFirst()
                .map(r -> (double)r[2])
                .orElse(0.0);

            balances.add(Map.of(
                "month", monthKey,
                "balance", balance
            ));
        }
        
        return balances;
    }

    public Transaction saveTransaction(Transaction transaction) {
        logger.debug("Saving transaction: {} for user: {}", transaction.getDescription(), transaction.getUser().getFirebaseUid());
        Transaction savedTransaction = transactionRepository.save(transaction);
        logger.debug("Saved transaction with ID: {} and type: {}", savedTransaction.getId(), savedTransaction.getType());
        return savedTransaction;
    }

    public Transaction updateTransaction(Long id, Transaction updatedTransaction, User user) {
        Optional<Transaction> optionalTransaction = transactionRepository.findByIdAndUser(id, user);
        if (optionalTransaction.isPresent()) {
            Transaction existingTransaction = optionalTransaction.get();
            existingTransaction.setAmount(updatedTransaction.getAmount());
            existingTransaction.setDescription(updatedTransaction.getDescription());
            existingTransaction.setDate(updatedTransaction.getDate());
            existingTransaction.setCategory(updatedTransaction.getCategory());
            return transactionRepository.save(existingTransaction);
        } else {
            return null;
        }
    }

    public boolean deleteTransaction(Long id, User user) {
        Optional<Transaction> optionalTransaction = transactionRepository.findByIdAndUser(id, user);
        if (optionalTransaction.isPresent()) {
            transactionRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public Transaction getTransactionByIdAndUser(Long id, User user) {
        return transactionRepository.findByIdAndUser(id, user).orElse(null);
    }
}