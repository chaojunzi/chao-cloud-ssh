package com.chao.cloud.ssh.websocket.ssh;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SshServer {

	// 主机名称
	private String hostname;
	// 用户名
	private String username;
	// 密码
	private String password;

}
