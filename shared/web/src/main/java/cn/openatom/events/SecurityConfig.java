package cn.openatom.events;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean SecretKeySpec jwtKey(@Value("${app.jwt-secret}") String secret) {
        byte[] bytes=secret.getBytes(StandardCharsets.UTF_8);
        if(bytes.length<32) throw new IllegalStateException("JWT_SECRET must contain at least 32 UTF-8 bytes");
        return new SecretKeySpec(bytes,"HmacSHA256");
    }
    @Bean JwtEncoder encoder(SecretKeySpec key) { return new NimbusJwtEncoder(new ImmutableSecret<>(key)); }
    @Bean JwtDecoder decoder(SecretKeySpec key) {
        NimbusJwtDecoder decoder=NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        JwtTimestampValidator time=new JwtTimestampValidator(Duration.ZERO);
        decoder.setJwtValidator(new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
                time, new JwtIssuerValidator("openatom-events"), jwt -> {
                    if(jwt.getExpiresAt()==null || !jwt.getSubject().matches("[0-9]+"))
                        return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
                                new org.springframework.security.oauth2.core.OAuth2Error("invalid_token"));
                    return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success();
                }));
        return decoder;
    }
    @Bean SecurityFilterChain security(HttpSecurity http, ObjectMapper json) throws Exception {
        var entry=(org.springframework.security.web.AuthenticationEntryPoint)(request,response,e)->{
            response.setStatus(401); response.setContentType("application/json;charset=UTF-8");
            json.writeValue(response.getWriter(),ApiResponse.error(401,"请登录，或登录凭证已过期"));
        };
        return http.csrf(x->x.disable()).sessionManagement(x->x.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(x->x.requestMatchers("/","/index.html","/error","/favicon.ico").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/auth/register","/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/events","/api/events/*").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(x->x.jwt(j->{}).authenticationEntryPoint(entry))
                .exceptionHandling(x->x.authenticationEntryPoint(entry).accessDeniedHandler((req,res,e)->{
                    res.setStatus(403); res.setContentType("application/json;charset=UTF-8");
                    json.writeValue(res.getWriter(),ApiResponse.error(403,"无权访问"));
                })).build();
    }
}
