var RESOURCE_TYPE = "resource";
var OPERATION_TYPE = "operation";
var PV_TYPE = "pv";

var ctx = ejs.newContext();
/** 国际化信息* */
ctx.i18n = {
	pvLineChart : {
		timeScale : {
			hour : '小时',
			day : '天',
			month : '月'
		},
		title : {
			hour : '按小时访问流量分析',
			day : '按天访问流量分析',
			month : '按月访问流量分析'
		}
	},
	pvTable : {
		globalTitle : 'CALIS全域应用系统访问流量一览表',
		generalTitle : '访问流量一览表',
		yTitle : '流量',
		header : {
			topy : '年',
			topm : '月',
			topd : '日',
			toph : '小时',
			date : '日期',
			gPageView : '全局浏览量',
			gVisits : '全局访问量',
			pageView : '浏览量',
			visits : '访问量',
			ort : '资源类型',
			oasc : '共享域代码',
			oaid : '应用系统ID',
			oat : '应用系统类型',
			op : '操作',
			gOps : '全局操作次数',
			ops : '操作次数'
		}
	},
	rsBar : {
		title : '资源被访排行',
		ort : '资源类型',
		pageView : '浏览次数'
	},
	rsTable : {
		title : '资源被访排行一览表',
		header : {
			ort : '资源类型',
			visit : '浏览次数'
		}
	},
	realtimeTable : {
		visit : {
			header : [ '应用系统名称', '访问时间', '所属学校', '省直辖市名称', '访客IP', '路径' ],
			title : '实时访问信息分析'
		},
		op : {
			header : [ '访问时间', '应用系统名称', '资源简单描述', '访客用户名', '所属学校', '省直辖市名称', '访客IP' ],
			title : '实时常规操作情况'
		}
	},
	opBar : {
		title : '操作排行',
		xTitle : '访问量',
		yTitle : '操作'
	},
	opTable : {
		title : '操作排行一览表'
	}
};
ctx.prototype = {};
/** 实时数据推送服务* */
ctx.cometService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.common.remote.core.comet.ICometService");
/** 应用系统树 * */
ctx.zTree = null;
/** 共享域中心代码* */
ctx.centerCode = null;
/** 应用系统类型 * */
ctx.appType = null;
/** 应用系统类型 * */
ctx.appId = null;
ctx.appSelectDisplay = null;
/** PV、Vist 表应当显示的名字 * */
ctx.pvvTableName = "";
/** 资源排行 表应当显示的名字 * */
ctx.resourceTableName = "";
/** 时间刻度* */
ctx.timeScale = null;
/** 当树节点被点中时* */
ctx.onTreeNodeClick = function(event, treeId, treeNode) {
	ctx.realTimeOpRows.clear();
	ctx.realTimeVisitRows.clear();
	if (treeNode.id == 1) {// 根节点
		ctx.centerCode = null;
		ctx.appType = null;
		ctx.appId = null;
		//ctx.initFirstQuery();
		document.getElementById('appList').style.display = 'none';
		doProgressQuery();
	} else {
		ctx.centerName = treeNode.center[3];
		if (treeNode.apptype) {/* 应用系统类型被选中 */
			ctx.centerCode = treeNode.center[2];
			ctx.appType = treeNode.apptype;
			ctx.appId = null;
			ctx.onAppTypeClick();
		} else {/* 共享域中心被选中 */
			ctx.centerCode = treeNode.center[2];
			ctx.appType = null;
			ctx.appId = null;
			ctx.onCenterClick();
		}
	}
};
/** 某个共享域被选中 * */
ctx.onCenterClick = function() {
	if (!ctx.appType) {
		document.getElementById('appList').style.display = 'none';
	}
	doProgressQuery();
};
/** 某个应用系统类型被点中时(树的三级节点)* */
ctx.onAppTypeClick = function() {
	document.getElementById('appList').style.display = 'block';
	/** 类型被点中时，出该域该类型的应用系统列表* */
	var userDefinedAppList = [];
	if (ctx.centerCode != '100000') {
		userDefinedAppList = [ [ '', 'all', '全部' + ctx.appType, '', '', '', '', '', '', '', '', ctx.centerName, '' ] ];
	}
	calis.rd.renderAppList("#appList", ctx.centerCode, ctx.appType, 'ctx.onAppSelect', "", userDefinedAppList);
	doProgressQuery();
};
/** 某个应用系统被选中时（应用系统列表）* */
ctx.onAppSelect = function(obj) {
	var appId = obj.options[obj.options.selectedIndex].value;
	var appSelectDisplay =  obj.options[obj.options.selectedIndex].innerHTML;
	if (appId == 'all') {
		ctx.appId = null;
		doProgressQuery();
	} else {
		ctx.appId = appId;
		var appInfo = calis.rd.getAppInfo(appId);
		var centerCode = appInfo[10];
		var appType = appInfo[5];
		doProgressQuery();
	}
	ctx.appSelectDisplay = appSelectDisplay;
};
/** 时间刻度被点中时 * */
ctx.onTimeScaleClick = function(scale) {
	ctx.timeScale = scale;
	calis.util.dateControl.changeTime(scale);
};
ctx.queryPvData = function(req, centerCode, appType, appId) {
	if (!centerCode && !appType && !appId) {
		/* 如果不传共享域代码，不传应用系统类型，也不传应用系统ID，则返回CALIS全域的统计 */
		req.metrics = [ "gPageView", "gVisits" ];
	} else {
		req.metrics = [ "pageView", "visits" ];
	}
	if (centerCode) {
		req.dimensions.push("oasc");
		req.filter = "oasc='" + centerCode + "'";
	}
	if (appType) {
		req.dimensions.push("oat");
		req.filter = req.filter + " and oat='" + appType + "'";
	}
	if (appId) {
		req.dimensions = [ "oaid" ];
		req.filter = "oaid='" + appId + "'";
	}
	ctx.filterByTimeSacle(req);
	var rows = calis.report.feedDataJSONReq(req);
	/** 产生一个从时间起点到时间终点的时间序列* */
	var timeSeq = calis.util.timeSeq(req.timeScale, req.startDate, req.endDate);
	ctx.renderRows(rows, req.header, timeSeq);

};
ctx.queryResourceData = function(req, centerCode, appType, appId) {
	if (!centerCode && !appType && !appId) {
		/* 如果不传共享域代码，不传应用系统类型，也不传应用系统ID，则返回CALIS全域的统计 */
		req.metrics = [ "gPageView", "gVisits" ];
	} else {
		req.metrics = [ "pageView", "visits" ];
	}
	req.dimensions.push("ort");
	if (centerCode) {
		req.dimensions.push("oasc");
		req.filter = "oasc='" + centerCode + "'";
	}
	if (appType) {
		req.dimensions.push("oat");
		req.filter = req.filter + " and oat='" + appType + "'";
	}
	if (appId) {
		req.dimensions = [ "ort", "oaid" ];
		req.filter = "oaid='" + appId + "'";
	}
	ctx.filterByTimeSacle(req);
	/** 按时间刻度查询* */
	if (req.filter) {
		req.filter = req.filter + " and ort <> 'p'"
	} else {
		req.filter = "ort <> 'p'"
	}
	var rows = calis.report.feedDataJSONReq(req);
	/** 按天局部统计* */
	/** 资源的时间刻度始终是天 * */
	var datePos = req.dimensions.length - 1;
	var dPos = datePos - 1;
	req.dimensions[dPos] = "topd";
	req.dimensions[datePos] = "date";
	req.timeScale = 'd';
	var rows2 = calis.report.feedDataJSONReq(req);
	if (rows.length == 0 && rows2.length == 0) {
		document.getElementById('resourceTab').style.display = 'none';
		document.getElementById('resourceTime').innerHTML = "无";
	} else {
		document.getElementById('resourceTab').style.display = 'block';
	}
	ctx.showOrHideResource('block');
	ctx.renderResourceRows(rows, req.header);
	req.header = [];
	for ( var i = 0; i < req.metrics.length; i++) {
		var m = req.metrics[i];
		var readM = ctx.i18n.pvTable.header[m];
		req.header.push(readM);
	}
	for ( var i = 0; i < req.dimensions.length; i++) {
		var d = req.dimensions[i];
		var readD = ctx.i18n.pvTable.header[d];
		req.header.push(readD);
	}
	ctx.showOrHideResource1('block');
	ctx.renderResourceRows1(rows2, req.header);
	ctx.onResourceTabSelected(document.getElementById('defaultResourceTab'), 'resource');

};
ctx.queryOpData = function(req, centerCode, appType, appId) {
	if (!centerCode && !appType && !appId) {
		/* 如果不传共享域代码，不传应用系统类型，也不传应用系统ID，则返回CALIS全域的统计 */
		req.metrics = [ "gOps" ];
	} else {
		req.metrics = [ "ops" ];
	}
	req.dimensions.push("op");
	if (centerCode) {
		req.dimensions.push("oasc");
		req.filter = "oasc='" + centerCode + "'";
	}
	if (appType) {
		req.dimensions.push("oat");
		req.filter = req.filter + " and oat='" + appType + "'";
	}
	if (appId) {
		req.dimensions = [ "oaid" ];
		req.filter = "oaid='" + appId + "'";
	}
	ctx.filterByTimeSacle(req);
	if (req.filter) {
		req.filter = req.filter + " and op <> 'v'"
	} else {
		req.filter = "op <> 'v'"
	}
	var rows = calis.report.feedDataJSONReq(req);
	/** 按天局部统计* */
	/** 资源的时间刻度始终是天 * */
	var datePos = req.dimensions.length - 1;
	var dPos = datePos - 1;
	req.dimensions[dPos] = "topd";
	req.dimensions[datePos] = "date";
	req.timeScale = 'd';
	var rows2 = calis.report.feedDataJSONReq(req);
	if (rows.length == 0 && rows2.length == 0) {
		document.getElementById('operationTab').style.display = 'none';
		document.getElementById('opTime').innerHTML = "无";
	} else {
		document.getElementById('operationTab').style.display = 'block';
	}

	ctx.showOrHideOperation('block');
	ctx.renderOperation(rows, req.header);
	req.header = [];
	for ( var i = 0; i < req.metrics.length; i++) {
		var m = req.metrics[i];
		var readM = ctx.i18n.pvTable.header[m];
		req.header.push(readM);
	}
	for ( var i = 0; i < req.dimensions.length; i++) {
		var d = req.dimensions[i];
		var readD = ctx.i18n.pvTable.header[d];
		req.header.push(readD);
	}
	ctx.showOrHideOperation1('block');
	ctx.renderOperation1(rows2, req.header);
	ctx.onOperationTabSelected(document.getElementById('defaultOperationTab'), 'operation');
};
/**
 * 共享域中心的代码是必须传的
 */
