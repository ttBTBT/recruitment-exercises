package cn.openatom.events;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="app_users", uniqueConstraints=@UniqueConstraint(name="uk_username", columnNames="username"))
public class User {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, length=50) private String username;
    @Column(name="password_hash", nullable=false, length=100) private String passwordHash;
    @Column(name="registered_at", nullable=false) private Instant registeredAt;
    protected User() {}
    public User(String username, String passwordHash) {
        this.username=username; this.passwordHash=passwordHash; this.registeredAt=Instant.now();
    }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Instant getRegisteredAt() { return registeredAt; }
}
