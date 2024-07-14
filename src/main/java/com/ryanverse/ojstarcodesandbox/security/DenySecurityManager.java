package com.ryanverse.ojstarcodesandbox.security;

import java.security.Permission;

/**
 * ClassName: DenySecurityManager
 * Package: com.ryanverse.ojstarcodesandbox.security
 * Description: 拒绝所有的权限
 *
 * @Author Haoran
 * @Create 2024/7/14 9:57
 * @Version 1.0
 */
public class DenySecurityManager extends SecurityManager{

	// 检查所有的权限
	@Override
	public void checkPermission(Permission perm) {
		throw new SecurityException("权限异常：" + perm.toString());
	}
}