ctx.queryData = function(centerCode, appType, appId, type) {
	var req = {
		startDate : new Date(),
		endDate : new Date(),
		dimensions : []
	}
	try {
		if (type == PV_TYPE) {
			ctx.queryPvData(req, centerCode, appType, appId);
		} else if (type == RESOURCE_TYPE) {
			ctx.queryResourceData(req, centerCode, appType, appId);
		} else if (type == OPERATION_TYPE) {
			ctx.queryOpData(req, centerCode, appType, appId);
		}
	} catch (e) {
		$().toastmessage('showToast', {
			text : "会话过期，请您退出重新访问！点击<a style=\"color:lightgreen\" href=\"javascript:alert('" + e.message + "')\">此处</a>查看详细",
			sticky : true,
			position : 'top-center',
			type : 'error',
			closeText : ''
		});
	}

};
ctx.showOrHideOperation1 = function(display) {
	document.getElementById('opChart1').style.display = display;
	document.getElementById('opTable1').style.display = display;
	document.getElementById('opPager1').style.display = display;
	document.getElementById('operation1').style.display = display;
};
ctx.showOrHideOperation = function(display) {
	document.getElementById('opTime').style.display = display;
	document.getElementById('opChart').style.display = display;
	document.getElementById('opTable').style.display = display;
	document.getElementById('opPager').style.display = display;
	document.getElementById('operation').style.display = display;
}
ctx.onOperationTabSelected = function(li, tabContainer) {
	if (li != null) {
		ctx.lastOperationTabSelected.className = '';
		li.className = 'resourceTabSelected';
		ctx.lastOperationTabSelected = li;
	}
	if (tabContainer == 'operation') {
		ctx.showOrHideOperation('block');
		ctx.showOrHideOperation1('none');
	}
	if (tabContainer == 'operation1') {
		ctx.showOrHideOperation('none');
		ctx.showOrHideOperation1('block');
	}
};
ctx.renderOperation = function(rows, header) {
	if (!ctx.opTsMap) {
		ctx.opTsMap = new java.util.HashMap();
	}
	ctx.opTsSortedD1Set = null;
	ctx.opTsD1 = null;
	ctx.opTsD2 = null;
	/**
	 * clear
	 */
	document.getElementById('opChart').innerHTML = "";
	document.getElementById('opChart').className = "";
	document.getElementById('opTable').innerHTML = "";
	document.getElementById('opPager').innerHTML = "";
	if (rows == null || rows.length == 0) {
		return;
	}
	calis.report.renderTableWithPager({
		tableContainer : '#opTable',
		pagerContainer : '#opPager',
		headerArray : header,
		obj2DArray : rows,
		caption : ctx.i18n.rsTable.title,
		perPageRowNum : 10,
		tableClass : "browser",
		pagerClass : "pager",
		printTable : true,
		download : true,
		dic : ctx.dic["op"]
	});
	ctx.opTsMap.clear();
	var tsMap = ctx.opTsMap;
	var opsPos = 0;
	var opPos = 1;
	/** 按时间刻度往上推* */
	var dPos = rows[0].length - 1;
	var hPos = rows[0].length - 2;
	for ( var i = 0; i < rows.length; i++) {
		var row = rows[i];
		var date = row[dPos];
		var h = row[hPos];
		var d = [];
		// d.push(row[opPos], row[opsPos]);
		var opName = ctx.dic["op"][row[opPos]];
		d.push(row[opsPos], opName == null ? row[opPos] : opName);
		if (!tsMap.containsKey(date)) {
			var hMap = new java.util.HashMap();
			hMap.put(h, [ d ]);
			tsMap.put(date, hMap);
		} else {
			var hMap = tsMap.get(date);
			if (!hMap.containsKey(h)) {
				hMap.put(h, [ d ]);
			} else {
				hMap.get(h).push(d);
			}
		}
	}
	ctx.opTsSortedD1Set = null;
	ctx.opTsD1 = null;
	ctx.opTsD2 = null;
	ctx.shiftOp(null, null);
};

