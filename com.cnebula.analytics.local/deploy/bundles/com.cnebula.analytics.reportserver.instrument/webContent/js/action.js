/**
*
*页面元素控制
*根据页面元素的选择组织查询的sql语句
*
**/
var actionCTX = {};
//actionCTX.filter = function(){
//	this.startDate = '';
//	this.endDate = '';
//	this.orgList = new java.util.ArrayList();
//};

actionCTX.filter = {
		startDate : '',
		endDate : '',
		orgList : new java.util.ArrayList()
};

/**
 * 维度
 * 维度是单选的，默认的维度是时间，粒度是日
 * key：topd 日粒度、topm月粒度、topy年粒度、org 机构、otil 检索词、osub 学科
 */
actionCTX.dimension = function(key,description){
		this.key = key;
		this.description = description;
};
actionCTX.dimensionItemMap = new java.util.HashMap();
/**
 *orgMap封装的是机构的信息,key:code,value是机构的名字
*/
actionCTX.orgMap = new java.util.HashMap();
actionCTX.initDimensionItemMap = function (){
	actionCTX.dimensionItemMap.clear();	
	actionCTX.dimensionItemMap.put("topd",new actionCTX.dimension("topd","日"));//日粒度
	actionCTX.dimensionItemMap.put("topm",new actionCTX.dimension("topm","月"));//月粒度
	actionCTX.dimensionItemMap.put("topy",new actionCTX.dimension("topy","年"));//年粒度
	actionCTX.dimensionItemMap.put("oaten",new actionCTX.dimension("oaten","机构"));//机构

	var appType = getAppType();
	if(appType == 'iri'){//教参
		actionCTX.dimensionItemMap.put("otil",new actionCTX.dimension("otil","检索词"));
		actionCTX.dimensionItemMap.put("osub",new actionCTX.dimension("osub","学科"));	
	}
};

/**
 * 指标
 * 
 */
actionCTX.metrics = function(key,filter,description){
		this.key = key;
		this.filter = filter;
		this.description = description;
};

actionCTX.metricsItemMap = new java.util.HashMap();
actionCTX.initMetricsItemMap = function(){
	actionCTX.metricsItemMap.clear();
	/**公共指标**/
	actionCTX.metricsItemMap.put("op.l",new actionCTX.metrics("ops","op = 'l' ","登录"));
	actionCTX.metricsItemMap.put("op.vm",new actionCTX.metrics("ops","op = 'vm' ","查看"));
	actionCTX.metricsItemMap.put("op.s",new actionCTX.metrics("ops","op = 's' ","检索"));
	/**公共指标 end**/
	var appType = getAppType();
	/**教参指标**/
	if(appType == 'iri'){
		actionCTX.metricsItemMap.put("op.s-ort.c",new actionCTX.metrics("ops","op = 's' AND ort in ('c','c-n','c-f')","课程信息检索次数"));
		actionCTX.metricsItemMap.put("op.s-ort.rf",new actionCTX.metrics("ops","op = 's' AND ort='rf'","教参书检索次数"));
		actionCTX.metricsItemMap.put("op.vm-ort.c",new actionCTX.metrics("ops","op = 'vm' AND ort in ('c','c-n','c-f')","课程信息访问次数"));
		actionCTX.metricsItemMap.put("op.vm-ort.rf",new actionCTX.metrics("ops","op = 'vm' AND ort in ('rf','rf-n','rf-f')","教参书访问次数"));
	}
	/**教参指标 end**/
};

/**
 * 刷新指标列
 * 教参：	1.时间、机构、学科的指标: 登录人次、课程信息的检索次数、教参书的检索次数、课程信息的访问次数、教参书的访问次数
 * 		2.检索词的指标: 检索次数
 * e读：	
 */
