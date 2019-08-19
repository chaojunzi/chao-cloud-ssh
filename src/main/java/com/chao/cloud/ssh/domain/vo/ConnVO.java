package com.chao.cloud.ssh.domain.vo;

import javax.validation.constraints.NotBlank;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 * 连接测试
 * @author 薛超
 * @since 2019年8月19日
 * @version 1.0.7
 */
@Data
public class ConnVO {

	private static final String KEY_TEMPLATE = "{}@{}:{}";

	/**
	 * 主机
	 */
	@NotBlank
	private String host;

	/**
	 * 端口
	 */
	private Integer port = 22;

	/**
	 * 用户名
	 */
	@NotBlank
	private String username;

	/**
	 * 密码
	 */
	@NotBlank
	private String password;

	public String getKey() {
		return StrUtil.format(KEY_TEMPLATE, username, host, port);
	}

}