ctx.shiftOp = function(d1, d2) {
	if (ctx.__opChart != null) {
		ctx.__opChart.destroy();
		ctx.__opChart = null;
	}
	document.getElementById('opChart').innerHTML = "";
	document.getElementById('opChart').className = "";
	var c = EasyServiceClient.newContext();
	c.d1 = ctx.opTsSortedD1Set;
	if (d1 != null && d2 == null) {
		var options = d1.options;
		for ( var i = 0; i < options.length; i++) {
			if (options[i].selected) {
				d1 = options[i].value;
				break;
			}
		}
		ctx.opTsD1 = d1;
		c.d2 = ctx.opTsMap.get(d1).keySet();
		calis.util.sort(c.d2);
		c.d1Selected = d1;
		c.d2Selected = c.d2[0];
		ctx.opTsD2 = c.d2Selected;
	}
	if (d1 == null && d2 != null) {
		var options = d2.options;
		for ( var i = 0; i < options.length; i++) {
			if (options[i].selected) {
				d2 = options[i].value;
				break;
			}
		}
		c.d1Selected = ctx.opTsD1;
		c.d2Selected = d2;
		c.d2 = ctx.opTsMap.get(c.d1Selected).keySet();
		calis.util.sort(c.d2);
		ctx.opTsD2 = d2;
	}
	if (ctx.opTsD1 == null && ctx.opTsD2 == null) {
		c.d1 = ctx.opTsMap.keySet();
		calis.util.sort(c.d1);
		ctx.opTsSortedD1Set = c.d1;
		c.d2 = ctx.opTsMap.get(c.d1[0]).keySet();
		calis.util.sort(c.d2);
		ctx.opTsD1 = c.d1[0];
		ctx.opTsD2 = c.d2[0];
		c.d1Selected = ctx.opTsD1;
		c.d2Selected = ctx.opTsD2;
	}
	if (ctx.timeScale == 'h') {
		c.d1Name = "日期";
		c.d2Name = "小时";
	}
	if (ctx.timeScale == 'd') {
		c.d1Name = "日期";
		c.d2Name = "天";
	}
	if (ctx.timeScale == 'm') {
		c.d1Name = "年";
		c.d2Name = "月";
	}
	var tplt = calis.getTemplete('./tplt/optime.tplt');
	ejs.renderComplexNode(tplt, '#opTime', c);
	var d2Map = ctx.opTsMap.get(ctx.opTsD1);
	var data = d2Map.get(ctx.opTsD2);
	ctx.__opChart = calis.report.renderHorizontalBarChart(data, ctx.i18n.opBar.title, ctx.i18n.opBar.xTitle, ctx.i18n.opBar.yTitle, 'opChart');
}
ctx.renderOperation1 = function(rows, header) {
	if (ctx.__opChart1 != null) {
		ctx.__opChart1.destroy();
		ctx.__opChart1 = null;
	}
	document.getElementById('opChart1').innerHTML = "";
	document.getElementById('opChart1').className = "";
	document.getElementById('opTable1').innerHTML = "";
	document.getElementById('opPager1').innerHTML = "";
	if (!rows || rows.length == 0) {
		return;
	}
	calis.report.renderTableWithPager({
		tableContainer : '#opTable1',
		pagerContainer : '#opPager1',
		headerArray : header,
		obj2DArray : rows,
		caption : ctx.i18n.opTable.title,
		perPageRowNum : 10,
		tableClass : "browser",
		pagerClass : "pager",
		printTable : true,
		download : true,
		dic : ctx.dic["op"]
	});
	var opsPos = 0;
	var opPos = 1;
	var opMap = new java.util.HashMap();
	for ( var i = 0; i < rows.length; i++) {
		var row = rows[i];
		var op = ctx.dic["op"][row[opPos]];
		op == null ? row[opPos] : op;
		var ops = opMap.get(op);
		if (ops == null) {
			opMap.put(op, parseInt(row[opsPos]));
		} else {
			ops += parseInt(row[opsPos]);
			opMap.put(op, ops);
		}
	}
	calis.util.sort(opMap);
	var l = opMap.size();
	var t = opMap.table;
	var data = [];
	for ( var i = 0; i < l; i++) {
		var d = [ t[i].value, t[i].key ];
		data.push(d);
	}
	ctx.__opChart1 = calis.report.renderHorizontalBarChart(data, ctx.i18n.opBar.title, ctx.i18n.opBar.xTitle, ctx.i18n.opBar.yTitle, 'opChart1');
};
ctx.filterByTimeSacle = function(req) {
	var header = [];
	if (req) {
		/* 全局的有可能是空 */
		if (!req.dimensions) {
			req.dimensions = [];
		}
	}
	var timeRange = calis.util.dateControl.getTimeRange();
	req.startDate = timeRange.startDate;
	req.endDate = timeRange.endDate;
	/**
	 * 保证date在最后一位，对于时间刻度为月的，则年时最后一位 这个是为了保证排序算法统一
	 */
	/* 小时统计 */
	if (ctx.timeScale == "h") {
		req.dimensions.push("toph");
		req.dimensions.push("date");
		req.timeScale = 'h';
	}
	/* 天统计 */
	if (ctx.timeScale == "d") {
		req.dimensions.push("topd");
		req.dimensions.push("date");
		req.timeScale = 'd';
		var today = new Date();
		if(req.endDate > today){
			req.endDate = today;
		}
	}
	/* 月统计 */
	if (ctx.timeScale == "m") {
		req.dimensions.push("topm");
		req.dimensions.push("topy");
		req.timeScale = 'm';
		var thisMonth = new Date();
		if(req.endDate > thisMonth){
			var y = thisMonth.getFullYear();
			var m = thisMonth.getMonth() + 1;
			var d = calis.util.getMaxDayOfMonth(y, m);
			req.endDate = new Date(y, m - 1, d);
		}
		
	}
	for ( var i = 0; i < req.metrics.length; i++) {
		header.push(ctx.i18n.pvTable.header[req.metrics[i]]);
	}
	for ( var i = 0; i < req.dimensions.length; i++) {
		header.push(ctx.i18n.pvTable.header[req.dimensions[i]]);
	}
	req.header = header;
};
ctx.showOrHideResource1 = function(display) {
	document.getElementById('resourceChart1').style.display = display;
	document.getElementById('resourceTable1').style.display = display;
	document.getElementById('resourcePager1').style.display = display;
	document.getElementById('resource1').style.display = display;
};
ctx.showOrHideResource = function(display) {
	document.getElementById('resourceTime').style.display = display;
	document.getElementById('resourceChart').style.display = display;
	document.getElementById('resourceTable').style.display = display;
	document.getElementById('resourcePager').style.display = display;
	document.getElementById('resource').style.display = display;
}
ctx.onResourceTabSelected = function(li, tabContainer) {
	if (li != null) {
		ctx.lastResourceTabSelected.className = '';
		li.className = 'resourceTabSelected';
		ctx.lastResourceTabSelected = li;
	}
	if (tabContainer == 'resource') {
		ctx.showOrHideResource('block');
		ctx.showOrHideResource1('none');
	}
	if (tabContainer == 'resource1') {
		ctx.showOrHideResource('none');
		ctx.showOrHideResource1('block');
	}
};
ctx.renderResourceRows = function(rows, header) {
	if (!ctx.resourceTsMap) {
		ctx.resourceTsMap = new java.util.HashMap();
	}
	ctx.resourceTsSortedD1Set = null;
	ctx.resourceTsD1 = null;
	ctx.resourceTsD2 = null;
	/**
	 * clear
	 */
	document.getElementById('resourceChart').innerHTML = "";
	document.getElementById('resourceChart').className = "";
	document.getElementById('resourceTable').innerHTML = "";
	document.getElementById('resourcePager').innerHTML = "";
	if (rows == null || rows.length == 0) {
		return;
	}
	calis.report.renderTableWithPager({
		tableContainer : '#resourceTable',
		pagerContainer : '#resourcePager',
		headerArray : header,
		obj2DArray : rows,
		caption : ctx.i18n.rsTable.title,
		perPageRowNum : 10,
		tableClass : "browser",
		pagerClass : "pager",
		printTable : true,
		download : true,
		dic : ctx.dic["ort"]
	});
	ctx.resourceTsMap.clear();
	var tsMap = ctx.resourceTsMap;
	var ortPos = 2;
	var pvPos = 0;
	/** 按时间刻度往上推* */
	var dPos = rows[0].length - 1;
	var hPos = rows[0].length - 2;
	for ( var i = 0; i < rows.length; i++) {
		var row = rows[i];
		var date = row[dPos];
		var h = row[hPos];
		var d = [];
		var res = ctx.dic["ort"][row[ortPos]] != null ? ctx.dic["ort"][row[ortPos]] : row[ortPos];
		d.push(res, row[pvPos]);
		if (!tsMap.containsKey(date)) {
			var hMap = new java.util.HashMap();
			hMap.put(h, [ d ]);
			tsMap.put(date, hMap);
		} else {
			var hMap = tsMap.get(date);
			if (!hMap.containsKey(h)) {
				hMap.put(h, [ d ]);
			} else {
				hMap.get(h).push(d);
			}
		}
	}
	ctx.resourceTsSortedD1Set = null;
	ctx.resourceTsD1 = null;
	ctx.resourceTsD2 = null;
	ctx.shiftResource(null, null);
}
ctx.shiftResource = function(d1, d2) {
	if (ctx.__bc != null) {
		ctx.__bc.destroy();
		ctx.__bc = null;
	}
	document.getElementById('resourceChart').innerHTML = "";
	document.getElementById('resourceChart').className = "";
	var c = EasyServiceClient.newContext();
	c.d1 = ctx.resourceTsSortedD1Set;
	if (d1 != null && d2 == null) {
		var options = d1.options;
		for ( var i = 0; i < options.length; i++) {
			if (options[i].selected) {
				d1 = options[i].value;
				break;
			}
		}
		ctx.resourceTsD1 = d1;
		c.d2 = ctx.resourceTsMap.get(d1).keySet();
		calis.util.sort(c.d2);
		c.d1Selected = d1;
		c.d2Selected = c.d2[0];
		ctx.resourceTsD2 = c.d2Selected;
	}
	if (d1 == null && d2 != null) {
		var options = d2.options;
		for ( var i = 0; i < options.length; i++) {
			if (options[i].selected) {
				d2 = options[i].value;
				break;
			}
		}
		c.d1Selected = ctx.resourceTsD1;
		c.d2Selected = d2;
		c.d2 = ctx.resourceTsMap.get(c.d1Selected).keySet();
		calis.util.sort(c.d2);
		ctx.resourceTsD2 = d2;
	}
	if (ctx.resourceTsD1 == null && ctx.resourceTsD2 == null) {
		c.d1 = ctx.resourceTsMap.keySet();
		calis.util.sort(c.d1);
		ctx.resourceTsSortedD1Set = c.d1;
		c.d2 = ctx.resourceTsMap.get(c.d1[0]).keySet();
		calis.util.sort(c.d2);
		ctx.resourceTsD1 = c.d1[0];
		ctx.resourceTsD2 = c.d2[0];
		c.d1Selected = ctx.resourceTsD1;
		c.d2Selected = ctx.resourceTsD2;
	}
	if (ctx.timeScale == 'h') {
		c.d1Name = "日期";
		c.d2Name = "小时";
	}
	if (ctx.timeScale == 'd') {
		c.d1Name = "日期";
		c.d2Name = "天";
	}
	if (ctx.timeScale == 'm') {
		c.d1Name = "年";
		c.d2Name = "月";
	}
	var tplt = calis.getTemplete('./tplt/restime.tplt');
	ejs.renderComplexNode(tplt, '#resourceTime', c);
	var d2Map = ctx.resourceTsMap.get(ctx.resourceTsD1);
	var data = d2Map.get(ctx.resourceTsD2);
	var _15Pos = data.length > 15 ? 15 : data.length;
	ctx.__bc = calis.report.renderBarChart(data.slice(0, _15Pos), ctx.i18n.rsBar.title, ctx.i18n.rsBar.ort, ctx.i18n.rsBar.pageView, 'resourceChart');
}
/**
 * 只是在页面做局部统计，结果并不像在后台计算那样准确
 */