actionCTX.refreshMetricsItemMap = function(dimensionKey){
	actionCTX.metricsItemMap.clear();
	/**公共指标**/
	actionCTX.metricsItemMap.put("op.l",new actionCTX.metrics("ops","op = 'l' ","登录"));
	actionCTX.metricsItemMap.put("op.vm",new actionCTX.metrics("ops","op = 'vm' ","查看"));
	actionCTX.metricsItemMap.put("op.s",new actionCTX.metrics("ops","op = 's' ","检索"));
	/**公共指标 end**/
	var appType = getAppType();
	if(appType == 'iri'){
		actionCTX.metricsItemMap.put("op.s-ort.c",new actionCTX.metrics("ops","op = 's' AND ort in ('c','c-n','c-f')","课程信息检索次数"));
		actionCTX.metricsItemMap.put("op.s-ort.rf",new actionCTX.metrics("ops","op = 's' AND ort='rf'","教参书检索次数"));
		actionCTX.metricsItemMap.put("op.vm-ort.c",new actionCTX.metrics("ops","op = 'vm' AND ort in ('c','c-n','c-f')","课程信息访问次数"));
		actionCTX.metricsItemMap.put("op.vm-ort.rf",new actionCTX.metrics("ops","op = 'vm' AND ort in ('rf','rf-n','rf-f')","教参书访问次数"));
		if(dimensionKey == 'otil'){//教参检索词维度
			actionCTX.metricsItemMap.clear();
			actionCTX.metricsItemMap.put("op.s",new actionCTX.metrics("ops","op = 's' ","检索"));
		}
	}
	
	actionCTX.renderMetricsItem();
};

actionCTX.renderMetricsItem = function(){
	var ctx = EasyServiceClient.newContext();
	ctx.itemMap = actionCTX.metricsItemMap;
	var tplt = calis.getTemplete('./tplt/metricsItem.tplt');
	ejs.renderComplexNode(tplt, '#metricsMenu',ctx);
};
actionCTX.renderDimensionItem = function(){
	var ctx = EasyServiceClient.newContext();
	ctx.itemMap = actionCTX.dimensionItemMap;
	var tplt = calis.getTemplete('./tplt/dimensionItem.tplt');
	ejs.renderComplexNode(tplt, '#dimensionMenu',ctx);
};
actionCTX.init = function(){
	$("#timeConditions").hide();
	$("#appList").hide();
	actionCTX.initDimensionItemMap();
	actionCTX.initMetricsItemMap();
	actionCTX.renderDimensionItem();
	actionCTX.renderMetricsItem();
	
	actionCTX.renderOrgList();
	actionCTX.doReport();
};
actionCTX.renderOrgList = function(){
	var ctx = EasyServiceClient.newContext();
	var tplt = calis.getTemplete('./tplt/orglist.tplt');
	var code = getCenterCode();
	if(code == null || code == '100000'){
		code = '-1';
	}
	var orgList = actionCTX.getOrgList(code);

	if(orgList == null || orgList == undefined){
		ctx.orgList = [];
		//ejs.renderComplexNode(null, '#institution',ctx);    
	}else{   
		ctx.orgList = orgList.toArray();
		for(var i=0;i<ctx.orgList.length;i++){
			actionCTX.orgMap.put(ctx.orgList[i].code,ctx.orgList[i].name);
			var subList=ctx.orgList[i].subList.toArray();
			for(var j=0;j < subList.length;j++){
	          	 var code = subList[j].code;
	          	 var orgName = subList[j].name;
	          	/**
	          	 * actionCTX.orgMap存放的是机构的key-code;value-name;
	          	 */
	          	 actionCTX.orgMap.put(code,orgName); 
			}
		}
		actionCTX.orgMap.put("100000","CALIS管理中心"); 
	};
	ejs.renderComplexNode(tplt, '#institution',ctx);
	//var orgList = actionCTX.getOrgList("-1");
};
/**
 * 根据条件对象,维度对象,指标列对象去后台查询数据,生成dataTable,生成各种图表的方法
 */
