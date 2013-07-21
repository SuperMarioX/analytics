if (!EasyServiceClient) {
	throw "需要EasyService平台远程服务调用JS客户端支持";
}
if (!ejs) {
	throw "需要EasyService平台模板支持";
}

var ctx = EasyServiceClient.newContext();

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

var UserInfoModule = (function() {
	var _userObj = null;
	var _userName = null;
	var _userCode = null;
	var _userType = null;
	var _userInfo = null;
	var _userSaasCode = null;
	var _userSaasInfo = null;
	
	return {
		setUserObj : function(obj) {
			_userObj = obj;
		},
		getUserName : function() {
			return _userName;
		},
		getUserCode : function() {
			return _userCode;
		},
		getUserType : function() {
			return _userType;
		},
		getUserInfo : function() {
			return _userInfo;
		},
		getUserSaasCode : function() {
			return _userSaasCode;
		},
		getUserSaasInfo : function() {
			return _userSaasInfo;
		},
		TypeInfoConstant : {
			calis : "calis",
			saas : "saas",
			library : "library"
		},
		init : function() {
			if(!_userObj) {
				throw {
					name: 'UserEmpty',
					message: 'user object missed'
				};
			}
			
			var obj = _userObj.user;
			_userName = obj.name;

			var chain = obj.extAttributes.table.pop();
			if (chain.key == "artifactChain") {
				var chainValue = chain.value;
				var index = chainValue.indexOf("=");
				var code = chainValue.substr(0, index);
				_userCode = code;

				if (code == "100000") {
					_userType = UserInfoModule.TypeInfoConstant.calis;
				} else {
					var type = provider.getTypeOfCode(code);
					
					
					if (type == "02") {	//共享域用户
						_userType = UserInfoModule.TypeInfoConstant.saas;
						_userInfo = provider.getNodeInfo(userCode);

					} else if (type == "03") {	//成员馆用户
						_userType = UserInfoModule.TypeInfoConstant.library;
						_userInfo = provider.getNodeInfo(_userCode);
						_userSaasCode = provider.getCenterOfLibrary(_userCode);		// 成员馆用户，记录该成员馆所属共享域信息
						_userSaasInfo = provider.getNodeInfo(_userSaasCode); 

						
						// 暂时将成员馆用户提升为该成员馆所属共享域的共享域用户
						/*_userType = UserInfoModule.TypeInfoConstant.saas;
						_userInfo = provider.getNodeInfo(_userCode);
						_userCode = provider.getCenterOfLibrary(_userCode);*/

					}
				}
			} else if (_userName == "admin") {
				_userCode = "100000";
				_userType = UserInfoModule.TypeInfoConstant.calis;
			}
		}
	};
})();


