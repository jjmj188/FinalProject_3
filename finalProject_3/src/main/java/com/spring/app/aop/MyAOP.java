package com.spring.app.aop;

import java.util.Arrays;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.spring.app.exception.BadWordException;

import lombok.extern.slf4j.Slf4j;

//=== 공통관심사 클래스(Aspect 클래스) 생성하기===//
//AOP(Aspect Oriented Programming)

@Aspect      // 공통관심사 클래스(Aspect 클래스)로 등록된다.
@Component   // bean 으로 등록된다. 
//!!! 중요 !!! MyspringbootApplication 클래스에서 @EnableAspectJAutoProxy 을 기재해야 한다.!!!

//== Lombok 라이브러리에서 제공하는 어노테이션 == 
@Slf4j                       // Lombok 이 log 객체를 자동으로 생성해줌. log 설정은 src/main/resources/application.yml 에 해두었음.
//@Getter                   // private 으로 설정된 필드 변수를 외부에서 접근하여 사용하도록 getter()메소드를 만들어 주는 것.
//@Setter                   // private 으로 설정된 필드 변수를 외부에서 접근하여 수정하도록 setter()메소드를 만들어 주는 것.
//@ToString                 // 객체를 문자열로 표현할 때 사용
//@RequiredArgsConstructor  // final 필드 또는 @NonNull이 붙은 필드에 대해 이 필드만 포함하는 생성자를 자동으로 생성해준다.
//@AllArgsConstructor       // 모든 필드 값을 파라미터로 받는 생성자를 만들어주는 것
//@NoArgsConstructor        // 파라미터가 없는 기본생성자를 만들어주는 것
//@Data                     // lombok 에서 사용하는 @Data 어노테이션은 @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor 를 모두 합쳐놓은 종합선물세트인 것이다.
public class MyAOP {
	
	// ===== Before Advice(보조업무) 만들기 ====== // 
	   /*
	       메서드 실행 파라미터 로깅(기록) 및 검증하기를 만들어 본다.
	       보통 디버깅 상황에서 어떤 사용자가 어떤 값을 들고 메서드에 진입했는지 확인해야 할때가 많다.
	       컨트롤러의 모든 메서드에 System.out.println 이나 log.info 를 넣는 대신에 AOP 로 1번 작업만으로 해결하도록 하겠다.
	       이어서 글쓰기, 댓글쓰기 등을 할 경우에 있어서 비속어를 사용할 경우 비속어 금지를 메시지를 출력한 후 
	       글쓰기, 댓글쓰기를 못하도록 AOP 로 1번 작업만으로 해결하도록 하겠다.  
	   */
	
