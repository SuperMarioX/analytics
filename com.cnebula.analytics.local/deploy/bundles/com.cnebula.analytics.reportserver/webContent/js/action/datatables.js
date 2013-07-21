/**
 * 当前js的主要功能加载后台返回的实体bean,生成dataTables报表.
 * author:suyunlong     date:2012.9.12
 */
var dataTableTarget = "datetables";
var dataTableCTX = {};
/**
 *设置dataTable显示的目标标签
*/
dataTableCTX.setTarget = function(tar) {
	dataTableTarget = tar;
};
dataTableCTX.renderDataTables = function(funcId) {
	dataTableReportModule.renderStatDataTable();
};
dataTableCTX.addLoading = function(dataTableTarget) {
	$('#'+dataTableTarget).html("");
	$('#'+dataTableTarget).addClass("loading");
};
var dataTableReportModule = (function(){
	var _dataTableTarget = "datetables";
	var _ap = "";
	renderDataTables = function(dataTableBean,size) {
		var titles = eval(dataTableBean.titles);
		var aaData = dataTableBean.aaData;
		$('#datetables').removeClass("loading");
		var th='<tfoot><tr><th style="text-align:left">全部汇总:</th>';
		var th;
		if(aaData.length > 0){
		for(var i=1;i<aaData[0].length;i++){
			th=th+'<th style="text-align:left"></th>';
		}
		}else{
			th='<th></th>';
		}
		var thEnd=th+'</tr></tfoot>';
		$('#datetables').html( '<table cellpadding="0" cellspacing="0" border="0" id="act">'+thEnd+'</table>' );
		 $('#act').dataTable({
			 "sDom": 'T<"clear"><"H"lfr>t<"F"ip>',
			 "oTableTools": {
		            "aButtons": [
		                   {
		                	   "sExtends" : "copy",
		                	   "sButtonText" : "复制"
		                   },
		                   {
		                	   "sExtends" : "print",
		                	   "sButtonText" : "打印",
		                	   "sInfo": "<h6>打印预览</h6><p>请使用浏览器打印功能进行打印，打印完成后按'Esc'退出"
		                   },
		                   {
		                	   "sExtends" : "xls",
		                	   "sButtonText" : "保存到Excel"
		                   }
		             ]
			 },
			"bJQueryUI" : true,
			"sPaginationType" : "full_numbers",
			"bFilter" : false,
			"aaData" : aaData,
			"aoColumns" : titles,
			"aaSorting" : [ [ 0, "desc" ] ],
			"date-eu-asc" : true,
			"oLanguage" : {
				"sLengthMenu" : "每页 _MENU_ 条记录",
				"sZeroRecords" : "无数据",
				"sInfo" : "当前第 _START_ 到  _END_ 条记录 共_TOTAL_条记录",
				"sInfoEmpty" : " ",
				"oPaginate" : {
					"sFirst" : "首页",
					"sLast" : "末页",
					"sNext" : "下一页",
					"sPrevious" : "上一页"
				}
			},
			"fnFooterCallback" : function(nRow) {
				if (aaData.length > 0) {
					for ( var i = aaData[0].length - size; i < aaData[0].length; i++) {
						var iTotalCount = 0;
						for ( var j = 0; j < aaData.length; j++) {
							iTotalCount += parseInt(aaData[j][i]);
							var nCells = nRow.getElementsByTagName('th');
							nCells[i].innerHTML = iTotalCount;
						}
					}
				}
			}
		} ); 

	
	};
	
	return {
		renderStatDataTable : function() {
		    dataTableCTX.setTarget(_dataTableTarget);
		    dataTableCTX.addLoading(_dataTableTarget);
			var timeScale = "d";
			var startDateFormat = Date.stringToDate(DateShortcutModule.getStartDate());
			var endDateFormat = Date.stringToDate(DateShortcutModule.getEndDate());
			var ap = DateApModule.getAp();
			var customMetrics;
			if(DataTableMetricsModule.getUseOrNot() == "false") {
				customMetrics = ChartMetricsModule.getCheckedQueue();
			}
			else {
				customMetrics = DataTableMetricsModule.getCheckedQueue();
			}
			/**
			 * 查询成员馆具体的filter
			 */
			var site = TopMenuModule.getSiteFilter();
			var oat = OatSelectModule.getOatFilter();
			if(ap != "") {
				_ap = ap;
			}
			if(ap == "toph")
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
				if(site != "") {
					if(req.filter != null && req.filter != '')
						req.filter += " AND ";
					req.filter = req.filter + site;
				}
				if(oat != "") {
					if(req.filter != null && req.filter != '')
						req.filter += " AND ";
					req.filter = req.filter + oat;
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
				
				
				if(ap == "topm") {
					req.groups.add("topy");
					req.groups.add("topm");
				}
				else if(ap == "topd") {
					req.groups.add("topy");
					req.groups.add("topm");
					req.groups.add("topd");
				}
				else if(ap == "toph") {
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
			if(ap == "topy"||ap == "topm"||ap == "topd"||ap == "toph"){
				/**
				  *维度为时间,调用dataTableCTX.renderTotalDataTable1函数,展示日期的dataTable.
			     */
				var dataTableBean = provider.dataTableService.getColmunsTimeData(reqlist);
				renderDataTables(dataTableBean,size);
				return;
			}else{
				 var dataTableBean = provider.dataTableService.getColmunsMetricsData(reqlist);
				 renderDataTables(dataTableBean,size);
				 return;
			}
		}
	};
})();