var TopMenuModule = (function(){
	var _siteFilter = "";//站点过滤条件
	var _site = ""; //当前激活的主站点
	var _ownedAppList = new java.util.ArrayList();//当前激活的站点支持的应用系统类型列表
	var librarySeled=0;//成员馆站点默认加载参数  只有第一次点击成员馆加载默认成员馆站点
	
	var mainSite = {};
	mainSite.ALL = "all";		//全站id
	mainSite.CENTER = "calis";//中心站点id
	mainSite.SAAS = "saas";	//共享域主站点id
	mainSite.LIB = "library";	//成员馆主站点id
	
	mainSite.init = function(){//初始化主站点
		if(UserInfoModule.getUserType() == UserInfoModule.TypeInfoConstant.library) {	// 成员馆用户，没有站点选择，不需要对站点信息进行加载
			_siteFilter = "oaten='" + UserInfoModule.getUserCode() +"'";
			_ownedAppList = provider.getOatListWithSite(UserInfoModule.getUserCode());
			AsideMenuModule.init();
			return ;
		}
		
		var tplt = provider.getTemplete('./tplt/site/parentMenu.htm');
		var ctx = EasyServiceClient.newContext();
		ctx.userType = UserInfoModule.getUserType();
		ctx.userCode = UserInfoModule.getUserCode();
		ejs.renderComplexNode(tplt, '#topMenu',ctx);
		childSite.init();
		
		
		$("a.site-selector").unbind("click");
		$("a.site-selector").bind("click",function(){
			mainSite.active($(this));
		});
		
		$("a.site-selector").eq(0).trigger('click');
	};
	mainSite.active = function(instActive){//某一主站点被激活
		if(instActive.hasClass("active")){
			return;
		}
		$("a.site-selector").removeClass("active");
		instActive.addClass("active");
		
		_site = instActive.attr("id");		// 主站点：全站、中心站、共享域、成员馆
		var hasmore = instActive.attr("hasContainer");		// 是否有子站点，全站、中心站没有
		
		if(_ownedAppList != null)
			_ownedAppList.clear();
		if(hasmore == 'false'){	//没有子站点时，触发 刷新左边菜单的动作
			_siteFilter = instActive.attr("filter");	// 没有子站点时，主站点有sitefilter信息，有子站点时，不存在该信息
			
			if(_site == mainSite.ALL) {	//根据站点信息和成员馆信息，初始化用户拥有的应用系统类型
				_ownedAppList = provider.getGlobalOatList();
			}
			else if(_site == mainSite.CENTER) {
				_ownedAppList = provider.getOatListWithSite("100000");
			}
			else if(UserInfoModule.getUserType() == UserInfoModule.TypeInfoConstant.saas && _site == mainSite.SAAS){		//共享域级别用户的共享域站点才有filter过滤条件
				_ownedAppList = provider.getOatListWithSite(UserInfoModule.getUserCode());
			}
			
			$("#childSite div").css("display","none");
			AsideMenuModule.init();
		}
		else {	//有子站点，激活子站点
			childSite.active();	
		}
	};
	var childSite = {};
	childSite.init = function(){//初始化子站点
		var libraryqtMap;
		var libraryqtList;
		var libraryqtStr="";
		var tplt = provider.getTemplete('./tplt/site/childSite.htm');
		var ctx = EasyServiceClient.newContext();
		ctx.provinceList=provider.getSaasCenter();//共享域子站点列表
		ctx.type = "calis";
		
		if(UserInfoModule.getUserType() == UserInfoModule.TypeInfoConstant.calis) {
			libraryqtMap = provider.getNodeInfoData("all");
			libraryqtList= provider.getSaasCenter();
		}
		else if(UserInfoModule.getUserType() == UserInfoModule.TypeInfoConstant.saas) {
			libraryqtMap=provider.getNodeInfoData(UserInfoModule.getUserCode());
			libraryqtList= provider.getSaasCenter();
			libraryqtStr=UserInfoModule.getUserCode();
		}
		ctx.libraryqtMap=libraryqtMap;
		ctx.libraryqtList=libraryqtList;
		ctx.libraryqtStr=libraryqtStr;
		ejs.renderComplexNode(tplt, '#childSite',ctx);
		childSite.saas.init();
		childSite.library.init();
	};
	childSite.active = function(){//激活子站点面板
		$("#childSite div").css("display","none");
		if(_site == mainSite.SAAS) {
			$("#childSite div").css("display","none");
			$("#saasChildSite").css("display","");
			$("#saasChildSite a").eq(0).trigger('click');
		}
		if(_site == mainSite.LIB){
			$("#librarychildsite").css("display","");
			$(".imageInputWrapper").css("display","");
			$("#selSize").css("display","");
			librarySeled = 0;
			if(librarySeled==0) {
				if(UserInfoModule.getUserType() == UserInfoModule.TypeInfoConstant.calis) {
					if(provider.getOatListWithSite("211010")!=null){
						$( "#keyword" ).val("北京大学");
						_siteFilter= "oaten='211010'";
						$("#keyword").attr("site","211010");
						_ownedAppList = provider.getOatListWithSite("211010");
						AsideMenuModule.init();
						$(".103190").css("display","");
						$("#provinceSelId").val("103190");
					}
					else{
						var librarymorenMap=provider.getNodeInfoData("all");
						var libraryqtList= provider.getSaasCenter();
						var librarychildmorenOb=libraryqtList.get(0);
						var librarychildOb=librarymorenMap.get(librarychildmorenOb[2]).get(0);
						$( "#keyword" ).val(librarychildOb[2]);
						_siteFilter= "oaten='"+librarychildmorenOb[2]+"'";
						$("#keyword").attr("site",librarychildmorenOb[2]);
						_ownedAppList = provider.getOatListWithSite(librarychildmorenOb[2]);
						AsideMenuModule.init();
						$("."+librarychildmorenOb[2]+"").css("display","");
						$("#provinceSelId").val(librarychildmorenOb[2]);
					}
				}
				else if(UserInfoModule.getUserType() == UserInfoModule.TypeInfoConstant.saas) {
					var userSaasCode=UserInfoModule.getUserCode();
					var librarychildList=provider.getNodeInfoData(userSaasCode);
			 		var librarychildOb = librarychildList.get(0);
					var librarychildSName=librarychildOb[2];
			 		$( "#keyword" ).val(librarychildSName);
			 		$( "#keyword" ).attr("site",userSaasCode); 
			 		_siteFilter="oaten='"+userSaasCode+"'";
			 		_ownedAppList = provider.getOatListWithSite(userSaasCode);
					AsideMenuModule.init();
				}
				librarySeled++;
			}
		}
	};
	childSite.saas = {};
	childSite.saas.init = function(){
		$("#saasChildSite a").unbind("click");
		$("#saasChildSite a").bind("click",function(){
			childSite.saas.active($(this));
		});
	};
	childSite.saas.active = function(instActiver){//激活某一共享域站点

		_siteFilter = instActiver.attr("filter");
		_ownedAppList = provider.getOatListWithSite(instActiver.attr("id"));
		
		$("#saasChildSite a.active").removeClass("active");
		instActiver.addClass("active");
		AsideMenuModule.init();
	};
	childSite.library = {};
	childSite.library.autocomplete = function(){
		var libraryMap;
		var org = [];
		
		if(UserInfoModule.getUserType() == UserInfoModule.TypeInfoConstant.calis) {
			/**
			 *如果是中心用户,返回全部的成员馆信息
			 *当前map的结构是key-oasc，v--List(list里面是成员馆信息String[])
			 */
			libraryMap=provider.getNodeInfoData("all");
			var keySet= libraryMap.keySet();
			for (var i in keySet){
				 var dataList=libraryMap.get(keySet[i]);
				 for(var i=0;i<dataList.size();i++){
					 var tenantArr=dataList.get(i);
				       var oaten=tenantArr[0];	
					   var name=tenantArr[2];	
					   var zh=tenantArr[3];
						if(zh != "" && name!=""&&oaten!=""){
						org.push({'zh' : zh , 'name' : name,'oaten' : oaten});
						zh="";
						name="";
						oaten="";
						}
				}
			}
		}
		else if(UserInfoModule.getUserType() == UserInfoModule.TypeInfoConstant.saas) {
			/**
			 *如果是saas用户,返回的是当前共享域下所有的成员馆信息
			 */
			libraryList=provider.getNodeInfoData(UserInfoModule.getUserCode());
			for(j=0;j<libraryList.size();j++){
				var dataList=libraryList.get(j);
				var oaten=dataList[0];	
				var name=dataList[2];	
				var zh=dataList[3];
				if(zh != "" && name!=""&&oaten!=""){
				org.push({'zh' : zh , 'name' : name,'oaten' : oaten});
				zh="";
				name="";
				oaten="";
				}
			}
		}
		
		$('#keyword').autocomplete(org, {
	        max: 12, //列表里的条目数 
	        minChars: 0, //自动完成激活之前填入的最小字符 
	        width: 400, //提示的宽度，溢出隐藏 
	        scrollHeight: 300, //提示的高度，溢出显示滚动条 
	        matchContains: true, //包含匹配，就是data参数里的数据，是否只要包含文本框里的数据就显示 
	        autoFill: false, //自动填充 
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
	        		$( "#keyword" ).attr("site","");
	        		return;
	        	 }
	        	 $( "#keyword" ).attr("site",row.oaten);
	          });
	};
	
	childSite.library.change = function(){//选择的成员馆改变，即keyword中的值改变
		$("#provinceSelId").bind("change", function() {		
			$("#sizechild li").css("display","none");
			$("."+this.options[this.options.selectedIndex].value).css("display","");
		});
	};
	childSite.library.active = function(){//成员馆站点的选择完成，相当于以前的enterId 被点击
		$("#enterId").bind("click", function() {
			var oaten = $("#keyword").attr("site");
			if(oaten==""||oaten==undefined){
				alert("请从下拉列表里选择成员馆！");
				return;
			}
			_siteFilter = "oaten='"+oaten+"'";			
			_ownedAppList = provider.getOatListWithSite(oaten);
			AsideMenuModule.init();
		});
	};
	childSite.library.dialog = function(){//初始化成员馆的dialog
		$("#librarychildsiteselect").bind("click", function() {
			$( "#dialog" ).dialog({modal:true,height:600,width:750});
			$("#librarychildsite").css("display","");
		});
		$("#dialog ul li a").bind("click", function() {
			//var code = $("#provinceId option:selected").val();
			$("#keyword").attr("value",$(this).attr("libName"));
			$("#keyword").attr("site",$(this).attr("site")); 
			$("#dialog").dialog("disable");
			$("#dialog").dialog("destroy");
		});
		$("#keyword").bind("focus", function() {
			//$( "#keyword" ).val("");
				$( "#keyword" ).css("background-color","");
				$( ".imageInputWrapper" ).css("border","solid 1px #000");
			});
			$("#keyword").bind("blur", function() {
				$( "#keyword" ).css("background-color","#F9F9F9");
				$( ".imageInputWrapper" ).css("border","");
			});
	};
	childSite.library.init = function(){
		
		childSite.library.autocomplete();
		childSite.library.dialog();
		childSite.library.change();
		childSite.library.active();
	};
	return {
		getOwnedList : function() {
		if(typeof _ownedAppList == "object" && _ownedAppList.indexOf("all") == -1 && _ownedAppList.size() > 1){	// 如果已经添加了“all”，则不再添加
			_ownedAppList.add("all");
		}
			return _ownedAppList;
		},
		getSiteFilter : function() {
			return _siteFilter;
		},
		getSite : function() {
			return _site;
		},
		init : function(){
			mainSite.init();
		}
	};
	
})();

