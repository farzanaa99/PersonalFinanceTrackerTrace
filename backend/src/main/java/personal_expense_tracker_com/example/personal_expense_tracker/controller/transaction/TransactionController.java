package personal_expense_tracker_com.example.personal_expense_tracker.controller.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.Category;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.Transaction;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.service.category.CategoryService;
import personal_expense_tracker_com.example.personal_expense_tracker.service.transaction.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;
    private final CategoryService categoryService;

    @Autowired
    public TransactionController(TransactionService transactionService, CategoryService categoryService) {
        this.transactionService = transactionService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<Transaction> getAllTransactions(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        logger.debug("Getting all transactions for user: {}", currentUser.getFirebaseUid());
        
        List<Transaction> transactions = transactionService.findAllByUser(currentUser);
        logger.debug("Retrieved {} transactions for user: {}", transactions.size(), currentUser.getFirebaseUid());
        
        // Additional debug logging for category issues
        for (Transaction transaction : transactions) {
            try {
                if (transaction.getCategory() != null) {
                    logger.debug("Transaction {}: Category ID={}, Name={}", 
                        transaction.getId(), 
                        transaction.getCategory().getId(), 
                        transaction.getCategory().getCategoryName());
                } else {
                    logger.warn("Transaction {}: Category is NULL", transaction.getId());
                }
            } catch (Exception e) {
                logger.error("Error accessing category for transaction {}: {}", transaction.getId(), e.getMessage());
            }
        }
        
        return transactions;
    }

    @GetMapping("/monthly-balance")
    public ResponseEntity<BigDecimal> getNetBalanceThisMonth(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        BigDecimal netBalance = transactionService.getNetBalanceThisMonth(currentUser);
        return new ResponseEntity<>(netBalance, HttpStatus.OK);
    }

    @GetMapping("/monthly-balances")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyBalances(
        @RequestParam int year, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<Map<String, Object>> balances = transactionService.getMonthlyNetBalances(year, currentUser);
        return ResponseEntity.ok(balances);
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<Transaction>> getTransactionsByDate(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<Transaction> transactions = transactionService.findByDateBetweenAndUser(start, end, currentUser);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/greater-than")
    public ResponseEntity<List<Transaction>> getTransactionsByAmountGreaterThan(@RequestParam Double amount, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<Transaction> transactions = transactionService.findByAmountGreaterThanAndUser(amount, currentUser);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/less-than")
    public ResponseEntity<List<Transaction>> getTransactionsByAmountLessThan(@RequestParam Double amount, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<Transaction> transactions = transactionService.findByAmountLessThanAndUser(amount, currentUser);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/by-category")
    public ResponseEntity<List<Transaction>> getTransactionsByCategory(@RequestParam String name, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<Transaction> transactions = transactionService.findByCategoryCategoryNameAndUser(name, currentUser);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/by-description")
    public ResponseEntity<List<Transaction>> getTransactionsByDescription(@RequestParam String keyword, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<Transaction> transactions = transactionService.findByDescriptionContainingIgnoreCaseAndUser(keyword, currentUser);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/order-by-amount")
    public ResponseEntity<List<Transaction>> getTransactionsOrderedByAmount(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<Transaction> transactions = transactionService.findAllByUserOrderByAmountDesc(currentUser);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/total-amount")
    public ResponseEntity<Double> getTotalAmount(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        Double totalAmount = transactionService.getTotalAmount(currentUser);
        return new ResponseEntity<>(totalAmount, HttpStatus.OK);
    }

    @GetMapping("/total-expenses")
    public ResponseEntity<BigDecimal> getTotalExpensesThisMonth(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        BigDecimal totalExpenses = transactionService.getTotalExpensesThisMonth(currentUser);
        return new ResponseEntity<>(totalExpenses, HttpStatus.OK);
    }

    @GetMapping("/total-income")
    public ResponseEntity<BigDecimal> getTotalIncomeThisMonth(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        BigDecimal totalIncome = transactionService.getTotalIncomeThisMonth(currentUser);
        return new ResponseEntity<>(totalIncome, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        
        logger.debug("Received Transaction with category ID: {}", 
            transaction.getCategory() != null ? transaction.getCategory().getId() : "null");
        
        // 1. Validate category exists
        if (transaction.getCategory() == null || transaction.getCategory().getId() == null) {
            logger.error("Transaction creation failed: No category provided");
            return ResponseEntity.badRequest().build();
        }
        
        // 2. Fetch and attach the full category
        Category category = categoryService.getCategoryByIdAndUser(
            transaction.getCategory().getId(), 
            currentUser
        );
        
        if (category == null) {
            logger.error("Category not found: {}", transaction.getCategory().getId());
            return ResponseEntity.badRequest().build();
        }
        
        // 3. Set the category and user
        transaction.setCategory(category);
        transaction.setUser(currentUser);
        
        Transaction createdTransaction = transactionService.saveTransaction(transaction);
        logger.debug("Created transaction with ID: {} and category: {}", 
            createdTransaction.getId(), createdTransaction.getCategory().getCategoryName());
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody Transaction updatedTransaction, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        
        logger.debug("Updating Transaction with ID: {} and category ID: {}", 
            id, updatedTransaction.getCategory() != null ? updatedTransaction.getCategory().getId() : "null");
        
        try {
            // If category is provided, validate it exists
            if (updatedTransaction.getCategory() != null && updatedTransaction.getCategory().getId() != null) {
                Category category = categoryService.getCategoryByIdAndUser(
                    updatedTransaction.getCategory().getId(), 
                    currentUser
                );
                
                if (category == null) {
                    logger.error("Category not found for update: {}", updatedTransaction.getCategory().getId());
                    return ResponseEntity.badRequest().build();
                }
                
                updatedTransaction.setCategory(category);
            }
            
            // Ensure user is set
            updatedTransaction.setUser(currentUser);
            
            Transaction transaction = transactionService.updateTransaction(id, updatedTransaction, currentUser);
            if (transaction != null) {
                return new ResponseEntity<>(transaction, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Failed to update transaction: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        boolean deleted = transactionService.deleteTransaction(id, currentUser);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        Transaction transaction = transactionService.getTransactionByIdAndUser(id, currentUser);
        if (transaction != null) {
            return new ResponseEntity<>(transaction, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}

