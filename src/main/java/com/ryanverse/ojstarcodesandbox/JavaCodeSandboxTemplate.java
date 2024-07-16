package com.ryanverse.ojstarcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.ryanverse.ojstarcodesandbox.model.ExecuteCodeRequest;
import com.ryanverse.ojstarcodesandbox.model.ExecuteCodeResponse;
import com.ryanverse.ojstarcodesandbox.model.ExecuteMessage;
import com.ryanverse.ojstarcodesandbox.model.JudgeInfo;
import com.ryanverse.ojstarcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ClassName: JavaCodeSandboxTemplate
 * Package: com.ryanverse.ojstarcodesandbox
 * Description:
 *
 * @Author Haoran
 * @Create 2024/7/15 17:18
 * @Version 1.0
 */
@Slf4j
public abstract class JavaCodeSandboxTemplate implements CodeSandbox {

	public static final String USER_DIR = "user.dir";
	public static final String GLOBAL_CODE_PATH = "tempCode";
	public static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
	public static final String SIMPLE_COMPUTE_MAIN = "testCode/simpleCompute/Main.java";
	public static final String SIMPLE_COMPUTE_ARGS_MAIN = "testCode/simpleComputeArgs/Main.java";
	public static final String UNSAFE_CODE_MAIN = "testCode/unsafeCode/RunFileError.java";

	private static final long TIME_OUT = 5000L;

	@Override
	public ExecuteCodeResponse executeCode (ExecuteCodeRequest executeCodeRequest) {
		List<String> inputList = executeCodeRequest.getInputList();
		String code = executeCodeRequest.getCode();
		String language = executeCodeRequest.getLanguage();

//        1. 把用户的代码保存为文件
		File userCodeFile = saveCodeToFile(code);

//        2. 编译代码，得到 class 文件
		ExecuteMessage compileFileExecuteMessage = compileFile(userCodeFile);
		System.out.println(compileFileExecuteMessage);

		// 3. 执行代码，得到输出结果
		List<ExecuteMessage> executeMessageList = runFile(userCodeFile, inputList);

//        4. 收集整理输出结果
		ExecuteCodeResponse outputResponse = getOutputResponse(executeMessageList);

//        5. 文件清理
		boolean b = deleteFile(userCodeFile);
		if (!b) {
			log.error("deleteFile error, userCodeFilePath = {}", userCodeFile.getAbsolutePath());
		}
		return outputResponse;
	}

	/**
	 * 1.将用户代码保存为文件
	 *
	 * @param code 用户代码
	 * @return 保存的文件
	 */
	public File saveCodeToFile (String code) {
		String userDir = System.getProperty(USER_DIR);
		String codePath = userDir + File.separator + GLOBAL_CODE_PATH;
		// 判断是否存在全局目录, 否则新建
		if (!FileUtil.exist(codePath)) {
			FileUtil.mkdir(codePath);
		}
		// 隔离存放用户代码
		String userCodeParentPath = codePath + File.separator + UUID.randomUUID();
		String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
		File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
		return userCodeFile;
	}

	/**
	 * 2.编译代码, 生成class文件
	 *
	 * @param userCodeFile 用户代码文件
	 * @return 编译结果
	 */
	public ExecuteMessage compileFile (File userCodeFile) {
		String compileCommand = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
		try {
			Process compileProcess = Runtime.getRuntime().exec(compileCommand);
			ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
			if (executeMessage.getExitValue() != 0) {
				throw new RuntimeException("编译错误");
			}
			return executeMessage;
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	/**
	 * 3.执行代码, 得到输出结果
	 *
	 * @param userCodeFile
	 * @param inputList
	 * @return
	 */
	public List<ExecuteMessage> runFile (File userCodeFile, List<String> inputList) {
		String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
		List<ExecuteMessage> executeMessageList = new ArrayList<>();
		for (String inputArgs : inputList) {
//			String runCmd = String.format("java -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=MySecurityManager Main", userCodeParentPath, SECURITY_MANAGER_PATH, inputArgs);
			String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);
			try {
				Process runProcess = Runtime.getRuntime().exec(runCmd);
				// 新建线程, 超时则销毁进程
				new Thread(() -> {
					try {
						Thread.sleep(TIME_OUT);
						runProcess.destroy();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}).start();
				ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
				System.out.println(executeMessage);
				executeMessageList.add(executeMessage);
			} catch (IOException e) {
				throw new RuntimeException("执行错误" + e);
			}
		}
		return executeMessageList;
	}

	/**
	 * 4.收集整理输出结果
	 *
	 * @param executeMessageList
	 * @return
	 */
	private ExecuteCodeResponse getOutputResponse (List<ExecuteMessage> executeMessageList) {
		ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
		List<String> outputList = new ArrayList<>();
		// 取最大值判断是否超时
		long maxTime = 0L;
		for (ExecuteMessage executeMessage : executeMessageList) {
			String errorMessage = executeMessage.getErrorMessage();
			if (StrUtil.isNotBlank(errorMessage)) {
				executeCodeResponse.setMessage(errorMessage);
				// 执行中存在错误
				executeCodeResponse.setStatus(3);
				break;
			}
			outputList.add(executeMessage.getSuccessMessage());
			Long time = executeMessage.getTime();
			if (time != null) {
				maxTime = Math.max(maxTime, time);
			}
		}
		// 执行成功
		if (outputList.size() == executeMessageList.size()) {
			executeCodeResponse.setStatus(1);
		}
		executeCodeResponse.setOutputList(outputList);
		JudgeInfo judgeInfo = new JudgeInfo();
		judgeInfo.setTime(maxTime);
		// 内存暂时不做处理, 实现较为复杂, 后续采用其他方式获取
		judgeInfo.setMemory(0L);
		executeCodeResponse.setJudgeInfo(judgeInfo);
		return executeCodeResponse;
	}

	/**
	 * 5.删除文件
	 *
	 * @param userCodeFile
	 * @return
	 */
	private boolean deleteFile (File userCodeFile) {
		if (userCodeFile.getParentFile() != null) {
			String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
			boolean del = FileUtil.del(userCodeParentPath);
			System.out.println("删除" + (del ? "成功" : "失败"));
			return del;
		}
		return true;
	}

	/**
	 * 6.获取错误响应
	 *
	 * @param e 异常
	 * @return 错误响应
	 */
	private ExecuteCodeResponse getErrorResponse (Throwable e) {
		ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
		executeCodeResponse.setOutputList(new ArrayList<>());
		executeCodeResponse.setMessage(e.getMessage());
		// 表示代码沙箱错误
		executeCodeResponse.setStatus(2);
		executeCodeResponse.setJudgeInfo(new JudgeInfo());
		return executeCodeResponse;
	}
}