		// === Pointcut(주업무)을 설정해야 한다. === //
		//     Pointcut 이란 공통관심사<예: 메서드 실행 파라미터 로깅(기록) 및 검증하기>를 필요로 하는 메서드를 말한다.
		@Pointcut("execution(public * com.spring.app.board.controller.BoardController.*(..))")
		public void controllerMethods(){}
		
		
		// === Before Advice(공통관심사, 보조업무)를 구현한다. === //
		@Before("controllerMethods()")
		public void logParameter(JoinPoint joinpoint) {// 메서드 실행 파라미터 로깅(기록) 및 검증하기를 하는 메서드 작성하기
			// JoinPoint joinpoint 는 포인트컷 되어진 주업무의 메서드이다.	
			
			String methodName = joinpoint.getSignature().getName(); // 포인트컷 되어진 주업무의 메서드명
		      // Spring AOP 에서 joinpoint.getSignature() 는 포인트컷 되어진 주업무의 메서드의 정보(리턴타입, 메서드명, 파라미터 변수 등)가 담긴 Signature 객체를 가져오는 것이다. 
		      /*
		         ---------------------------------------------------------------------
		           메서드                       설명
		         ---------------------------------------------------------------------
		           getName()                  메서드의 이름을 반환
		           getDeclaringTypeName()     클래스의 풀 경로(패키지 포함)를 반환
		           toShortString()            메서드를 짧게 요약한 문자열 반환
		           toLongString()             메서드의 전체 사양(접근제한자, 리턴타입 등) 반환
		         ----------------------------------------------------------------------     
		       */
			
			// After Advice(보조업무)          ==>  Pointcut(주업무)가 성공으로 끝나든 또는 실패로 끝나든 Pointcut(주업무)가 끝나면 실행되어지는 것  
		    // AfterReturning Advice(보조업무) ==>  Pointcut(주업무)가 성공으로 끝나면 실행되어지는 것 
		    // AfterThrowing Advice(보조업무)  ==>  Pointcut(주업무)가 실패로 끝나면 실행되어지는 것
		
			// Around Advice(보조업무) =>Before Advice+Advice
			
			Object[] args = joinpoint.getArgs(); // 포인트컷 되어진 주업무의 메서드의 파라미터를 가져온다. 
			// log 생성을 위하여 Lombok 라이브러리에서 제공하는 어노테이션인 @Slf4j 을 사용하면 Lombok 이 log 객체를 자동으로 생성해준다.
		       // 메서드명 출력하기
		       log.info(">>> [MyAOP Before] Method Name : {}", methodName); 
		       
		       
		     //비속어 등록하기(배열로 등록)
				String[] arr_forbiddenWord= {"개썌끼","씨발","시발","이창익","정창기"};
				
				//비속어 등록하기(List로 등록)
				List<String> list_forbiddenWord1 = Arrays.asList("개썌끼","씨발","시발","이창익","정창기");
				List<String> list_forbiddenWord2 = List.of("개썌끼","씨발","시발","이창익","정창기");
				
				for(Object arg: args) {
					// 메서드의 파라미터 타입 출력하기
		            log.info(">>> [MyAOP Before] Argument Type : {}", arg.getClass().getSimpleName());
		            //ModelAndView
		            //BoardDTO
		            //CommentDTO
		            // 메서드의 파라미터에 입력된 데이터값 출력하기
		             log.info(">>> [MyAOP Before] Argument Value : {}", arg.toString());
		             /*
		             ModelAndView [view=[null]; model=null]
		             BoardDTO(seq=null, memberid=joung, name=정민정, subject=저의 친구 이창익을 소개합니다., content=<p>&nbsp;이창익</p>, pw=1234, readcount=null, regdate=null, status=null, previousseq=null, previoussubject=null, nextseq=null, nextsubject=null, commentCount=null)
		              CommentDTO(seq=null, memberid=joung, name=정민정, content=친구 정창기 입니다, regdate=null, boardSeq=217, parentSeq=null, status=null, ddcnt=null)
		              CommentDTO(seq=null, memberid=joung, name=정민정, content=또 다른 친구 이창익 입니다, regdate=null, boardSeq=217, parentSeq=33, status=null, ddcnt=null)
		             */
		             if(arg.toString().startsWith("BoardDTO") ||
		                     arg.toString().startsWith("CommentDTO") ||
		                     arg.toString().startsWith("{") ) { // arg.toString().startsWith("{") 은 댓글쓰기, 대댓글쓰기에서 보내어주는 데이터를 data:{"키":"값","키":"값"} 로 한 경우이다.
		            	 for(String forbidden_word : arr_forbiddenWord) {
		            		 if(arg.toString().contains(forbidden_word)) {
		            			// 비속어가 포함된 경우라면
		                         System.out.println("##### 비속어 \""+forbidden_word+"\" 를 사용했어요 #####");
		                         
		                         // 글내용을 검사하여 비속어가 포함된 경우라면 
		                         // 우리가 만든 com.spring.app.exception.BadWordException 예외절(RuntimeException 을 상속받은 예외절 클래스)을 
		                         // 발생시켜서 주업무 메서드 실행을 강제로 중단하도록 한다.
		                         
		                         throw new BadWordException("비속어 \""+forbidden_word+"\" 를 사용하시면 안됩니다.");
		                         
		            		 }
		            	 }//end of for
		             }
				}//end of for
		
		
		}

}
