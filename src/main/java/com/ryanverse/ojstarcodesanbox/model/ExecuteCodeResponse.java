package com.ryanverse.ojstarcodesanbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ClassName: ExecuteCodeResponse
 * Package: com.ryanverse.ojstarcodesanbox.judge.codesandbox.model
 * Description: 示例代码沙箱(仅供开发测试)
 *
 * @Author Haoran
 * @Create 2024/7/11 19:53
 * @Version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {

	private List<String> outputList;

	private String message;

	private Integer status;

	private JudgeInfo judgeInfo;
}