actionCTX.doReport = function(){
	
	/**
	 * 点击查询之前增加进度条
	 */
	progressWin = new ca.win('./tplt/progress.htm', null, 200, 100, true);
	progressWin.open();
	var orgItemValue="";
	var orgCondition="";
	var startDate = $('#startDate').val();
	var endDate = $('#endDate').val();
	if(startDate != null && startDate != ''){
		startDate = new Date(Date.parse(startDate.replace(/-/g,   "/")));
	}else{
		startDate = new Date(getDayTime(-1));
	}
	if(endDate != null && endDate != ''){
		endDate = new Date(Date.parse(endDate.replace(/-/g,   "/")));
	}else{
		endDate = new Date(getDayTime(-1));
	}
	var dimensionKey = $('input:radio[name="dimensionItem"]:checked').val();
	if(dimensionKey==null||dimensionKey=='undefined'||dimensionKey==''){
		/**默认查询的时候加载三个指标，由于加载的是昨天的时间,x轴颗粒度精确到小时**/
		dimensionKey = 'toph';
	}
	var mcItem = $('input:checkbox[name="metricsItem"]:checked');
	var orgItem = $('input:checkbox[name="orgItem"]:checked');
	/**选中的机构作为检索的条件**/
	for(var i=0;i<orgItem.length;i++){
		if(orgItem.length>1){
            if(i+1 == orgItem.length){
             orgItemValue+=orgItem[i].value;
			}else{
			 orgItemValue+=orgItem[i].value+",";
			}
		}else{
			orgItemValue+=orgItem[i].value;
		}
	}
	if(orgItem.length > 0){
		orgCondition = "oaten in ("+orgItemValue+")";
	}
	/**
	 *如果没有选择时间刻度,默认为"d"
	 */
	var timeScale="d";
	if(dimensionKey=="toph"){
	    timeScale="h"; 
	}
	var reqlist = new java.util.ArrayList();
	if(mcItem != null && mcItem.length > 0){
		$.each( mcItem, function(){
				var reqId = $(this).val();
				var metrics = actionCTX.metricsItemMap.get(reqId);
				var req = calis.report.newDataExportRequest();
				req.id = reqId;
				req.startDate = startDate;
				req.endDate = endDate;
				/**设置选择的时间刻度**/
				req.timeScale=timeScale;
				/**select column**/
				req.dimensions.add(dimensionKey);
				var keys = reqId.split("-");
				for(var i=0;i<keys.length;i++){
					var key = keys[i].split(".")[0];
					req.dimensions.add(key);
				}
				req.metrics.add(metrics.key);
				/**select column end**/
				
				/**sql where **/
				req.filter = metrics.filter;
				if(orgCondition != ""){
					if(req.filter != null && req.filter != ''){
						req.filter = req.filter + " AND ";
					}
					req.filter = req.filter + orgCondition;
					/**选择机构的时候增加默认的机构维度**/
					req.dimensions.add("oaten");
				}
				/**group by**/
				if(dimensionKey == 'topm'){
					req.groups.add("topy");
				}else if(dimensionKey == 'topd'){
					req.groups.add("topy");
					req.groups.add("topm");
				}else if(dimensionKey == 'toph'){
					req.groups.add("date");
				}
				req.groups.add(dimensionKey);
				/**group by end**/
				
				/**maxResult**/
				if(dimensionKey == 'oaten'){
					/**如果用户选择的是按机构维度，返回的数据是排行前十的机构**/
					req.maxResults=10;
				}
				/**maxResult end**/
				
				reqlist.add(req);
			 });
	}else{
		//查看为默认的指标
		//actionCTX.metricsItemMap.put("op.vm",new actionCTX.metrics("ops","op = 'vm' ","查看"));
		
		var req = calis.report.newDataExportRequest();
		req.id = "op.vm";
		req.startDate = startDate;
		req.endDate = endDate;
		/**设置选择的时间刻度**/
		req.timeScale=timeScale;
		/**select column**/
		req.dimensions.add(dimensionKey);
		req.dimensions.add("op");
		req.metrics.add("ops");
		/**select column end**/
		
		/**sql where **/
		req.filter = "op = 'vm'";
		/**group by**/
		if(dimensionKey == 'topm'){
			req.groups.add("topy");
		}else if(dimensionKey == 'topd'){
			req.groups.add("topy");
			req.groups.add("topm");
		}else if(dimensionKey == 'toph'){
			req.groups.add("date");
		}
		req.groups.add(dimensionKey);
		/**group by end**/
		
		reqlist.add(req);
	}
	var rowsMap = calis.report.feedDataRAWReqList(reqlist);
	/**
	 *根据前台的条件，生成各种图表
	 */
	actionCTX.renderDateLineChart(startDate,endDate,dimensionKey,rowsMap);
	/**
	 *根据后台返回的数据，生成dataTable
	 */
	actionCTX.renderDataTable(rowsMap);
	/**
	 * 图表生成后,dataTable生成后关闭进度条.
	 */
	progressWin.close();
};
/**
 * 解析出原始数据中一行数据的列头
 */
actionCTX.getColHead = function(row){
	var colhead = "";
	for(var i=1;i<row.length;i++){
		colhead = colhead + "/" + row[i];
	}
	try{
		colhead = colhead.substr(1);
		var dimensionKey = $('input:radio[name="dimensionItem"]:checked').val();
		if(dimensionKey == 'oaten'){
			if(actionCTX.orgMap.get(colhead)){
				colhead = actionCTX.orgMap.get(colhead);	
			}
		}
	}catch(e){}
	
	return colhead;
};

