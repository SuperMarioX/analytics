if (!EasyServiceClient) {
	throw "需要EasyService平台远程服务调用JS客户端支持";
}
if (!ejs) {
	throw "需要EasyService平台模板支持";
}

var reportService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.reportservice.ICAReportService");
var chartReportService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.reportservice.IChartService");
var dataTableService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.reportservice.IDataTableService");
var realTimeService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.logservice.IRealTimeDataQueryService");
var reportorService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.reportservice.IReportorService");

/**扩展日期对象，支持日期的格式化、支持获取本月、上月的开始日期、结束日期 **/

//格式化日期,默认格式为"yyyyMMdd"
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
		fmt = 'yyyy-MM-dd';
	}
	if (/(y+)/.test(fmt))
		fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
	for ( var k in o)
		if (new RegExp("(" + k + ")").test(fmt))
			fmt = fmt.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k] : ("00" + o[k]).substr(("" + o[k]).length));
	return fmt;
};

//获得某月的天数   
Date.getMonthDays = function (myYear,myMonth){   
  var monthStartDate = new Date(myYear, myMonth, 1);    
  var monthEndDate = new Date(myYear, myMonth+1, 1);    
  var   days   =   (monthEndDate   -   monthStartDate)/(1000   *   60   *   60   *   24);    
  return   days;    
};

//获得本月的开始日期   
Date.getMonthStartDate = function (){
	var d = new Date();
  var monthStartDate = new Date(d.getFullYear(), d.getMonth(), 1);    
  return monthStartDate.format();   
};

//获得本月的结束日期   
Date.getMonthEndDate = function (){
	var d = new Date();
  var monthEndDate = new Date(d.getFullYear(), d.getMonth(), Date.getMonthDays(d.getFullYear(),d.getMonth()));    
  return monthEndDate.format();
};  

//获得上月开始时间
Date.getLastMonthStartDate = function (){
	var d = new Date();
	var lastMonthStartDate = new Date(d.getFullYear(), d.getMonth()-1, 1);
	return lastMonthStartDate.format(); 
};

//获得上月结束时间
Date.getLastMonthEndDate = function (){
	var d = new Date();
	var lastmonth = d.getMonth()-1;
	var lastMonthEndDate = new Date(d.getFullYear(), lastmonth, Date.getMonthDays(d.getFullYear(),lastmonth));
	return lastMonthEndDate.format();
};

//获得昨天的日期
Date.getYesterdayDate = function() {
	var d = new Date();
	var yesterdayDate = new Date(d.getFullYear(), d.getMonth(), d.getDate()-1);
	return yesterdayDate.format();
};

//将String类型的日期转换为Date类型
Date.stringToDate = function(strDate) {
	return new Date(Date.parse(strDate.replace(/-/g,   "/")));
}

//判断开始日期和结束日期之间是否包含完整的月
Date.containsMonth = function(startDate, endDate){
	var sd = new Date(Date.parse(startDate.replace(/-/g,   "/")));
	var ed = new Date(Date.parse(endDate.replace(/-/g,   "/")));
	
	var year1 = sd.getFullYear();
	var month1 = sd.getMonth();
	var day1 = sd.getDate();
	
	var year2 = ed.getFullYear();
	var month2 = ed.getMonth();
	var day2 = ed.getDate();
	
	var c1 = year1+"-"+month1;
	var c2 = year2+"-"+month2;
	
	
	if(c1 != c2){
		return true;
	}
	
	var c3 = sd.getDate();
	var c4 = ed.getDate();
	if(c3 == "01" && c4 == Date.getMonthDays(year2,month2)){
		return true;
	}
	return false;
};
/**
* 参数为日期格式的字符串 
* 返回值：0 相等,1 startDate 小于 endDate , -1 startDate 大于 endDate
*/
Date.comparer = function(startDate,endDate){
	var start = new Date(Date.parse(startDate.replace(/-/g,   "/")));
	var end = new Date(Date.parse(endDate.replace(/-/g,   "/")));
  
  if(isNaN(start)) start = new Date().getTime();
  if(isNaN(end)) end = new Date().getTime();
  
  if(start - end > 0) return -1;
  if(start - end < 0) return 1;
  
  return 0;
};
/**扩展日期对象，支持日期的格式化、支持获取本月、上月的开始日期、结束日期 end**/

