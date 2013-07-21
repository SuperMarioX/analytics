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
	return {
		
		renderDataTables : function(dataTableBean,size) {
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
	
		
		}
		
	};
})();