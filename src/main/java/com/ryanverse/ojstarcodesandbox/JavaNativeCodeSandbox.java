package com.ryanverse.ojstarcodesandbox;

import cn.hutool.core.io.resource.ResourceUtil;
import com.ryanverse.ojstarcodesandbox.model.ExecuteCodeRequest;
import com.ryanverse.ojstarcodesandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * ClassName: JavaNativeCodeSandbox
 * Package: com.ryanverse.ojstarcodesandbox
 * Description: Java原生代码沙箱
 *
 * @Author Haoran
 * @Create 2024/7/15 17:57
 * @Version 1.0
 */
@Component
public class JavaNativeCodeSandbox extends JavaCodeSandboxTemplate {

	public static void main(String[] args) {
		JavaDockerCodeSandboxOld javaNativeCodeSandbox = new JavaDockerCodeSandboxOld();
		ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
		executeCodeRequest.setLanguage("java");
		executeCodeRequest.setInputList(Arrays.asList("1 2", "1 3"));
		String code = ResourceUtil.readStr(SIMPLE_COMPUTE_ARGS_MAIN, StandardCharsets.UTF_8);
		executeCodeRequest.setCode(code);
		ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
		System.out.println(executeCodeResponse);
	}

	@Override
	public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
		return super.executeCode(executeCodeRequest);
	}
}
