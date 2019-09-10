package com.chao.cloud.ssh.websocket;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

import com.chao.cloud.common.core.SpringContextUtil;
import com.chao.cloud.common.exception.BusinessException;
import com.chao.cloud.ssh.dal.entity.XcConfig;
import com.chao.cloud.ssh.service.XcConfigService;
import com.chao.cloud.ssh.websocket.health.MsgEnum;
import com.chao.cloud.ssh.websocket.health.WsMsgDTO;
import com.chao.cloud.ssh.websocket.ssh.SshClient;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.extra.ssh.JschUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ServerEndpoint("/ssh/{sid}")
public class SshWebSocket extends BaseWsSocket<String> {

	private SshClient client;

    @OnOpen
	public void onOpen(Session session, @PathParam("sid") String sid) {
		boolean exist = super.exist(sid);
		if (exist) {// 关闭连接
			this.alreadyLogin(session);
			return;
		}
		// 连接websocket
		super.open(session, sid);
		// 做多个用户处理的时候，可以在这个地方来 ,判断用户id和
		XcConfigService configService = SpringContextUtil.getBean(XcConfigService.class);
		XcConfig config = configService.getById(sid.split("@")[0]);
		if (BeanUtil.isEmpty(config)) {
			sendMessage(WsMsgDTO.buildMsg(MsgEnum.CLOSE, "无效的连接地址"));
			return;
		}
		com.jcraft.jsch.Session sshSession = JschUtil.createSession(config.getHost(), config.getPort(),
				config.getUsername(), config.getPassword());
		// 配置服务器信息
		this.client = new SshClient(sshSession, this);
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
			client.write(message);
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
		} finally {
			IoUtil.close(client);
		}
	}

	@OnClose
	public void onClose() {
		IoUtil.close(client);
		super.onClose();
	}

	@OnError
	public void onError(Session session, Throwable error) {
		IoUtil.close(client);
		super.onError(session, error);
	}

}
