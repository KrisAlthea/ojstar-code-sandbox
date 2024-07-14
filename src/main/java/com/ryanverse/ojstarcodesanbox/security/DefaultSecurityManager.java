package com.ryanverse.ojstarcodesanbox.security;

import java.security.Permission;

/**
 * ClassName: DefaultSecurityManager
 * Package: com.ryanverse.ojstarcodesanbox.security
 * Description: 默认的安全管理器
 *
 * @Author Haoran
 * @Create 2024/7/14 9:52
 * @Version 1.0
 */
public class DefaultSecurityManager extends SecurityManager {

	// 检查所有的权限
	@Override
	public void checkPermission(Permission perm) {
		System.out.println("默认不做任何限制");
		System.out.println(perm);
		// super.checkPermission(perm);
	}
}
