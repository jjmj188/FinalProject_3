package com.spring.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseCookie;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.spring.app.security.jwt.JwtAccessDeniedHandler;
import com.spring.app.security.jwt.JwtAuthenticationEntryPoint;
import com.spring.app.security.jwt.JwtAuthenticationFilter;
import com.spring.app.security.jwt.JwtTokenProvider;
import com.spring.app.security.loginsuccess.MyAuthenticationSuccessHandler;
import com.spring.app.security.model.MemberDAO;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.Cookie;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private MyAuthenticationSuccessHandler successHandler;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MemberDAO memberDAO;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // JwtAuthenticationFilter 빈 등록
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            )
            .httpBasic(httpBasic -> httpBasic.disable())

            // 세션 + JWT 병행 유지
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/chat/**").permitAll()
                .requestMatchers("/ws-chat/**").permitAll()
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()

                // 관리자
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // 로그인 필요
                .requestMatchers(
                    "/mypage/**",
                    "/product/sell",
                    "/product/sellRegister",
                    "/product/wishlist/**"
                ).authenticated()

                // 공개 페이지
                .requestMatchers(
                    "/",
                    "/member/**",
                    "/index.up",
                    "/security/**",
                    "/product/product_list",
                    "/product/product_list_more",
                    "/product/price_check",
                    "/product/share",
                    "/product/product_detail/**",
                    "/product/product_user_profile",
                    "/product/wordSearchShow",
                    "/actuator/**",
                    "/adminupload/**"
                ).permitAll()

                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/security/login")
                .loginProcessingUrl("/security/login/process")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureUrl("/security/login?error=true")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/security/logout")
                .addLogoutHandler((request, response, authentication) -> {
                    Cookie[] cookies = request.getCookies();

                    if (cookies != null) {
                        for (Cookie cookie : cookies) {
                            if ("refreshToken".equals(cookie.getName())) {
                                try {
                                    String email = jwtTokenProvider.getEmailFromToken(cookie.getValue());
                                    memberDAO.deleteRefreshToken(email);
                                } catch (Exception ignored) {}
                                break;
                            }
                        }
                    }

                    ResponseCookie deletedAccess = ResponseCookie.from("accessToken", "")
                            .httpOnly(true)
                            .path("/")
                            .maxAge(0)
                            .build();

                    ResponseCookie deletedRefresh = ResponseCookie.from("refreshToken", "")
                            .httpOnly(true)
                            .path("/")
                            .maxAge(0)
                            .build();

                    response.addHeader("Set-Cookie", deletedAccess.toString());
                    response.addHeader("Set-Cookie", deletedRefresh.toString());
                })
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // JWT 필터 추가
            .addFilterBefore(jwtAuthenticationFilter(),
                             UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/bootstrap-4.6.2-dist/**",
                                 "/css/**",
                                 "/Highcharts-10.3.1/**",
                                 "/images/**",
                                 "/upload/**",
                                 "/jquery-ui-1.13.1.custom/**",
                                 "/js/**",
                                 "/smarteditor/**");
    }
}