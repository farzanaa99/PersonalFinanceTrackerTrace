package personal_expense_tracker_com.example.personal_expense_tracker.controller.auth;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.service.auth.FirebaseAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private FirebaseAuthService firebaseAuthService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            User user = firebaseAuthService.authenticateUser(loginRequest.getIdToken());
            
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("message", "Login successful");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestBody TokenRequest tokenRequest) {
        try {
            boolean isValid = firebaseAuthService.isValidToken(tokenRequest.getIdToken());
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            
            if (isValid) {
                User user = firebaseAuthService.authenticateUser(tokenRequest.getIdToken());
                response.put("user", user);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    public static class LoginRequest {
        private String idToken;

        public LoginRequest() {}

        public LoginRequest(String idToken) {
            this.idToken = idToken;
        }

        public String getIdToken() {
            return idToken;
        }

        public void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }

    public static class TokenRequest {
        private String idToken;

        public TokenRequest() {}

        public TokenRequest(String idToken) {
            this.idToken = idToken;
        }

        public String getIdToken() {
            return idToken;
        }

        public void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }
} 
