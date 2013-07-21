/**
 * 当前js文件主要是为各个应用系统做接口,公共函数可以生成table报表  @date:2012.7.26
 * 可以生成不同的图表，图表的数据主要来自参数data参数，还可以在内存初始化的时候放入一些操作标题,该js文件主要做公共的图表以及table报表展示调用。 author:suyunlong 
**/
var ctx = ejs.newContext();	
//var calis1={};
(function() {
	calis.reportView = function() {
		this.initialize.apply(this, arguments);
	};
/**
	 * 某个应用系统生成table表格接口
	 * @data：系统页面上创建报表时后台返回的数据
	 * @header：table的表头
	 * @tdivId:table对应div的id
	 * @pdivId：table数据的分页对应div的id
	 * @appOpTitle：table数据展示对应的标题
	 * @pageNumber：table数据展示页面上分页的条数
	 **/
	calis.reportView.queryAppTableData = function(data,header,tdivId,pdivId,appOpTitle,pageNumber) {  
		//var req = {
		//startDate : new Date(),
		//endDate : new Date(),
		//dimensions : []
		//}
		//var rows = calis.report.feedDataJSONReq(req);
		var rows=data;
		if(tdivId==""||pdivId==""||header==""||appOpTitle==""||pageNumber==""){
			return;
		}
		document.getElementById(tdivId).innerHTML = "无";
		document.getElementById(pdivId).className = "";
		if (!rows || rows.length == 0) {
			return;
		}
		calis.report.renderTableWithPager({
			tableContainer : '#'+tdivId+'',
			pagerContainer : '#'+pdivId+'',
			headerArray : header,
			obj2DArray : rows,
			caption : appOpTitle,
			perPageRowNum : pageNumber,
			tableClass : "browser",
			pagerClass : "pager",
			printTable : true,
			download : true,
		});
	};
/**
 * 某个应用系统生成曲线图表格接口
 * @data：某个应用系统图表的数据结构（例如当前data数据源为教参具体的查询报表，该曲线图展示的就是教参系统对应的曲线图）
 * @chartTitle图表对应的名称
 * @xName：图表X轴坐标的名称
 * @yName：图表y轴坐标的名称
 * @target：图表在页面上对应的divid
 **/
	calis.reportView.renderCurveBarChart = function(data, chartTitle, xName, yName, target) {
		document.getElementById(target).className = "chart";
		$(document).ready(function(){
			  var cosPoints = data;
			  for (var i=0; i<2*Math.PI; i+=0.1){
			     cosPoints.push([i, Math.cos(i)]);
			  }
			  var plot3 = $.jqplot(target, [cosPoints], { 
			      series:[{showMarker:false}],
			      axes:{
			        xaxis:{
			          label:xName,
			          labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
			          labelOptions: {
			            fontFamily: 'Georgia, Serif',
			            fontSize: '12pt'
			          }
			        },
			        yaxis:{
			          label:yName,
			          labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
			          labelOptions: {
			            fontFamily: 'Georgia, Serif',
			            fontSize: '12pt'
			          }
			        }
			      }
			  });
			});
	};
  /**
 * 某个应用系统生成柱状图表格接口
 * @data：某个应用系统图表的数据结构（例如当前data数据源为教参具体的查询报表，该曲线图展示的就是教参系统对应的柱状图）
 * @chartTitle图表对应的名称
 * @xName：图表X轴坐标的名称
 * @yName：图表y轴坐标的名称
 * @target：图表在页面上对应的divid
 * @图表对应的数据结构样例为：var data = [['登录人次', 7], ['课程信息检索次数', 9], ['教参书检索次数', 15],['课程信息被访问量', 12], ['教参书全文访问量', 3],['馆际互借量（非外教中心）', 6], ['馆际互借量（外教中心）', 18]];
 **/
	calis.reportView.renderColumnBarChart = function(data, chartTitle, xName, yName, target) {
		document.getElementById(target).className = "chart";
		$(document).ready(function(){
			  var plot1b = $.jqplot(target, [data], {
			    title: xName,
			    series:[{renderer:$.jqplot.BarRenderer}],
			    axesDefaults: {
			        tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
			        tickOptions: {
			          fontFamily: 'Georgia',
			          fontSize: '10pt',
			          angle: -30
			        }
			    },
			    axes: {
			      xaxis: {
			        renderer: $.jqplot.CategoryAxisRenderer
			      }
			    }
			  });
			});
	};
	/**
 * 某个应用系统生成饼状图表接口
 * @data：某个应用系统图表的数据结构（例如当前data数据源为教参具体的查询报表，该曲线图展示的就是教参系统对应的饼状图）
 * @chartTitle图表对应的名称
 * @xName：图表X轴坐标的名称
 * @yName：图表y轴坐标的名称
 * @target：图表在页面上对应的divid
 * @图表对应的数据结构样例 var data = [
			    ['登录人次', 12],['课程信息检索次数', 9], ['教参书检索次数', 14],
			    ['课程信息被访问量', 16],['教参书全文访问量', 7], ['馆际互借量（非外教中心）', 9]
			  ];
 **/
	calis.reportView.renderPieBarChart = function(data, chartTitle, xName, yName, target) {
		document.getElementById(target).className = "chart";
		$(document).ready(function(){
			  var plot1 = jQuery.jqplot (target, [data],
			    {
				  title: xName,
			      seriesDefaults: {
			        renderer: jQuery.jqplot.PieRenderer,
			        rendererOptions: {
			          showDataLabels: true
			        }
			      },
			      legend: { show:true, location: 'e' }
			    }
			  );
			});
	};
	/**
 * 某个应用系统生成不同颜色柱状图表接口
 * @data：某个应用系统图表的数据结构（例如当前data数据源为教参具体的查询报表，该函数展示的就是教参系统对应的不同颜色柱状图表）
 * @chartTitle图表对应的名称
 * @xName：图表X轴坐标的名称
 * @yName：图表y轴坐标的名称
 * @target：图表在页面上对应的divid
 * @图表对应的数据结构样例 var data = [
			    [14, 32, 41, 44, 40, 37, 29], [7, 12, 15, 17, 20, 27, 39]
			  ];
 **/
	calis.reportView.renderRoundBarChart = function(data, chartTitle, xName, yName, target) {
		document.getElementById(target).className = "chart";
		$(document).ready(function(){
			  var line1 = data[0];
			  var line2 = data[1];
			  var plot4 = $.jqplot(target, [line1, line2], {
			      title: xName,
			      stackSeries: true,
			      seriesDefaults: {
			          renderer: $.jqplot.BarRenderer,
			          rendererOptions:{barMargin: 25},
			          pointLabels:{show:true, stackedValue: true}
			      },
			      axes: {
			          xaxis:{renderer:$.jqplot.CategoryAxisRenderer}
			      }
			  });
			});
	};
	/**
	 * 在内存中初始化某个应用系统标题,或者对应的操作属性
	 * @date：2012.7.26
	 * @样例数据：ctx.dicModel={
		op: {
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
		},
		ort:{
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
		},
		st:{
			"1" : "本科生",
			"2" : "研究生",
			"3" : "博士",
			"4" : "教师",
			"5" : "留学生",
			"6" : "成人教育",
			"7" : "校外读者",
			"8" : "其它"
		},
	};
	 **/
	calis.reportView.initAppDic = function(dicModel) {
		ctx.mdic = {};
		ctx.dic = {};
		for ( var i = 0; i < ctx.logcols.size(); i++) {
			ctx.mdic[ctx.logcols.get(i).name] = ctx.logcols.get(i).shortDesc;
		}
		ctx.dic["op"] =ctx.dicModel.op;
		ctx.dic["ort"] =ctx.dicModel.ort;
		ctx.dic["st"] =ctx.dicModel.st;
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
	};
})();