var AsideMenuModule = (function(){
	var _asideMenuObj = null;
	var _twoMenuObj = null;
	var _threeMenuObj = null;
	var _funcId = null;
	var _funcInfo = null;
	var _funcName = null;
	var _oatList = null;
	
	var asideMenu =	{};
	asideMenu.init = function() {	//初始化功能选择菜单
		var tplt = provider.getTemplete('./tplt/aside/menu.htm');
		ctx.funcMap = provider.getFuncMap();
		ctx.funcPchildMap = provider.getFuncPchildMap();
		ctx.funcInfoMap = provider.getFuncInfoMap();
		ctx.ownList = TopMenuModule.getOwnedList();
		ctx.userType = UserInfoModule.getUserType();
		ctx.topmenu = TopMenuModule.getSite();
		ejs.renderComplexNode(tplt, '#aside',ctx);
		
		_asideMenuObj = $("a.asideMenu");
		_twoMenuObj = $("a.twoMenu");
		_threeMenuObj = $("a.threeMenu");
		
		if(_asideMenuObj.size() == 0) {		
			throw {
				name : 'Initialization Error',
				message : 'the AsideMenu has sth. wrong when initialize first'
			}
		}
		
		_asideMenuObj.unbind("click");
		_asideMenuObj.bind("click", function(){
			$(".threeMenuUL").css("display","none");
			if(_twoMenuObj.attr("value")=='dk'){
				_twoMenuObj.attr("value","gb");
				$("a.twoMenu").css("background-position","13px -2611px");
			}
			asideMenu.active($(this));
		});
		_twoMenuObj.unbind("click");
		_twoMenuObj.bind("click", function(){
			if(_twoMenuObj.attr("value")=='gb'){
				$(".threeMenuUL").css("display","");
				_twoMenuObj.attr("value","dk");
				$("a.twoMenu").css("background-position","13px -2571px");
			}else{
				$(".threeMenuUL").css("display","none");
				_twoMenuObj.attr("value","gb");
				$("a.twoMenu").css("background-position","13px -2611px");
			}
			
		});
		_threeMenuObj.unbind("click");
		_threeMenuObj.bind("click", function(){
			asideMenu.active($(this));
		});
	};
	asideMenu.active = function(instActiver) {	//激活菜单选项
		_funcId = instActiver.attr("id");
		_funcName = instActiver.html();
		
		_asideMenuObj.removeClass("active");
		_threeMenuObj.removeClass("active");
		instActiver.addClass("active");
		
		var general = provider.getFuncInfoMap().get(_funcId)[14];
		if(general == 'true')
		{
			_oatList = TopMenuModule.getOwnedList().toArray();
		}else{
			_oatList = TopMenuModule.getOwnedList().intersection(provider.getFuncMap().get(_funcId));
		}
		
		_funcInfo = provider.getFuncInfoMap().get(_funcId);
		
		var loadhtm = _funcInfo[11];
		
		if(loadhtm == "main.htm") {
			var tplt = provider.getTemplete('./tplt/main.htm');
			
			ctx.title = _funcName;
			ctx.oatList = _oatList;
			ctx.oatInfoMap = provider.getOatInfoNameMap();
			ctx.metricName = _funcInfo[8];
			ejs.renderComplexNode(tplt, '#main',ctx);
			
			if(_funcId == "systemenv") {
				SystemEnvModule.init("true");
			}
			else {
				SystemEnvModule.init("false");
			}
			try {
				DateShortcutModule.init();
				DateApModule.init(_funcInfo[6]);
				OatSelectModule.init(_funcInfo[7]);
				ChartMetricsModule.init(_funcInfo[3], _funcInfo[1]);
				DataTableMetricsModule.init(_funcInfo[5]);
				chartCTX.renderChart(_funcId);
				dataTableCTX.renderDataTables(_funcId);
			} catch(e) {
				// 异常处理
			}
		}
		else if(loadhtm == "realtime.htm") {
			var tplt = provider.getTemplete('./tplt/realtime.htm');
			ctx.title = _funcName;
			ctx.oatList = _oatList;
			ctx.oatInfoMap = provider.getOatInfoNameMap();
			ctx.metricName = _funcInfo[8];
			ejs.renderComplexNode(tplt, '#main',ctx);
			SystemEnvModule.init("false");
			OatSelectModule.init(_funcInfo[7]);
			ChartMetricsModule.init(_funcInfo[3], _funcInfo[1]);
	//		DataTableMetricsModule.init(_funcInfo[5], _funcInfo[4]);
			ChartReportModule.renderRealTimeChart();
		}
		else if(loadhtm == "todayData.htm") {
			var tplt = provider.getTemplete('./tplt/realtime.htm');
			ctx.title = _funcName;
			ctx.oatList = _oatList;
			ctx.oatInfoMap = provider.getOatInfoNameMap();
			ctx.metricName = _funcInfo[8];
			ejs.renderComplexNode(tplt, '#main',ctx);
			SystemEnvModule.init("false");
			OatSelectModule.init(_funcInfo[7]);
			ChartMetricsModule.init(_funcInfo[3], _funcInfo[1]);
	//		DataTableMetricsModule.init(_funcInfo[5], _funcInfo[4]);
			ChartReportModule.renderTodayDataChart();
		}
		
		
	};
	
	return {
		getFuncId : function() {
			return _funcId;
		},
		getFuncInfo : function() {
			return _funcInfo;
		},
		init : function() {
			asideMenu.init();
			//默认选中一个菜单
			$("div#aside a:not(.open)").eq(0).trigger('click');
		}
	};
})();


