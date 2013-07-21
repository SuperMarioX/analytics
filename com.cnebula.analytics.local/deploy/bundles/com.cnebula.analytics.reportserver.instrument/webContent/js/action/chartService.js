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
	var destory = function(){
		if(_realTimeInterval){
			clearInterval(_realTimeInterval);
			_realTimeInterval = null;
		}
		if(_todayInterval){
			clearInterval(_todayInterval);
			_todayInterval = null;
		}
	}
	return {
		
	renderPieChart : function(dataBean) {
		destory();
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
	},
	renderDetPieChart : function(dataBean) {
		destory();
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
	},
	renderLineChart : function(ap,dataBean) {
		destory();
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
				minTickInterval : _minTimeInterval[ap],
				dateTimeLabelFormats : _timeFormats[ap]
			},
			yAxis : {
				min : 0,
				minTickInterval : 1,
				title : {
					text : ""
				}
			},
			tooltip : {
				xDateFormat : _xDateFormat[ap],
				shared : true
			},

			series : seriesData
		});
	},
	renderTodayChart : function(oatenOascID, metric,todayData,oat,title) {
		destory();
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
	},
	renderDynamicChart : function(oatenOascID, metric, dataBean, oat,title) {
		destory();
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
	},
	renderBarChart : function(dataBean) {
		destory();
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
	}
	};
})();