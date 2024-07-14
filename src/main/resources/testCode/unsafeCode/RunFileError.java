import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * ClassName: WriteFileError
 * Package: com.ryanverse.ojstarcodesanbox.unsafe
 * Description: 运行其他程序
 *
 * @Author Haoran
 * @Create 2024/7/13 22:04
 * @Version 1.0
 */
public class Main {

	public static void main(String[] args) throws InterruptedException, IOException {
		String userDir = System.getProperty("user.dir");
		String filePath = userDir + File.separator + "src/main/resources/TrojanHorse.bat";
		Process process = Runtime.getRuntime().exec(filePath);
		process.waitFor();
		// 分批获取进程的正常输出
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		// 逐行读取
		String compileOutputLine;
		while ((compileOutputLine = bufferedReader.readLine()) != null) {
			System.out.println(compileOutputLine);
		}
		System.out.println("执行异常程序成功");
	}
}