var DateShortcutModule = (function(){
	var _startDate = null;
	var _endDate = null;
	var _yesterdayObj = null;
	var _lastMonthObj = null;
	var _dateFromToObj = null;
	var _tempFuncId = "";
	var _tempFuncInfo = null;
	var _datepicker = null;
	
	var dateShort = {};
	dateShort.init = function() {	//初始化时间范围
		_startDate = Date.getYesterdayDate();
		_endDate = Date.getYesterdayDate();
		_yesterdayObj.addClass("active");
		_lastMonthObj.removeClass("active");
		dateShort.dateFromTo.showDateTime();
	};
	
	dateShort.yesterday = {};	//“昨日”选择控件
	dateShort.yesterday.init = function() { //初始化“昨日”时间范围选择控件
		_yesterdayObj = $("#bsnMdelYesterday");
		
		if(_yesterdayObj.size() == 0) {
			throw {
				name : 'Initialization Error',
				message : 'the "yesterday" DOM elements should be initialize first'
			}
		}
		_yesterdayObj.unbind("click");
		_yesterdayObj.bind("click", function(){
			dateShort.yesterday.active();
		});
	};
	dateShort.yesterday.active = function() {	//激活“昨日”时间范围选择控件
		_startDate = Date.getYesterdayDate();
		_endDate = Date.getYesterdayDate();
		_yesterdayObj.addClass("active");
		_lastMonthObj.removeClass("active");
		
		dateShort.dateFromTo.showDateTime();
		
		DateApModule.init(AsideMenuModule.getFuncInfo()[6]);
		chartCTX.renderChart(AsideMenuModule.getFuncId());
		dataTableCTX.renderDataTables(AsideMenuModule.getFuncId());
	};
	
	dateShort.lastmonth = {};	//“上月”时间范围选择控件
	dateShort.lastmonth.init = function() {	//初始化 “上月”时间范围选择控件
		_lastMonthObj = $("#bsnMdelLastMonth");
		
		if(_lastMonthObj.size() == 0) {
			throw {
				name : 'Initialization Error',
				message : 'the "lastmonth" DOM elements should be initialize first'
			}
		}
		
		_lastMonthObj.unbind("click");
		_lastMonthObj.bind("click", function(){
			dateShort.lastmonth.active();
		});
	};
	dateShort.lastmonth.active = function() {	//激活 “上月”时间范围选择控件
		_startDate = Date.getLastMonthStartDate();
		_endDate = Date.getLastMonthEndDate();
		_yesterdayObj.removeClass("active");
		_lastMonthObj.addClass("active");
		
		dateShort.dateFromTo.showDateTime();
		
		DateApModule.init(AsideMenuModule.getFuncInfo()[6]);
		chartCTX.renderChart(AsideMenuModule.getFuncId());
		dataTableCTX.renderDataTables(AsideMenuModule.getFuncId());
	};
	
	dateShort.dateFromTo = {};		// 日期选择控件
	dateShort.dateFromTo.init = function() { // 初始化日期选择控件
		_dateFromToObj = $("#date-range-field span");
		
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
						

						dateShort.dateFromTo.showDateTime();
						DateApModule.init(AsideMenuModule.getFuncInfo()[6]);
						chartCTX.renderChart();
						dataTableCTX.renderDataTables();
					},

				});


		$("#timepickerConfirm").bind("click", function(){		// 确定按钮
			dateShort.dateFromTo.onConfirm();
		});
		
		$("#timepickerCancel").bind("click", function(){		// 取消按钮
			dateShort.dateFromTo.onCancel();
		});
		
		$('#datepicker-calendar').hide();
		$('#date-range-field').bind('click', function() {
			$('#datepicker-calendar').toggle();
			return false;
		});
	};
	dateShort.dateFromTo.onConfirm = function() {
		$('#datepicker-calendar').hide();
		_yesterdayObj.removeClass("active");
		_lastMonthObj.removeClass("active");
		$('#datepicker-calendar').DatePickerSetConfirmed();

	};
	dateShort.dateFromTo.onCancel = function() {
		$('#datepicker-calendar').hide();
		$('#datepicker-calendar').DatePickerClear();
		var date = new Array();
		date.push( Date.stringToDate(_startDate));
		date.push( Date.stringToDate(_endDate));
		$('#datepicker-calendar').DatePickerSetDate(date, true);
	};
	dateShort.dateFromTo.showDateTime = function() {
		_dateFromToObj.text(_startDate + "\n\b\n\b\n\b\n\b" + "至" + "\n\b\n\b\n\b\n\b" +  _endDate);
		var date = new Array();
		date.push( Date.stringToDate(_startDate));
		date.push( Date.stringToDate(_endDate));
		$('#datepicker-calendar').DatePickerSetDate(date, true);
	};
	
	return {
		getStartDate : function() {
			return _startDate;
		},
		getEndDate : function() {
			return _endDate;
		},
		getDatePicker : function() {
			return _datepicker;
		},
		init : function() {
			dateShort.yesterday.init();
			dateShort.lastmonth.init();
			dateShort.dateFromTo.init();
			dateShort.init();
		}
	};
})();

