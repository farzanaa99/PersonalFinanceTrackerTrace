package personal_expense_tracker_com.example.personal_expense_tracker.controller.recurring;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.RecurringTransaction;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.service.recurring.RecurringTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/recurring-transactions")

public class RecurringTransactionController {

    @Autowired
    private RecurringTransactionService recurringTransactionService;

    @GetMapping
    public ResponseEntity<List<RecurringTransaction>> getAllActiveRecurringTransactions(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<RecurringTransaction> transactions = recurringTransactionService.getAllActiveRecurringTransactions(currentUser);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<RecurringTransaction>> getRecurringTransactionsByType(
            @PathVariable String type, HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            RecurringTransaction.TransactionType transactionType = 
                RecurringTransaction.TransactionType.valueOf(type.toUpperCase());
            List<RecurringTransaction> transactions = 
                recurringTransactionService.getRecurringTransactionsByType(transactionType, currentUser);
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<RecurringTransaction>> getRecurringTransactionsByCategory(
            @PathVariable Long categoryId, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<RecurringTransaction> transactions = 
            recurringTransactionService.getRecurringTransactionsByCategory(categoryId, currentUser);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/pattern/{pattern}")
    public ResponseEntity<List<RecurringTransaction>> getRecurringTransactionsByPattern(
            @PathVariable String pattern, HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            RecurringTransaction.RecurrencePattern recurrencePattern = 
                RecurringTransaction.RecurrencePattern.valueOf(pattern.toUpperCase());
            List<RecurringTransaction> transactions = 
                recurringTransactionService.getRecurringTransactionsByPattern(recurrencePattern, currentUser);
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecurringTransaction> getRecurringTransactionById(@PathVariable Long id) {
        Optional<RecurringTransaction> transaction = recurringTransactionService.getRecurringTransactionById(id);
        return transaction.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RecurringTransaction> createRecurringTransaction(
            @RequestBody RecurringTransaction recurringTransaction, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        recurringTransaction.setUser(currentUser);
        RecurringTransaction created = recurringTransactionService.createRecurringTransaction(recurringTransaction);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransaction> updateRecurringTransaction(
            @PathVariable Long id, @RequestBody RecurringTransaction updatedTransaction) {
        RecurringTransaction updated = recurringTransactionService.updateRecurringTransaction(id, updatedTransaction);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateRecurringTransaction(@PathVariable Long id) {
        recurringTransactionService.deactivateRecurringTransaction(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/process-due")
    public ResponseEntity<Void> processDueRecurringTransactions() {
        recurringTransactionService.processDueRecurringTransactions();
        return ResponseEntity.ok().build();
    }
} 
