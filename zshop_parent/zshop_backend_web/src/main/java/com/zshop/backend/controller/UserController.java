package com.zshop.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/backend/user")
public class UserController {

	@RequestMapping("/login")
	public String login() {
		return "main";
	}
	
}
