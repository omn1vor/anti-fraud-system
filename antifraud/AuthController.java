package antifraud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/user")
    @ResponseStatus(HttpStatus.CREATED)
    public User register(@Valid @RequestBody User user) {
        return authService.register(user);
    }

    @GetMapping("/list")
    public List<User> getUsers() {
        return authService.getUsers();
    }

    @DeleteMapping("/user/{username}")
    public Map<String, String> deleteUser(@PathVariable String username) {
        return authService.deleteUser(username);
    }

    @PutMapping("/role")
    public User setRole(@Valid @RequestBody RoleChangeRequest request) {
        return authService.setRole(request);
    }

    @PutMapping("/access")
    public Map<String, String> switchLocked(@Valid @RequestBody SwitchLockedRequest request) {
        return authService.switchLocked(request);
    }
}

class RoleChangeRequest {
    @NotNull
    String username;
    @NotNull
    String role;

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}

class SwitchLockedRequest {
    @NotNull
    String username;
    @NotNull @Pattern(regexp = "^(LOCK|UNLOCK)$")
    String operation;

    public String getUsername() {
        return username;
    }

    public String getOperation() {
        return operation;
    }
}