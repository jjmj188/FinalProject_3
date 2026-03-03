package com.spring.app.exception;

public class BadWordException extends RuntimeException{// RuntimeException 을 상속받은 예외절 클래스 생성하기
	
	private static final long serialVersionUID = 1L;

	public BadWordException(String message) {
	      super(message);
	   }
}