var provider = {};
provider.__tpltCache = new java.util.HashMap();
//提供模板
provider.getTemplete = function(url) {
	var tplt = provider.__tpltCache.get(url);
	if (!tplt) {
		try {
			var t = ejs.getNodeText(url);
			provider.__tpltCache.put(url, t);
			tplt = t;
		}
		catch(e) {
			return null;
		}
	}
	return tplt;
};
provider.newReportDataRequest = function() {
	return {
		_t_ : 'com.cnebula.analytics.reportserver.bean.ReportDataRequest',
		startDate : new Date(),
		endDate : new Date(),
		siteId : '',
		appId : '',
		grain : '',
		maxResults : 1000,
		rmlist : new java.util.ArrayList()
	};
};
provider.newReportorMetrics = function() {
	return {
		_t_ : 'com.cnebula.analytics.reportserver.bean.ReportorMetrics',
		name : '',
		filter : '',
		dimensions : '',
		description : ''
	};
};

var _reportor;
var reportModule = {};
reportModule.appId = null;
reportModule.siteId = null;
reportModule.init = function(instMenu){
	var tplt = provider.getTemplete('./tplt/'+instMenu.loadpage);
	var _ctx = EasyServiceClient.newContext();
	_ctx.instMenu = instMenu;
	_ctx.applicationMap = _reportor.applicationMap;
	ejs.renderComplexNode(tplt, '#main', _ctx);
	
	dateModule.init(instMenu);
	applistModule.init(instMenu);
	chartModule.init(instMenu);
};

var siteModule = {};
siteModule.curId = null;
siteModule.init = function() {
	_reportor = reportorService.getReportor();
	var siteMap = _reportor.siteMap;
	var tplt = provider.getTemplete('./tplt/site/site.htm');
	var _ctx = EasyServiceClient.newContext();
	_ctx.siteMap = siteMap;
	ejs.renderComplexNode(tplt, "#topMenu", _ctx);
	
	$("a.site-selector").unbind("click");
	$("a.site-selector").bind("click",function(){
		siteModule.active($(this));
	});
	
	$("a.site-selector").eq(0).trigger('click');
	
};
siteModule.active = function(instActive) {
	if(instActive.hasClass("active")){
		return;
	}
	$("a.site-selector").removeClass("active");
	instActive.addClass("active");
	var saasChild = $("#saasChildSite");
	if(saasChild){
		$("#saasChildSite").hide();
	}
	
	var curId = instActive.attr("id");// 主站点：全站、中心站、共享域、成员馆
	if(curId == 'saas'){//共享域站点被激活
		siteModule.saasChild.init();
		return;
	}
	var instSite = _reportor.siteMap.get(instActive[0].id);
	menuModule.init(instSite);
	siteModule.curId = instSite.id;
};
siteModule.saasChild = {};
siteModule.saasChild.init = function(){
	$("#saasChildSite").toggle();
	$("#saasChildSite a").unbind("click");
	$("#saasChildSite a").bind("click", function() {
		siteModule.saasChild.active($(this));
	});
	$("#saasChildSite a").eq(0).trigger('click');
};
siteModule.saasChild.active = function(instActive){
	
	if (instActive.hasClass("active"))return;	
	
	$("#saasChildSite a.active").removeClass("active");
	instActive.addClass("active");
	
	var instSite = _reportor.siteMap.get(instActive[0].id);
	menuModule.init(instSite);
	siteModule.curId = instSite.id;
};
var menuModule = {};
menuModule.init = function(instSite){
	var tplt = provider.getTemplete('./tplt/aside/menu.htm');
	var _ctx = EasyServiceClient.newContext();
	_ctx.topMenus = instSite.topMenus;
	ejs.renderComplexNode(tplt, '#aside', _ctx);
	
	$("a.asideMenu").unbind("click");
	$("a.asideMenu").bind("click", function(){
		menuModule.active($(this),instSite);
	});
	// 菜单初始化完成后，激活第一个菜单
	$("div#aside a:not(.open)").eq(0).trigger('click');
	
};
menuModule.active = function(instActive,instSite){
	if(instActive.hasClass("active")){
		return;
	}
	$("a.asideMenu").removeClass("active");
	instActive.addClass("active");
	var instMenu = instSite.menuMap.get(instActive[0].id);
	reportModule.init(instMenu);
};