ctx.renderResourceRows1 = function(rows, header) {
	if (ctx.__bc1 != null) {
		ctx.__bc1.destroy();
		ctx.__bc1 = null;
	}
	document.getElementById('resourceChart1').innerHTML = "";
	document.getElementById('resourceChart1').className = "";
	document.getElementById('resourceTable1').innerHTML = "";
	document.getElementById('resourcePager1').innerHTML = "";
	if (!rows || rows.length == 0) {
		return;
	}
	calis.report.renderTableWithPager({
		tableContainer : '#resourceTable1',
		pagerContainer : '#resourcePager1',
		headerArray : header,
		obj2DArray : rows,
		caption : ctx.i18n.rsTable.title,
		perPageRowNum : 10,
		tableClass : "browser",
		pagerClass : "pager",
		printTable : true,
		download : true,
		dic : ctx.dic["ort"]
	});
	var pvPos = 0;
	var ortPos = 2;
	var ortMap = new java.util.HashMap();
	for ( var i = 0; i < rows.length; i++) {
		var row = rows[i];
		var ort = ctx.dic["ort"][row[ortPos]] != null ? ctx.dic["ort"][row[ortPos]] : row[ortPos];
		var pv = row[pvPos];
		if (ortMap.containsKey(ort)) {
			var pvAdd = ortMap.get(ort);
			ortMap.put(ort, parseInt(pvAdd) + parseInt(pv));
		} else {
			ortMap.put(ort, parseInt(pv));
		}
	}
	calis.util.sort(ortMap);
	var l = 15 > ortMap.size() ? ortMap.size() : 15;
	var t = ortMap.table;
	var data = [];
	for ( var i = 0; i < l; i++) {
		var d = [ t[i].key, t[i].value ];
		data.push(d);
	}
	var _15Pos = data.length > 15 ? 15 : data.length;
	ctx.__bc1 = calis.report.renderBarChart(data.slice(0, _15Pos), ctx.i18n.rsBar.title, ctx.i18n.rsBar.ort, ctx.i18n.rsBar.pageView, 'resourceChart1');
};
/**
 * 使用该方法的前提条件是xy是有序的，tSeq是有序的，且同序； xy的x轴是时间序列的整数表示，tSeq时间序列也是整数表示，
 * 且xy的x轴的时间序列的格式与tSeq时间序列的格式相同
 */
