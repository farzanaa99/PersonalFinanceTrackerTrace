package personal_expense_tracker_com.example.personal_expense_tracker.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.service.auth.FirebaseAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

    @Autowired
    private FirebaseAuthService firebaseAuthService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        logger.debug("AuthInterceptor called for: {}", requestURI);
        
        if (requestURI.startsWith("/api/auth") || 
            requestURI.equals("/error") || 
            request.getMethod().equals("OPTIONS")) {
            logger.debug("Skipping auth for: {}", requestURI);
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("Authorization header: {}", authHeader != null ? "present" : "missing");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return false;
        }

        String idToken = authHeader.substring(7);
        logger.debug("Token extracted, length: {}", idToken.length());

        try {
            User user = firebaseAuthService.authenticateUser(idToken);
            
            request.setAttribute("currentUser", user);
            request.setAttribute("firebaseUid", user.getFirebaseUid());
            
            logger.debug("Authentication successful for user: {}", user.getFirebaseUid());
            return true;
        } catch (Exception e) {
            logger.debug("Authentication failed: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token: " + e.getMessage());
            return false;
        }
    }
} 
