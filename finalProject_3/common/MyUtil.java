package com.spring.app.common;

import jakarta.servlet.http.HttpServletRequest;

public class MyUtil {
	// *** ? 다음의 데이터까지 포함한 현재 URL 주소를 알려주는 메소드를 생성 *** //
	public static String getCurrentURL(HttpServletRequest request) {
		
		String currentURL = request.getRequestURL().toString();
		
		//System.out.println("currentURL=>" +currentURL);
		//currentURL=>http://localhost:9090/MyMVC/shop/prodView.up
		
		String queryString = request.getQueryString();
		//System.out.println("queryString=>"+queryString);
		//queryString=>pnum=58
		//queryString=>null(POST 방식일 경우)
		
		if(queryString!=null) {//GET 방식일 경우
			currentURL += "?"+queryString;
			//System.out.println("currentURL=>"+currentURL);
			//currentURL=>http://localhost:9090/MyMVC/shop/prodView.up?pnum=58
		}
		
		String ctxPath = request.getContextPath();
		// /MyMVC
		
		int beginIndex = currentURL.indexOf(ctxPath) + ctxPath.length();
		//27 = 21 + 6
		
		currentURL = currentURL.substring(beginIndex);
		//System.out.println("currentURL=>"+currentURL);
		//currentURL=>/shop/prodView.up?pnum=58
		
		return currentURL;
	}
}