actionCTX.createRAWArray = function(size){
	var obj = new Array(size);
	for(var i=0;i<size;i++){
		obj[i] = "0";
	}
	return obj;
};

actionCTX.renderDataTable = function(rows){
	var aaData = [];
	var titles = [{"sTitle" : ""}];
	var keys = rows.keySet();
	var fmtrowmap = new java.util.HashMap();// key:第一列的值  value：列头对应的各个指标的数据
	
	for(var i=0;i<keys.length;i++){
		var metrics = actionCTX.metricsItemMap.get(keys[i]);
		titles.push({"sTitle" : metrics.description});
		var list = rows.get(keys[i]);
		for(var j=0;j<list.size();j++){
			var row = list.get(j);
			var colhead = actionCTX.getColHead(row);
			if(fmtrowmap.containsKey(colhead)){
				var obj = fmtrowmap.get(colhead);
				obj[i] = row[0];
				fmtrowmap.put(colhead,obj);
			}else{
				var obj = actionCTX.createRAWArray(keys.length);
				obj[i] = row[0];
				fmtrowmap.put(colhead,obj);
			}
		}
	}
	var fmtrowkey = fmtrowmap.keySet();
	for(var i=0;i<fmtrowkey.length;i++){
		var colhead = fmtrowkey[i];
		var fmtrow = fmtrowmap.get(colhead);
		var row = [];
		row = row.concat(colhead,fmtrow);
		aaData.push(row);
	}
	
	
	
	$('#acTable').html( '<table cellpadding="0" cellspacing="0" border="0" id="act"></table>' );
	 $('#act').dataTable( {
		 	"bJQueryUI": true,
	        "sPaginationType": "full_numbers",
	        "bFilter": false,
	        "aaData": aaData,
	        "aoColumns": titles,
	        "oLanguage": {
	            "sLengthMenu": "每页 _MENU_ 条记录",
	            "sZeroRecords": "无数据",
	            "sInfo": "当前第 _START_ 到  _END_ 条记录 共_TOTAL_条记录",
	            "sInfoEmpty": " ",
	            "oPaginate": {
	            	"sFirst":"首页",
		            "sLast":"末页",
		            "sNext":"下一页",
		            "sPrevious":"上一页"
	              }
	        }
	    } );   
};
/**
 * 得到机构列表
 * @param code	上级机构代码
 */
actionCTX.getOrgList = function(code){
	return calis.rd.treeNodeInfoMap.get(code);
};
actionCTX.suborgList = function(orgId)
{
	$("#suborglist fieldset").slideUp();
	$("#org_tbl_"+orgId).toggle("slow");
};

$(function() {
	$( "#dateYesterday" ).addClass("unSelDate",1000);
	$( "#dateLastMonth" ).addClass("unSelDate",1000);
	
	$( "#dateYesterday" ).click(function(){
		$( "#dateYesterday" ).switchClass("unSelDate","selDate");
		$( "#dateLastMonth" ).switchClass("selDate","unSelDate");
		
		actionCTX.onDateChange('dateYesterday');
		return false;	
	});
	$("#dateLastMonth").click(function(){
		$( "#dateLastMonth" ).switchClass("unSelDate","selDate");
		$( "#dateYesterday" ).switchClass("selDate","unSelDate");
		
		actionCTX.onDateChange('dateLastMonth');
		return false;
	});
	/**jquery 日期控件**/
	$( "#dateFrom" ).attr("readonly",true);
	$( "#dateTo" ).attr("readonly",true);
	
	$( "#dateFrom" ).datepicker({
		changeMonth: true,
		changeYear: true,
		dateFormat: 'yy-mm-dd',
		onSelect: function( selectedDate ) {
			$( "#dateTo" ).datepicker( "option", "minDate", selectedDate );
			$( "#dateYesterday" ).switchClass("selDate","unSelDate");
			$( "#dateLastMonth" ).switchClass("selDate","unSelDate");
			
			actionCTX.onDateChange('dateFrom');
		}
		
	});
	
	$( "#dateTo" ).datepicker({
		changeMonth: true,
		changeYear: true,
		dateFormat: 'yy-mm-dd', 
		onSelect: function( selectedDate ) {
			$( "#dateFrom" ).datepicker( "option", "maxDate", selectedDate );
			$( "#dateLastMonth" ).switchClass("selDate","unSelDate");
			$( "#dateYesterday" ).switchClass("selDate","unSelDate");
			
			actionCTX.onDateChange('dateTo');
		}
	});
	*/
	/**jquery 日期控件 end**/
});

