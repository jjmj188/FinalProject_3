package com.spring.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정.
 * - 회원가입/로그인 페이지 및 처리 URL 허용
 * - 폼 로그인: /member/loginEnd, 파라미터 email, password
 * - 비밀번호 암호화: BCryptPasswordEncoder (회원가입 시 사용)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(auth -> auth
				/* 로그인/회원가입: 비인증 허용 */
				.requestMatchers("/member/login", "/member/register", "/member/registerEnd").permitAll()
				.requestMatchers("/member/emailDuplicateCheck", "/member/nicknameDuplicateCheck").permitAll()
				/* 정적 리소스 */
				.requestMatchers("/css/**", "/js/**", "/images/**", "/smarteditor/**", "/jquery-ui-1.13.1.custom/**", "/Highcharts-10.3.1/**").permitAll()
				.requestMatchers("/favicon.ico", "/error").permitAll()
				/* 메인 등 기타: 인증 후 접근 (필요 시 permitAll 로 변경 가능) */
				.requestMatchers("/", "/index", "/index/**").permitAll()
				.anyRequest().authenticated()
			)
			.formLogin(form -> form
				.loginPage("/member/login")
				.loginProcessingUrl("/member/loginEnd")
				.usernameParameter("email")
				.passwordParameter("password")
				.defaultSuccessUrl("/", true)
				.failureUrl("/member/login?error=true")
			)
			.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/")
				.invalidateHttpSession(true)
			)
			.csrf(csrf -> csrf.configure(http));

		return http.build();
	}

	/**
	 * 회원가입 시 비밀번호 암호화에 사용.
	 * 로그인 검증 시에도 Spring Security가 이 Encoder로 비밀번호를 비교합니다.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
