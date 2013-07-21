var reportModel = (function(){

	var _ctx;

	/*
	 * 从后台返回的报告者对象
	 * 程序的所有处理逻辑都依靠该对象
	 * */
	var _reportor;
	/*
	 *	当前选择的站点id 
	 */
	var _siteId = "";
	var _factSiteId = "";
	/*
	 *	机构的过滤条件，可能的情况：
	 * 	1.为空，表示查询全站；
	 * 	2.包含oasc，表示查询共享域；
	 * 	3.包含oaten，表示查询成员馆；
	 * 	4.若_oatenListFilter不为空，则不使用_siteFilter，表示在共享域中过滤出部分成员馆，可以不使用共享域信息
	 * 	重选站点时，该字段重新设置
	 */
	var _siteFilter = "";
	/*
	 *	勾选多个成员馆时将过滤条件保存在该字段中
	 *	重选功能时，该字段清空 
	 */
	var _oatenListFilter = "";
	/*
	 * 当前选择的站点所拥有的所有应用系统
	 * 
	 */
	var _siteApp = null;
	/*
	 * 当前选择的菜单功能的id
	 * */
	var _menuId = "";
	/*
	 * 当前选择的菜单功能的图表数据处理函数和表格数据处理函数
	 */
	var chartProcessor = null;
	var tableProcessor = null;
	var processorMap = {};
	
	var _startDate = "";
	var _endDate = "";
	var _ap = "";
	var _oat = "";
	var _oatFilter = "";
	var _initMainObj = null;
	var _initMainMap = {};
	
	
	var _chartCheckedQueue = new java.util.ArrayList();
	var _tableCheckedQueue = new java.util.ArrayList();
	
	if (!EasyServiceClient) {
		throw "需要EasyService平台远程服务调用JS客户端支持";
	}
	if (!ejs) {
		throw "需要EasyService平台模板支持";
	}
	
	_geneMetrics = function(obj) {
		var metrics = {
			key : obj.val(),
			title : obj.attr("title"),
			filter : obj.attr("filter"),
			name : obj.attr("metricsName"),
			dimensions : obj.attr("dimensions"),
			description : obj.attr("description"),
			limits : obj.attr("limits"),
			tableLimits : obj.attr("tableLimits"),
			chartType : obj.attr("chartType"),
			chartTitle : obj.attr("chartTitle")
		};
		return metrics;
	}
	
	
	var init = {};

	init.getReportor = function() {
		_reportor = provider.reportorService.getReportor();
		siteModule.init();
	};

	init.entryFunc = function() {
		init.getReportor();
	};
	
	var siteModule = {};
	siteModule.init = function() {
		var siteList = _reportor.siteList;
		
		if(siteList.size() > 1) {
			var tplt = provider.getTemplete('./tplt/site/site.htm');
			_ctx = EasyServiceClient.newContext();
			_ctx.siteList = siteList;
			_ctx.siteMap = _reportor.sites;
			ejs.renderComplexNode(tplt, "#topMenu", _ctx);
			
			$("a.site-selector").unbind("click");
			$("a.site-selector").bind("click",function(){
				siteModule.active($(this));
			});
			
			siteModule.childSite.init();
			$("a.site-selector").eq(0).trigger('click');
			
		}
		else {
			_siteId = siteList.get(0);
			var siteInfo = _reportor.sites.get(_siteId);
			_factSiteId = siteInfo.id;
			_siteFilter = siteInfo.filter;
			_siteApp = siteInfo.app;
			menuModule.init();
		}
	};
	siteModule.active = function(instActive) {
		if(instActive.hasClass("active")){
			return;
		}
		$("a.site-selector").removeClass("active");
		instActive.addClass("active");
		
		_siteId = instActive.attr("id");		// 主站点：全站、中心站、共享域、成员馆
		_factSiteId = "";
		var siteObj = $("#" + _siteId + "ChildSite");
		var childSiteObj = $("#childSite").children();
		for(var i = 0; i < childSiteObj.length; i++) {
			$($("#childSite").children()[i]).css("display", "none");
		}
			
		if(siteObj.size() != 0) {
			siteObj.css("display", "block");
			siteModule.childSite.active();
		}
		else {
			_siteFilter = instActive.attr("filter");
			_siteApp = _reportor.sites.get(_siteId).app;
			menuModule.init();
		}
	};
	siteModule.childSite = {};
	siteModule.childSite.init = function() {
		var tplt = provider.getTemplete('./tplt/site/childSite2.htm');
		_ctx = EasyServiceClient.newContext();
		_ctx.userType = _reportor.userType;
		_ctx.saasSites = _reportor.sites.get("saas");
		_ctx.librarySite = _reportor.sites.get("library");
		ejs.renderComplexNode(tplt, "#childSite", _ctx);
		
		siteModule.childSite.action();
		
	};
	siteModule.childSite.action = function() {
		
		$("#saasChildSite a").unbind("click");
		$("#saasChildSite a").bind("click", function() {
			if ($(this).hasClass("active"))
				return;

			_factSiteId = $(this).attr("id");
			_siteFilter = $(this).attr("filter");
			_siteApp = _reportor.sites.get("saas").childSitesMap.get(_factSiteId).app;

			$("#saasChildSite a.active").removeClass("active");
			$(this).addClass("active");
			menuModule.init();
		});

		$(".site-input").unbind("click");
		$(".site-input").bind("focus", function() {
			$(this).css("background-color", "");
			$(".imageInputWrapper").css("border", "solid 1px #000");
		});

		$(".site-input").unbind("blur");
		$(".site-input").bind("blur", function() {
			$(this).css("background-color", "#F9F9F9");
			$(".imageInputWrapper").css("border", "");
		});

		siteModule.childSite.autocomplete();

		$("#libInputButton").unbind("click");
		$("#libInputButton").bind("click",function() {
			var inputAct = $(".site-input");
			var value = inputAct.val();
			if (value == "" || value == undefined) {
				alert("请选择成员馆！");
				return;
			}
			_siteFilter = inputAct.attr("filter");
			_factSiteId = inputAct.attr("id");
			var pid = inputAct.attr("pid");
			_siteApp = _reportor.sites.get("library").childSitesMap
				.get(pid).childSitesMap.get(_factSiteId).app;
			menuModule.init();
		});

		$("#libListButton").bind("click", function() {
			$("#dialog").dialog({
				modal : true,
				height : 600,
				width : 750
			});
			if (_reportor.sites.get("library").childSitesList.indexOf('103190') >= 0) {
				$("#saasListSelect").val("103190");
				$(".103190").css("display", "");
			} else {
				$("." + $("#saasListSelect").val()).css("display", "");
			}
		});

		$("#saasListSelect").bind("change", function() {
			$("#sizechild li").css("display", "none");
			$("." + this.options[this.options.selectedIndex].value).css("display", "");
		});

		$("#dialog ul li a").bind("click", function() {
			$(".site-input").attr("value", $(this).attr("libName"));
			$(".site-input").attr("id", $(this).attr("id"));
			$(".site-input").attr("pid", $(this).attr("pid"));
			$(".site-input").attr("filter", $(this).attr("filter"));
			$("#dialog").dialog("disable");
			$("#dialog").dialog("destroy");
		});

	};
	siteModule.childSite.autocomplete = function() {
		var org = [];
		
		var libSaasList = _reportor.sites.get("library").childSitesList;
		var libSaasMap = _reportor.sites.get("library").childSitesMap;
		for(var i = 0; i < libSaasList.size(); i++) {
			var libraryList = libSaasMap.get(libSaasList.get(i)).childSitesList;
			var libraryMap = libSaasMap.get(libSaasList.get(i)).childSitesMap;
			for(var j = 0; j < libraryList.size(); j++) {
				var librarySite = libraryMap.get(libraryList.get(j));
				var libId = librarySite.id;
				var name = librarySite.name;
				var filter = librarySite.filter;
				var spells = librarySite.spell;

				org.push({
					'zh' : spells.get(0),
					'name' : name,
					'id' : libId,
					'pid' : libSaasList.get(i),
					'filter' : filter
				});
			}
		}
		
		
		
		$(".site-input").autocomplete(org, {
	        max: 12, 					//列表里的条目数 
	        minChars: 0, 				//自动完成激活之前填入的最小字符 
	        width: 400, 				//提示的宽度，溢出隐藏 
	        scrollHeight: 300, 			//提示的高度，溢出显示滚动条 
	        matchContains: true, 		//包含匹配，就是data参数里的数据，是否只要包含文本框里的数据就显示 
	        autoFill: false, 			//自动填充 
	        mustMatch: true,
	        formatItem: function(row, i, max) { 
	        	return row.name; 
	        }, 
	        formatMatch: function(row, i, max) { 
	        	return row.oaten + row.name + row.zh; 
	        }, 
	        formatResult: function(row) {
	        	return row.name; 
	        }
		}).result(function(event, row, formatted){
			if(row == undefined){
				var defaultLibrary = _reportor.sites.get("library").defLibrary;
				var libInfo = defaultLibrary.split('#');
				$(".site-input").attr("id", libInfo[0]);
		        $(".site-input").attr("pid", libInfo[3]);
		        $(".site-input").attr("filter", libInfo[2]);
		        $(".site-input").attr("value", libInfo[1]);
				return;
	        }
	        $(".site-input").attr("id", row.id);
	        $(".site-input").attr("pid", row.pid);
	        $(".site-input").attr("filter", row.filter);
	        $(".site-input").attr("value", row.name);
	    });
	}
	siteModule.childSite.active = function() {
		if(_siteId == "saas") {
			$("#saasChildSite a.active").removeClass("active");
			$("#saasChildSite a").eq(0).trigger('click');
		}
		else if(_siteId == "library") {
			var defaultLibrary = _reportor.sites.get("library").defLibrary;
			var libInfo = defaultLibrary.split('#');
			$(".site-input").attr("id", libInfo[0]);
	        $(".site-input").attr("pid", libInfo[3]);
	        $(".site-input").attr("filter", libInfo[2]);
	        $(".site-input").attr("value", libInfo[1]);
	        
			$("#libInputButton").eq(0).trigger('click');
		}
		
	};

	
	
	var menuModule = {};
	menuModule.init = function() {
		var menu = _reportor.menus.get(_siteId);
		
		var tplt = provider.getTemplete('./tplt/aside/menu2.htm');
		_ctx = EasyServiceClient.newContext();
		_ctx.menu = menu;
		_ctx.menuMap = _reportor.menuMap;
		_ctx.siteApp = _siteApp;
		ejs.renderComplexNode(tplt, '#aside', _ctx);
		
		menuModule.asideMenuObj = $("a.asideMenu");
		menuModule.twoMenuObj = $("a.twoMenu");
		menuModule.threeMenuObj = $("a.threeMenu");
		
		menuModule.asideMenuObj.unbind("click");
		menuModule.asideMenuObj.bind("click", function(){
			$(".threeMenuUL").css("display", "none");
			if(menuModule.twoMenuObj.attr("value") == 'dk'){
				menuModule.twoMenuObj.attr("value", "gb");
				$("a.twoMenu").css("background-position", "13px -2611px");
			}
			menuModule.active($(this));
		});
		menuModule.twoMenuObj.unbind("click");
		menuModule.twoMenuObj.bind("click", function(){
			if(menuModule.twoMenuObj.attr("value") == 'gb'){
				$(".threeMenuUL").css("display", "");
				menuModule.twoMenuObj.attr("value", "dk");
				$("a.twoMenu").css("background-position", "13px -2571px");
			}else{
				$(".threeMenuUL").css("display", "none");
				menuModule.twoMenuObj.attr("value", "gb");
				$("a.twoMenu").css("background-position", "13px -2611px");
			}
			
		});
		menuModule.threeMenuObj.unbind("click");
		menuModule.threeMenuObj.bind("click", function(){
			menuModule.active($(this));
		});
		
		// 菜单初始化完成后，激活第一个菜单
		$("div#aside a:not(.open)").eq(0).trigger('click');
	};
	menuModule.active = function(instActiver) {	//激活菜单选项
		_menuId = instActiver.attr("id");
		
		menuModule.asideMenuObj.removeClass("active");
		menuModule.threeMenuObj.removeClass("active");
		instActiver.addClass("active");
		
		var menu = _reportor.menuMap.get(_menuId);
		var appList = null;
		if(menu.general == "true") {
			appList = _siteApp.toArray();
		}
		else {
			appList = _siteApp.intersection(menu.app);
		}
		
		if(appList.length > 1 && appList.indexOf("all") == -1)
			appList.push("all");
		
	//	var tplt = provider.getTemplete('./tplt/' + menu.loadPage);
		var tplt = provider.getTemplete('./tplt/mainReport.htm');
		_ctx = EasyServiceClient.newContext();
		_ctx.title = menu.name;
		_ctx.appList = appList;
		_ctx.apps = _reportor.apps;
		_ctx.metricName = menu.metricName;
		ejs.renderComplexNode(tplt, '#main', _ctx);
		
		menuModule.mainPage.init(menu);
		
		chartProcessor && chartProcessor();
		tableProcessor && tableProcessor();
	};
	menuModule.mainPage = {};
	menuModule.mainPage.init = function(menuInfo) {
		dateModule.able = menuInfo.date;
		dateApModule.able = menuInfo.timeAp;
		appModule.defApp = menuInfo.defaultOat;
		chartMetricsModule.able = menuInfo.chartShow;
		chartMetricsModule.checkedQueueSize = parseInt(menuInfo.chartSize, 10);
		tableMetricsModule.able = menuInfo.tableShow;
		tableMetricsModule.checkedQueueSize = parseInt(menuInfo.tableSize, 10);
		systemEnv.able = "false";
		
		chartProcessor = processorMap[menuInfo.chartProcessor];
		tableProcessor = processorMap[menuInfo.tableProcessor];
		
		_initMainObj = _initMainMap[menuInfo.id];
		if(_initMainObj != null && _initMainObj != undefined) {
			_initMainObj.able = "true";
		}
		
		systemEnv.init();
		dateModule.init();
		dateApModule.init();
		appModule.init();
		chartMetricsModule.init();
		tableMetricsModule.init();
	};
	
	var dateModule = {};
	dateModule.able = "true";
	dateModule.dateObj = null;
	dateModule.yesterdayObj = null;
	dateModule.lastMonthObj = null;
	dateModule.dateFromToObj = null;
	dateModule.datepicker = null;
	dateModule.init = function() {	//初始化时间范围
		dateModule.dateObj = $("#dateDiv");
		dateModule.yesterdayObj = $("#bsnMdelYesterday");
		dateModule.lastMonthObj = $("#bsnMdelLastMonth");
		dateModule.dateFromToObj = $("#date-range-field span");
		
		if(dateModule.able == "false") {
			dateModule.dateObj.css("display", "none");
			return ;
		}
			
		
		dateModule.action();
		
		_startDate = Date.getYesterdayDate();
		_endDate = Date.getYesterdayDate();
		dateModule.yesterdayObj.addClass("active");
		dateModule.lastMonthObj.removeClass("active");
		dateModule.dateFromTo.showDateTime();
	};
	dateModule.action = function() {
		dateModule.yesterdayObj.unbind("click");
		dateModule.yesterdayObj.bind("click", function(){
			_startDate = Date.getYesterdayDate();
			_endDate = Date.getYesterdayDate();
			dateModule.yesterdayObj.addClass("active");
			dateModule.lastMonthObj.removeClass("active");
			
			dateModule.dateFromTo.showDateTime();
			
			dateApModule.reset();
			
			chartProcessor && chartProcessor();
			tableProcessor && tableProcessor();
		});
		
		dateModule.lastMonthObj.unbind("click");
		dateModule.lastMonthObj.bind("click", function(){
			_startDate = Date.getLastMonthStartDate();
			_endDate = Date.getLastMonthEndDate();
			dateModule.yesterdayObj.removeClass("active");
			dateModule.lastMonthObj.addClass("active");
			
			dateModule.dateFromTo.showDateTime();
			
			dateApModule.reset();
			chartProcessor && chartProcessor();
			tableProcessor && tableProcessor();
		});
		
		dateModule.dateFromTo.action();
	};

	dateModule.dateFromTo = {};		// 日期选择控件
	dateModule.dateFromTo.action = function() { // 初始化日期选择控件
		_checkExternalClick = function(event) {
			var $target = $(event.target);
			if ($target.closest("#DateSelect").length == 0) {
				$("#datepicker-calendar").css("display", "none");
				var date = new Array();
				date.push( Date.stringToDate(_startDate));
				date.push( Date.stringToDate(_endDate));
				$('#datepicker-calendar').DatePickerSetDate(date, true);
			}
		}
		$(document).mousedown(_checkExternalClick);

		var to = new Date();
		to.setDate(to.getDate() - 1);
		var from = new Date();
		from.setFullYear(from.getFullYear() - 1);
		_datepicker = null;
		_datepicker = $('#datepicker-calendar').DatePicker(
				{
					inline : true,
					date : [to, to],
					calendars : 3,
					mode : 'range',
					current : new Date(to.getFullYear(), to.getMonth() - 1, to.getDate()),
					starts : 0,
					locale: {
				          daysMin: ["日", "一", "二", "三", "四", "五", "六"],
				          months: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
				          monthsShort: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
				        },
					onChange : function(dates, el) {
						if(dates == "") {
							_startDate = Date.getYesterdayDate();
							_endDate = Date.getYesterdayDate();
						}
						else {
							_startDate = new Date(dates[0]).getFullYear() + '-'
								+ (new Date(dates[0]).getMonth() + 1) + '-'
								+ new Date(dates[0]).getDate();
							_endDate = new Date(dates[1]).getFullYear() + '-'
								+ (new Date(dates[1]).getMonth() + 1) + '-'
								+ new Date(dates[1]).getDate();
						}
						dateModule.dateFromTo.showDateTime();
						
						dateApModule.reset();
						chartProcessor && chartProcessor();
						tableProcessor && tableProcessor();
					},

				});


		$("#timepickerConfirm").bind("click", function(){		// 确定按钮
			$('#datepicker-calendar').hide();
			dateModule.yesterdayObj.removeClass("active");
			dateModule.lastMonthObj.removeClass("active");
			$('#datepicker-calendar').DatePickerSetConfirmed();
		});
		
		$("#timepickerCancel").bind("click", function(){		// 取消按钮
			$('#datepicker-calendar').hide();
			$('#datepicker-calendar').DatePickerClear();
			var date = new Array();
			date.push( Date.stringToDate(_startDate));
			date.push( Date.stringToDate(_endDate));
			$('#datepicker-calendar').DatePickerSetDate(date, true);
		});
		
		$('#datepicker-calendar').hide();
		$('#date-range-field').bind('click', function() {
			$('#datepicker-calendar').toggle();
			return false;
		});
	};
	dateModule.dateFromTo.showDateTime = function() {
		dateModule.dateFromToObj.text(_startDate + "\n\b\n\b\n\b\n\b" + "至" + "\n\b\n\b\n\b\n\b" +  _endDate);
		var date = new Array();
		date.push( Date.stringToDate(_startDate));
		date.push( Date.stringToDate(_endDate));
		$('#datepicker-calendar').DatePickerSetDate(date, true);
	};
	
	dateApModule = {};
	dateApModule.able = "true";
	dateApModule.timeSpanObj = null;
	dateApModule.byHourObj = null;
	dateApModule.byDayObj = null;
	dateApModule.byMonthObj = null;
	dateApModule.init = function() {
		dateApModule.timeSpanObj = $("#TimeSpan");
		if(dateApModule.able == "true") {	
			dateApModule.timeSpanObj.css("display", "block");
			dateApModule.byHourObj = $("#ByHour");
			dateApModule.byDayObj = $("#ByDay");
			dateApModule.byMonthObj = $("#ByMonth");
			
			dateApModule.action();

			
			_ap = "toph";
			dateApModule.byHourObj.addClass("active");
			dateApModule.byDayObj.removeClass("active");
			dateApModule.byMonthObj.removeClass("active");
			if (Date.containsMonth(_startDate, _endDate))
				dateApModule.byMonthObj.removeClass("disable");
			else
				dateApModule.byMonthObj.addClass("disable");	
		}
		else {
			dateApModule.timeSpanObj.css("display", "none");
			dateApModule.timeSpanObj = null;
			_ap = "";
			dateApModule.byHourObj = null;
			dateApModule.byDayObj = null;
			dateApModule.byMonthObj = null;
		}
	};
	dateApModule.reset = function() {
		if(dateApModule.able == "true") {
			_ap = "toph";
			dateApModule.byHourObj.addClass("active");
			dateApModule.byDayObj.removeClass("active");
			dateApModule.byMonthObj.removeClass("active");
			if (Date.containsMonth(_startDate, _endDate))
				dateApModule.byMonthObj.removeClass("disable");
			else
				dateApModule.byMonthObj.addClass("disable");
		}
	}
	dateApModule.action = function() {
		dateApModule.byHourObj.unbind("click");
		dateApModule.byHourObj.bind("click", function(){
			if($(this).hasClass("active")) {
				return ;
			}
			_ap = "toph";
			dateApModule.byHourObj.addClass("active");
			dateApModule.byDayObj.removeClass("active");
			dateApModule.byMonthObj.removeClass("active");
			
			chartProcessor && chartProcessor();
			tableProcessor && tableProcessor();
		});
		
		dateApModule.byDayObj.unbind("click");
		dateApModule.byDayObj.bind("click", function(){
			if($(this).hasClass("active") || $(this).hasClass("disable")) {
				return ;
			}
			_ap = "topd";
			dateApModule.byHourObj.removeClass("active");
			dateApModule.byDayObj.addClass("active");
			dateApModule.byMonthObj.removeClass("active");
			
			chartProcessor && chartProcessor();
			tableProcessor && tableProcessor();
		});
			
		dateApModule.byMonthObj.unbind("click");
		dateApModule.byMonthObj.bind("click", function(){
			if($(this).hasClass("active") || $(this).hasClass("disable")) {
				return ;
			}
			_ap = "topm";
			dateApModule.byHourObj.removeClass("active");
			dateApModule.byDayObj.removeClass("active");
			dateApModule.byMonthObj.addClass("active");
			
			chartProcessor && chartProcessor();
			tableProcessor && tableProcessor();
		});	
		
	};
	
	var appModule = {};
	appModule.able = "true";
	appModule.defApp = "";
	appModule.init = function() {
		appModule.action();
		
		var oatlist = $(".dropdown li a");
		oatlist.each(function(){
			var v = $(this);
			if(v.attr("id") == appModule.defApp) {
				$("#apptext").html(v.html());
				_oat = v.attr("id");
				_oatFilter = v.attr("filter");
			}
		});
		if($("#apptext").html() == "") {
			var temp = $(".dropdown li a:first");
			$("#apptext").html(temp.html());
			_oat = temp.attr("id");
			_oatFilter = temp.attr("filter");
		}
	};
	appModule.action = function() {
		$(".dropdown li a").hover(function() {
			$(this).addClass("ui-state-focus");
		}, function() {
			$(this).removeClass("ui-state-focus");
		});
		
		$(".dropdown dt").unbind("click")
		$(".dropdown dt").bind("click",function(){
			if($("#applistdd").css("display") == "block") {
				$("#applistdd").css("display", "none");
			}
			else {
				$("#applistdd").css("display", "block");
			}
		});
		
		$(".dropdown li a").unbind("click")
		$(".dropdown li a").bind("click",function(){
			$("#apptext").html($(this).html());
			_oat = $(this).attr("id");
			_oatFilter = $(this).attr("filter");
			
			$("#applistdd").css("display", "none");
			
			chartProcessor && chartProcessor();
			tableProcessor && tableProcessor();
		});
		
		/* Close if clicked elsewhere. */
		_checkExternalClick = function(event) {

			var $target = $(event.target);
			if ($target.closest("#applist").length == 0) {
				$("#applistdd").css("display", "none");
			}
		}
		$(document).mousedown(_checkExternalClick);
	};
	
	var systemEnv = {};
	systemEnv.selItem = "";			// 选择的系统环境选项
	systemEnv.able = "false";
	systemEnv.systemEnvObj = null;
	systemEnv.init = function() {	// 初始化 系统环境选择对象
		
		systemEnv.systemEnvObj = $("#systemEnvPanel");
		if(systemEnv.able == "true") {	//显示
			systemEnv.systemEnvObj.css("display", "block");
		}
		else {	//不显示
			systemEnv.systemEnvObj.css("display", "none");
			return ;
		}
		
		$("input:radio[name='systemenv']").unbind("click");
		$("input:radio[name='systemenv']").bind("click", function(){
			systemEnv.active($(this));
		});
		
		var v = $("input:radio[name='systemenv']:checked");
		if(v) {
			systemEnv.selItem = v.attr("id");
		}
		else {
			systemEnv.selItem = "";
		}
	}
	systemEnv.active = function(instActive) {	// 激活 系统环境选择对象
		if(instActive.attr("id") != systemEnv.selItem) {
			systemEnv.selItem = instActive.attr("id");
			
			chartMetricsModule.init();
			tableMetricsModule.init();

			chartProcessor && chartProcessor();
			tableProcessor && tableProcessor();
		}
	};
	systemEnv.getValue = function() {
		return systemEnv.selItem;
	};
	_initMainMap["systemenv"] = systemEnv;
	
	var chartMetricsModule = {};
	chartMetricsModule.able = "true";
	chartMetricsModule.checkedQueueSize = 1;		// 当前的指标选择最大数目
	chartMetricsModule.defaultQueueSize = 1;		// 默认的指标选择最大数目，点击功能后后，该值被设置，不会被更改，使用该变量是为加入时间对比使用，使用时间对比时，只能选择单指标
	chartMetricsModule.chartMetricsIndicatorObj = null;
	chartMetricsModule.chartMetricsOptionObj = null;
	chartMetricsModule.chartMetricsObj = null;
	chartMetricsModule.init = function() {		// 初始化 指标选择模块
		if(chartMetricsModule.able == "false") {		// 是否显示指标选择模块
			$("#chartMetricsIndicator").css("display", "none");
			$("#chart").css("display", "none");
			return ;
		}
		else {
			$("#chartMetricsIndicator").css("display", "true");
			$("#chart").css("display", "true");
		}
		
		chartMetricsModule.defaultQueueSize = chartMetricsModule.checkedQueueSize
		_chartCheckedQueue.clear();
		
		var tplt = provider.getTemplete('./tplt/' + _menuId + '/metrics.htm');
		_ctx = EasyServiceClient.newContext();
		_ctx.oat = _oat;
		if(_initMainObj != null && _initMainObj != undefined)
			_ctx.initVal = _initMainObj.getValue();
		_ctx.metricsItemName = "chartMetrics";
		ejs.renderComplexNode(tplt, '#chartMetricsOptionIndicator', _ctx);
		
		chartMetricsModule.chartMetricsIndicatorObj = $("#chartMetricsIndicatorText");
		chartMetricsModule.chartMetricsOptionObj = $("#chartMetricsOptionContainer2");
		chartMetricsModule.chartMetricsObj = $('input:checkbox[name="chartMetrics"]');
		
		
		var checkedItem = $('input:checkbox[name="chartMetrics"]:checked');
		checkedItem.each(function() {
			var item = _geneMetrics($(this));
			_chartCheckedQueue.add(item);
		});
		
		chartMetricsModule.action();
		chartMetricsModule.refresh();
		
		
		/* Close if clicked elsewhere. */
		_checkExternalClick = function(event) {

			var $target = $(event.target);
			if ($target.closest("#chartMetricsOptionContainer").length == 1) {
				return;
			}
			if ($target.closest("#chartMetricsIndicatorWrapper").length == 0) {
				chartMetricsModule.chartMetricsOptionObj.css("display", "none");
			}
		};
		$(document).mousedown(_checkExternalClick);
	};
	chartMetricsModule.action = function() {
		chartMetricsModule.chartMetricsObj.unbind("click");
		chartMetricsModule.chartMetricsObj.bind("click", function(){	// 指标点击事件
			var instActive = $(this);
			if (instActive[0].checked == false) {// 取消选中
				if (_chartCheckedQueue.size() == 1) {
					instActive[0].checked = true;
					return;
				} else {
					for ( var i = 0; i < _chartCheckedQueue.size(); i++) {
						if (instActive[0].value == _chartCheckedQueue.get(i).key) {
							_chartCheckedQueue.remove(i);
						}
					}
				}
			} else {	// 选中
				_chartCheckedQueue.add(_geneMetrics(instActive));
				if (_chartCheckedQueue.size() > chartMetricsModule.checkedQueueSize) {		// 当前选中的数目超过最大可选择数目
					$('input:checkbox[name="chartMetrics"]:checked').each(function() {
						if ($(this).val() == _chartCheckedQueue.get(0).key) {
							$(this)[0].checked = false;
							_chartCheckedQueue.remove(0);
							return false;
						}
					});
				}
			}
			chartMetricsModule.refresh();
			
			chartProcessor && chartProcessor();
			if(tableMetricsModule.able != "false" && tableMetricsModule.checkedQueueSize == 0)
				tableProcessor && tableProcessor();
		});
		
		chartMetricsModule.chartMetricsIndicatorObj.unbind("click")
		chartMetricsModule.chartMetricsIndicatorObj.bind("click", function(){
			var vis = chartMetricsModule.chartMetricsOptionObj.css("display");
			if (vis == "none")
				chartMetricsModule.chartMetricsOptionObj.css("display", "block");
			else
				chartMetricsModule.chartMetricsOptionObj.css("display", "none");
		});
		
	};
	chartMetricsModule.refresh = function() {
		var showStr = "", i;
		for (i = 0; i < _chartCheckedQueue.size(); i++) {
			if (i > 0) {
				showStr += "、";
			}
			showStr += _chartCheckedQueue.get(i).title;
		}
		chartMetricsModule.chartMetricsIndicatorObj.html(showStr);
	};
	
	var tableMetricsModule = {};
	tableMetricsModule.able = "true";
	tableMetricsModule.useChartMetric = false;
	tableMetricsModule.dataTableMetricsOptionObj;
	tableMetricsModule.dataTableMetricsObj;
	tableMetricsModule.dataTablePanelObj;			// 表格指标面板对象
	tableMetricsModule.dataTableObj;				// 表格对象
	tableMetricsModule.checkedQueueSize = -1;
	tableMetricsModule.init = function() {
		tableMetricsModule.dataTableObj = $("#datetables");
		tableMetricsModule.dataTablePanelObj = $("#advfilterBar");
		
		if(tableMetricsModule.able == "false") {
			tableMetricsModule.dataTableObj.css("display", "none");
			tableMetricsModule.dataTablePanelObj.css("display", "none");
			return ;
		}
		else {
			tableMetricsModule.dataTableObj.css("display", "block");
			tableMetricsModule.dataTablePanelObj.css("display", "block");
		}

		if(tableMetricsModule.checkedQueueSize == 0) {
			tableMetricsModule.useChartMetric = true;
			tableMetricsModule.dataTablePanelObj.css("display", "none");
			return ;
		}
		else {
			tableMetricsModule.useChartMetric = false;
			tableMetricsModule.dataTablePanelObj.css("display", "block");
		}
		
		_tableCheckedQueue.clear();
		
		var tplt = provider.getTemplete('./tplt/' + _menuId + '/metrics.htm');
		_ctx = EasyServiceClient.newContext();
		_ctx.oat = _oat;
		if(_initMainObj != null && _initMainObj != undefined)
			_ctx.initVal = _initMainObj.getValue();
		_ctx.metricsItemName = "customMetrics";
		ejs.renderComplexNode(tplt, '#customMetricsOptionIndicator', _ctx);
		
		tableMetricsModule.dataTableMetricsOptionObj = $("#customMetricsOptionIndicator");
		tableMetricsModule.dataTableMetricsObj = $('input:checkbox[name="customMetrics"]');
		
		tableMetricsModule.action();

		var checkedItem = $('input:checkbox[name="customMetrics"]:checked');
		checkedItem.each(function() {
			var item = _geneMetrics($(this));
			_tableCheckedQueue.add(item);
			
		});
	};
	tableMetricsModule.action = function() {
		$("#filterTabs").easytabs({
			collapsible : true,
	      	collapsedByDefault : true,
	      	tabActiveClass : 'selected'
		});	
		
		tableMetricsModule.dataTableMetricsObj.unbind("click");
		tableMetricsModule.dataTableMetricsObj.bind("click", function() {
			var instActive = $(this);
			if (instActive[0].checked == false) {// 取消选中
				if (_tableCheckedQueue.size() == 1) {
					instActive[0].checked = true;
					return;
				} else {
					for ( var i = 0; i < _tableCheckedQueue.size(); i++) {
						if (instActive[0].value == _tableCheckedQueue.get(i).key) {
							_tableCheckedQueue.remove(i);
						}
					}
				}
			} else {// 选中
				_tableCheckedQueue.add(_geneMetrics($(instActive)));
			}
		});
		
		$("#btn1").unbind("click");
		$("#btn1").bind("click", function() {
			tableProcessor && tableProcessor();
		});
	};
	
	var processorModule = {};
	processorModule.commonChart = function() {
		if (chartMetricsModule.able == "false") {
			return;
		}
		
		chartCTX.addLoading("chart");
		var timeScale = "d";

		var startDateFormat = Date.stringToDate(_startDate);
		var endDateFormat = Date.stringToDate(_endDate);
		/**
		 * 查询成员馆具体的filter
		 */

		var chartType = "";

		if (_ap == "toph")
			timeScale = "h";

		var reqlist = new java.util.ArrayList();
		if (_chartCheckedQueue.size() == 0) {
			alert("图表指标为空,图表统计失败");
			return;
		}
		for ( var i = 0; i < _chartCheckedQueue.size(); i++) {
			var metrics = _chartCheckedQueue.get(i);
			var req = provider.newDataExportRequest();
			req.id = metrics.title;
			req.startDate = startDateFormat;
			req.endDate = endDateFormat;

			var dimensionsTemp = metrics.dimensions;
			if (dimensionsTemp) {
				var dimensions = dimensionsTemp.split(",");
				for ( var j = 0; j < dimensions.length; j++) {
					req.dimensions.add(dimensions[j]);
					req.groups.add(dimensions[j]);
				}
			}

			/** time scale* */
			req.timeScale = timeScale;

			/** select column 指标数据列* */
			req.metrics.add(metrics.name);

			/** sql where * */
			req.filter = metrics.filter;
			/**操作结果集x轴名称* */
			req.chartTitle = metrics.chartTitle;
			if (_siteFilter != "") {
				if (req.filter != null && req.filter != '')
					req.filter += " AND ";
				req.filter = req.filter + _siteFilter;
			}
			if (_oatFilter != "") {
				if (req.filter != null && req.filter != '')
					req.filter += " AND ";
				req.filter = req.filter + _oatFilter;
				req.matrixKeys.add("oat");
			}

			if (req.filter.indexOf("op=") != -1
					|| req.filter.indexOf("op<>") != -1
					|| req.filter.indexOf("op in") != -1)
				req.matrixKeys.add("op");
			if (req.filter.indexOf("ort") != -1
					|| req.filter.indexOf("ort<>") != -1
					|| req.filter.indexOf("ort in") != -1)
				req.matrixKeys.add("ort");
			if (req.filter.indexOf("oaten") != -1
					|| req.filter.indexOf("oaten<>") != -1
					|| req.filter.indexOf("oaten in") != -1)
				req.matrixKeys.add("oaten");
			if (req.filter.indexOf("oasc=") != -1
					|| req.filter.indexOf("oasc<>") != -1
					|| req.filter.indexOf("oasc in") != -1
					|| req.filter.indexOf("oasc!=") != -1)
				req.matrixKeys.add("oasc");

			if (_ap == "topm") {
				req.groups.add("topy");
				req.groups.add("topm");
			} else if (_ap == "topd") {
				req.groups.add("topy");
				req.groups.add("topm");
				req.groups.add("topd");
			} else if (_ap == "toph") {
				req.groups.add("toph");
			}

			/** maxResult* */
			var limits = metrics.limits;
			if (limits != null && limits != "") {
				var limitInt = parseInt(limits);
				req.maxResults = limitInt;
			} else {
				req.maxResults = 10000;
			}

			chartType = metrics.chartType;
			reqlist.add(req);
		}
		

		if (chartType == "line") {
			/**
			 *处理曲线图情况
			 */
			var dataBean = provider.chartReportService.getLineChart(reqlist);
			ChartReportModule.renderLineChart(_ap,dataBean);

		} else if (chartType == "pie") {
			/**
			 *处理饼状图情况
			 */
			var dataBean = provider.chartReportService.getPieChart(reqlist);
			if (dataBean.series.length == 1) {
				ChartReportModule.renderPieChart(dataBean);
			} else if (dataBean.series.length == 2) {

				ChartReportModule.renderDetPieChart(dataBean);

			}

		} else if (chartType == "Bar") {
			/**
			 *处理柱状图情况
			 */
			var dataBean = provider.chartReportService.getBarChart(reqlist);
			ChartReportModule.renderBarChart(dataBean);

		}
	};
	processorModule.realtimeChart = function() {
		var oatenOascID;
		if(_factSiteId == ''){
			oatenOascID = 'all';
		}else{
			oatenOascID = _factSiteId;
		}
		if (chartMetricsModule.able == "false") {
			return;
		}
		var index = _chartCheckedQueue.get(0).name;
		var title = _chartCheckedQueue.get(0).title;
		if (index == 'pageView') {
			var PVSeriesData = provider.getPVSeriesData(oatenOascID, _oat);
			ChartReportModule.renderDynamicChart(oatenOascID, index, PVSeriesData, _oat,title);//index 指标
		} else {
			var VSeriesData = provider.getVSeriesData(oatenOascID,_oat);
			ChartReportModule.renderDynamicChart(oatenOascID, index, VSeriesData, _oat,title);
		}
	};
	processorModule.todayChart = function() {
		var oatenOascID;
		if(_factSiteId == ''){
			oatenOascID = 'all';
		}else{
			oatenOascID = _factSiteId;
		}
		if (chartMetricsModule.able == "false") {
			return;
		}
		var index = _chartCheckedQueue.get(0).name;
		var title = _chartCheckedQueue.get(0).title;
		if (index == 'pageView') {
			var PVTodayData = provider.getPVTodayData(oatenOascID,_oat);
			ChartReportModule.renderTodayChart(oatenOascID, index,PVTodayData,_oat,title);
		} else {
			var VTodayData = provider.getVTodayData(oatenOascID,_oat);
			ChartReportModule.renderTodayChart(oatenOascID, index,VTodayData,_oat,title);
		}
	};
	processorModule.commonTable = function() {

		if(tableMetricsModule.able == "false")
			return ;
		
		    dataTableCTX.setTarget("datetables");
		    dataTableCTX.addLoading("datetables");
			var timeScale = "d";
			var startDateFormat = Date.stringToDate(_startDate);
			var endDateFormat = Date.stringToDate(_endDate);
			var customMetrics;
			if(tableMetricsModule.checkedQueueSize == 0) {
				customMetrics = _chartCheckedQueue;
			}
			else {
				customMetrics = _tableCheckedQueue;
			}
			/**
			 * 查询成员馆具体的filter
			 */
			if(_ap == "toph")
				timeScale = "h";
			var reqlist = new java.util.ArrayList();
			if(customMetrics.size() == 0){
				alert("dataTables指标为空,表格展示失败");
				return;
			}
			for(var i=0; i<customMetrics.size(); i++) {
				var metrics = customMetrics.get(i);
				var req = provider.newDataExportRequest();
				/**id设置为指标名称**/
				req.id = metrics.title;
				req.startDate = startDateFormat;
				req.endDate = endDateFormat;
				
				var dimensionsTemp = metrics.dimensions;
				if(dimensionsTemp != null && dimensionsTemp != "") {
					var dimensions = dimensionsTemp.split(",");
					for(var j=0; j<dimensions.length; j++) {
						req.dimensions.add(dimensions[j]);
						req.groups.add(dimensions[j]);
					}
				}
				/**time scale**/
				req.timeScale = timeScale;
				
				/**select column**/
				req.metrics.add(metrics.name);
				/**sql where **/
				req.filter = metrics.filter;
				/**除指标以外的列名描述**/
				req.description = metrics.description;
				if(_siteFilter != "") {
					if(req.filter != null && req.filter != '')
						req.filter += " AND ";
					req.filter = req.filter + _siteFilter;
				}
				if(_oatFilter != "") {
					if(req.filter != null && req.filter != '')
						req.filter += " AND ";
					req.filter = req.filter + _oatFilter;
					req.matrixKeys.add("oat");
				}
				if(req.filter.indexOf("op=") != -1 || req.filter.indexOf("op<>") != -1 || req.filter.indexOf("op in") != -1)
					req.matrixKeys.add("op");
				if(req.filter.indexOf("ort") != -1 || req.filter.indexOf("ort<>") != -1 || req.filter.indexOf("ort in") != -1)
					req.matrixKeys.add("ort");
				if(req.filter.indexOf("oaten") != -1 || req.filter.indexOf("oaten<>") != -1 || req.filter.indexOf("oaten in") != -1)
					req.matrixKeys.add("oaten");
				if(req.filter.indexOf("oasc=") != -1 || req.filter.indexOf("oasc<>") != -1 || req.filter.indexOf("oasc in") != -1 || req.filter.indexOf("oasc!=") != -1)
					req.matrixKeys.add("oasc");
				
				
				if(_ap == "topm") {
					req.groups.add("topy");
					req.groups.add("topm");
				}
				else if(_ap == "topd") {
					req.groups.add("topy");
					req.groups.add("topm");
					req.groups.add("topd");
				}
				else if(_ap == "toph") {
					req.groups.add("toph");

				}
				/**maxResult**/
				var limits = metrics.tableLimits;
				if(limits != "" && limits != null) {
					var limitInt = parseInt(limits);
					req.maxResults = limitInt;
				}
				else {
					req.maxResults = 10000;
				}
				reqlist.add(req);
			}
			/**
			  *展示dataTables时候,需要知道从哪列开始统计列
			  *数组长度-size是需要开始的统计列
		     */
			var size = customMetrics.size();
			if(dateApModule.able == "true"){
				/**
				  *维度为时间,调用dataTableCTX.renderTotalDataTable1函数,展示日期的dataTable.
			     */
				var dataTableBean = provider.dataTableService.getColmunsTimeData(reqlist);
				dataTableReportModule.renderDataTables(dataTableBean,size);
				return;
			}else{
				 var dataTableBean = provider.dataTableService.getColmunsMetricsData(reqlist);
				 dataTableReportModule.renderDataTables(dataTableBean,size);
				 return;
			}
			
			
	};
	processorMap["commonchart"] = processorModule.commonChart;
	processorMap["realtimechart"] = processorModule.realtimeChart;
	processorMap["todaychart"] = processorModule.todayChart;
	processorMap["commontable"] = processorModule.commonTable;

	return {
		initReport : function(obj) {
			init.entryFunc();
		}, 
		getOatFilter : function() {
			return _oatFilter;
		},
		getStartDate : function(){
			return _startDate;
		},
		getEndDate : function(){
			return _endDate;
		},
		getChartCheckedQueue : function(){
			return _chartCheckedQueue;
		},
		getTableCheckedQueue : function(){
			return _tableCheckedQueue;
		},
		getAp : function(){
			return _ap;
		},
		getSiteFilter : function(){
			return _siteFilter;
		},
		getSite : function(){
			return _siteId;
		}
	}
})();




$(document).ready(function(){
	if(window.location.href.indexOf('#') > 0){
		window.location.href = window.location.href.substring(0,window.location.href.indexOf('#'));
		return;
	}
	if (! (jQuery.browser.mozilla && jQuery.browser.version.split('.')[0] >= 4)) {
		$().toastmessage('showToast', {
            text: "为避免页面响应缓慢，建议您使用firefox浏览器4.0以后的版本！请点击<a style='color:lightgreen;' href='http://firefox.com.cn/download/'>此处</a>下载",
            sticky: true,
            position: 'top-center',
            type: 'warning',
            closeText: ''
        });

	}
	
	initUserInfo();
});