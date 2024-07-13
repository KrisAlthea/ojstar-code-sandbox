package com.ryanverse.ojstarcodesanbox;


import com.ryanverse.ojstarcodesanbox.model.ExecuteCodeRequest;
import com.ryanverse.ojstarcodesanbox.model.ExecuteCodeResponse;

/**
 * ClassName: CodeSandbox
 * Package: com.ryanverse.ojstarcodesanbox.judge.codesandbox
 * Description: 代码沙箱接口
 *
 * @Author Haoran
 * @Create 2024/7/11 19:48
 * @Version 1.0
 */
public interface CodeSandbox {
	ExecuteCodeResponse executeCode (ExecuteCodeRequest executeCodeRequest);
}
