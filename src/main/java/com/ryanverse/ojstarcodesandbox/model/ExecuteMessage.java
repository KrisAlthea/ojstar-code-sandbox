package com.ryanverse.ojstarcodesandbox.model;

import lombok.Data;

/**
 * ClassName: ExecuteMessage
 * Package: com.ryanverse.ojstarcodesandbox.model
 * Description: 进程执行信息
 *
 * @Author Haoran
 * @Create 2024/7/13 16:08
 * @Version 1.0
 */
@Data
public class ExecuteMessage {

	private Integer exitValue;

	private String successMessage;

	private String errorMessage;

	private Long  time;

	private Long memory;
}
