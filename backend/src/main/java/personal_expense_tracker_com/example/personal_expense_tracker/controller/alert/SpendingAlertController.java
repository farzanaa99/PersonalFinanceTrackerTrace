package personal_expense_tracker_com.example.personal_expense_tracker.controller.alert;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.SpendingAlert;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.service.alert.SpendingAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/spending-alerts")
public class SpendingAlertController {

    @Autowired
    private SpendingAlertService spendingAlertService;

    @GetMapping
    public ResponseEntity<List<SpendingAlert>> getUnreadAlerts(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<SpendingAlert> alerts = spendingAlertService.getUnreadAlerts(currentUser);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getUnreadAlertCount(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        long count = spendingAlertService.getUnreadAlertCount(currentUser);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/type/{alertType}")
    public ResponseEntity<List<SpendingAlert>> getAlertsByType(@PathVariable String alertType, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<SpendingAlert> alerts = spendingAlertService.getAlertsByType(alertType, currentUser);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<SpendingAlert>> getAlertsByCategory(@PathVariable Long categoryId, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        List<SpendingAlert> alerts = spendingAlertService.getAlertsByCategory(categoryId, currentUser);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<SpendingAlert>> getRecentAlerts(
            @RequestParam(defaultValue = "24") int hours, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<SpendingAlert> alerts = spendingAlertService.getRecentAlerts(since, currentUser);
        return ResponseEntity.ok(alerts);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAlertAsRead(@PathVariable Long id) {
        spendingAlertService.markAlertAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAlertsAsRead(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        spendingAlertService.markAllAlertsAsRead(currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate/budget")
    public ResponseEntity<Void> generateBudgetAlerts(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        spendingAlertService.generateBudgetAlerts(currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate/weekend")
    public ResponseEntity<Void> generateWeekendSpendingAlert(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        spendingAlertService.generateWeekendSpendingAlert(currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate/unusual")
    public ResponseEntity<Void> generateUnusualSpendingAlert(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        spendingAlertService.generateUnusualSpendingAlert(currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate/all")
    public ResponseEntity<Void> generateAllAlerts(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        spendingAlertService.generateBudgetAlerts(currentUser);
        spendingAlertService.generateWeekendSpendingAlert(currentUser);
        spendingAlertService.generateUnusualSpendingAlert(currentUser);
        return ResponseEntity.ok().build();
    }
} 
