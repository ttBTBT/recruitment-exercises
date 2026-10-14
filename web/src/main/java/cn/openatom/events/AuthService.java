package cn.openatom.events;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    public record Credentials(@NotBlank @Pattern(regexp="[A-Za-z0-9_]{3,50}", message="用户名须为3-50位字母、数字或下划线") String username,
                              @NotBlank @Size(min=8,max=72,message="密码长度须为8-72位") String password) {}
    public record AuthResult(String token, Long userId, String username, Instant expiresAt) {}
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final JwtEncoder encoder;
    public AuthService(UserRepository users, PasswordEncoder passwords, JwtEncoder encoder) {
        this.users=users; this.passwords=passwords; this.encoder=encoder;
    }
    @Transactional
    public void register(Credentials body) {
        if(body.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw new BusinessException(400,"密码UTF-8编码不能超过72字节");
        if(users.existsByUsername(body.username())) throw new BusinessException(409,"用户名已存在");
        users.saveAndFlush(new User(body.username(), passwords.encode(body.password())));
    }
    public AuthResult login(Credentials body) {
        User user=users.findByUsername(body.username()).orElseThrow(()->new BusinessException(401,"用户名或密码错误"));
        if(!passwords.matches(body.password(),user.getPasswordHash())) throw new BusinessException(401,"用户名或密码错误");
        Instant now=Instant.now(), expiry=now.plus(7,ChronoUnit.DAYS);
        JwtClaimsSet claims=JwtClaimsSet.builder().issuer("openatom-events").subject(user.getId().toString())
                .issuedAt(now).expiresAt(expiry).claim("username",user.getUsername()).build();
        String token=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
        return new AuthResult(token,user.getId(),user.getUsername(),expiry);
    }
    public AuthResult resume(Jwt jwt) {
        User user=users.findById(Long.parseLong(jwt.getSubject())).orElseThrow(()->new BusinessException(401,"用户已不存在"));
        // Preserve original expiry: returning to login must not extend the seven-day window.
        return new AuthResult(jwt.getTokenValue(),user.getId(),user.getUsername(),jwt.getExpiresAt());
    }
}
