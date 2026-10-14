package cn.openatom.events;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    private final Validator validator;
    public AuthController(AuthService auth, Validator validator) { this.auth=auth; this.validator=validator; }
    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody AuthService.Credentials body) {
        auth.register(body); return ApiResponse.ok("注册成功",null);
    }
    @PostMapping("/login")
    public ApiResponse<AuthService.AuthResult> login(@AuthenticationPrincipal Jwt jwt,
            @RequestBody(required=false) AuthService.Credentials body) {
        if(jwt!=null) return ApiResponse.ok("登录成功",auth.resume(jwt));
        if(body==null || !validator.validate(body).isEmpty()) throw new BusinessException(400,"请填写合法的用户名和密码");
        return ApiResponse.ok("登录成功",auth.login(body));
    }
}
