package antifraud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    UserRepository userRepo;
    @Autowired
    PasswordEncoder encoder;

    public User register(User user) {
        if (userRepo.existsByUsername(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        user.setPassword(encoder.encode(user.getPassword()));
        if (userRepo.count() == 0) {
            user.setRole(UserRole.ADMINISTRATOR);
        } else {
            user.setRole(UserRole.MERCHANT);
            user.setLocked(true);
        }
        userRepo.save(user);
        return user;
    }

    public List<User> getUsers() {
        return userRepo.findAll();
    }

    public Map<String, String> deleteUser(String username) {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        userRepo.delete(user);
        return Map.of(
                "username", username,
                "status", "Deleted successfully!");
    }

    public User setRole(RoleChangeRequest request) {
        if (!userRepo.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        User user = userRepo.findByUsername(request.getUsername());
        if (!user.getRole().isChangeable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        UserRole role;
        try {
            role = UserRole.valueOf(request.getRole());
            if (!role.isChangeable()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (role == user.getRole()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        user.setRole(role);
        userRepo.save(user);
        return user;
    }

    public Map<String, String> switchLocked(SwitchLockedRequest request) {
        if (!userRepo.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        User user = userRepo.findByUsername(request.getUsername());
        if (user.getRole() == UserRole.ADMINISTRATOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        boolean locking = "LOCK".equalsIgnoreCase(request.getOperation());
        user.setLocked(locking);
        userRepo.save(user);
        String msg = String.format("User %s %s!", user.getUsername(), locking ? "locked" : "unlocked");
        return Map.of("status", msg);
    }
}