actionCTX.onDateChange = function(type){
	if(type == 'dateYesterday'){
		var d = new Date();
		d.setDate(d.getDate() - 1);//获取昨天的日期
		var y = d.getFullYear();
		var m = d.getMonth()+1;//获取当前月份的日期
		var day = d.getDate();
		d=y+"-"+m+"-"+day;
		$("#startDate").val(d);
		$("#endDate").val(d);
		$("#dateFrom").val('');
		$("#dateTo").val('');
		
		actionCTX.initDimensionItemMap();
		actionCTX.dimensionItemMap.remove("topy");//年粒度
		actionCTX.dimensionItemMap.remove("topm");//月粒度
		actionCTX.renderDimensionItem();
		
	}else if(type == 'dateLastMonth'){
		var d = new Date();
		var year = d.getFullYear();
		var month = d.getMonth();
		//返回的月份是从0开始的,故上月时间不用减一，如果当前月份是1月份，上月应该是去年的12月份,下面函数做了处理。
		if(month < 1){
			year = year - 1;
			month = 12;
		}
		$("#startDate").val(year+"-"+month+"-01");
		$("#endDate").val(year+"-"+month+"-31");
		$("#dateFrom").val('');
		$("#dateTo").val('');
		
		actionCTX.initDimensionItemMap();
		actionCTX.dimensionItemMap.remove("topy");//年粒度
		actionCTX.renderDimensionItem();
		
	}else if(type == 'dateFrom' || type == 'dateTo'){
		
		var startDate = $("#dateFrom").val();
		var endDate = $("#dateTo").val();
		
		if(!isDate(startDate)){
			startDate = '';
		}
		if(!isDate(endDate)){
			endDate = '';
		}
		$("#startDate").val(startDate);
		$("#endDate").val(endDate);
		
		if(startDate != '' && endDate != ''){
			var sd = startDate.split("-");
			var ed = endDate.split("-");
			actionCTX.initDimensionItemMap();
			if(sd[0] == ed[0]){//非跨年
				actionCTX.dimensionItemMap.remove("topy");//月粒度
			}
			if(sd[0] == ed[0] && sd[1] == ed[1]){//非跨月
				actionCTX.dimensionItemMap.remove("topy");//年粒度
				actionCTX.dimensionItemMap.remove("topm");//月粒度
			}
			if(startDate == endDate){
				actionCTX.dimensionItemMap.put("toph",new actionCTX.dimension("toph","小时"));//24时粒度
			}
			actionCTX.renderDimensionItem();
		}
	}
};
/**
 *时间维度的出折线图，检索词维度出柱状图
 *date:2012.8.22
 */