var applistModule = {};
applistModule.curId = null;
applistModule.init = function(instMenu){
	if(!$("#applist")){
		return;
	}
	
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
		applistModule.active(_reportor.applicationMap.get($(this)[0].id));
		$("#applistdd").css("display", "none");
		
	});
	$(".dropdown li a").hover(function() {
		$(this).addClass("ui-state-focus");
	}, function() {
		$(this).removeClass("ui-state-focus");
	});
	
	if(instMenu.defaultOat){
		var application = _reportor.applicationMap.get(instMenu.defaultOat);
		applistModule.active(application);
	}else{
		$(".dropdown li a:first").trigger("click");
	}
};
applistModule.active = function(instApplication){
	if(!instApplication){
		throw "未找到配置的应用系统类型，请检查配置项。";
	}
	var name = instApplication.name;
	$("#apptext").html(name);
	applistModule.curId = instApplication.id;
};
/**DateWidget handle start**/
var dateModule = {};
dateModule._dateWidget = null;
dateModule._startDate = null;
dateModule._endDate = null;
dateModule._grain = null;
dateModule.DateSelectBar = {};
dateModule.DateSelect = {};
dateModule.TimeSpan = {};



dateModule.init = function(instMenu){
	dateModule._dateWidget = instMenu.dateWidget;
	var DateSelectBar = $("#DateSelectBar");
	var DateSelect = $("#DateSelect");
	var TimeSpan = $("#TimeSpan");
	
	if(dateModule._dateWidget && dateModule._dateWidget.enabled){
		dateModule.DateSelectBar.init();
		dateModule.DateSelect.init();
	}else{
		DateSelectBar.hide();
		DateSelect.hide();
		TimeSpan.hide();
	}
};
dateModule.DateSelectBar.init = function(){
	if(!$("#DateSelectBar")){
		return;
	}
	var yesterday = $("#bsnMdelYesterday");
	var lastMonth = $("#bsnMdelLastMonth");
	
	yesterday.unbind("click");
	yesterday.bind("click", function(){
		yesterday.addClass("active");
		lastMonth.removeClass("active");
		dateModule.active(Date.getYesterdayDate(), Date.getYesterdayDate());
	});
	lastMonth.unbind("click");
	lastMonth.bind("click", function(){
		lastMonth.addClass("active");
		yesterday.removeClass("active");
		dateModule.active(Date.getLastMonthStartDate(), Date.getLastMonthEndDate());
	});
	yesterday.trigger('click');
	
};
dateModule.DateSelect.init = function(){
	var to = new Date();
	to.setDate(to.getDate() - 1);
	var from = new Date();
	from.setFullYear(from.getFullYear() - 1);
	var _datepicker = null;
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
						dateModule.active(Date.getYesterdayDate(), Date.getYesterdayDate());
					}
					else {
						var startDate = new Date(dates[0]).getFullYear() + '-'
							+ (new Date(dates[0]).getMonth() + 1) + '-'
							+ new Date(dates[0]).getDate();
						var endDate = new Date(dates[1]).getFullYear() + '-'
							+ (new Date(dates[1]).getMonth() + 1) + '-'
							+ new Date(dates[1]).getDate();
						dateModule.active(startDate, endDate);
					}
				},

			});
	
	$("#timepickerConfirm").bind("click", function(){		// 确定按钮
		$('#datepicker-calendar').hide();
		$("#bsnMdelYesterday").removeClass("active");
		$("#bsnMdelLastMonth").removeClass("active");
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
dateModule.TimeSpan.init = function(startDate,endDate){
	dateModule._grain = null;
	if(!dateModule._dateWidget.isGrain){
		return;
	}
	var byHour = $("#ByHour");
	var byDay = $("#ByDay");
	var byMonth = $("#ByMonth");
	
	byHour.unbind("click");
	byHour.bind("click",function(){
		byHour.addClass("active");
		byDay.removeClass("active");
		byMonth.removeClass("active");
		dateModule._grain = "toph";
	});
	byDay.unbind("click");
	byDay.bind("click",function(){
		byDay.addClass("active");
		byHour.removeClass("active");
		byMonth.removeClass("active");
		dateModule._grain = "topd";
	});
	byMonth.unbind("click");
	byMonth.bind("click",function(){
		byMonth.addClass("active");
		byHour.removeClass("active");
		byDay.removeClass("active");
		dateModule._grain = "topm";
	});
	
	if (Date.containsMonth(startDate, endDate)){
		byMonth.removeClass("disable");
	}else{
		byMonth.addClass("disable");
	}
	byHour.trigger("click");
};

dateModule.show = function(startDate,endDate) {
	$("#date-range-field span").text(startDate + "\n\b\n\b\n\b\n\b" + "至" + "\n\b\n\b\n\b\n\b" +  endDate);
	var date = new Array();
	date.push( Date.stringToDate(startDate));
	date.push( Date.stringToDate(endDate));
	$('#datepicker-calendar').DatePickerSetDate(date, true);
};
dateModule.active = function(startDate,endDate){
	dateModule._startDate = startDate;
	dateModule._endDate = endDate;
	
	dateModule.show(startDate,endDate);
	dateModule.TimeSpan.init(startDate,endDate);
	
	processorModule.chart();
	
};
/**DateWidget handle end**/
var chartModule = {};
chartModule.metricsList = new java.util.ArrayList(); //metrics list
chartModule.init = function(instMenu){
	if(!instMenu.chartWidget.enabled){
		return;
	}
	var tplt = provider.getTemplete('./tplt/' + instMenu.id + '/metrics.htm');
	var _ctx = EasyServiceClient.newContext();
	_ctx.metricsItemName = "chartMetrics";
	ejs.renderComplexNode(tplt, '#chartMetricsOptionIndicator', _ctx);
	
	chartModule.metricsList = new java.util.ArrayList();
	
	var chartMetrics = $('input:checkbox[name="chartMetrics"]');
	chartMetrics.unbind("click");
	chartMetrics.bind("click",function(){
		chartModule.active($(this),instMenu.chartWidget);
	});
	
	var textObj = $("#chartMetricsIndicatorText");
	var containerObj = $("#chartMetricsOptionContainer");
	
	textObj.unbind("click")
	textObj.bind("click", function(){
		var dval = containerObj.css("display");
		if (dval == "none")
			containerObj.css("display", "block");
		else
			containerObj.css("display", "none");
	});
	$('input:checkbox[name="chartMetrics"]:checked').each(function() {
		chartModule.metricsList.add($(this));
	});
	chartModule.show();
	
	/* Close if clicked elsewhere. */
	_checkExternalClick = function(event) {

		var $target = $(event.target);
		if ($target.closest("#chartMetricsOptionContainer").length == 1) {
			return;
		}
		if ($target.closest("#chartMetricsIndicatorWrapper").length == 0) {
			containerObj.css("display", "none");
		}
	};
	$(document).mousedown(_checkExternalClick);
};
chartModule.active = function(instActive,instChartWidget){
	
	var chk = instActive[0].checked;
	if(chk){//选中
		chartModule.metricsList.add(instActive);
		if(chartModule.metricsList.size() > instChartWidget.size){
			chartModule.metricsList.remove(0);
		}
	}else{//取消选中
		if(chartModule.metricsList.size() == 1){
			instActive[0].checked = true;
			return;
		}else{
			for(var i=0; i<chartModule.metricsList.size(); i++){
				if(!chartModule.metricsList.get(i)[0].checked){
					chartModule.metricsList.remove(i);
					break;
				}
			}
		}
	}
	chartModule.show();
};
chartModule.show = function(){
	var str = "";
	for(var i=0; i<chartModule.metricsList.size(); i++){
		if(i+1 == chartModule.metricsList.size()){
			str = str + chartModule.metricsList.get(i)[0].title;
		}else{
			str = str + chartModule.metricsList.get(i)[0].title + "、";
		}
	}
	$("#chartMetricsIndicatorText").html(str);	
};

var processorModule = {};
processorModule.chart = function(instChartWidget){
	if(!instChartWidget || !instChartWidget.enabled){
		return;
	}
	var processor = eval(instChartWidget.processor);
	processor(instChartWidget);
};
processorModule.chart.handle = function(instChartWidget){
	
	chartCTX.addLoading("chart");
	
	var timeScale = "d";

	var reportDataRequest = provider.newReportDataRequest();
	
	reportDataRequest.startDate = Date.stringToDate(dateModule._startDate);
	reportDataRequest.endDate = Date.stringToDate(dateModule._endDate);
	reportDataRequest.grain = dateModule._grain;
	reportDataRequest.maxResults = instChartWidget.maxResults;
	reportDataRequest.siteId = siteModule.curId;
	reportDataRequest.appId = applistModule.curId;
	
	for(var i=0; i<chartModule.metricsList.size(); i++){
		var reportorMetrics = provider.newReportorMetrics();
		var m = metricsList.get(i);
		reportorMetrics.name = m.attr("metricsName");
		reportorMetrics.filter = m.attr("filter");
		reportorMetrics.dimensions = m.attr("dimensions");
		reportorMetrics.description = m.attr("description");
		reportDataRequest.rmlist.add(reportorMetrics);
	}
	
	var dataBean = chartReportService.getLineChart(reportDataRequest);	
};
processorModule.datatables = function(){
	
};

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
