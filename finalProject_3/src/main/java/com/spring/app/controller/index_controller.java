package com.spring.app.controller; 

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class index_controller {
	@GetMapping("/")
    public String home() {
        return "index"; // templates/index.html
    }
}