ctx.margeTimeSeq = function(xy, tSeq) {
	var xyIndex = 0;
	var nxy = [];
	for ( var tIndex = 0; tIndex < tSeq.length; tIndex++) {
		var t = tSeq[tIndex];
		if (!xy[xyIndex]) {
			nxy.push([ t, 0 ]);
			continue;
		}
		var x = xy[xyIndex][0];
		if (t < x) {
			nxy.push([ t, 0 ]);
		} else if (t == x) {
			nxy.push([ t, xy[xyIndex][1] ]);
			xyIndex++;
		}
	}
	return nxy;
};
ctx.__pvLineChart = null;
ctx.renderRows = function(rows, header, timeSeq) {
	/**
	 * clear
	 */
	document.getElementById('pvChart').innerHTML = "无";
	document.getElementById('pvChart').className = "";
	document.getElementById('pvTable').innerHTML = "";
	document.getElementById('pvPager').innerHTML = "";
	ctx.pTotal = 0;
	ctx.vTotal = 0;
	var pvTotalTplStr = "(总计：浏览量<%=pTotal%>，访问量<%=vTotal%>)";
	if (ctx.__pvLineChart != null) {
		ctx.__pvLineChart.destroy();
		ctx.__pvLineChart = null;
	}
	if (rows) {
		document.getElementById('pvChart').innerHTML = "";
		if (rows.length <= 0) {
			ejs.renderSimpleNode(pvTotalTplStr, ctx, "#pvtotal");
			return;
		}
		var pv = [];
		var visit = [];
		/**
		 * 倒数第一个
		 * 
		 * @see ctx.filterByTimeSacle *
		 */
		var colOfDatePos = rows[0].length - 1;
		for ( var i = 0; i < rows.length; i++) {
			/** 一行* */
			var row = rows[i];
			/** 横轴是时间刻度* */
			var x;
			if (ctx.timeScale == 'h') {
				/** 对于时间刻度是小时的，倒数第2位是小时数* */
				var h = row[colOfDatePos - 1];
				h = h.length > 1 ? h : '0' + h;
				var date = row[colOfDatePos];
				var sortCol = parseInt(date + h);
				row.push(sortCol);
				x = sortCol;
			}
			if (ctx.timeScale == 'd') {
				/** 对于时间刻度是天的，天已经包含在date里面了* */
				var date = row[colOfDatePos];
				x = parseInt(date);
			}
			if (ctx.timeScale == 'm') {
				/** 对于时间刻度是月的，不能包含天的元素，但date里含有天，因此在查询时不带date这个维度，这里需要拼出到月的时间，即:yyyyMM* */
				var y = row[colOfDatePos];
				var m = row[colOfDatePos - 1];
				m = m.length > 1 ? m : '0' + m;
				var date = parseInt(y + m);
				x = date;
			}
			pv.push([ x, parseInt(rows[i][0]) ]);
			visit.push([ x, parseInt(rows[i][1]) ]);
		}
		/* 对时间轴进行升序排序 */
		calis.util.sort(pv, 0, true);
		calis.util.sort(visit, 0, true);
		/* 图相关的处理 */
		pv = ctx.margeTimeSeq(pv, timeSeq);
		visit = ctx.margeTimeSeq(visit, timeSeq);
		for ( var i = 0; i < pv.length; i++) {
			ctx.pTotal += pv[i][1];
			ctx.vTotal += visit[i][1];
		}
		var data = [ pv, visit ];
		var title;
		var timeCategory;
		if (ctx.timeScale == 'h') {
			title = ctx.i18n.pvLineChart.title.hour;
			timeCategory = ctx.i18n.pvLineChart.timeScale.hour;
		} else if (ctx.timeScale == 'd') {
			title = ctx.i18n.pvLineChart.title.day;
			timeCategory = ctx.i18n.pvLineChart.timeScale.day;
		} else if (ctx.timeScale == 'm') {
			title = ctx.i18n.pvLineChart.title.month;
			timeCategory = ctx.i18n.pvLineChart.timeScale.month;
		}
		var tableTitle = "";
		if (ctx.centerCode == '1') {
			tableTitle = ctx.i18n.pvTable.globalTitle;
		}
		if (ctx.centerCode != '1' && ctx.appType && !ctx.appId) {
			tableTitle = ctx.centerName + ctx.appType.toUpperCase() + ctx.i18n.pvTable.generalTitle;
		}
		if (ctx.centerCode != '1' && ctx.appType && ctx.appId) {
			tableTitle = tableTitle = ctx.centerName + ' - ' + calis.rd.getAppInfo(ctx.appId)[2] + ctx.i18n.pvTable.generalTitle;
		}
		/*
		 * 显示表格时按照最后一位排序（最后一位的时间精度是时间刻度）
		 */
		calis.util.sort(rows, rows[0].length - 1);
		calis.report.renderTableWithPager({
			tableContainer : '#pvTable',
			pagerContainer : '#pvPager',
			headerArray : header,
			obj2DArray : rows,
			caption : tableTitle,
			perPageRowNum : 10,
			tableClass : "browser",
			pagerClass : "pager",
			printTable : true,
			download : true
		});
		ejs.renderSimpleNode(pvTotalTplStr, ctx, "#pvtotal");
		ctx.__pvLineChart = calis.report.renderDateLineChart(ctx.timeScale, data, title, timeCategory, ctx.i18n.pvTable.yTitle, 'pvChart');
	}
};
/**
 * 实时分页类
 * 
 * @param arrayLength
 *            数组长度，每个数组元素存放一个一维数组，代表table的每一行
 * @param perpageLength
 *            每页的记录数目
 * @param download
 *            是否打印
 * @returns {RealTimePager}
 */
