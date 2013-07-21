if (!EasyServiceClient) {
	throw "需要EasyService平台远程服务调用JS客户端支持";
}
if (!ejs) {
	throw "需要EasyService平台模板支持";
}
if (!Date.prototype.format) {
	Date.prototype.format = function(fmt) {
		var o = {
			"M+" : this.getMonth() + 1, // month
			"d+" : this.getDate(), // day
			"h+" : this.getHours(), // hour
			"m+" : this.getMinutes(), // minute
			"s+" : this.getSeconds(), // second
			"q+" : Math.floor((this.getMonth() + 3) / 3), // quarter
			"S" : this.getMilliseconds()
		// millisecond
		}
		if (!fmt) {
			fmt = 'yyyyMMdd';
		}
		if (/(y+)/.test(fmt))
			fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
		for ( var k in o)
			if (new RegExp("(" + k + ")").test(fmt))
				fmt = fmt.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k] : ("00" + o[k]).substr(("" + o[k]).length));
		return fmt;
	};
}
var calis = {};
(function() {
	calis.__tpltCache = new java.util.HashMap();
	calis.getTemplete = function(url) {
		var tplt = calis.__tpltCache.get(url);
		if (!tplt) {
			var t = ejs.getNodeText(url);
			calis.__tpltCache.put(url, t);
			tplt = t;
		}
		return tplt;
	};
	calis.util = function() {
		this.initialize.apply(this, arguments);
	};
	calis.util.__maxDayOfLeapYear = {
		1 : 31,
		2 : 29,
		3 : 31,
		4 : 30,
		5 : 31,
		6 : 30,
		7 : 31,
		8 : 31,
		9 : 30,
		10 : 31,
		11 : 30,
		12 : 31
	};
	calis.util.__maxDayOfGernYear = {
		1 : 31,
		2 : 28,
		3 : 31,
		4 : 30,
		5 : 31,
		6 : 30,
		7 : 31,
		8 : 31,
		9 : 30,
		10 : 31,
		11 : 30,
		12 : 31
	};
	calis.util.getMaxDayOfMonth = function(year, month) {
		var isLeapYear = false;
		if (month != 2) {
			return calis.util.__maxDayOfGernYear[month];
		}
		if (year > 0) {
			if (year % 400 == 0) {
				isLeapYear = true;
			} else if (year % 100 == 0) {
				isLeapYear = false;
			} else if (year % 4 == 0) {
				isLeapYear = true;
			} else {
				isLeapYear = false;
			}
		} else {
			/** 不应当发生* */
			return -1;
		}
		if (isLeapYear) {
			return calis.util.__maxDayOfLeapYear[month];
		} else {
			return calis.util.__maxDayOfGernYear[month];
		}
	};
	calis.util.dateControl = {
		queryCondition : 'd',
		timeRange : {
			startTime : {
				day : '',
				month : '',
				year : ''
			},
			endTime : {
				day : '',
				month : '',
				year : ''
			}
		},
		getById : function(id) {
			return document.getElementById(id);
		},
		changeTime : function(dateType) {
			if (dateType == 'h') {
				this.getById('startDay').className = this.getById('startMonth').className = this.getById('startYear').className = "showInline";
				this.getById('endDay').className = this.getById('endMonth').className = this.getById('endYear').className = "showInline";
				this.queryCondition = "h";
				this.getById('hourCat').className = 'selTime';
				this.getById('dayCat').className = this.getById('monthCat').className = 'unSelTime';
			} else if (dateType == 'd') {
				this.getById('startDay').className = "hide";
				this.getById('endDay').className = "hide";
				this.getById('startMonth').className = "showInline"
				this.getById('endMonth').className = "showInline"
				this.queryCondition = "d";
				this.getById('dayCat').className = 'selTime';
				this.getById('hourCat').className = this.getById('monthCat').className = 'unSelTime';
			} else if (dateType == 'm') {
				this.getById('startDay').className = "hide";
				this.getById('startMonth').className = "hide";
				this.getById('endDay').className = "hide";
				this.getById('endMonth').className = "hide";
				this.queryCondition = "m";
				this.getById('monthCat').className = 'selTime';
				this.getById('hourCat').className = this.getById('dayCat').className = 'unSelTime';
			}
		},
		initDate : function(yEleId, mEleId, dEleId) {
			var thisObj = this;
			var ds = new _DS(yEleId, mEleId, dEleId, {
				MaxYear : new Date().getFullYear(),
				MinYear : new Date().getFullYear() - 5,
				onChange : function() {
					if (yEleId == "startYear") {
						thisObj.timeRange.startTime.day = this.Day;
						thisObj.timeRange.startTime.month = this.Month;
						thisObj.timeRange.startTime.year = this.Year;
					} else if (yEleId == "endYear") {
						thisObj.timeRange.endTime.day = this.Day;
						thisObj.timeRange.endTime.month = this.Month;
						thisObj.timeRange.endTime.year = this.Year;
					}
				}
			});
			ds.onChange();
		},
		getTimeRange : function() {
			if (this.queryCondition == "h") {
				var obj = {
					startDate : new Date(this.timeRange.startTime.year, this.timeRange.startTime.month - 1, this.timeRange.startTime.day),
					endDate : new Date(this.timeRange.endTime.year, this.timeRange.endTime.month - 1, this.timeRange.endTime.day)
				}
				return obj;
			} else if (this.queryCondition == "d") {
				var eyear = parseInt(this.timeRange.endTime.year);
				var emonth = this.timeRange.endTime.month;
				var obj = {
					startDate : new Date(this.timeRange.startTime.year, this.timeRange.startTime.month - 1, 1),
					endDate : new Date(this.timeRange.endTime.year, this.timeRange.endTime.month - 1, calis.util.getMaxDayOfMonth(eyear, emonth))
				}
				return obj;
			} else if (this.queryCondition == "m") {
				var obj = {
					startDate : new Date(this.timeRange.startTime.year, 0, 1),
					endDate : new Date(this.timeRange.endTime.year, 11, 31)
				}
				return obj;
			}
		}
	};
	calis.util.timeSeq = function(timeScale, sDate, eDate) {
		/**
		 * format为： yyyyMMddhh yyyyMMdd yyyyMM
		 */
		var sy = sDate.getFullYear();
		var sm = sDate.getMonth() + 1;
		var sd = sDate.getDate();
		var ey = eDate.getFullYear();
		var em = eDate.getMonth() + 1;
		var ed = eDate.getDate();

		var rst = [];
		var format = null;
		if (timeScale == 'h') {
			var y = sy
			for (; y <= ey; y++) {
				var m = 1;
				var mMax = 12;
				if (y == sy) {
					m = sm;
				}
				if (y == ey) {
					mMax = em;
				}
				for (; m <= mMax; m++) {
					var d = 1;
					var dMax = calis.util.getMaxDayOfMonth(y, m);
					if (y == sy && m == sm) {
						d = sd;
					}
					if (y == ey && m == em) {
						dMax = ed;
					}
					for (; d <= dMax; d++) {
						ms = m < 10 ? '0' + m : '' + m;
						ds = d < 10 ? '0' + d : '' + d;
						for ( var h = 0; h <= 23; h++) {
							hs = h < 10 ? '0' + h : '' + h;
							rst.push(parseInt(y + ms + ds + hs));
						}
					}
				}
			}
		}
		if (timeScale == 'd') {
			var y = sy
			for (; y <= ey; y++) {
				var m = 1;
				var mMax = 12;
				if (y == sy) {
					m = sm;
				}
				if (y == ey) {
					mMax = em;
				}
				for (; m <= mMax; m++) {
					var d = 1;
					var dMax = calis.util.getMaxDayOfMonth(y, m);
					if (y == sy && m == sm) {
						d = sd;
					}
					if (y == ey && m == em) {
						dMax = ed;
					}
					for (; d <= dMax; d++) {
						ms = m < 10 ? '0' + m : '' + m;
						ds = d < 10 ? '0' + d : '' + d;
						rst.push(parseInt(y + ms + ds));
					}
				}
			}
		}
		if (timeScale == 'm') {
			var y = sy
			for (; y <= ey; y++) {
				var m = 1;
				var mMax = 12;
				if (y == sy) {
					m = sm;
				}
				if (y == ey) {
					mMax = em;
				}
				for (; m <= mMax; m++) {
					m = m < 10 ? '0' + m : '' + m;
					rst.push(parseInt(y + m));
				}
			}
		}
		if (timeScale == 'y') {
			var y = sy
			for (; y <= ey; y++) {
			 rst.push(parseInt(y));
			}
		}
		return rst;
	};
	calis.util.invokeRawBindingService = function(obj) {
		var form = document.createElement("form");
		var iframe = document.createElement("iframe");
		var iframeId = '___iframeTarget';
		iframe.id = iframeId;
		iframe.style.visibility = 'hidden';
		if (!obj.action) {
			return null;
		} else {
			form.action = obj.action;
			form.method = "POST";
			form.target = iframeId;
			form.style.visibility = 'hidden';
		}
		if (!obj.method) {
			return null;
		} else {
			var plen = 0;
			if (obj.params) {
				plen = obj.params.length;
			} else {
				obj.params = [];
			}
			var m = obj.method + "_" + plen
			var d = new Date();
			var id = d.getTime();
			var req = {
				"id" : id + '',
				"method" : m,
				"params" : obj.params
			}
			var reqStr = EasyServiceRuntime.toEJson(req);
			var freq = document.createElement("input");
			freq.name = "request";
			freq.value = reqStr;
			freq.type = "text";
			var fpf = document.createElement("input");
			fpf.name = ".pf";
			fpf.value = "es:ws-raw-http";
			fpf.type = "text";
			var fct = document.createElement("input");
			fct.name = ".contentType";
			fct.type = "text";
			if (obj.responseContentType) {
				fct.value = obj.responseContentType;
			} else {
				fct.value = "text/plain";
			}
			form.appendChild(freq);
			form.appendChild(fpf);
			form.appendChild(fct);
			document.body.appendChild(form);
			document.body.appendChild(iframe);
			form.submit();
			document.body.removeChild(form);
			document.body.removeChild(iframe);
		}
	};
	calis.util.sort = function(arr, valOps, asc) {
		var _this = this;
		var len = 0;
		if (!arr) {
			return arr;
		}
		if (arr instanceof Array) {
			len = arr.length;
		} else if (typeof arr == 'object') {
			len = arr.size();
		}
		this.getValue = function(a, i) {
			if (a instanceof Array) {
				return a[i];
			}
			if (a._t_) {
				if (a._t_ == "java.util.Map" || a._t_ == "java.util.HashMap") {
					return a.table[i];
				}
			}
		};
		this.setValue = function(a, i, o) {
			if (a instanceof Array) {
				a[i] = o;
			}
			if (a._t_) {
				if (a._t_ == "java.util.Map" || a._t_ == "java.util.HashMap") {
					a.table[i] = o;
				}
			}
		};
		this.compareTo = function(a, b) {
			var va, vb;
			if (a instanceof Array) {
				va = parseInt(a[valOps]);
				vb = parseInt(b[valOps]);
			} else {
				if (a.key) {
					va = a.value;
					vb = b.value;
				} else {
					va = parseInt(a);
					vb = parseInt(b);
				}
			}
			if (va > vb) {
				if (asc) {
					return 1;
				} else {
					return -1;
				}
			} else {
				if (asc) {
					return -1;
				} else {
					return 1;
				}
			}
		};
		this.quickSort = function(arr, low, high) {
			var i = low;
			var j = high;
			if (low < high) {
				var key = _this.getValue(arr, low);
				while (i < j) {
					while (i < j && _this.compareTo(_this.getValue(arr, j), key) > 0) {
						j--;
					}
					if (i < j) {
						_this.setValue(arr, i, _this.getValue(arr, j));
						i++;
					}
					while (i < j && _this.compareTo(_this.getValue(arr, i), key) < 0) {
						i++;
					}
					if (i < j) {
						_this.setValue(arr, j, _this.getValue(arr, i));
						j--;
					}
				}
				_this.setValue(arr, i, key);
				_this.quickSort(arr, low, i - 1);
				_this.quickSort(arr, i + 1, high);
			}
		};
		_this.quickSort(arr, 0, len - 1);
	};
	calis.rd = function() {
		this.initialize.apply(this, arguments);
	};
	calis.rd.rcsDataQueryService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.common.rd.IRCSDataQueryService");
	calis.rd.centerList = calis.rd.rcsDataQueryService.listSaasCenter();
	calis.rd.centerAppTypeObj = null;
	calis.rd.cachedNodeInfoMap = null;
	calis.rd.center_AppTypeAppMap_Map = new java.util.HashMap();
	calis.rd.appInfoMap = new java.util.HashMap();
	calis.rd.treeNodeInfoMap = calis.rd.rcsDataQueryService.getTreeNodeInfoMap();
	calis.rd.getCenterList = function() {
		return calis.rd.centerList;
	}
	calis.rd.getAppTypeOfSaasCenter = function(centerCode) {
		if (calis.rd.centerAppTypeObj) {
			return calis.rd.centerAppTypeObj[centerCode];
		}
		return null;
	};
	{
		var bs = new EasyServiceClient.BatchCall();
		for ( var i = 0; i < calis.rd.centerList.size(); i++) {
			var ccode = calis.rd.centerList.get(i)[2];
			bs.add(ccode, 'com.cnebula.analytics.common.rd.IRCSDataQueryService', 'getAppTypeOfSaasCenter', [ ccode ]);
		}
		calis.rd.centerAppTypeObj = bs.execute();
	}
	calis.rd.getAppList = function(centerCode, appType) {
		var map = calis.rd.center_AppTypeAppMap_Map;
		var appTypeMap = map.get(centerCode);
		if (!appTypeMap) {
			var appType2apps = this.rcsDataQueryService.getAppOfAppType(centerCode);
			if (!appType2apps) {
				return new java.util.HashMap();
			}
			map.put(centerCode, appType2apps);
			return map.get(centerCode).get(appType);
		} else {
			return appTypeMap.get(appType);
		}
	};
	calis.rd.getAppInfo = function(appId) {
		if (calis.rd.appInfoMap.containsKey(appId)) {
			return calis.rd.appInfoMap.get(appId);
		} else {
			var appInfo = calis.rd.rcsDataQueryService.getAppInfo(appId);
			if (appInfo) {
				calis.rd.appInfoMap.put(appInfo.get(1), appInfo.values);
				return appInfo.values;
			}
		}
	};
	calis.rd.getNodeInfo = function(nodeCode) {
		if (calis.rd.cachedNodeInfoMap == null) {
			calis.rd.cachedNodeInfoMap = calis.rd.rcsDataQueryService.listNodeInfo();
		}
		return calis.rd.cachedNodeInfoMap.get(nodeCode);
	};
	calis.rd.renderAppList = function(container, centerCode, appType, clickFunc, lClass, userDefinedList) {
		var appList = calis.rd.getAppList(centerCode, appType);
		var tplt = calis.getTemplete('./tplt/alist.tplt');
		var ctx = EasyServiceClient.newContext();
		ctx.applist = new java.util.ArrayList();
		if (!lClass) {
			ctx.lClass = null;
		} else {
			ctx.lClass = lClass;
		}
		if (!clickFunc) {
			ctx.clickFunc = null;
		} else {
			ctx.clickFunc = clickFunc;
		}
		if (userDefinedList) {
			for ( var i = 0; i < userDefinedList.length; i++) {
				ctx.applist.add(userDefinedList[i]);
				calis.rd.appInfoMap.put(userDefinedList[i][1], userDefinedList[i]);
			}
		}
		for ( var i = 0; i < appList.size(); i++) {
			var appInfo = appList.get(i);
			var nodeInfo = calis.rd.getNodeInfo(appInfo[9]);
			appInfo.push(nodeInfo.get(3));
			if (!calis.rd.appInfoMap.containsKey(appInfo[1])) {
				calis.rd.appInfoMap.put(appInfo[1], appInfo);
			}
			ctx.applist.add(appInfo);
		}
		ejs.renderComplexNode(tplt, container, ctx);
	};

	calis.report = function() {
		this.initialize.apply(this, arguments);
	};
	calis.report._tableObjMap = new java.util.HashMap();
	calis.report.reportService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.reportservice.ICAReportService");
	calis.report.complexReportService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.reportservice.IComplexReportService");
	calis.report.newDataExportRequest = function() {
		return {
			_t_ : 'com.cnebula.analytics.common.DataExportRequest',
			startDate : new Date(),
			endDate : new Date(),
			filter : '',
			maxResults : 10000,
			timeScale : 'd',
			sort : new java.util.ArrayList(),
			dimensions : new java.util.ArrayList(),
			metrics : new java.util.ArrayList(),
			groups : new java.util.ArrayList(),
			id : ''
		};
	};
	calis.report.feedDataJSONReq = function(obj) {
		var header;
		var req = calis.report.newDataExportRequest();
		if (obj.header) {
			header = obj.header;
		}
		if (obj.metrics) {
			for ( var i = 0; i < obj.metrics.length; i++) {
				req.metrics.add(obj.metrics[i]);
			}
		}
		if (obj.dimensions) {
			for ( var i = 0; i < obj.dimensions.length; i++) {
				req.dimensions.add(obj.dimensions[i]);
			}
		}
		if (obj.timeScale) {
			req.timeScale = obj.timeScale;
		} else {
			req.timeScale = 'h';
		}
		if (obj.filter) {
			req.filter = obj.filter;
		}
		req.startDate = obj.startDate;
		req.endDate = obj.endDate;
		return calis.report.feedDataRAWReq(req);
	};
	calis.report.feedDataRAWReq = function(request) {
		if (!request) {
			return null;
		} else {
			return calis.report.reportService.feedData(request);
		}
	};
	calis.report.feedDataRAWReqList = function(requestList){
		if (!requestList) {
			return null;
		} else {
			return calis.report.complexReportService.feedData(requestList);
		}
	}
	calis.report.cacheTable = function(id, table) {
		calis.report._tableObjMap.remove(id);
		calis.report._tableObjMap.put(id, table);
	};
	calis.report.getCachedTable = function(id) {
		return calis.report._tableObjMap.get(id);
	};
	calis.report.downloadRows = function(header, rows, fileName) {
		if (!fileName) {
			fileName = "table.xls";
		}
		calis.util.invokeRawBindingService({
			action : "/easyservice/com.cnebula.analytics.reportservice.ICAReportService/" + fileName,
			method : "convert",
			params : [ header, rows ],
			responseContentType : "application/vnd.ms-excel"
		});
	};
	calis.report.renderTable = function(tpltUrl, container, headerArray, obj2DArray, caption, tableClass, dic) {
		var tplt;
		if (tpltUrl.innerHTML) {
			tplt = tpltUrl
		}
		tplt = calis.getTemplete(tpltUrl);
		var ctx = EasyServiceClient.newContext();
		ctx.header = headerArray;
		ctx.rows = obj2DArray;
		ctx.dic = dic;
		/* 模板中对未定义的变量支持有问题 */
		if (caption) {
			ctx.caption = caption;
		} else {
			ctx.caption = null;
		}
		if (tableClass) {
			ctx.tClass = tableClass;
		} else {
			ctx.tClass = null;
		}
		ejs.renderComplexNode(tplt, container, ctx);
		$('#jtable_render').dataTable();
	};
	calis.report.downloadTableData = function(tableId) {
		var t = calis.report.getCachedTable(tableId);
		calis.report.downloadRows(t.headerArray, t.obj2DArray, 'data.xls');
	};
	calis.report.printTableData = function(tableId) {
		var t = calis.report.getCachedTable(tableId);
		var frm = window.frames['printFrame'];

		frm.document.body.innerHTML = "";
		var table = frm.document.createElement('div');
		calis.report.renderTable('./tplt/table.tplt', table, t.headerArray, t.obj2DArray);
		frm.document.body.appendChild(table);
		frm.focus();
		frm.print();
		frm.document.body.innerHTML = "";
	};
	calis.report.readerPager = function(tpltUrl, container, rowCount, perPageRowNum, curPage, pageClass, tableId, printTable, download, dic) {
		if (!container) {
			return;
		}
		var id = container.substring(1);
		var obj = document.getElementById(id);
		if (obj) {
			obj.innerHTML = "";
		}
		var tplt = calis.getTemplete(tpltUrl);
		var vId = tableId;
		var t = null;
		if (vId) {
			t = calis.report.getCachedTable(vId);
		}
		if (t) {
			perPageRowNum = t.perPageRowNum;
		}
		if (rowCount <= 0) {
			rowCount = 1;
		}
		var pageCount = (rowCount % perPageRowNum) == 0 ? (rowCount / perPageRowNum) : Math.ceil(rowCount / perPageRowNum);
		if (curPage <= 0) {
			curPage = 1;
		}
		if (curPage > pageCount) {
			curPage = pageCount;
		}
		var ctx = EasyServiceClient.newContext();
		ctx.tpltUrl = tpltUrl;
		ctx.container = container;
		ctx.pageCount = pageCount;
		ctx.perPageRowNum = perPageRowNum;
		ctx.rowCount = rowCount;
		ctx.pageClass = pageClass;
		ctx.curPage = curPage;
		ctx.download = download;
		ctx.printTable = printTable;
		if (t) {
			var start = (curPage - 1) * perPageRowNum;
			var end = start + perPageRowNum;
			var arr = t.obj2DArray.slice(start, end);
			var c = rowCount - start;
			if (c < perPageRowNum && arr[0]) {
				var empArr = new Array();
				for ( var i = arr[0].length; i >= 0; --i) {
					empArr.push('');
				}
				c = end - rowCount;
				for (; c >= 0; --c) {
					arr.push(empArr);
				}
			}
			if (t.onPageChange) {
				t.onPageChange(vId, ctx.curPage);
			}
			calis.report.renderTable('./tplt/table.tplt', t.tableContainer, t.headerArray, arr, t.caption, t.tableClass, t.dic);
		}
		ctx.valueId = vId;
		ejs.renderComplexNode(tplt, container, ctx);
	};
	calis.report.renderTableWithPager = function(obj, tableId, curpage) {
		if (!obj) {
			return;
		}
		var rowCount = obj.obj2DArray.length;
		var t = {
			tableContainer : obj.tableContainer,
			pagerContainer : obj.pagerContainer,
			headerArray : obj.headerArray,
			obj2DArray : obj.obj2DArray,
			caption : obj.caption,
			perPageRowNum : obj.perPageRowNum,
			tableClass : obj.tableClass,
			pagerClass : obj.pagerClass,
			id : !tableId ? obj.tableContainer : tableId,
			printTable : obj.printTable == null ? false : obj.printTable,
			download : obj.download == null ? false : obj.download,
			onPageChange : obj.onPageChange,
			dic : obj.dic
		};
		var curpage = !curpage ? 1 : curpage;
		calis.report.cacheTable(t.id, t);
		calis.report.readerPager('./tplt/page.tplt', t.pagerContainer, rowCount, t.perPageRowNum, curpage, t.pagerClass, t.id, t.printTable, t.download, t.dic);
	};
	calis.report.renderDateLineChart = function(timeScale, data, chartTitle, xName, yName, target) {
		var parsePatterns = {
			'd' : 'yyyyMMdd',
			'h' : 'yyyyMMddHH',
			'm' : 'yyyyMM'
		};
		var formatPatterns = {
			'd' : '%y-%m-%d',
			'h' : '%m-%d:%H',
			'm' : '%y-%m'
		};
		var intervals = Math.ceil(data[0].length / 12);
		var tickIntervals = {
			'd' : intervals + ' days',
			'h' : intervals + ' hours',
			'm' : intervals + ' months'
		};
		// 校对到标准时间
		var detTimes = {
			'd' : -3600000 * 8,
			'h' : 0,
			'm' : -3600000 * 8
		};
		for ( var i = 0; i < data.length; i++) {
			for ( var j = 0; j < data[i].length; j++) {
				var tdate = new Date(0);
				gadgets.i18n.parseDateTime(parsePatterns[timeScale], data[i][j][0] + "", 0, tdate);
				tdate.setTime(tdate.getTime() + detTimes[timeScale]);
				data[i][j][0] = tdate;
			}
		}
		document.getElementById(target).className = "chart";
		var options = {
			title : chartTitle,
			seriesDefaults : {
				showMarker : data[0].length < 40
			},
			series : [ {
				label : '浏&nbsp览&nbsp量&nbsp',
				// color:'#ff0000', lineWidth:1,
				// markerOptions:{style:'square'},
				neighborThreshold : -1
			}, {
				label : '访&nbsp问&nbsp量&nbsp',
				// color:'#00ff00', lineWidth:4,
				// markerOptions:{style:'circle'},
				neighborThreshold : -1
			} ],
			legend : {
				show : true,
				location : 'ne',
				placement : 'insideGrid'
			},
			axesDefaults : {
				useSeriesColor : true
			},
			axes : {
				xaxis : {
					label : xName,
					renderer : $.jqplot.DateAxisRenderer,
					min : data[0][0][0],
					max : data[0][data[0].length - 1][0],
					tickInterval : tickIntervals[timeScale],
					tickOptions : {
						formatString : formatPatterns[timeScale]
					}
				},
				yaxis : {
					label : yName,
					min : 0,
					tickOptions : {
						formatString : '%d'
					}
				}
			},
			cursor : {
				show : true,
				zoom : true,
				// looseZoom:true,
				showTooltip : false,
				constrainZoomTo : 'x'
			},
			highlighter : {
				show : true,
				showLabel : true,
				tooltipAxes : 'y',
				tooltipLocation : 'ne'
			}
		};
		return $.jqplot(target, data, options);
	};

	calis.report._barChartSeriesMap = new java.util.HashMap();
	calis.report.renderBarChart = function(data, chartTitle, xName, yName, target, horizontal) {
		document.getElementById(target).className = "chart";
		/* 假设第一个为最大,且x轴为字符类型，y轴为数字类型 */
		var series = [];
		var sData = [];
		var ticks = [ '' ];
		for ( var i = 0; i < data.length; i++) {
			var s = {
				label : data[i][0]
			};
			series.push(s);
			sData.push([ data[i][1] ]);
		}
		var dMax = data[0];
		var dMin = data[data.length - 1];
		var yMax = 0;
		var yMin = 0;
		if (dMax) {
			yMax = dMax[1];
			yMax = Math.ceil(parseInt(yMax) * 1.05);
		}
		if (dMin) {
			yMin = dMin[1];
			yMin = parseInt(parseInt(yMin) * 0.80);
		}
		var interVal = Math.ceil((yMax - yMin) / 15);
		var options = {
			title : chartTitle,
			seriesDefaults : {
				renderer : $.jqplot.BarRenderer,
				rendererOptions : {
					fillToZero : true,
					barWidth : 24,
					barPadding : 15,
					barDirection : horizontal ? 'horizontal' : 'vertical'
				},
				pointLabels : {
					show : true
				}
			},
			series : series,
			legend : {
				show : true,
				placement : 'insideGrid',
				location : 'ne'
			},
			axes : {
				xaxis : {
					label : xName,
					renderer : $.jqplot.CategoryAxisRenderer,
					ticks : ticks
				},
				yaxis : {
					label : yName,
					min : yMin,
					max : yMax,
					tickInterval : interVal,
					tickOptions : {
						formatString : '%d'
					}
				}
			}
		}
		try {
			$('#' + target).qtip("destroy");
		} catch (e) {
		}
		calis.report._barChartSeriesMap.put(target, series);
		var showTip = false;
		$('#' + target).qtip({
			content : series[0].label,
			position : {
				target : 'mouse',
				adjust : {
					mouse : true
				}
			},
			hide : {
				delay : 0,
				effect : {
					length : 0
				}
			},
			show : {
				delay : 1,
				effect : {
					length : 0
				}
			},
			api : {
				beforeShow : function() {
					return showTip;
				}
			}
		});
		$('#' + target).bind('jqplotDataHighlight', function(ev, seriesIndex, pointIndex, data) {
			var target = ev.currentTarget.getAttribute('id');
			showTip = true;
			var qtip = $('#' + target).qtip('api');
			qtip.hide();
			var content = calis.report._barChartSeriesMap.get(target)[seriesIndex].label;
			qtip.updateContent(content, true);
			qtip.show();
			showTip = false;
		});
		$('#' + target).bind('jqplotDataUnhighlight', function(ev) {
			var target = ev.currentTarget.getAttribute('id');
			var qtip = $('#' + target).qtip('api');
			qtip.hide();
		});
		return $.jqplot(target, sData, options);
	};

	calis.report.renderHorizontalBarChart = function(data, chartTitle, xName, yName, target) {
		document.getElementById(target).className = "chart";
		var dMax = data[0][0];
		var dMin = data[data.length - 1][0];
		var xMax = 0;
		var xMin = 0;
		if (dMax) {
			xMax = Math.ceil(parseInt(dMax) * 1.05);
		}
		if (dMin) {
			xMin = parseInt(parseInt(dMin) * 0.80);
		}
		var interVal = Math.ceil((xMax - xMin) / data.length);
		var series = [];
		var sData = [];
		for ( var i = data.length - 1; i >= 0; i--) {
			var s = {
				label : data[i][1]
			};
			series.push(s);
			sData.push([ [ data[i][0], '' ] ]);
		}
		var options = {
			title : chartTitle,
			seriesDefaults : {
				renderer : $.jqplot.BarRenderer,
				pointLabels : {
					show : true,
					location : 'e',
					edgeTolerance : -15
				},
				shadowAngle : 135,
				rendererOptions : {
					fillToZero : true,
					barWidth : 24,
					barPadding : 15,
					barDirection : 'horizontal'
				}
			},
			series : series,
			legend : {
				show : true,
				placement : 'insideGrid',
				location : 'ne'
			},
			axes : {
				xaxis : {
					label : xName,
					min : xMin,
					max : xMax,
					tickInterval : interVal,
					tickOptions : {
						formatString : '%d'
					}
				},
				yaxis : {
					renderer : $.jqplot.CategoryAxisRenderer,
					label : yName
				}
			}
		};
		try {
			$('#' + target).qtip("destroy");
		} catch (e) {
		}
		var showTip = false;
		calis.report._barChartSeriesMap.put(target, series);
		$('#' + target).qtip({
			content : series[0].label,
			position : {
				target : 'mouse',
				adjust : {
					mouse : true
				}
			},
			hide : {
				delay : 0,
				effect : {
					length : 0
				}
			},
			show : {
				delay : 1,
				effect : {
					length : 0
				}
			},
			api : {
				beforeShow : function() {
					return showTip;
				}
			}
		});
		$('#' + target).bind('jqplotDataHighlight', function(ev, seriesIndex, pointIndex, data) {
			showTip = true;
			var target = ev.currentTarget.getAttribute('id');
			var qtip = $('#' + target).qtip('api');
			qtip.hide();
			var content = calis.report._barChartSeriesMap.get(target)[seriesIndex].label;
			qtip.updateContent(content, true);
			qtip.show();
			showTip = false;
		});
		$('#' + target).bind('jqplotDataUnhighlight', function(ev) {
			var target = ev.currentTarget.getAttribute('id');
			var qtip = $('#' + target).qtip('api');
			qtip.hide();
		});
		return $.jqplot(target, sData, options);
	};
})();