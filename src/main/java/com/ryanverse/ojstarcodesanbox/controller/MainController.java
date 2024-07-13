package com.ryanverse.ojstarcodesanbox.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: MainController
 * Package: com.ryanverse.ojstarcodesanbox.controller
 * Description:
 *
 * @Author Haoran
 * @Create 2024/7/12 19:16
 * @Version 1.0
 */
@RestController("/")
public class MainController {

	@GetMapping("/health")
	public String health() {
		return "ok";
	}
}
