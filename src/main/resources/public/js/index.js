var $, tab, dataStr, layer;
layui.config({
	base : "/js/"
}).extend({
	"bodyTab" : "bodyTab"
})
layui.use([ 'bodyTab', 'form', 'element', 'layer', 'jquery' ], function() {
	var form = layui.form, element = layui.element;
	$ = layui.$;
	// 内容初始化
	$("#footer").text(adminConfig.footer);// 页脚
	$(".logoTitle").text(adminConfig.logoTitle);// logo 标题
	// 用户信息
	$("#info").click(function() {
		parent.layer.open({
			type : 2,
			title : '基本信息',
			maxmin : false,
			shadeClose : true, // 点击遮罩关闭层
			area : [ '20%', '38%' ],
			content : '/xc/user/info/' // iframe的url
		});
	});
	//
	layer = parent.layer === undefined ? layui.layer : top.layer;
	tab = layui.bodyTab({
		openTabNum : "50", // 最大可打开窗口数量
		url : "/menu/leftList" // 获取左侧菜单json地址
	});

	// 通过顶部菜单获取左侧二三级菜单 注：此处只做演示之用，实际开发中通过接口传参的方式获取导航数据
	function getData(json) {
		var user_menu = $("#userkey").val() + ":menus";
		var menus = window.sessionStorage.getItem(user_menu);
		if (menus == null) {
			$.ajaxSettings.async = false;// 同步执行
			$.getJSON(tab.tabConfig.url, function(data) {
				window.sessionStorage.setItem(user_menu, JSON.stringify(data))
				menus = data;
			});
		} else {
			console.log("user_menu 已缓存:" + user_menu)
		}
		dataStr = menus;
		// 重新渲染左侧菜单
		tab.render();
	}
	// 页面加载时判断左侧菜单是否显示
	// 通过顶部菜单获取左侧菜单
	$(".topLevelMenus li,.mobileTopLevelMenus dd").click(function() {
		if ($(this).parents(".mobileTopLevelMenus").length != "0") {
			$(".topLevelMenus li").eq($(this).index()).addClass("layui-this").siblings().removeClass("layui-this");
		} else {
			$(".mobileTopLevelMenus dd").eq($(this).index()).addClass("layui-this").siblings().removeClass("layui-this");
		}
		$(".layui-layout-admin").removeClass("showMenu");
		$("body").addClass("site-mobile");
		getData($(this).data("menu"));
		// 渲染顶部窗口
		tab.tabMove();
	})

	// 隐藏左侧导航
	$(".hideMenu").click(function() {
		if ($(".topLevelMenus li.layui-this a").data("url")) {
			layer.msg("此栏目状态下左侧菜单不可展开"); // 主要为了避免左侧显示的内容与顶部菜单不匹配
			return false;
		}
		$(".layui-layout-admin").toggleClass("showMenu");
		// 渲染顶部窗口
		tab.tabMove();
	})

	// 通过顶部菜单获取左侧二三级菜单 注：此处只做演示之用，实际开发中通过接口传参的方式获取导航数据
	getData("contentManagement");

	// 手机设备的简单适配
	$('.site-tree-mobile').on('click', function() {
		$('body').addClass('site-mobile');
	});
	$('.site-mobile-shade').on('click', function() {
		$('body').removeClass('site-mobile');
	});

	// 添加新窗口
	$("body").on("click", ".layui-nav .layui-nav-item a:not('.mobileTopLevelMenus .layui-nav-item a')", function() {
		// 如果不存在子级
		if ($(this).siblings().length == 0) {
			addTab($(this));
			$('body').removeClass('site-mobile'); // 移动端点击菜单关闭菜单层
		}
		$(this).parent("li").siblings().removeClass("layui-nav-itemed");
	});
	// 清除缓存
	$(".clearCache").click(function() {
		window.sessionStorage.clear();
		window.localStorage.clear();
		var index = layer.msg('清除缓存中，请稍候', {
			icon : 16,
			time : false,
			shade : 0.8
		});
		setTimeout(function() {
			layer.close(index);
			layer.msg("缓存清除成功！");
		}, 1000);
	});
});
// 打开新窗口
function addTab(_this) {
	tab.tabAdd(_this);
}
// 新增tab（子页面）
function leafTabAdd(url, title, icon) {
	tab.leafTabAdd(url, title, strToNumber(url), icon);
}

// 刷新当前页面
function reloadCurrTab() {
	$(".clildFrame .layui-tab-item.layui-show").find("iframe")[0].contentWindow.location.reload();
}

function strToNumber(str) {
	var result = '1';
	var reg = /[a-z]/i;
	for (var i = 0; i < str.length; i++) {
		if (reg.test(str[i])) {
			result += str[i].charCodeAt();
		} else if (isPositiveInteger(str[i])) {
			result += str[i];
		} else {
			result += 0;
		}
	}
	return result;
}
// 是否为正整数
function isPositiveInteger(s) {
	var re = /^[0-9]+$/;
	return re.test(s)
}