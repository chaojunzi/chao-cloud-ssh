package com.chao.cloud.ssh.websocket.ssh;

import lombok.Data;

@Data
public class SshConfig {
	private String host;
	private String username;
	private String password;
	private int port = 22;

}