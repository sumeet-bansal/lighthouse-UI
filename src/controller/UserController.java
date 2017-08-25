package controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.*;

public class UserController {
	@ResponseBody
	@RequestMapping(value="/login", method=RequestMethod.POST)
	public String loginUser(HttpServletRequest req, HttpServletResponse res) {
		System.out.println("A user attempt login!");
		return "false";
	}

	@ResponseBody
	@RequestMapping(value="/signup", method=RequestMethod.POST)
	public String signupUser(HttpServletRequest req, HttpServletResponse res) {
		System.out.println("A user attempt signup!");
		return "false";
	}

	@ResponseBody
	@RequestMapping(value="/logout", method=RequestMethod.POST)
	public String logoutUser(HttpServletRequest req, HttpServletResponse res) {
		System.out.println("A user attempt logout!");
		return "{\"result\":\"true\"}";
	}

}
