/**
 * 当前js的主要功能是分析后台返回的数据结构,拆分数据结构组装成新的数据结构生成jqplot图表. author:suyunlong
 * date:2012.9.5
 */
var chartCTX = {};
chartCTX.renderChart = function(funcId) {
	ChartReportModule.renderStatChart();
};

chartCTX.addLoading = function(chartTarget) {
	$('#' + chartTarget).html("");
	$('#' + chartTarget).addClass("loading");
};

var ChartReportModule = (function() {
	var _realTimeInterval = null;	//实时分析的setInterval() 返回的 ID
	var _todayInterval = null;		//今日统计的setInterval() 返回的 ID
	var _target = "chart";
	var _chartObj = null;
	var _ap = "";
	// 图表中显示的时间格式
	var _timeFormats = {
		"toph" : {
			day : '%H',
			hour : '%H',
		},
		"topd" : {
			hour : "",
			day : '%m月%e日',
			month : '%m月%e日',
			year : '%m月%e日',
		},
		"topm" : {
			day : '%y年%m月',
			month : '%y年%m月',
			year : '%y年%m月'
		}
	};
	// 图标中的点的最小间隔
	var _minTimeInterval = {
		"topMin" : 60 * 1000,
		"toph" : 3600 * 1000,
		"topd" : 24 * 3600 * 1000,
		"topm" : 30 * 24 * 3600 * 1000
	};
	// 图标提示文本中的x轴显示内容
	var _xDateFormat = {
		"toph" : "<b>%H时</b>",
		"topd" : "<b>%m月%e日</b>",
		"topm" : "<b>%y年%m月</b>"
	};

	renderPieChart = function(dataBean) {
		var seriesData = eval("[" + dataBean.series + "]");

		_chartObj = new Highcharts.Chart({
			chart : {
				renderTo : _target,
				plotBackgroundColor : null,
				plotBorderWidth : null,
				plotShadow : false,
				type : 'pie'
			},
			title : {
				text : "<br>"
			},
			exporting : {
				enabled : "false"
			},
			tooltip : {
				pointFormat : '{series.name}: <b>{point.percentage}%</b>',
				percentageDecimals : 2
			},
			plotOptions : {
				pie : {
					allowPointSelect : true,
					cursor : 'pointer',
					dataLabels : {
						enabled : true
					}
				}
			},
			series : seriesData
		});
	};
	renderDetPieChart = function(dataBean) {
		var seriesData = eval(dataBean.series);

		var colors = Highcharts.getOptions().colors;
		_chartObj = new Highcharts.Chart({
			chart : {
				renderTo : 'chart',
				plotBackgroundColor : null,
				plotBorderWidth : null,
				plotShadow : false,
				type : 'pie'
			},
			title : {
				text : "<br>"
			},
			exporting : {
				enabled : "false"
			},
			tooltip : {
				pointFormat : '{series.name}: <b>{point.percentage}%</b>',
				percentageDecimals : 2
			},
			plotOptions : {
				pie : {
					allowPointSelect : true,
					cursor : 'pointer',
					dataLabels : {
						enabled : true
					}
				}
			},
			series : [ {
				name : dataBean.category[0],
				data : eval("[" + dataBean.series[0] + "]"),
				size : '60%',
				dataLabels : {
					formatter : function() {
						return this.y > 5 ? this.point.name : null;
					},
					color : 'white',
					distance : -30
				}
			}, {
				name : dataBean.category[1],
				data : eval("[" + dataBean.series[1] + "]"),
				innerSize : '60%',
				dataLabels : {
					formatter : function() {
						// display only if larger than 1
						return this.point.name;
					}
				}
			} ]

		});
	}
	renderLineChart = function(dataBean) {
		var seriesData = eval("[" + dataBean.series + "]");

		_chartObj = new Highcharts.Chart({
			chart : {
				renderTo : _target,
				type : 'line',
				marginRight : 10
			},
			title : {
				text : "<br>"
			},
			exporting : {

			},
			xAxis : {
				type : 'datetime',
				minTickInterval : _minTimeInterval[_ap],
				dateTimeLabelFormats : _timeFormats[_ap]
			},
			yAxis : {
				min : 0,
				minTickInterval : 1,
				title : {
					text : ""
				}
			},
			tooltip : {
				xDateFormat : _xDateFormat[_ap],
				shared : true
			},

			series : seriesData
		});
	};
	renderTodayChart = function(oatenOascID, metric,todayData,oat,title) {
		var seriesData = eval("[" + todayData + "]");
		
		_chartObj = new Highcharts.Chart({
			chart : {
				renderTo : _target,
				type : 'spline',
				marginRight : 10,
				events : {
					load : function() {

						var series = this.series[0];

						_todayInterval = setInterval(
								function() {
									var pointData;
									if (metric == 'pageView') {
										pointData = provider.getPVTodayY(oatenOascID,oat);
									} else {
										pointData = provider.getVTodayY(oatenOascID,oat);
									}
									seriesData[pointData[0]][1] = pointData[1];
									series.setData(seriesData);
									
								}, 5 * 60 * 1000);
					}
				}
			},
			title : {
				text : ''
			},
			xAxis : {
				type : 'datetime',
				minTickInterval : _minTimeInterval["toph"],
				dateTimeLabelFormats : _timeFormats["toph"]
			},
			yAxis : {
				title : {
					text : ''
				},
				minTickInterval : 1,
				min : -1,
				plotLines : [ {
					value : 0,
					width : 1,
					color : '#808080'
				} ]
			},
			tooltip : {
				formatter : function() {
					return '<b>'
							+ Highcharts
									.dateFormat('%H'+'时', this.x)
							+ '<br/>' 
							+ this.series.name + ' : '
							+ Highcharts.numberFormat(this.y, 0);
				}
			},
			legend : {
				enabled : false
			},
			exporting : {
				enabled : false
			},
			series : [ {
				name : title,
				data : seriesData
			} ]
		});
	};
	renderDynamicChart = function(oatenOascID, metric, dataBean, oat,title) {
		var seriesData = eval("[" + dataBean.data + "]");
		var endMin = parseInt(dataBean.endMin);
		_chartObj = new Highcharts.Chart({
			chart : {
				renderTo : _target,
				type : 'spline',
				marginRight : 10,
				events : {
					load : function() {

						var series = this.series[0];

						_realTimeInterval = setInterval(
								function() {
									var pointData;
									if (metric == 'pageView') {
										var pointData = provider.getPVXY(
												oatenOascID,++endMin, oat);
									} else {
										var pointData = provider.getVXY(
												oatenOascID,++endMin, oat);
									}
									var point = eval(pointData);
									series.addPoint(point, true, true);
								}, 60 * 1000);
					}
				}
			},
			title : {
				text : ''
			},
			xAxis : {
				type : 'datetime',
				minTickInterval : _minTimeInterval["topMin"],
				dateTimeLabelFormats : { // don't display the dummy year
					minute : '%H:%M',
				}
			},
			yAxis : {
				title : {
					text : ''
				},
				minTickInterval : 1,
				plotLines : [ {
					value : 0,
					width : 1,
					color : '#808080'
				} ]
			},
			tooltip : {
				formatter : function() {
					return '<b>'
							+ Highcharts
									.dateFormat('%H时%M分', this.x)
							+ '<br/>' 
							+ this.series.name + ' : '
							+ Highcharts.numberFormat(this.y, 0);
				}
			},
			legend : {
				enabled : false
			},
			exporting : {
				enabled : false
			},
			series : [ {
				name : title,
				data : seriesData
			} ]
		});
	}
	renderBarChart = function(dataBean) {
		var seriesData = eval("[" + dataBean.series + "]");
		var categoryData = eval("[" + dataBean.category + "]");
		var enabled = false;
		if (categoryData[0].category.length > 0) {
			enabled = true;
		}
		_chartObj = new Highcharts.Chart({
			chart : {
				renderTo : _target,
				type : 'column'
			//     margin: [ 50, 50, 100, 80]
			},
			title : {
				text : "<br>"
			},
			exporting : {
				enabled : "false"
			},
			xAxis : {
				categories : categoryData[0].category,
				labels : {
					rotation : -15,
					align : 'right',
					style : {
						fontSize : '13px',
						fontFamily : 'Verdana, sans-serif'
					}
				}
			},
			yAxis : {
				min : 0,
				title : {
					text : ''
				},
				allowDecimals : false
			},
			legend : {
				enabled : enabled,
			},
			plotOptions : {
				column : {
					pointPadding : 0.2,
					borderWidth : 0,
					pointWidth : 30
				}
			},
			tooltip : {
				formatter : function() {
					return '<b>' + seriesData[0].name + this.y + '</b><br/>'
							+ this.x;
				}
			},
			series : seriesData
		});
	};
	destory = function(){
		if(_realTimeInterval){
			clearInterval(_realTimeInterval);
			_realTimeInterval = null;
		}
		if(_todayInterval){
			clearInterval(_todayInterval);
			_todayInterval = null;
		}
	};

	return {
		renderStatChart : function() {

			if (ChartMetricsModule.getUseOrNot() == "false") {
				return;
			}

			chartCTX.addLoading(_target);
			var timeScale = "d";

			var startDateFormat = Date.stringToDate(DateShortcutModule
					.getStartDate());
			var endDateFormat = Date.stringToDate(DateShortcutModule
					.getEndDate());
			var ap = DateApModule.getAp();
			var oat = OatSelectModule.getOatFilter();
			var chartMetrics = ChartMetricsModule.getCheckedQueue();
			/**
			 * 查询成员馆具体的filter
			 */
			var site = TopMenuModule.getSiteFilter();

			var chartType = "";

			if (ap != "") {
				_ap = ap;
			}
			if (ap == "toph")
				timeScale = "h";

			var reqlist = new java.util.ArrayList();
			if (chartMetrics.size() == 0) {
				alert("图表指标为空,图表统计失败");
				return;
			}
			for ( var i = 0; i < chartMetrics.size(); i++) {
				var metrics = chartMetrics.get(i);
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
				if (site != "") {
					if (req.filter != null && req.filter != '')
						req.filter += " AND ";
					req.filter = req.filter + site;
				}
				if (oat != "") {
					if (req.filter != null && req.filter != '')
						req.filter += " AND ";
					req.filter = req.filter + oat;
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

				if (ap == "topm") {
					req.groups.add("topy");
					req.groups.add("topm");
				} else if (ap == "topd") {
					req.groups.add("topy");
					req.groups.add("topm");
					req.groups.add("topd");
				} else if (ap == "toph") {
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

			destory();
			if (chartType == "line") {
				/**
				 *处理曲线图情况
				 */
				var dataBean = provider.chartReportService
						.getLineChart(reqlist);
				renderLineChart(dataBean);

				return;
			} else if (chartType == "pie") {
				/**
				 *处理饼状图情况
				 */
				var dataBean = provider.chartReportService.getPieChart(reqlist);
				if (dataBean.series.length == 1) {
					renderPieChart(dataBean);
				} else if (dataBean.series.length == 2) {

					renderDetPieChart(dataBean);

				}

				/*if(dataBean.series.length == 1) {
					简单饼状图
					renderPieChart(dataBean);
				}
				else if(dataBean.series.length == 2) {
					一层下钻的饼状图，或双指标饼状图
					renderPieChartWithDrillDown(dataBean);
				}*/

				return;
			} else if (chartType == "Bar") {
				/**
				 *处理柱状图情况
				 */
				var dataBean = provider.chartReportService.getBarChart(reqlist);
				renderBarChart(dataBean);

				return;
			}

		},
		renderRealTimeChart : function() {
			var oatenOascID;
			var site = TopMenuModule.getSiteFilter();
			if(site == ''){
				oatenOascID = 'all';
			}else if(site == "oasc<> '100000'"){
				oatenOascID = 'allSaas';
			}else{
				var zhengze = /^\w+='(\d+)'$/;
				zhengze.test(site);
				oatenOascID = RegExp.$1;
			}
			if (ChartMetricsModule.getUseOrNot() == "false") {
				return;
			}
			destory();
			var chartMetrics = ChartMetricsModule.getCheckedQueue();
			var _oat = OatSelectModule.getOat();
			var index = chartMetrics.get(0).name;
			var title = chartMetrics.get(0).title;
			if (index == 'pageView') {
				var PVSeriesData = provider.getPVSeriesData(oatenOascID, _oat);
				renderDynamicChart(oatenOascID, index, PVSeriesData, _oat,title);//index 指标
			} else {
				var VSeriesData = provider.getVSeriesData(oatenOascID,_oat);
				renderDynamicChart(oatenOascID, index, VSeriesData, _oat,title);
			}

		},
		renderTodayDataChart : function() {
			var oatenOascID;
			var site = TopMenuModule.getSiteFilter();
			if(site == ''){
				oatenOascID = 'all';
			}else if(site == "oasc<> '100000'"){
				oatenOascID = 'allSaas';
			}else{
				var zhengze = /^\w+='(\d+)'/;
				zhengze.test(site);
				oatenOascID = RegExp.$1;
			}
			if (ChartMetricsModule.getUseOrNot() == "false") {
				return;
			}
			destory();
			var chartMetrics = ChartMetricsModule.getCheckedQueue();
			var _oat = OatSelectModule.getOat();
			var index = chartMetrics.get(0).name;
			var title = chartMetrics.get(0).title;
			if (index == 'pageView') {
				var PVTodayData = provider.getPVTodayData(oatenOascID,_oat);
				renderTodayChart(oatenOascID, index,PVTodayData,_oat,title);
			} else {
				var VTodayData = provider.getVTodayData(oatenOascID,_oat);
				renderTodayChart(oatenOascID, index,VTodayData,_oat,title);
			}

		}
	};
})();