var DateApModule = (function(){
	var _ap = "";
	var _timeSpanObj = null;
	var _byHourObj = null;
	var _byDayObj = null;
	var _byMonthObj = null;
	var _useOrNot = "true";
	
	var dateap = {};
	dateap.init = function() {
		_timeSpanObj = $("#TimeSpan");
		if(_useOrNot == "true") {	
			_timeSpanObj.css("display", "block");
			
			dateap.toph.init();
			dateap.topd.init();
			dateap.topm.init();
			
			_ap = "toph";
			_byHourObj.addClass("active");
			_byDayObj.removeClass("active");
			_byMonthObj.removeClass("active");
			if (Date.containsMonth(DateShortcutModule.getStartDate(), DateShortcutModule.getEndDate()))
				$("#ByMonth").removeClass("disable");
			else
				$("#ByMonth").addClass("disable");	
		}
		else {
			_timeSpanObj.css("display", "none");
			_timeSpanObj = null;
			_ap = "";
			_byHourObj = null;
			_byDayObj = null;
			_byMonthObj = null;
		}
	};
	
	dateap.toph = {};
	dateap.toph.init = function() {
		_byHourObj = $("#ByHour");
		_byHourObj.unbind("click");
		_byHourObj.bind("click", function(){
			dateap.toph.active();
		});
	};
	dateap.toph.active = function() {
		if(_byHourObj.hasClass("active")) {
			return ;
		}
		_ap = "toph";
		_byHourObj.addClass("active");
		_byDayObj.removeClass("active");
		_byMonthObj.removeClass("active");
		
		chartCTX.renderChart(AsideMenuModule.getFuncId());
		dataTableCTX.renderDataTables(AsideMenuModule.getFuncId());
	};
	
	dateap.topd = {};
	dateap.topd.init = function() {
		_byDayObj = $("#ByDay");
		_byDayObj.unbind("click");
		_byDayObj.bind("click", function(){
			dateap.topd.active();
		});
	};
	dateap.topd.active = function() {
		if(_byDayObj.hasClass("active") || _byDayObj.hasClass("disable")) {
			return ;
		}
		_ap = "topd";
		_byHourObj.removeClass("active");
		_byDayObj.addClass("active");
		_byMonthObj.removeClass("active");
		
		chartCTX.renderChart(AsideMenuModule.getFuncId());
		dataTableCTX.renderDataTables(AsideMenuModule.getFuncId());
	};
	
	dateap.topm = {};
	dateap.topm.init = function() {
		_byMonthObj = $("#ByMonth");
		_byMonthObj.unbind("click");
		_byMonthObj.bind("click", function(){
			dateap.topm.active();
		});
	};
	dateap.topm.active = function() {
		if(_byMonthObj.hasClass("active") || _byMonthObj.hasClass("disable")) {
			return ;
		}
		_ap = "topm";
		_byHourObj.removeClass("active");
		_byDayObj.removeClass("active");
		_byMonthObj.addClass("active");
		
		chartCTX.renderChart(AsideMenuModule.getFuncId());
		dataTableCTX.renderDataTables(AsideMenuModule.getFuncId());
	}
	
	
	
	return {
		getAp : function() {
			return _ap;
		},
		getUseOrNot : function() {
			return _useOrNot;
		},
		init : function(showPara) {
			/**
			 * 判断该面板是否显示
			 * */
			if(showPara == "false")
				_useOrNot = "false";
			else
				_useOrNot = "true";

			dateap.init();
		}
	};
})();

