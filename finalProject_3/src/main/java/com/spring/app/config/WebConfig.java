package com.spring.app.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer{
	// URL 경로와 외부경로를 매핑시켜주는 설정 클래스 생성하기
	@Value("${file.upload-dir}")// import 시 org.springframework.beans.factory.annotation.Value 로 해야 한다.
	private String uploadDir;// 파일 업로드시 필요한 경로를 잡아주는 것이다. 값은 application.yml 파일에 설정해둔 값을 사용한다는 뜻이다.
	
	@Value("${file.photoupload-dir}") 
    private String photouploadDir; // 글쓰기시 스마트에디터를 통해 사진을 올리는 경로를 잡아주는 것이다.
   
    @Value("${file.emailattachfile-dir}") 
    private String emailattachfileDir; // 이메일 작성시 첨부파일의 경로를 잡아주는 것이다.
   
    @Value("${file.images-dir}") 
    private String imagesDir; // 이메일 작성시 첨부파일의 경로를 잡아주는 것이다.

    @Override
    public void  addResourceHandlers(ResourceHandlerRegistry registry) {
    	registry.addResourceHandler("/upload/**")
        .addResourceLocations("file:" + imagesDir + "/");
    	// Spring boot 에게 웹브라우저로 부터 "/upload/** 로 요청이 오면 실제 파일시스템의 upload 폴더를 찾아라" 라고 설정해주는 것이다.
        // !!! 중요한 것은 꼭 접두어인 file: 과 접미어인 / 를 반드시 넣어주어야 한다.!!!  
        // 그리고 스프링시큐리티 설정파일인 com.spring.app.security.config.SecurityConfig 에서 excludeUri 에 "/upload/**" 을 추가해 주어야 한다.
    	
    	registry.addResourceHandler("/photoupload/**")
        .addResourceLocations("file:"+photouploadDir+"/");
    	// 그리고 스프링시큐리티 설정파일인 com.spring.app.security.config.SecurityConfig 에서 excludeUri 에 "/photoupload/**" 을 추가해 주어야 한다.

    	registry.addResourceHandler("/emailattachfile/**")
        .addResourceLocations("file:"+emailattachfileDir+"/");
    	// 그리고 스프링시큐리티 설정파일인 com.spring.app.security.config.SecurityConfig 에서 excludeUri 에 "/emailattachfile/**" 을 추가해 주어야 한다.
    
    	registry.addResourceHandler("/images/**")
        .addResourceLocations("file:"+imagesDir+"/", "classpath:/static/images/");
		// Spring 은 다음의 순서대로 찾는다. 
		// 제일먼저, 외부 업로드 폴더를 먼저 검색하고(file_images/쉐보레.jpg) 있으면 이것을 사용하고,
		// 만약에 없으면 static 을 검색한다.(static/images/쉐보레.jpg)
		// 그리고 스프링시큐리티 설정파일인 com.spring.app.security.config.SecurityConfig 에서 excludeUri 에 "/images/**" 을 추가해 주어야 한다. 
    
    }
}
