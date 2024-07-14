package com.ryanverse.ojstarcodesandbox.security;

import java.security.Permission;

/**
 * ClassName: MySecurityManager
 * Package: com.ryanverse.ojstarcodesandbox.security
 * Description: 自定义安全管理器
 *
 * @Author Haoran
 * @Create 2024/7/14 10:00
 * @Version 1.0
 */
public class MySecurityManager extends SecurityManager{

	// 检查所有权限
	@Override
	public void checkPermission(Permission perm) {
//		super.checkPermission(perm);
	}

	// 检查执行文件权限
	@Override
	public void checkExec(String cmd) {
		throw new SecurityException("checkExec 权限异常：" + cmd);
	}

	// 检查文件读取权限
	@Override
	public void checkRead(String file) {
		throw new SecurityException("checkRead 权限异常：" + file);
	}

	// 检查文件写入权限
	@Override
	public void checkWrite(String file) {
		throw new SecurityException("checkWrite 权限异常：" + file);
	}

	// 检查文件删除权限
	@Override
	public void checkDelete(String file) {
		throw new SecurityException("checkDelete 权限异常：" + file);
	}

	// 检查网络连接权限
	@Override
	public void checkConnect(String host, int port) {
		throw new SecurityException("checkConnect 权限异常：" + host + ":" + port);
	}
}
