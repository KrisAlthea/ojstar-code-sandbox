package com.ryanverse.ojstarcodesandbox.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PingCmd;
import com.github.dockerjava.core.DockerClientBuilder;

/**
 * ClassName: DockerDemo
 * Package: com.ryanverse.ojstarcodesandbox.docker
 * Description: DockerDemo
 *
 * @Author Haoran
 * @Create 2024/7/14 15:38
 * @Version 1.0
 */
public class DockerDemo {
	public static void main (String[] args) {
		// 获取docker client
		DockerClient dockerClient = DockerClientBuilder.getInstance().build();
		PingCmd pingCmd = dockerClient.pingCmd();
		pingCmd.exec();
	}
}
