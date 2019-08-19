package com.chao.cloud.ssh.websocket.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.chao.cloud.common.exception.BusinessException;
import com.chao.cloud.ssh.websocket.SshWebSocket;
import com.chao.cloud.ssh.websocket.health.MsgEnum;
import com.chao.cloud.ssh.websocket.health.WsMsgDTO;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.extra.ssh.JschUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SshClient implements AutoCloseable {
	// 定义一个flag,来停止线程用
	private boolean stop = false;
	// shell信息
	private ChannelShell shell;
	// 写命令到服务器
	private InputStream in;
	private OutputStream out;
	// 与客户端连接的socket回话
	private SshWebSocket socket;

	public SshClient(Session session, SshWebSocket socket) {
		this.socket = socket;
		try {
			this.shell = JschUtil.openShell(session);
			this.in = shell.getInputStream();
			this.out = shell.getOutputStream();
			startWriter();
		} catch (Exception e) {
			throw new BusinessException("连接失败");
		}
	}

	/**
	 * 向服务器端写数据
	 */
	private void startWriter() {
		// 启动多线程，来获取我们运行的结果
		ThreadUtil.execAsync(() -> {
			try {
				// 读取数据
				while (!stop && shell != null && shell.isConnected() && !shell.isClosed()) { // session是打开的状态
					// 写数据到客户端
					writeToWeb(in);
				}
			} catch (Exception e) {
				log.error("[error--->{}]", e);
			}
		});

	}

	/**
	 * 写数据到服务器端，让机器执行命令
	 * @param cmd
	 * @return 
	 */
	public void write(String cmd) {
		try {
			this.out.write(cmd.getBytes());
			this.out.flush();
		} catch (IOException e) {
			log.error("[error--->{}]", e);
		}

	}

	/**
	 * 关闭连接
	 */
	public void disconnect() {
		try {
			if (shell != null) {
				JschUtil.close(shell.getSession());
				shell.disconnect();
			}
		} catch (Exception e) {
			log.error("{}", e);
		}
		stop = true;
		IoUtil.close(in);
		IoUtil.close(out);
	}

	/**
	 * 写数据到web控制界面
	 * @param in
	 * @throws  Exception 
	 */
	private void writeToWeb(InputStream in) throws Exception {
		// 一个UDP 的用户数据报的数据字段长度为8192字节
		byte[] buff = new byte[8192];
		int len = 0;
		StringBuffer sb = new StringBuffer();
		while ((len = in.read(buff)) > 0) {
			// 设定从0 开始
			sb.setLength(0);
			// 读取数组里面的数据，进行补码
			for (int i = 0; i < len; i++) {
				// 进行补码操作
				char c = (char) (buff[i] & 0xff);
				sb.append(c);
			}
			String line = new String(sb.toString().getBytes("ISO-8859-1"), "UTF-8");
			socket.sendMessage(WsMsgDTO.buildMsg(MsgEnum.RECEIVE, line));
		}

	}

	@Override
	public void close() throws Exception {
		disconnect();
	}
}
