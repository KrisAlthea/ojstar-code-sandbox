package com.ryanverse.ojstarcodesanbox.utils;

import cn.hutool.core.util.StrUtil;
import com.ryanverse.ojstarcodesanbox.model.ExecuteMessage;
import org.springframework.util.StopWatch;

import java.io.*;

/**
 * ClassName: ProcessUtils
 * Package: com.ryanverse.ojstarcodesanbox.utils
 * Description: 进程工具类
 *
 * @Author Haoran
 * @Create 2024/7/13 16:07
 * @Version 1.0
 */
public class ProcessUtils {

	/**
	 * 运行进程并获取进程的输出信息
	 * @param runProcess 运行的进程
	 * @param opName
	 * @return 进程的输出信息
	 */
	public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String opName) {
		ExecuteMessage executeMessage = new ExecuteMessage();

		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			// 等待程序执行，获取错误码
			int exitValue = runProcess.waitFor();
			executeMessage.setExitValue(exitValue);
			// 正常退出
			if (exitValue == 0) {
				System.out.println(opName + "成功");
				// 分批获取进程的正常输出
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
				StringBuilder compileOutputStringBuilder = new StringBuilder();
				// 逐行读取
				String compileOutputLine;
				while ((compileOutputLine = bufferedReader.readLine()) != null) {
					compileOutputStringBuilder.append(compileOutputLine);
				}
				executeMessage.setSuccessMessage(compileOutputStringBuilder.toString());
			} else {
				// 异常退出
				System.out.println(opName + "失败，错误码： " + exitValue);
				// 分批获取进程的正常输出
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
				StringBuilder compileOutputStringBuilder = new StringBuilder();
				// 逐行读取
				String compileOutputLine;
				while ((compileOutputLine = bufferedReader.readLine()) != null) {
					compileOutputStringBuilder.append(compileOutputLine);
				}
				executeMessage.setSuccessMessage(compileOutputStringBuilder.toString());

				// 分批获取进程的错误输出
				BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
				StringBuilder errorCompileOutputStringBuilder = new StringBuilder();

				// 逐行读取
				String errorCompileOutputLine;
				while ((errorCompileOutputLine = errorBufferedReader.readLine()) != null) {
					errorCompileOutputStringBuilder.append(errorCompileOutputLine);
				}
				executeMessage.setErrorMessage(errorCompileOutputStringBuilder.toString());
			}
			stopWatch.stop();
			executeMessage.setTime(stopWatch.getTotalTimeMillis());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return executeMessage;
	}

	/**
	 * 运行交互式进程并获取进程的输出信息
	 * 供参考, 待完善
	 * @param runProcess 运行的进程
	 * @param args 进程的输入参数
	 * @return 进程的输出信息
	 */
	public static ExecuteMessage runInteractProcessAndGetMessage(Process runProcess, String args) {
		ExecuteMessage executeMessage = new ExecuteMessage();

		try {
			// 向控制台输入程序
			OutputStream outputStream = runProcess.getOutputStream();
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
			String[] s = args.split(" ");
			String join = StrUtil.join("\n", s) + "\n";
			outputStreamWriter.write(join);
			// 相当于按了回车，执行输入的发送
			outputStreamWriter.flush();

			// 分批获取进程的正常输出
			InputStream inputStream = runProcess.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder compileOutputStringBuilder = new StringBuilder();
			// 逐行读取
			String compileOutputLine;
			while ((compileOutputLine = bufferedReader.readLine()) != null) {
				compileOutputStringBuilder.append(compileOutputLine);
			}
			executeMessage.setSuccessMessage(compileOutputStringBuilder.toString());
			// 记得资源的释放，否则会卡死
			outputStreamWriter.close();
			outputStream.close();
			inputStream.close();
			runProcess.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return executeMessage;
	}
}
