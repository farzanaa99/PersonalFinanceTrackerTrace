package personal_expense_tracker_com.example.personal_expense_tracker.service.user;

import personal_expense_tracker_com.example.personal_expense_tracker.entity.User;
import personal_expense_tracker_com.example.personal_expense_tracker.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(String firebaseUid, String email, String displayName) {
        User user = new User(firebaseUid, email, displayName);
        return userRepository.save(user);
    }

    public Optional<User> getUserByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getOrCreateUser(String firebaseUid, String email, String displayName) {
        Optional<User> existingUser = userRepository.findByFirebaseUid(firebaseUid);
        if (existingUser.isPresent()) {
            return existingUser.get();
        } else {
            return createUser(firebaseUid, email, displayName);
        }
    }

    public boolean userExists(String firebaseUid) {
        return userRepository.existsByFirebaseUid(firebaseUid);
    }

    public User updateUserDisplayName(String firebaseUid, String displayName) {
        Optional<User> user = userRepository.findByFirebaseUid(firebaseUid);
        if (user.isPresent()) {
            User existingUser = user.get();
            existingUser.setDisplayName(displayName);
            return userRepository.save(existingUser);
        }
        return null;
    }

    public void deactivateUser(String firebaseUid) {
        Optional<User> user = userRepository.findByFirebaseUid(firebaseUid);
        if (user.isPresent()) {
            User existingUser = user.get();
            existingUser.setActive(false);
            userRepository.save(existingUser);
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
} 
