package com.ryanverse.ojstarcodesandbox.model;

import lombok.Data;

/**
 * ClassName: JudgeCase
 * Package: com.ryanverse.ojstarcodesandbox.model.dto.question
 * Description:
 *
 * @Author Haoran
 * @Create 2024/7/7 1:29
 * @Version 1.0
 */
@Data
public class JudgeInfo {
	/**
	 * 程序执行信息
	 */
	private String message;

	/**
	 * 消耗内存
	 */
	private Long memory;

	/**
	 * 消耗时间
	 */
	private Long time;
}
