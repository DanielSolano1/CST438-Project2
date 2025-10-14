package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class Group11Application {

	public static void main(String[] args) {
		SpringApplication.run(Group11Application.class, args);
	}
	@GetMapping("/home")
	public String index() {
		return "This is the root page";
	}

}
