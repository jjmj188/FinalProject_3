package com.spring.app.config; // 패키지명은 현재 프로젝트에 맞게 유지

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
@EnableMethodSecurity // 컨트롤러에서 @PreAuthorize 사용을 위해 추가
public class SecurityConfig {

    // 우리가 만든 커스텀 로그인 성공 핸들러 의존성 주입
    @Autowired
    private MyAuthenticationSuccessHandler successHandler;

    // 비밀번호 암호화 객체
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. CSRF 공격 방어 비활성화 (AJAX POST 요청을 위해 필수)
            .csrf(csrf -> csrf.disable()) 
            
            // 2. iframe 허용 (스마트에디터나 약관 동의 iframe 사용 시 필수)
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            )
            
            // 3. 권한 설정
            .authorizeHttpRequests(auth -> auth
                // View 포워딩 및 에러 페이지 발생 시 시큐리티가 막지 않도록 허용
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                
                // ★ 핵심 수정 부분: 시세조회, 장터, 경매장 등 누구나 볼 수 있어야 하는 메뉴의 경로를 추가했습니다!
                // (주의: 실제 Controller에 연결하신 URL 매핑 주소와 똑같이 맞춰주셔야 합니다.)
                .requestMatchers(
                    "/", 
                    "/index.up", 
                    "/member/**"     
                ).permitAll() 
                
                .anyRequest().authenticated() 
            )
            
            // 4. 폼 로그인 설정
            .formLogin(form -> form
                .loginPage("/member/login")              // 커스텀 로그인 페이지 URL
                .loginProcessingUrl("/login/process")    // HTML 폼의 action 경로
                .usernameParameter("email")              // 이메일 파라미터
                .passwordParameter("password")           // 비밀번호 파라미터명
                .successHandler(successHandler)          // 커스텀 성공 핸들러 장착
                .permitAll()
            )
            
            // 5. 로그아웃 설정
            .logout(logout -> logout
                .logoutUrl("/logout")                    // 로그아웃 처리 URL
                .logoutSuccessUrl("/")                   // 로그아웃 성공 시 메인으로 이동
                .invalidateHttpSession(true)             // 세션 날리기
                .deleteCookies("JSESSIONID")             // 쿠키 날리기
                .permitAll()
            );

        return http.build();
    }

    // 정적 리소스(CSS, JS, 이미지 등) 시큐리티 필터 무시
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