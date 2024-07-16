package com.ryanverse.ojstarcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import com.ryanverse.ojstarcodesandbox.model.ExecuteCodeRequest;
import com.ryanverse.ojstarcodesandbox.model.ExecuteCodeResponse;
import com.ryanverse.ojstarcodesandbox.model.ExecuteMessage;
import com.ryanverse.ojstarcodesandbox.model.JudgeInfo;
import com.ryanverse.ojstarcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * ClassName: JavaNativeCodeSandboxOld
 * Package: com.ryanverse.ojstarcodesandbox
 * Description: Java原生代码沙箱
 *
 * @Author Haoran
 * @Create 2024/7/12 19:47
 * @Version 1.0
 */
public class JavaNativeCodeSandboxOld implements CodeSandbox {

	private static final String USER_DIR = "user.dir";
	private static final String GLOBAL_CODE_PATH = "tempCode";
	private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
	private static final String SIMPLE_COMPUTE_MAIN = "testCode/simpleCompute/Main.java";
	private static final String SIMPLE_COMPUTE_ARGS_MAIN = "testCode/simpleComputeArgs/Main.java";
	private static final String UNSAFE_CODE_MAIN = "testCode/unsafeCode/RunFileError.java";

	private static final long TIME_OUT = 5000L;
	private static final List<String> blackList = Arrays.asList("Files", "exec");
	private static final WordTree WORD_TREE;
	static {
		// 初始化字典树
		WORD_TREE = new WordTree();
		WORD_TREE.addWords(blackList);
	}

	public static final String SECURITY_MANAGER_PATH = "D:\\WorkSpace\\IDEA_ws\\ojstar-code-sanbox\\src\\main\\resources\\security";

	public static void main (String[] args) {
		JavaNativeCodeSandboxOld javaNativeCodeSandboxOld = new JavaNativeCodeSandboxOld();
		ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
		executeCodeRequest.setLanguage("java");
		executeCodeRequest.setInputList(Arrays.asList("1 2", "1 3"));
//		String code = ResourceUtil.readStr("testCode/simpleCompute/Main.java", StandardCharsets.UTF_8);
		String code = ResourceUtil.readStr(UNSAFE_CODE_MAIN, StandardCharsets.UTF_8);
		executeCodeRequest.setCode(code);
		ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandboxOld.executeCode(executeCodeRequest);
		System.out.println(executeCodeResponse);
	}

	@Override
	public ExecuteCodeResponse executeCode (ExecuteCodeRequest executeCodeRequest) {
//		System.setSecurityManager(new DenySecurityManager());

		List<String> inputList = executeCodeRequest.getInputList();
		String code = executeCodeRequest.getCode();
		String language = executeCodeRequest.getLanguage();

//		//  校验代码中是否包含黑名单中的禁用词
//		FoundWord foundWord = WORD_TREE.matchWord(code);
//		if (foundWord != null) {
//			System.out.println("包含禁止词：" + foundWord.getFoundWord());
//			return null;
//		}


		// 1.把用户代码保存为文件
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
		// 2.编译代码, 生成class文件
		String compileCommand = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
		try {
			Process compileProcess = Runtime.getRuntime().exec(compileCommand);
			ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
			System.out.println(executeMessage);
		} catch (IOException e) {
			return getErrorResponse(e);
		}
		// 3.执行代码
		List<ExecuteMessage> executeMessageList = new ArrayList<>();
		for (String inputArgs : inputList) {
			String runCmd = String.format("java -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=MySecurityManager Main", userCodeParentPath, SECURITY_MANAGER_PATH, inputArgs);
//			String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);
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
//				ExecuteMessage executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess, inputArgs);
				System.out.println(executeMessage);
				executeMessageList.add(executeMessage);
			} catch (IOException e) {
				return getErrorResponse(e);
			}
		}
		// 4.收集整理输出结果
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
		// 5.文件清理
		if (userCodeFile.getParentFile() != null) {
			boolean del = FileUtil.del(userCodeParentPath);
			System.out.println("删除" + (del ? "成功" : "失败"));
		}
		return executeCodeResponse;
	}

	/**
	 * 获取错误响应
	 *
	 * @param e 异常
	 * @return 错误响应
	 */
	private ExecuteCodeResponse getErrorResponse(Throwable e) {
		ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
		executeCodeResponse.setOutputList(new ArrayList<>());
		executeCodeResponse.setMessage(e.getMessage());
		// 表示代码沙箱错误
		executeCodeResponse.setStatus(2);
		executeCodeResponse.setJudgeInfo(new JudgeInfo());
		return executeCodeResponse;
	}}
