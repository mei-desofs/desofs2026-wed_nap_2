package isep.desosfs.arcadehaven.Domain;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    public void deactivate() {
        this.active = false;
    }

    public void changeRole(Role role) {
        if (this.role == Role.ADMIN) {
            throw new RuntimeException("Cannot change admin role");
        }
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }
}