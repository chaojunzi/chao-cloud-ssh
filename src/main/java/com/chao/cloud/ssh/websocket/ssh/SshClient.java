package com.chao.cloud.ssh.websocket.ssh;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.chao.cloud.ssh.websocket.SshWebSocket;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SshClient {

	// 服务器信息
	private SshServer server;
	// 连接信息
	private Connection conn = null;
	// term的session信息
	private Session session = null;
	// 写命令到服务器
	private BufferedWriter out = null;
	// 与客户端连接的socket回话
	private SshWebSocket socket;

	public SshClient(SshServer server, SshWebSocket socket) {
		this.server = server;
		this.socket = socket;
	}

	/**
	 * 连接到目标服务器
	 * 
	 * @return
	 */
	public boolean connect() {
		try {
			String hostname = this.server.getHostname();
			String username = this.server.getUsername();
			String password = this.server.getPassword();
			// 建立连接
			conn = new Connection(hostname, 22);
			// 连接
			conn.connect();

			// 授权
			boolean isAuthenticate = conn.authenticateWithPassword(username, password);
			if (isAuthenticate) {
				// 打开连接
				session = conn.openSession();
				// 打开bash
				// session.requestPTY("bash");
				session.requestPTY("xterm", 90, 30, 0, 0, null);
				// 启动shell
				session.startShell();
				// 向客户端写数据
				startWriter();
				// 输出流
				out = new BufferedWriter(new OutputStreamWriter(session.getStdin(), "utf-8"));
				// 开启term
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			log.error("[error--->{}]", e);
			return false;
		}
	}

	/**
	 * 向服务器端写数据
	 */
	private void startWriter() {
		// 启动多线程，来获取我们运行的结果
		CompletableFuture.runAsync(() -> {
			while (true) {
				if (socket == null) {
					break;
				}
				if (session == null) {
					break;
				}
				// 写数据到客户端
				List<String> lines = CollUtil.toList();
				IoUtil.readUtf8Lines(session.getStdout(), lines);
				lines.forEach(l -> socket.sendMessage(l));
			}
		});

	}

	/**
	 * 写数据到服务器端，让机器执行命令
	 * @param cmd
	 * @return 
	 */
	public boolean write(String cmd) {
		try {
			this.out.write(cmd);
			this.out.flush();
			return true;
		} catch (IOException e) {
			log.error("[error--->{}]", e);
			return false;
		}

	}

	/**
	 * 关闭连接
	 */
	public void disconnect() {
		try {
			// 将与服务器端的连接关闭掉，并设置为空
			conn.close();
			session.close();
			session = null;
			conn = null;
		} catch (Exception e) {
			log.error("[error--->{}]", e);
		}
	}
}
