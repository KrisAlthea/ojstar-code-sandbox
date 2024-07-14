package com.ryanverse.ojstarcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.ryanverse.ojstarcodesandbox.model.ExecuteCodeRequest;
import com.ryanverse.ojstarcodesandbox.model.ExecuteCodeResponse;
import com.ryanverse.ojstarcodesandbox.model.ExecuteMessage;
import com.ryanverse.ojstarcodesandbox.model.JudgeInfo;
import com.ryanverse.ojstarcodesandbox.utils.ProcessUtils;
import org.springframework.util.StopWatch;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: JavaNativeCodeSandbox
 * Package: com.ryanverse.ojstarcodesandbox
 * Description: Java原生代码沙箱
 *
 * @Author Haoran
 * @Create 2024/7/12 19:47
 * @Version 1.0
 */
public class JavaDockerCodeSandbox implements CodeSandbox {

    private static final String USER_DIR = "user.dir";
    private static final String GLOBAL_CODE_PATH = "tempCode";
    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
    private static final String SIMPLE_COMPUTE_MAIN = "testCode/simpleCompute/Main.java";
    private static final String SIMPLE_COMPUTE_ARGS_MAIN = "testCode/simpleComputeArgs/Main.java";
    private static final String UNSAFE_CODE_MAIN = "testCode/unsafeCode/RunFileError.java";

    private static final long TIME_OUT = 5000L;

    public static final Boolean FIRST_INIT = true;


    public static final String SECURITY_MANAGER_PATH = "D:\\WorkSpace\\IDEA_ws\\ojstar-code-sanbox\\src\\main\\resources\\security";

    public static void main(String[] args) {
        JavaDockerCodeSandbox javaNativeCodeSandbox = new JavaDockerCodeSandbox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setLanguage("java");
        executeCodeRequest.setInputList(Arrays.asList("1 2", "1 3"));
        String code = ResourceUtil.readStr(SIMPLE_COMPUTE_ARGS_MAIN, StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
//		System.setSecurityManager(new DenySecurityManager());

        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();


        // 1.把用户代码保存为文件
        String userDir = System.getProperty(USER_DIR);
        String codePath = userDir + File.separator + GLOBAL_CODE_PATH;
        // 判断是否存在全局目录, 否则新建
        if (!FileUtil.exist(codePath)) {
            FileUtil.mkdir(codePath);
        }
        // 2.隔离存放用户代码
        String userCodeParentPath = codePath + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        // 编译代码, 生成class文件
        String compileCommand = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCommand);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            System.out.println(executeMessage);
        } catch (IOException e) {
            return getErrorResponse(e);
        }
        // 3.创建容器, 把代码复制到容器内
        // 获取docker client
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        String image = "openjdk:8-alpine";
        // 拉取镜像
        if (FIRST_INIT) {
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    System.out.println("下载镜像：" + item.getStatus());
                    super.onNext(item);
                }
            };
            try {
                pullImageCmd
                        .exec(pullImageResultCallback)
                        .awaitCompletion();
            } catch (InterruptedException e) {
                System.out.println("拉取镜像异常");
                throw new RuntimeException(e);
            }
            System.out.println("下载完成");
        }

        // 创建容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
		HostConfig hostConfig = new HostConfig();
		hostConfig.withMemory(100*1000*1000L);
        hostConfig.withMemorySwap(0L);
		hostConfig.withCpuCount(1L);
//        hostConfig.withSecurityOpts(Arrays.asList("seccomp=安全管理配置字符串"));
		hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app")));
        CreateContainerResponse createContainerResponse = containerCmd
				.withHostConfig(hostConfig)
                .withNetworkDisabled(true)
                .withReadonlyRootfs(true)
				.withAttachStdin(true)
				.withAttachStdout(true)
				.withAttachStderr(true)
				.withTty(true)
                .exec();
		System.out.println(createContainerResponse);
        String containerId = createContainerResponse.getId();

        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();
        // 执行命令并获取结果
        // docker exec keen_blackwell java -cp /app Main 1 3
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            StopWatch stopWatch = new StopWatch();
            String[] inputArgsArray = inputArgs.split(" ");
            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, inputArgsArray);
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .exec();
            System.out.println("创建执行命令: " + execCreateCmdResponse);

            ExecuteMessage executeMessage = new ExecuteMessage();
            final String[] successMessage = {null};
            final String[] errorMessage = {null};
            long time = 0L;
            final boolean[] timeout = {true};
            String execId = execCreateCmdResponse.getId();
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
                @Override
                public void onComplete(){
                    timeout[0] = false;
                    super.onComplete();
                }
                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)) {
                        errorMessage[0] = new String(frame.getPayload());
                        System.out.println("输出错误结果：" + errorMessage[0]);
                    } else {
                        successMessage[0] = new String(frame.getPayload());
                        System.out.println("输出结果：" + successMessage[0]);
                    }
                    super.onNext(frame);
                }
            };
            // 获取占用的内存
            final long[] maxMemory = {0L};
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {

                @Override
                public void onNext(Statistics statistics) {
                    System.out.println("内存占用：" + statistics.getMemoryStats().getUsage());
                    maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
                }

                @Override
                public void close() throws IOException {

                }

                @Override
                public void onStart(Closeable closeable) {

                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {

                }
            });
            statsCmd.exec(statisticsResultCallback);
            try {
                stopWatch.start();
                dockerClient.execStartCmd(execId)
                        .exec(execStartResultCallback)
                        .awaitCompletion(TIME_OUT, TimeUnit.MICROSECONDS);
//                        .awaitCompletion(); 传入参数获取不到memory
                stopWatch.stop();
                time = stopWatch.getLastTaskTimeMillis();
                statsCmd.close();
            } catch (InterruptedException e) {
                System.out.println("程序执行异常");
                throw new RuntimeException(e);
            }
            executeMessage.setSuccessMessage(successMessage[0]);
            executeMessage.setErrorMessage(errorMessage[0]);
            executeMessage.setTime(time);
            executeMessage.setMemory(maxMemory[0]);
            executeMessageList.add(executeMessage);
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
    }
}
