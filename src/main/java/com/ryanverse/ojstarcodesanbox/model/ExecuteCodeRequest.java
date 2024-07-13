package com.ryanverse.ojstarcodesanbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ClassName: ExecuteCodeRequest
 * Package: com.ryanverse.ojstarcodesanbox.judge.codesandbox.model
 * Description:
 *
 * @Author Haoran
 * @Create 2024/7/11 19:50
 * @Version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRequest {
	private List<String> inputList;
	private String code;
	private String language;
}
