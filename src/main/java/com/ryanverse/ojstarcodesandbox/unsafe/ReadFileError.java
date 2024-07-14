package com.ryanverse.ojstarcodesandbox.unsafe;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * ClassName: ReadFileError
 * Package: com.ryanverse.ojstarcodesandbox.unsafe
 * Description: 读取服务器文件
 *
 * @Author Haoran
 * @Create 2024/7/13 21:21
 * @Version 1.0
 */
public class ReadFileError {
	public static void main(String[] args) throws InterruptedException, IOException {
		String userDir = System.getProperty("user.dir");
		String filePath = userDir + File.separator + "src/main/resources/application.yml";
		List<String> allLines = Files.readAllLines(Paths.get(filePath));
		System.out.println(String.join("\n", allLines));
	}
}