ctx.RealTimeRows = function(arrayLength, perpageLength) {
	this.arrayLength = arrayLength;
	this.recordCount = 0;// 记录集数目
	this.result = new Array(this.arrayLength);
	this.pos = this.arrayLength;
	this.perpageLength = perpageLength;

	this.clear = function() {
		document.getElementById("realtimePVTable").innerHTML = "";
		document.getElementById("realtimePVPager").innerHTML = "";
		document.getElementById("realtimeOPTable").innerHTML = "";
		document.getElementById("realtimeOPPager").innerHTML = "";
		this.recordCount = 0;
		this.result = new Array(this.arrayLength);
		this.pos = this.arrayLength;
	}
	this.addRecord = function(arr) {
		if (--this.pos < 0) {
			this.pos = this.arrayLength - 1;
		}
		this.result[this.pos] = arr;
		if (++this.recordCount >= this.arrayLength) {
			this.recordCount = this.arrayLength;
		}
		return this;
	};
	this.getRows = function() {
		
		if(this.recordCount < this.arrayLength){
			return this.result.slice(this.pos,this.arrayLength);
		}else{
			var afterPos = this.result.slice(this.pos, this.arrayLength);
			var beforePos = this.result.slice(0, this.pos);
			return afterPos.concat(beforePos);
		}
	};
};
/** 初始化 * */
ctx.init = function() {
	ctx.lastResourceTabSelected = document.getElementById('defaultResourceTab');
	ctx.lastOperationTabSelected = document.getElementById('defaultOperationTab');
	ctx.initRealTimeRender();
	ctx.initLogColumns();
	ctx.initTree();
	ctx.initDic();
	ctx.initDatePicker();
	ctx.initFirstQuery();
};
ctx.initLogColumns = function() {
	ctx.logcols = calis.report.reportService.getLogColumns();
	ctx.logcolsMap = new java.util.HashMap();
	ctx.logcolsIdx = {};
	for ( var i = 0; i < ctx.logcols.size(); i++) {
		ctx.logcolsMap.put(ctx.logcols.get(i).name, ctx.logcols.get(i));
		ctx.logcolsIdx[ctx.logcols.get(i).name] = ctx.logcols.get(i).id;
	}
};
ctx.initDatePicker = function() {
	calis.util.dateControl.initDate("startYear", "startMonth", "startDay");
	calis.util.dateControl.initDate("endYear", "endMonth", "endDay");
	calis.util.dateControl.changeTime('d');
	ctx.timeScale = calis.util.dateControl.queryCondition;
};
ctx.initDic = function() {
	ctx.mdic = {};
	ctx.dic = {};
	for ( var i = 0; i < ctx.logcols.size(); i++) {
		ctx.mdic[ctx.logcols.get(i).name] = ctx.logcols.get(i).shortDesc;
	}
	ctx.dic["op"] = {
		v : "访问",
		vw : "查看",
		s : "检索",
		i : "馆际互借",
		af : "加入收藏",
		d : "下载",
		l : "登录",
		q : "退出",
		'l.np' : "普通登录",
		'l.fd' : "联合认证登录"
	};
	ctx.dic["ort"] = {
		p : "页面",
		j : "期刊",
		"j-d" : "期刊细览",
		"j-h" : "期刊馆藏",
		a : "期刊文章",
		"a-d" : "期刊文章细览",
		'r' : '古籍',
		'r-d' : '古籍细览',
		'r-d-t' : '古籍文本格式细览',
		'r-d-h' : '古籍HTML格式细览',
		'r-i' : '古籍图片',
		'r-i.h' : '古籍图片高精度',
		'r-i.m' : '古籍图片中精度',
		'r-i.l' : '古籍图片低精度',
		'r-eb' : '古籍电子书'
	};
	ctx.dic["st"] = {
		"1" : "本科生",
		"2" : "研究生",
		"3" : "博士",
		"4" : "教师",
		"5" : "留学生",
		"6" : "成人教育",
		"7" : "校外读者",
		"8" : "其它"
	};
	var orgdic = {};
	calis.rd.getNodeInfo(""); // to load cachedNodeInfoMap
	var vs = calis.rd.cachedNodeInfoMap.valueSet();
	for ( var i = 0; i < vs.length; i++) {
		try {
			orgdic[vs[i].get(5)] = vs[i].get(2);
		} catch (e) {
		}
	}
	ctx.dic["lorg"] = ctx.dic["sorg"] = ctx.dic["sten"] = ctx.dic["oaten"] = ctx.dic["oasc"] = orgdic;
	ctx.tsByDic = function(name, val) {
		var sd = ctx.dic[name]
		if (sd == null) {
			return val;
		}
		var rt = sd[val];
		return rt == null ? val : rt;
	}
}

