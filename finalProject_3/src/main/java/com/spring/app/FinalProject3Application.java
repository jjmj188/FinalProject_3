package com.spring.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


//프로젝트명+Application 으로 되어진 클래스가 프로젝트가 동작할 수 있게 해주는 시작점인 메인 클래스 파일이다. 
//이 클래스에는 @SpringBootApplication 어노테이션이 붙어있는데  @SpringBootApplication 어노테이션 속에는 
//@ComponentScan, @SpringBootConfiguration, @EnableAutoConfiguration 등이 들어있기에 이러한 여러 어노테이션들이 다같이 사용되어진다.  
//@SpringBootApplication 어노테이션 속에 포함된 @ComponentScan 어노테이션의 기능은  
//프로젝트명+Application 클래스의 base-package 인 "com.spring.app" 및 그 하위(자식) package 에 있는 여러개의 클래스들 중 
//@Component 어노테이션이 붙어있는 클래스들은 bean 으로 만들어 주는데 이러한 bean 들을 모두 스캔해서 읽어오는 기능을 해주는 것이다.

/*
	Spring Security 라이브러리를 추가한 경우 Spring Security 기능이 추가되어 사이트에 접속하면 기본적으로 login 화면이 실행한다.
	스프링 부트의 Spring Security 기능을 제거하기 위해서는 아래처럼 @SpringBootApplication 애노테이션에
	exclude = SecurityAutoConfiguration.class 를 추가하면 된다.
*/
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@EnableAspectJAutoProxy  // Application 클래스에 @EnableAspectJAutoProxy 를 추가하여 AOP(Aspect Oriented Programming)클래스를 찾을 수 있게 해준다. 우리는 com.spring.app.aop.MyAop 이 AOP 클래스 이다.
//@EnableScheduling        // === #208. @EnableScheduling 어노테이션을 사용하여 스프링스케줄러를 구현하도록 한다. Scheduler는 기본적으로 Spring Boot에 포함되어 있기 때문에 추가적으로 build.gradle 파일에 dependency 를 추가할 필요는 없고, Application 클래스에 @EnableScheduling 를 추가만 해주면 Scheduler 를 기능을 사용할 수 있게 된다.
public class FinalProject3Application {

	public static void main(String[] args) {
		SpringApplication.run(FinalProject3Application.class, args);
	}
	/*
	   스프링부트 애플리케이션은 내장 WAS(Tomcat, Jetty, Undertow 등)를 구동시키면 자동적으로 이 main() 메서드가 시작진입점으로서 자동적으로 실행된다.
    이 main() 메서드 속에 있는 SpringApplication.run() 메서드를 호출하여 실행시키는데, 이 SpringApplication.run() 메서드의 기능은 해당 프로젝트 웹 애플리케이션을 실행하는데 필요한 모든 빈들을 스캔해서 작동이 되도록 구성(세팅)시켜 주는 것이다. 
	*/
}
