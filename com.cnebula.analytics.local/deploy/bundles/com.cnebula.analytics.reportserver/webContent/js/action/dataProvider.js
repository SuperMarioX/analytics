
if (!EasyServiceClient) {
	throw "需要EasyService平台远程服务调用JS客户端支持";
}
if (!ejs) {
	throw "需要EasyService平台模板支持";
}

java.util.List.prototype.intersection = function(obj) {
	var res = new Array();
	var self = this.values;
	if(!self || !obj)
		return res;
	obj = obj.toArray();
	self.sort();
	obj.sort();
	var i=0, j=0;
	while(i<self.length && j<obj.length) {
		if(self[i] == obj[j]) {
			res.push(self[i]);
			i++;
			j++;
		}
		else if(self[i] < obj[j])
			i++;
		else if(self[i] > obj[j])
			j++;
	}
	return res;
}


var provider = {};

provider.__tpltCache = new java.util.HashMap();
provider.reportService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.reportservice.ICAReportService");
provider.rcsDataQueryService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.common.rd.IRCSDataQueryService");
provider.menuDataQueryService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.common.IMenuQueryService");
provider.chartReportService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.reportservice.IChartService");
provider.realTimeService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.logservice.IRealTimeDataQueryService");
provider.dataTableService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.reportservice.IDataTableService");
provider.reportorService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.reportservice.IReportorService");

provider.newDataExportRequest = function() {
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
		matrixKeys : new java.util.ArrayList(),
		groups : new java.util.ArrayList(),
		id : '',
		chartTitle :'',
		description :''
	};
};


// 提供模板
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



provider.getPVSeriesData = function(oatenOascID,oat) {
	return provider.realTimeService.getPVSeriesData(oatenOascID,oat);
};

provider.getVSeriesData = function(oatenOascID,oat) {
	return provider.realTimeService.getVSeriesData(oatenOascID,oat);
};

provider.getPVXY = function(oatenOascID,qtMinute , oat) {
	return provider.realTimeService.getPVXY(oatenOascID,qtMinute , oat);
};

provider.getVXY = function(oatenOascID,qtMinute , oat) {
	return provider.realTimeService.getVXY(oatenOascID,qtMinute , oat);
};

provider.getPVTodayData = function(oatenOascID,oat) {
	return provider.realTimeService.getPVTodayData(oatenOascID,oat);
};

provider.getVTodayData = function(oatenOascID,oat) {
	return provider.realTimeService.getVTodayData(oatenOascID,oat);
};

provider.getPVTodayY = function(oatenOascID,oat) {
	return provider.realTimeService.getPVTodayY(oatenOascID,oat);
};

provider.getVTodayY = function(oatenOascID,oat) {
	return provider.realTimeService.getVTodayY(oatenOascID,oat);
};
