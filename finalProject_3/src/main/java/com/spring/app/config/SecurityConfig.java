package com.spring.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.spring.app.security.loginsuccess.MyAuthenticationSuccessHandler;

import jakarta.servlet.DispatcherType;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 컨트롤러에서 @PreAuthorize 사용을 위해 필수
public class SecurityConfig {

    @Autowired
    private MyAuthenticationSuccessHandler successHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            )
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                
                // ★ 누구나 볼 수 있는 주소 허용
                .requestMatchers(
                    "/", 
                    "/member/**",
                    "/index.up",
                    "/security/**",       // 회원가입, 로그인 관련 모두 허용
                    "/product/product_list",      // 장터
                    "/product/price_check", // 시세조회
                    "/product/auction",   // 경매장
                    "/product/share",
                    "/product/product_detail", // 상품 상세
                    "/product/product_user_profile"
                ).permitAll() 
                
                // 위에서 허용한 URL 외의 요청(예: /product/sell)은 자동 로그인 요구
                .anyRequest().authenticated() 
            )
            .formLogin(form -> form
                // ★ 불일치 수정: MemberController의 @RequestMapping("/security/")에 맞춤
                .loginPage("/security/login")            
                .loginProcessingUrl("/security/login/process") // HTML form의 action 경로와 일치시킬 것
                .usernameParameter("email")              
                .passwordParameter("password")           
                .successHandler(successHandler)          
                .failureUrl("/security/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/security/logout") 
                .logoutSuccessUrl("/") 
                .invalidateHttpSession(true) 
                .deleteCookies("JSESSIONID") 
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/bootstrap-4.6.2-dist/**", 
                                 "/css/**", 
                                 "/Highcharts-10.3.1/**",
                                 "/images/**", 
                                 "/jquery-ui-1.13.1.custom/**", 
                                 "/js/**", 
                                 "/smarteditor/**");  
    }
}