actionCTX.renderDateLineChart= function(startDate,endDate,dimensionKey,rowsMap){
	var target="HistogramImage";
	var timeScale;
	/**
	 *该数组针对线性表加载选中的各个指标的描述
	 */
	var arrayTitle=new Array();
	var keySet= rowsMap.keySet();
	var arrayData=new Array();
	var arrayObjDic=new Array();
	if(dimensionKey=="topy"||dimensionKey=="topm"||dimensionKey=="topd"||dimensionKey=="toph"){
		var yerStr;
		var monthStr;
		var dayStr;
		var hourStr;
		var year;
		var month;
		var day;
		var hour;
		/**
		 *用户选择时间维度为"年"
		 */
		if(dimensionKey=="topy"){
		/**
	      *前台需要时间刻度补充数据，格式化时间，以及时间排序
		*/	
		timeScale="y";
		for (var i in keySet){
			var arrayTopyData=new Array();
		    var dataList=rowsMap.get(keySet[i]);
			for(j=0;j<dataList.size();j++){
				var arrayObj = new Array();
				var ops=parseInt(dataList.get(j)[0]);
				if(ops==null){
					ops=0;
				}
				year=parseInt(dataList.get(j)[1]);
				arrayObj.push(year);
				arrayObj.push(ops);
				arrayTopyData.push(arrayObj);
			};
			/*对时间轴进行升序排序 */
		    calis.util.sort(arrayTopyData, 0, true);
		    /* 
			 * 根据选择的时间刻度，计算出一个时间范围内的时间序列，主要是用于后面时间段的补齐年份数据
			*/
		    var timeSeq = calis.util.timeSeq(timeScale,startDate,endDate);
		    /* 
			 * 使用margeTimeSeq方法时间轴必须是排序的数组或者时间序列,否则该函数调用会发生问题
			 * 如果不想补没有月份的数据，可以直接注释掉下面的方法
			*/
		    arrayTopyData = ctx.margeTimeSeq(arrayTopyData,timeSeq); 
			arrayData.push(arrayTopyData);
			 /* 
			 * 根据选中指标的key，获取map中对应的value封装成一个数组,用于线性图表中对不同指标的描述
			 */
			arrayTitle.push(actionCTX.metricsItemMap.get(keySet[i]).description);
		}
		}
		/**
		 *用户选择时间维度为"月"
		 */
		if(dimensionKey=="topm"){
			timeScale="m";
			for (var i in keySet){
				var arrayTopmData=new Array();
			    var dataList=rowsMap.get(keySet[i]);
				for(j=0;j<dataList.size();j++){
					var arrayObj = new Array();
					yerStr=dataList.get(j)[1];
					month=parseInt(dataList.get(j)[2]);
					if(month < 10){
						monthStr=yerStr+"0"+month;
					}else{
						monthStr=yerStr+""+month;
					}
					arrayObj.push(parseInt(monthStr));
					var ops=parseInt(dataList.get(j)[0]);
					if(ops==null){
						ops=0;
					}
					arrayObj.push(ops);
					arrayTopmData.push(arrayObj);
				};
				/*对时间轴进行升序排序 */
			    calis.util.sort(arrayTopmData, 0, true);
			    /* 
				 * 根据选择的时间刻度，计算出一个时间范围内的时间序列，主要是用于后面时间段的补齐月份数据
				*/
			    var timeSeq = calis.util.timeSeq(timeScale,startDate,endDate);
			    /* 
				 * 使用margeTimeSeq方法时间轴必须是排序的数组或者时间序列,否则该函数调用会发生问题
				 * 如果不想补没有月份的数据，可以直接注释掉下面的方法
				*/
			    arrayTopmData = ctx.margeTimeSeq(arrayTopmData,timeSeq); 
				arrayData.push(arrayTopmData);
				 /* 
				 * 根据选中指标的key，获取map中对应的value封装成一个数组,用于线性图表中对不同指标的描述
				 */
				arrayTitle.push(actionCTX.metricsItemMap.get(keySet[i]).description);
			}
		}
		/**
		 *用户选择时间维度为"日"
		 */
		if(dimensionKey=="topd"){
			timeScale="d";
			for (var i in keySet){
				var arrayTopdData=new Array();
			    var dataList=rowsMap.get(keySet[i]);
				for(j=0;j<dataList.size();j++){
					var arrayObj = new Array();
					var ops=parseInt(dataList.get(j)[0]);
					if(ops==null){
						ops=0;
					}
					yerStr=dataList.get(j)[1];
				    month=parseInt(dataList.get(j)[2]);
					day=parseInt(dataList.get(j)[3]);
					if(month < 10){
						monthStr="0"+month;
					}else{
						monthStr=""+month;
					}
					if(day < 10){
						dayStr=yerStr+monthStr+"0"+day;
					}else{
						dayStr=yerStr+monthStr+""+day;
					}
					arrayObj.push(parseInt(dayStr));
					arrayObj.push(ops);
					arrayTopdData.push(arrayObj);
				};
				  /*对时间轴进行升序排序 */
			    calis.util.sort(arrayTopdData, 0, true);
			    /* 
				 * 根据选择的时间刻度，计算出一个时间范围内的时间序列，主要是用于后面时间段的补齐数据
				*/
			    var timeSeq = calis.util.timeSeq(timeScale,startDate,endDate);
			    /* 
				 * 使用margeTimeSeq方法时间轴必须是排序的数组或者时间序列,否则该函数调用会发生问题
				*/
			    arrayTopdData = ctx.margeTimeSeq(arrayTopdData,timeSeq); 
				arrayData.push(arrayTopdData);
				 /* 
				 * 根据选中指标的key，获取map中对应的value封装成一个数组,用于线性图表中对不同指标的描述
				 */
				arrayTitle.push(actionCTX.metricsItemMap.get(keySet[i]).description);
			}
		}
		/**
		 *用户选择时间维度为"小时",用户选择为小时,返回的数据结构不同于年月日，组装数据结构为date+hour
		 */
		if(dimensionKey=="toph"){
			timeScale="h"; 
			for (var i in keySet){
				var arrayTophData=new Array();
			    var dataList=rowsMap.get(keySet[i]);
				for(j=0;j<dataList.size();j++){
					var arrayObj = new Array();
					var ops=parseInt(dataList.get(j)[0]);
					if(ops==null){
						ops=0;
					}
					/**
					 *注意date的数据格式为20120801
					 */
				    date=dataList.get(j)[1];
					hour=parseInt(dataList.get(j)[2]);
					if(hour< 10)
					{
						hourStr=date+"0"+hour;
					}else{
						hourStr=date+""+hour;
					}
					/**
					 *注意下面数据结构的组装,图标以及函数的实现主要由数据结构组装实现
					 */
					arrayObj.push(parseInt(hourStr));
					arrayObj.push(ops);
					arrayTophData.push(arrayObj);
				};
				 /*对时间轴进行升序排序 */
			    calis.util.sort(arrayTophData, 0, true);
			    /* 
				 * 根据选择的时间刻度，计算出一个时间范围内的时间序列，主要是用于后面小时时间段的补齐数据
				*/
			    var timeSeq = calis.util.timeSeq(timeScale,startDate,endDate);
			    /* 
				 * 使用margeTimeSeq方法时间轴必须是排序的数组或者时间序列,否则该函数调用会发生问题
				*/
			    arrayTophData = ctx.margeTimeSeq(arrayTophData,timeSeq); 
				arrayData.push(arrayTophData);
				/* 
				 * 根据选中指标的key，获取map中对应的value封装成一个数组,用于线性图表中对不同指标的描述
				 */
				arrayTitle.push(actionCTX.metricsItemMap.get(keySet[i]).description);
			}	
		}
		//}
		document.getElementById(target).innerHTML="";
		actionCTX.renderCurveBarChart(timeScale,arrayData, "", "", "",target,arrayTitle);
	}else if(dimensionKey=="otil"||dimensionKey=="osub"||dimensionKey == 'oaten'){
		var arrayTitle=new Array();
		var orgArray=new Array();
		for (var i in keySet){
		    var dataList=rowsMap.get(keySet[i]);
		    /*var orgArray=orgItemValue.split(",");
			for(var k = 0;k < orgArray.length;k++){
				var code=orgArray[k];
			}*/
		    var arrayOtil = new Array();
            if(dataList.size() == 0){
            	/**
				 *报表显示柱状图的时候，处理数据为空的情况.
				 */
            	var arrayObj = new Array();
            	arrayObj.push(0);
            	arrayObj.push("");
            	arrayOtil.push(arrayObj);
            }else{
		    for(j=0;j<dataList.size();j++){
				var arrayObj = new Array();
				var ops=parseInt(dataList.get(j)[0]);
				if(ops==null){
					ops=0;
				}
				arrayObj.push(ops);
				if(dimensionKey == 'oaten'){
					/**
					 *如果选择的是机构,则从orgMap中获取机构的名字
					 */
					arrayObj.push(actionCTX.orgMap.get(dataList.get(j)[1]));
					/**
					 *如果选择的是机构,将机构代码放进orgArray数组,主要是为了计算不重复机构的数量
					 */
					orgArray.push(dataList.get(j)[1]);
				}else{
					arrayObj.push(dataList.get(j)[1]);
					/**
					 *如果选择的是检索词,或者学科指标,将检索词或者学科放进orgArray数组
					 */
					orgArray.push(dataList.get(j)[1]);
				}
				arrayOtil.push(arrayObj);
			}
            };
			arrayData.push(arrayOtil);
			arrayTitle.push(actionCTX.metricsItemMap.get(keySet[i]).description);
		}
		document.getElementById(target).innerHTML="";
		/**
		 *将重复的机构代码(检索词或者学科数据)过滤掉,主要为了计算机构图表所需的高度.
		 */
		orgArray = districtArray(orgArray);
		actionCTX.renderColumnBarChart(arrayData, "","","", target,arrayTitle,orgArray);
	}
	try {
		/**
		 *重新创建对象之前，销毁旧的jqplot对象，防止内存溢出.
		*/
		$('#' + target).qtip("destroy");
	} catch (e) {
	}
};
/**
 *数据结构显示为柱状图
*/
actionCTX.renderColumnBarChart = function(data, chartTitle, xName, yName, target,arrayTitle,orgArray) {
	/**
	 *dataLength选中指标的个数
	 */
	var dataLength=data.length;
	/**
	 *arraySize 机构(检索词或者学科数据)的个数
	 */
	var arraySize=orgArray.length;
	/**
	 *根据机构个数(检索词或者学科数据)，指标个数，以及柱状图的宽度动态计算出图表的高度,这一块算法是关键。
	 */
	var imageHeight=parseInt(dataLength) * parseInt(arraySize) * 12;
	if(imageHeight < 400){
		document.getElementById(target).style.height="400px";
	}else{
	    document.getElementById(target).style.height=imageHeight+"px";
	}
	$(document).ready(function(){
		//var data=[[[24, "150900"]],[[24, "150900"]], [[23, "150900"]]]; 
		var plot2 = $.jqplot(target,data, {        
		seriesDefaults: {            
			renderer:$.jqplot.BarRenderer,
			pointLabels: { show: true, location: 'e', edgeTolerance: -15 },            
			shadowAngle: 135,          
			rendererOptions: {               
				barDirection: 'horizontal',
				fillToZero : true,
				barPadding : 2,
				barMargin: 10,   //柱状体组之间间隔
				barWidth : 10,
					},
			}, 
			series : [ {
				label : arrayTitle[0],
				neighborThreshold : -1
			}, {
				label : arrayTitle[1],
			}, {
				label : arrayTitle[2],
			} , {
				label : arrayTitle[3],
			} , {
				label : arrayTitle[4],
			} , {
				label : arrayTitle[5],
			} , {
				label : arrayTitle[6],
			}  
			
			],
			  legend: {            
				show : true,
				location : 'ne',
				placement : 'insideGrid'    
					},
			axes: {           
				yaxis: {                
				renderer: $.jqplot.CategoryAxisRenderer,
				}       
			},
			
			});
		}); 
};
/**
 *数据结构显示为折线图
*/
actionCTX.renderCurveBarChart = function(timeScale, data, chartTitle, xName, yName, target,arrayTitle) {
	//document.getElementById(target).className = "chart";
	document.getElementById(target).style.height="400px";
	var parsePatterns = {
		'd' : 'yyyyMMdd',
		'h' : 'yyyyMMddHH',
		'm' : 'yyyyMM',
		'y' : 'yyyy'	
	};
	var formatPatterns = {
		'd' : '%y-%m-%d',
		'h' : '%m-%d:%H',
		'm' : '%y-%m',
		'y' : '%y'	
	};
	var intervals = Math.ceil(data[0].length / 12);
	var tickIntervals = {
		'd' : intervals + ' days',
		'h' : intervals + ' hours',
		'm' : intervals + ' months',
		'y' : intervals + ' years',	
	};
	// 校对到标准时间
	var detTimes = {
		'd' : -3600000 * 8,
		'h' : 0,
		'm' : -3600000 * 8,
		'y' : -3600000 * 8
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
			label : arrayTitle[0],
			neighborThreshold : -1
		}, {
			label : arrayTitle[1],
		}, {
			label : arrayTitle[2],
		} , {
			label : arrayTitle[3],
		} , {
			label : arrayTitle[4],
		} , {
			label : arrayTitle[5],
		} , {
			label : arrayTitle[6],
		}  
		
		],
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
//检验日期格式，合法的格式例如：2000-10-29,月份和天必须是两位，能检测闰年。
function isDate(str){
	var reg=/^(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)$/;
	if (reg.test(str)) return true;
	return false;
}
function getDayTime(DayCount){
	    var date = new Date();
	    date.setDate(date.getDate()+DayCount);//获取DayCount天后的日期
	    var y = date.getFullYear();
	    var m = date.getMonth()+1;//获取当前月份的日期
	    var d = date.getDate();
	    return y+"/"+m+"/"+d;
}
/**
 *去除重复的机构代码，主要从数组里去除掉重复的机构代码
*/
function districtArray(array){ 
	var result = new Array(); 
	for(var i in array){ 
	if(result.indexOf(array[i]) == -1){ 
	result.push(array[i]); 
	} 
	} 
	return result; 
	};

