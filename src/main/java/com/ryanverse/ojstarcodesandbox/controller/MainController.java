package com.ryanverse.ojstarcodesandbox.controller;

import com.ryanverse.ojstarcodesandbox.JavaNativeCodeSandbox;
import com.ryanverse.ojstarcodesandbox.model.ExecuteCodeRequest;
import com.ryanverse.ojstarcodesandbox.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ClassName: MainController
 * Package: com.ryanverse.ojstarcodesandbox.controller
 * Description:
 *
 * @Author Haoran
 * @Create 2024/7/12 19:16
 * @Version 1.0
 */
@RestController("/")
public class MainController {

	// 定义鉴权请求头和密钥
	private static final String AUTH_REQUEST_HEADER = "auth";

	private static final String AUTH_REQUEST_SECRET = "secretKey";

	@Resource
	private JavaNativeCodeSandbox javaNativeCodeSandbox;

	@GetMapping("/health")
	public String health () {
		return "ok";
	}

	/**
	 * 执行代码
	 *
	 * @param executeCodeRequest 请求参数
	 * @return 执行结果
	 */
	@PostMapping("/executeCode")
	ExecuteCodeResponse executeCode (@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request,
	                                 HttpServletResponse response) {
		// 基本的认证
		String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
		if (!AUTH_REQUEST_SECRET.equals(authHeader)) {
			response.setStatus(403);
			return null;
		}
		if (executeCodeRequest == null) {
			throw new IllegalArgumentException("executeCodeRequest is null");
		}
		return javaNativeCodeSandbox.executeCode(executeCodeRequest);
	}
}
