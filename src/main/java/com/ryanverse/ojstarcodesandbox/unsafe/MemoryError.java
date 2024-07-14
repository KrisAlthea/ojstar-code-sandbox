package com.ryanverse.ojstarcodesandbox.unsafe;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: MemoryError
 * Package: com.ryanverse.ojstarcodesandbox.unsafe
 * Description: 无限占用空间
 *
 * @Author Haoran
 * @Create 2024/7/13 21:06
 * @Version 1.0
 */
public class MemoryError {

	public static void main (String[] args) {
		List<byte[]> bytes = new ArrayList<>();
		while (true) {
			bytes.add(new byte[1024 * 1024]);
		}
	}
}
