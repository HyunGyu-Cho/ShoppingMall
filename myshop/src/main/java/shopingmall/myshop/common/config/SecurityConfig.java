package shopingmall.myshop.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // csrf 기능 끄기
            .csrf(csrf -> csrf.disable())
                // formLogin 기능 끄기
            .formLogin(form -> form.disable())
                // HttpBasic 인증 끄기
            .httpBasic(basic -> basic.disable())
            .authorizeHttpRequests(auth -> auth
                                        .requestMatchers(HttpMethod.POST, "/api/members").permitAll()
                                        .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                                        .anyRequest().permitAll()
            )   
            .build();
    }
}
