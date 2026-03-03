package com.spring.app.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice   // @ControllerAdvice 은 모든 컨트롤러에 대해서 보조업무(조언)를 해주는 클래스. HTML 페이지를 반환해줌.
// @ControllerAdvice 의 역할은 전역예외처리(@ExceptionHandler)를 해주는 것으로서
// 모든 컨트롤러에서 발생하는 특정 예외를 한 곳에서 가로채서 처리해준다. 각 컨트롤러 마다 try-catch 를 넣을 필요가 없어진다.
//@RestControllerAdvice  // @RestControllerAdvice = @ControllerAdvice + @ResponseBody. 만약에 HTML 페이지가 아니라 JSON 데이터를 반환하고 싶을 때 사용함.
public class GlobalExceptionHandler {
	
	@ExceptionHandler(BadWordException.class)// 모든 곳에서 BadWordException 이 발생했을때 예외처리를 해주겠다는 말이다. 
	public String handleBadWordException(BadWordException e, Model model) {
		
		model.addAttribute("errorMessage", e.getMessage());// model 저장소에 BadWordException 에러메시지를 저장하여
		
		
		return "error_forbidden_word";  // error_forbidden_word.html 페이지에서 보여주도록 한다.
	       //  src/main/resources/templates/error_forbidden_word.html 페이지를 만들어야 한다.
	}
}
