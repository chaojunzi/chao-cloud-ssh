package com.chao.cloud.ssh.websocket;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

import com.chao.cloud.common.exception.BusinessException;
import com.chao.cloud.ssh.websocket.health.MsgEnum;
import com.chao.cloud.ssh.websocket.health.WsMsgDTO;
import com.chao.cloud.ssh.websocket.ssh.SshClient;
import com.chao.cloud.ssh.websocket.ssh.SshConfig;
import com.chao.cloud.ssh.websocket.ssh.SshServer;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ServerEndpoint("/ssh/{sid}")
public class SshWebSocket extends BaseWsSocket<String> {

	// 客户端
	SshClient client = null;

	/**
	 * 连接建立成功调用的方法
	 *  
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam("sid") String sid) {
		boolean exist = super.exist(sid);
		if (exist) {// 关闭连接
			this.alreadyLogin(session);
			return;
		}
		sendMessage("连接成功--->");
		// 做多个用户处理的时候，可以在这个地方来 ,判断用户id和
		SshConfig sshConfig = new SshConfig();
		// 配置服务器信息
		SshServer server = new SshServer(sshConfig.getHost(), sshConfig.getUsername(), sshConfig.getPassword());
		// 初始化客户端
		client = new SshClient(server, this);
		// 连接服务器
		Assert.state(client.connect(), "服务连接失败[{}:{}]", sshConfig.getHost(), sid);
		super.onOpen(session, sid);
	}

	/**
	 * 收到客户端消息后调用的方法
	 *
	 * @param message 客户端发送过来的消息*/
	@OnMessage
	public void onMessage(String message, Session session) {
		log.info("收到来自窗口" + sid + "的信息:" + message);
		// 处理连接
		try {
			// 当客户端不为空的情况
			if (client != null) {
				if ("exit".equals(message)) {
					session.close();
					return;
				}
				// 写入前台传递过来的命令，发送到目标服务器上
				client.write(message);
			}
		} catch (Exception e) {
			log.error("[error--->{}]", e);
			super.sendMessage("An error occured, websocket is closed.");
			IoUtil.close(session);
		}

	}

	// 已经登录
	private void alreadyLogin(Session session) {
		try {
			session.getBasicRemote().sendText(JSONUtil.toJsonStr(WsMsgDTO.buildMsg(MsgEnum.CLOSE, "您已经登录")));
			if (session.isOpen()) {
				session.close();
			}
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
	}

	@OnClose
	public void onClose() {
		super.onClose();
	}

	@OnError
	public void onError(Session session, Throwable error) {
		super.onError(session, error);
	}

}
