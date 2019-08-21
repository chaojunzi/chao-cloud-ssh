package com.chao.cloud.ssh.controller;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.chao.cloud.common.entity.Response;
import com.chao.cloud.common.util.EntityUtil;
import com.chao.cloud.common.web.HealthController;
import com.chao.cloud.common.web.HealthController.CoreParam;
import com.chao.cloud.ssh.constant.XcConstant;
import com.chao.cloud.ssh.dal.entity.XcConfig;
import com.chao.cloud.ssh.dal.entity.XcGroup;
import com.chao.cloud.ssh.domain.dto.MenuLayuiDTO;
import com.chao.cloud.ssh.domain.vo.ConnVO;
import com.chao.cloud.ssh.service.XcConfigService;
import com.chao.cloud.ssh.service.XcGroupService;
import com.jcraft.jsch.Session;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ssh.JschUtil;
import cn.hutool.json.JSONUtil;

/**
 * @功能：
 * @author： 超君子
 * @时间：2019-07-23
 * @version 1.0.0
 */
@RequestMapping
@Controller
@Validated
public class SshController extends BaseController {
	@Autowired
	private XcConfigService xcConfigService;
	@Autowired
	private XcGroupService xcGroupService;

	/**
	 * 首页
	 * @return
	 */
	@RequestMapping({ "", "/", "/index" })
	public String index() {
		return "index";
	}

	/**
	 * 主页面
	 * @return
	 */
	@RequestMapping("main")
	public String main(Model m) {
		CoreParam core = HealthController.coreParam();
		m.addAttribute("core", core);
		return "main";
	}

	/**
	 * ssh连接页面
	 * @param m
	 * @param id
	 * @return
	 */
	@RequestMapping("/ssh/conn")
	public String conn(Model m, @NotNull Integer id) {
		m.addAttribute("id", id);
		return "ssh/conn";
	}

	/**
	 * 测试是否连接成功
	 */
	@ResponseBody
	@RequestMapping("/ssh/test")
	public Response<String> test(@Valid ConnVO vo) {
		try {
			Session session = JschUtil.openSession(vo.getHost(), vo.getPort(), vo.getUsername(), vo.getPassword());
			session.disconnect();
		} finally {
			JschUtil.close(vo.getKey());
		}
		return Response.ok();
	}

	/**
	 * 系统左侧导航菜单
	 * @return
	 */
	@RequestMapping("/menu/leftList")
	@ResponseBody
	public List<MenuLayuiDTO> leftList() {
		// 加载基础菜单
		String json = ResourceUtil.readUtf8Str("public/json/menu.json");
		List<MenuLayuiDTO> menus = JSONUtil.toList(JSONUtil.parseArray(json), MenuLayuiDTO.class);
		//
		List<XcGroup> list = xcGroupService.list();
		int id = 1;
		if (CollUtil.isNotEmpty(list)) {
			for (XcGroup g : list) {
				menus.add(buildMenu(XcConstant.TOP_ID, g.getId(), XcConstant.TOP_ICON, g.getName(), null));
				if (id < g.getId()) {
					id = g.getId();
				}
			}
			id++;
		}
		List<XcConfig> configs = xcConfigService.list();
		if (CollUtil.isNotEmpty(configs)) {
			for (XcConfig c : configs) {
				menus.add(buildMenu(c.getGroupId(), id++, XcConstant.CHILDREN_ICON, c.getTitle(),
						StrUtil.format(XcConstant.HREF_TEMPLATE, c.getId())));
			}
		}
		// 递归
		return EntityUtil.toTreeAnnoList(menus, XcConstant.TOP_ID);
	}

	private MenuLayuiDTO buildMenu(Integer parentId, Integer menuId, String icon, String title, String href) {
		return MenuLayuiDTO.of()//
				.setMenuId(menuId)//
				.setParentId(parentId)//
				.setIcon(icon)//
				.setTitle(title)//
				.setHref(href);
	}

}