/** 初始化树 * */
ctx.initTree = function() {
	var setting = {
		view : {
			dblClickExpand : false,
			showLine : false,/* 节点之间的连线 */
			selectedMulti : true,
			expandSpeed : ($.browser.msie && parseInt($.browser.version) <= 6) ? "" : "fast"
		},
		data : {
			simpleData : {
				enable : true,
				idKey : "id",
				pIdKey : "pId",
				rootPId : ""
			}
		},
		callback : {
			onClick : ctx.onTreeNodeClick
		}
	};
	var calisNodes = [];
	calisNodes.push({
		id : 1,
		pId : 0,
		name : "CALIS",
		open : true
	});
	///*
	var centerList = calis.rd.getCenterList();
	for ( var i = 0; i < centerList.size() && i < 5; i++) {
		var center = centerList.get(i);
		calisNodes.push({
			id : center[2],
			pId : 1,
			name : center[3],
			'center' : center
		});
		var appTypeList = calis.rd.getAppTypeOfSaasCenter(center[2]);
		for ( var j = 0; j < appTypeList.size(); j++) {
			var appType = appTypeList.get(j);
			calisNodes.push({
				id : appType[1],
				pId : center[2],
				name : appType[0],
				'apptype' : appType,
				'center' : center
			});
		}
	}
	//*/
	var t = $("#tree");
	t = $.fn.zTree.init(t, setting, calisNodes);
	ctx.zTree = $.fn.zTree.getZTreeObj("tree");
};
/** 初始化第一次查询* */
ctx.initFirstQuery = function() {
	ctx.easyPvTabSelected = true;
	ctx.queryData(null, null, null, PV_TYPE);
};
/** 初始化实时查询* */
ctx.initRealTimeRender = function() {
	ctx.realTimeVisitRows = new ctx.RealTimeRows(4500, 15);
	ctx.realTimeOpRows = new ctx.RealTimeRows(4500, 15);
	setTimeout(ctx.realTimerFunc, 0);
}
/** 实时查询的定时器回调函数* */
ctx.realTimerFunc = function() {
	var rt = ctx.cometService.subscribe("realtime/calog", 100000, function(rt) {
		try {
			if (rt != null) {
				var rtCenterCode = rt[ctx.logcolsIdx["oasc"]];
				var rtAppType = rt[ctx.logcolsIdx["oat"]];
				var rtAppId = rt[ctx.logcolsIdx["oaid"]];
				var shouldAdd = false;
				if (ctx.centerCode != null) {
					if (ctx.appType == null) {
						if (ctx.centerCode == rtCenterCode) {
							shouldAdd = true;
						}
					} else {
						if (ctx.appId != null) {
							if (ctx.centerCode == rtCenterCode && ctx.appType == rtAppType && ctx.appId == rtAppId) {
								shouldAdd = true;

							}
						}
						if (ctx.centerCode == rtCenterCode && ctx.appType == rtAppType) {
							shouldAdd = true;
						}
					}
				} else { // calis all
					shouldAdd = true;
				}

				if (shouldAdd) {
					// 应用系统名称 访问时间 所属学校 省直辖市名称 访客IP 路径
					var time = rt[ctx.logcolsIdx.topy] + "-" + rt[ctx.logcolsIdx.topm] + "-" + rt[ctx.logcolsIdx.topd] + " " + rt[ctx.logcolsIdx.toph] + ":"
							+ rt[ctx.logcolsIdx.topms];
					ctx.realTimeVisitRows.addRecord([ rt[ctx.logcolsIdx["oat"]], time, ctx.tsByDic("oaten", rt[ctx.logcolsIdx["oaten"]]), rt[ctx.logcolsIdx["lst"]],
							rt[ctx.logcolsIdx["lip"]], rt[ctx.logcolsIdx["orid"]] ]);
					// 访问时间 应用系统名称 资源简单描述 访客用户名 所属学校 省直辖市名称 访客IP
					ctx.realTimeOpRows.addRecord([ time, rt[ctx.logcolsIdx["oat"]], rt[ctx.logcolsIdx["otil"]], rt[ctx.logcolsIdx["sid"]],
							ctx.tsByDic("oaten", rt[ctx.logcolsIdx["oaten"]]), rt[ctx.logcolsIdx["lst"]], rt[ctx.logcolsIdx["lip"]] ]);

					var rows = ctx.realTimeVisitRows.getRows();
					calis.report.renderTableWithPager({
						tableContainer : '#realtimePVTable',
						pagerContainer : '#realtimePVPager',
						headerArray : ctx.i18n.realtimeTable.visit.header,
						obj2DArray : rows,
						caption : ctx.i18n.realtimeTable.visit.title,
						perPageRowNum : 15,
						tableClass : "realtime",
						pagerClass : "pager",
						printTable : true,
						download : true,
						onPageChange : function(tableId, curPage) {
							ctx.realTimeVisitRows.tableId = tableId;
							ctx.realTimeVisitRows.curPage = curPage;
						},
						id : ctx.realTimeVisitRows.id
					}, ctx.realTimeVisitRows.tableId, ctx.realTimeVisitRows.curPage);

					rows = ctx.realTimeOpRows.getRows();
					calis.report.renderTableWithPager({
						tableContainer : '#realtimeOPTable',
						pagerContainer : '#realtimeOPPager',
						headerArray : ctx.i18n.realtimeTable.op.header,
						obj2DArray : rows,
						caption : ctx.i18n.realtimeTable.op.title,
						perPageRowNum : 15,
						tableClass : "browser",
						pagerClass : "pager",
						printTable : true,
						download : true,
						onPageChange : function(tableId, curPage) {
							ctx.realTimeOpRows.tableId = tableId;
							ctx.realTimeOpRows.curPage = curPage;
						},
						id : ctx.realTimeVisitRows.id
					}, ctx.realTimeOpRows.tableId, ctx.realTimeOpRows.curPage);
				}
			}
		} finally {
			setTimeout(ctx.realTimerFunc, 0);
		}
	});
};
function doQuery() {
	if (ctx.easyPvTabSelected) {
		ctx.queryData(ctx.centerCode, ctx.appType, ctx.appId, PV_TYPE);
	} else if (ctx.easyResTabSelected) {
		ctx.queryData(ctx.centerCode, ctx.appType, ctx.appId, RESOURCE_TYPE);
	} else if (ctx.easyOpTabSelected) {
		ctx.queryData(ctx.centerCode, ctx.appType, ctx.appId, OPERATION_TYPE);
	} else if (ctx.easyAcTabSelected){
		actionCTX.init();
	}
	try {
		clearTimeout(timer);
		progressWin.close();
	} catch (e) {
	}
}
function doProgressQuery() {
	progressWin = new ca.win('./tplt/progress.htm', null, 200, 100, true);
	progressWin.open();
	timer = setTimeout(doQuery, 100);
};
function getCenterCode(){
	return ctx.centerCode;
}
function getAppType(){
	return ctx.appType;
}
