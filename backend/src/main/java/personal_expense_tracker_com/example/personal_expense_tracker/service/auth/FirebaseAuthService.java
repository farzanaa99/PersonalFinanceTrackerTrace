package personal_expense_tracker_com.example.personal_expense_tracker.service.auth;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

@Service
public class FirebaseAuthService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthService.class);

    @Autowired
    private UserService userService;

    public User authenticateUser(String idToken) throws Exception {
        try {
            logger.debug("Checking if Firebase is initialized...");
            if (FirebaseApp.getApps().isEmpty()) {
                logger.error("FirebaseApp with name [DEFAULT] doesn't exist.");
                throw new Exception("Firebase is not initialized");
            }
            
            logger.debug("Verifying Firebase token...");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(idToken).get();
            String firebaseUid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            String displayName = decodedToken.getName() != null ? decodedToken.getName() : email;

            logger.debug("Token verified successfully for user: {}", firebaseUid);
            return userService.getOrCreateUser(firebaseUid, email, displayName);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error verifying Firebase token: {}", e.getMessage(), e);
            throw new Exception("Invalid Firebase token: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during authentication: {}", e.getMessage(), e);
            throw e;
        }
    }

    public String getFirebaseUidFromToken(String idToken) throws Exception {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                throw new Exception("Firebase is not initialized");
            }
            
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(idToken).get();
            return decodedToken.getUid();
        } catch (InterruptedException | ExecutionException e) {
            throw new Exception("Invalid Firebase token: " + e.getMessage());
        }
    }

    public boolean isValidToken(String idToken) {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                return false;
            }
            
            FirebaseAuth.getInstance().verifyIdTokenAsync(idToken).get();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
} 
