package com.ryanverse.ojstarcodesandbox.unsafe;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * ClassName: WriteFileError
 * Package: com.ryanverse.ojstarcodesandbox.unsafe
 * Description: 写入木马
 *
 * @Author Haoran
 * @Create 2024/7/13 22:04
 * @Version 1.0
 */
public class WriteFileError {
	public static void main (String[] args) throws InterruptedException, IOException {
		String userDir = System.getProperty("user.dir");
		String filePath = userDir + File.separator + "src/main/resources/TrojanHorse.bat";
		String content = "java -version 2>&1";
		Files.write(Paths.get(filePath), Arrays.asList(content));
		System.out.println("写入木马成功");
	}
}
