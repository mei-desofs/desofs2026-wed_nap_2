package isep.desosfs.arcadehaven.Controller;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Dto.Response.GameResponse;
import isep.desosfs.arcadehaven.Dto.Response.UserResponse;
import isep.desosfs.arcadehaven.Service.AdminService;
import isep.desosfs.arcadehaven.Service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final GameService gameService;

    public AdminController(AdminService adminService, GameService gameService) {
        this.adminService = adminService;
        this.gameService = gameService;
    }

    // User management
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.deactivateUser(id));
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.activateUser(id));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> changeUserRole(@PathVariable UUID id,
                                                        @RequestParam Role role) {
        return ResponseEntity.ok(adminService.changeUserRole(id, role));
    }

    // Game management
    @GetMapping("/games")
    public ResponseEntity<List<GameResponse>> getAllGames() {
        return ResponseEntity.ok(gameService.getAllGames());
    }

    @PatchMapping("/games/{id}/approve")
    public ResponseEntity<GameResponse> approveGame(@PathVariable UUID id) {
        return ResponseEntity.ok(gameService.approveGame(id));
    }

    @PatchMapping("/games/{id}/remove")
    public ResponseEntity<GameResponse> removeGame(@PathVariable UUID id) {
        return ResponseEntity.ok(gameService.removeGame(id));
    }
}
