package com.ryanverse.ojstarcodesandbox.unsafe;

/**
 * ClassName: SleepError
 * Package: com.ryanverse.ojstarcodesandbox.unsafe
 * Description: 无限睡眠, 阻塞程序执行
 *
 * @Author Haoran
 * @Create 2024/7/13 20:50
 * @Version 1.0
 */
public class SleepError {
	public static void main(String[] args) throws InterruptedException {
		long ONE_HOUR = 60*60*1000L;
		Thread.sleep(ONE_HOUR);
		System.out.println("wake up");
	}
}