//应用系统选择模块
var OatSelectModule = (function(){
	var _oatFilter = "";
	var _oat = "";
		
	var oatselect = {};
	oatselect.init = function() {
		$(".dropdown li a").hover(function() {
			$(this).addClass("ui-state-focus");
		}, function() {
			$(this).removeClass("ui-state-focus");
		});
		
		$(".dropdown dt").unbind("click")
		$(".dropdown dt").bind("click",function(){
			oatselect.active();
		});
		$(".dropdown li a").unbind("click")
		$(".dropdown li a").bind("click",function(){
			oatselect.click($(this));
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
	
	oatselect.active = function() {		// 激活 应用系统选择对象
		if($("#applistdd").css("display") == "block") {
			$("#applistdd").css("display", "none");
		}
		else {
			$("#applistdd").css("display", "block");
		}
	};
	oatselect.click = function(instActive) {	// 点击 应用系统选择对象中的选项
		$("#apptext").html(instActive.html());
		_oatSelect = instActive.attr("value");
		_oat = _oatSelect;
		_oatFilter = instActive.attr("filter");
		
		$("#applistdd").css("display", "none");
		if(AsideMenuModule.getFuncId() == "realTime" ) {
			ChartReportModule.renderRealTimeChart();
		}else if(AsideMenuModule.getFuncId() == "todayData" ){
			ChartReportModule.renderTodayDataChart();
		}
		else {
			chartCTX.renderChart(AsideMenuModule.getFuncId());
			dataTableCTX.renderDataTables(AsideMenuModule.getFuncId());
		}
	
	};
	
	return {
		getOatFilter : function() {
			return _oatFilter;
		},
		getOat : function() {
			return _oat;
		},
		init : function(oat) {

			oatselect.init(); 
			
			var oatlist = $(".dropdown li a");
			oatlist.each(function(){
				var v = $(this);
				if(v.attr("value") == oat) {
					$("#apptext").html(v.html());
					_oatSelect = v.attr("value");
					_oatFilter = v.attr("filter");
					_oat = oat;
				}
			});
			if($("#apptext").html() == "") {
				var temp = $(".dropdown li a:first");
				$("#apptext").html(temp.html());
				_oatSelect = temp.attr("value");
				_oatFilter = temp.attr("filter");
				_oat = temp.attr("id");
			}
			
		}
	};
})();

/**
 * 系统环境模块
 * */
var SystemEnvModule = (function() {
	var _useOrNot = "false";
	var _selItem = "";			// 选择的系统环境选项
	var _systemEnvObj = null;
	
	var systemenv = {};
	systemenv.init = function() {	// 初始化 系统环境选择对象
		_systemEnvObj = $("#systemEnvPanel");
		if(_useOrNot == "true") {	//显示
			_systemEnvObj.css("display", "block");
		}
		else {	//不显示
			_systemEnvObj.css("display", "none");
			return ;
		}
		
		$("input:radio[name='systemenv']").unbind("click");
		$("input:radio[name='systemenv']").bind("click", function(){
			systemenv.active($(this));
		});
		
		var v = $("input:radio[name='systemenv']:checked");
		if(v) {
			_selItem = v.attr("id");
		}
		else {
			_selItem = "";
		}
	}
	systemenv.active = function(instActive) {	// 激活 系统环境选择对象
		if(instActive.attr("id") != _selItem) {
			_selItem = instActive.attr("id");
			
//			DateShortcutModule.init();
//			DateApModule.init("false");
//			OatSelectModule.init("iri");
			ChartMetricsModule.init("true", 1);
			DataTableMetricsModule.init("alone");
			chartCTX.renderChart(AsideMenuModule.getFuncId());
			dataTableCTX.renderDataTables(AsideMenuModule.getFuncId());
		}
	};
	
	return {
		getUseOrNot : function() {
			return _useOrNot;
		},
		getSelItem : function() {
			return _selItem;
		},
		init : function(_use) {
			if(_use == "true") {
				_useOrNot = "true";
			}
			else {
				_useOrNot = "false";
			}
			
			systemenv.init();
		}
	};
})();

/**
 * 图标指标选择模块
 * 如果不使用表格指标选择模块，则该模块也供表格数据的报表查询使用
 */
var ChartMetricsModule = (function() {
	var _useOrNot = "true";
	var _checkedQueue = new java.util.ArrayList();
	var _checkedQueueSize = 1;		// 当前的指标选择最大数目
	var _defaultQueueSize = 1;		// 默认的指标选择最大数目，点击功能后后，该值被设置，不会被更改，使用该变量是为加入时间对比使用，使用时间对比时，只能选择单指标
	var _chartMetricsIndicatorObj = null;
	var _chartMetricsOptionObj = null;
	var _chartMetricsObj = null;
	var _funcId = "";
	
	var chartmetrics = {};
	chartmetrics.init = function() {		// 初始化 指标选择模块
		if(_useOrNot == "false") {		// 是否显示指标选择模块
			$("#chartMetricsIndicator").css("display", "none");
			$("#chart").css("display", "none");
			return ;
		}
		else {
			$("#chartMetricsIndicator").css("display", "true");
			$("#chart").css("display", "true");
		}
		
		_funcId = AsideMenuModule.getFuncId();
		_checkedQueue.clear();
		
		_chartMetricsIndicatorObj = $("#chartMetricsIndicatorText");
		_chartMetricsOptionObj = $("#chartMetricsOptionContainer");
		
		
		chartmetricsoption.init();			// 初始化  指标显示容器对象
		chartmetricsindicator.init();		// 初始化 所选指标名称显示对象
		
		
		chartmetricsindicator.refresh();
		
		
		/* Close if clicked elsewhere. */
		_checkExternalClick = function(event) {

			var $target = $(event.target);
			if ($target.closest("#chartMetricsOptionContainer").length == 1) {
				return;
			}
			if ($target.closest("#chartMetricsIndicatorWrapper").length == 0) {
				_chartMetricsOptionObj.css("display", "none");
			}
		};
		$(document).mousedown(_checkExternalClick);
	};
	
	var chartmetricsindicator = {};		// 所选指标名称显示对象
	chartmetricsindicator.init = function() {	// 初始化 所选指标名称显示对象
		
		if(_chartMetricsIndicatorObj.size() == 0) {
			throw {
				name : 'Initialization Error',
				message : 'the "chartMetricsIndicatorText" DOM elements(chart metrics) should be initialize first'
			}
		}
		
		_chartMetricsIndicatorObj.unbind("click")
		_chartMetricsIndicatorObj.bind("click", function(){
			chartmetricsindicator.active();
		});
	};
	chartmetricsindicator.active = function() {	// 激活 所选指标名称显示对象：切换指标显示容器的显示属性
		var vis = _chartMetricsOptionObj.css("display");
		if (vis == "none")
			_chartMetricsOptionObj.css("display", "block");
		else
			_chartMetricsOptionObj.css("display", "none");
	};
	chartmetricsindicator.refresh = function() {	// 刷新  所选指标名称显示对象：选中或取消选中后重新显示
		var showStr = "", i;
		for (i = 0; i < _checkedQueue.size(); i++) {
			if (i > 0) {
				showStr += "、";
			}
			showStr += _checkedQueue.get(i).title;
		}
		_chartMetricsIndicatorObj.html(showStr);
	};
	
	var chartmetricsoption = {};		// 指标显示容器对象
	chartmetricsoption.init = function() {	// 初始化  指标显示容器对象
		if(_chartMetricsOptionObj.size() == 0) {
			throw {
				name : 'Initialization Error',
				message : 'the "chartMetricsOptionContainer" DOM elements(chart metrics) should be initialize first'
			}
		}
		
		ctx.func = _funcId;
		ctx.oat = OatSelectModule.getOat();
		if(SystemEnvModule.getUseOrNot() == "true") {
			tplt = provider.getTemplete('./tplt/' + ctx.func + '/' + SystemEnvModule.getSelItem() +'_metrics.htm');
		}
		else {
			tplt = provider.getTemplete('./tplt/' + ctx.func + '/metrics.htm');
		}
		ctx.metricsItemName = "chartMetrics";
		ejs.renderComplexNode(tplt, '#chartMetricsOptionIndicator',ctx);
		
		_chartMetricsObj = $('input:checkbox[name="chartMetrics"]');
		if(_chartMetricsObj.size() == 0) {
			throw {
				name : 'Initialization Error',
				message : 'the "chartMetrics Item" DOM elements(chart metrics) should be initialize first'
			}
		}
		
		var checkedItem = $('input:checkbox[name="chartMetrics"]:checked');
		checkedItem.each(function() {
			var item = _geneMetrics($(this));
			_checkedQueue.add(item);
		});
		
		_chartMetricsObj.unbind("click");
		_chartMetricsObj.bind("click", function(){	// 指标点击事件
			var _sel = $(this);
			chartmetricsoption.click(_sel);
		});
	};
	chartmetricsoption.click = function(instActive) {	// 激活  指标显示容器对象：选中或者取消选中动作

		if (instActive[0].checked == false) {// 取消选中
			if (_checkedQueue.size() == 1) {
				instActive[0].checked = true;
				return;
			} else {
				for ( var i = 0; i < _checkedQueue.size(); i++) {
					if (instActive[0].value == _checkedQueue.get(i).key) {
						_checkedQueue.remove(i);
					}
				}
			}
		} else {	// 选中
			_checkedQueue.add(_geneMetrics(instActive));
			if (_checkedQueue.size() > _checkedQueueSize) {		// 当前选中的数目超过最大可选择数目
				$('input:checkbox[name="chartMetrics"]:checked').each(function() {
					if ($(this).val() == _checkedQueue.get(0).key) {
						$(this)[0].checked = false;
						_checkedQueue.remove(0);
						return false;
					}
				});
			}
		}
		chartmetricsindicator.refresh();
		
		if(AsideMenuModule.getFuncId() == "realTime") {
			ChartReportModule.renderRealTimeChart();
		}
		else if(AsideMenuModule.getFuncId() == "todayData" ){
			ChartReportModule.renderTodayDataChart();
		}
		else {
			chartCTX.renderChart(AsideMenuModule.getFuncId());
			if(DataTableMetricsModule.getUseOrNot() == "false") {
				dataTableCTX.renderDataTables(AsideMenuModule.getFuncId());
			}
		}
	
		
	};
	
	return {
		getUseOrNot : function() {
			return _useOrNot;
		},
		getCheckedQueue : function() {
			return _checkedQueue;
		},
		setDefaultQueueSize : function(size) {
			_checkedQueueSize = size;
		},
		init : function(showPara, size) {
			
			if(showPara == "false") {
				_useOrNot = "false";
			}
			else {
				_useOrNot = "true";
			}
			_defaultQueueSize = size;
			_checkedQueueSize = size;
			
			chartmetrics.init();
		}
	}
})();

var DataTableMetricsModule = (function(){
	var _checkedQueue = new java.util.ArrayList();
	var _dataTableMetricsOptionObj;
	var _dataTableMetricsObj;
	var _dataTablePanelObj;			// 表格指标面板对象
	var _dataTableObj;				// 表格对象
	var _checkedQueueSize = -1;
	var _useOrNot = "true";
	var _showOrNot = "true";
	var _funcId = "";
	
	
	var datatablemetrics = {};	// 表格指标对象
	datatablemetrics.init = function() {	//初始化 表格指标对象
		_funcId = AsideMenuModule.getFuncId();
		_checkedQueue.clear();
		
		ctx.func = _funcId;
		ctx.oat = OatSelectModule.getOat();
		if(SystemEnvModule.getUseOrNot() == "true") {
			tplt = provider.getTemplete('./tplt/' + _funcId + '/' + SystemEnvModule.getSelItem() +'_metrics.htm');
		}
		else {
			tplt = provider.getTemplete('./tplt/' + _funcId + '/metrics.htm');
		}
		
		ctx.metricsItemName = "customMetrics";
		ejs.renderComplexNode(tplt, '#customMetricsOptionIndicator',ctx);
		
		_dataTableMetricsOptionObj = $("#customMetricsOptionIndicator");
		_dataTableMetricsObj = $('input:checkbox[name="customMetrics"]');

		if(_dataTableMetricsOptionObj.size() == 0 ||  _dataTableMetricsObj.size() == 0) {
			throw {
				name : 'Initialization Error',
				message : 'the DOM elements(dataTable metrics) should be initialize first'
			}
		}
		

		$("#filterTabs").easytabs({
			collapsible:true,
	      	collapsedByDefault:true,
	      	tabActiveClass:'selected'
		});	
		
		_dataTableMetricsObj.unbind("click");
		_dataTableMetricsObj.bind("click", function() {
			datatablemetrics.click($(this));
		});
		
		$("#btn1").unbind("click");
		$("#btn1").bind("click", function() {
			dataTableCTX.renderDataTables();
		});

		var checkedItem = $('input:checkbox[name="customMetrics"]:checked');
		checkedItem.each(function() {
			var item = _geneMetrics($(this));
			_checkedQueue.add(item);
			
		});
	};
	datatablemetrics.click = function(instActive) {	//点击 表格指标对象选项
		if (instActive[0].checked == false) {// 取消选中
			if (_checkedQueue.size() == 1) {
				instActive[0].checked = true;
				return;
			} else {
				for ( var i = 0; i < _checkedQueue.size(); i++) {
					if (instActive[0].value == _checkedQueue.get(i).key) {
						_checkedQueue.remove(i);
					}
				}
			}
		} else {// 选中
			_checkedQueue.add(_geneMetrics($(instActive)));
		}
	};
	
	return {
		getUseOrNot : function() {
			return _useOrNot;
		},
		getShowOrNot : function() {
			return _showOrNot;
		},
		getCheckedQueue : function() {
			return _checkedQueue;
		},
		setDefaultQueueSize : function(size) {
			_defaultQueueSize = size;
			_checkedQueueSize = size;
		},
		init : function(_type, showPara) {
			_dataTableObj = $("#datetables");
			_dataTablePanelObj = $("#advfilterBar");
			
			if(showPara != null && showPara == false) {
				_showOrNot = "false";
				_dataTableObj.css("display", "none");
				_dataTablePanelObj.css("display", "none");
				return ;
			}
			
			
			if(_type == "uniform") {
				_useOrNot = "false";
				_dataTablePanelObj.css("display", "none");
				return ;
			}
			else {
				_useOrNot = "true";
				_dataTablePanelObj.css("display", "block");
				
			}
			
			datatablemetrics.init();

		}
	};
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
	
	
	initWebsite();
	
	if(!initUserInfo()){
		ObsEvent.trigger("userInit");
	};
});

function initWebsite() {
	ObsEvent.listen("userObjInit", UserInfoModule,  UserInfoModule.setUserObj);
	ObsEvent.listen("userInit", UserInfoModule, UserInfoModule.init);
	ObsEvent.listen("userInit", TopMenuModule, TopMenuModule.init);

}


var GetFunction = (function() {
	var functionMap = {
		"suppDataOfSub" : suppDataOfSub
	}
	
	return {
		getFunction : function(name){
			return functionMap[name];
		}
	}
})();

/**
 *  补充学科数据
 *  学科共有13大类，若查出的数据中没有某类，则填上该类，数据补0
 * */
function suppDataOfSub(rowsMap) {
	var keys = rowsMap.keySet();
	var suppRowsMap = new java.util.HashMap();
	
	for(var i=0; i<keys.length; i++) {
		var list = rowsMap.get(keys[i]);
		var metricsObj = JSON.parse(keys[i]);
		var metricsName = metricsObj.name;
		metricsName = metricsName.toUpperCase();
		var dataTempMap = new java.util.HashMap();
		var rowsData = new Array();
		
		for (j = 0; j < list.size(); j++) {
			var row = eval(list.get(j));
			var subCode = row[0]["OSUB"];
			
			if(subCode.length < 2) {
				continue;
			}
			else if(subCode.length > 2) {
				subCode = subCode.substr(0, 2);
			}
			dataTempMap.putAdd(subCode, parseInt(row[0][metricsName]));
		}
		for(j = 1; j <= 13; j++) {
			var subData = j < 10 ? ("0" + j) : (j + "");
			var rowData = new Object();
			rowData.OSUB = subData;
		//	rowData.push(subData);
			var num = dataTempMap.get(subData);
			if(num == null) {
				num = 0;
			}
		//	rowData.push(num);
			rowData.OPS = num;
			rowsData.push(rowData);
		}
		var jsonTitle = new Array();
		jsonTitle[0] = "OSUB";
		jsonTitle[1] = "OPS";
		var temp = JSON.stringify(rowsData);
		suppRowsMap.put(keys[i], temp);
	
	}
	
	return suppRowsMap;
}