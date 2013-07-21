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

var testConfig = (function(){
	var _isTest = true;
	return {
		getIsTestTimeRange: function(){
			return false;
		},
		getIsTestCollStat: function() {
			return true;
		}
		
	}
})();

var provider = {};

provider.__tpltCache = new java.util.HashMap();
provider.reportService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.reportservice.ICAReportService");
provider.rcsDataQueryService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.common.rd.IRCSDataQueryService");
provider.menuDataQueryService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.common.IMenuQueryService");
provider.chartReportService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.reportservice.IChartService");
provider.realTimeService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.logservice.IRealTimeDataQueryService");
provider.dataTableService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.analytics.reportservice.IDataTableService");
provider.codeNameMap = null;
provider.funcMap = null;
provider.funcInfoMap = null;
provider.oatInfoMap = null;
provider.globalOatList = null;
provider.codeOatListMap = null;
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


provider.getCenterOfLibrary = function(code) {
	return provider.rcsDataQueryService.getCenterOfApp(code);
}

/**
 * 对应的表结构
 * rcs_nid,rcs_n_t,rcs_nc,rcs_nn,rcs_nsn,rcs_npl,rcs_ncl
 * 返回的数组结构
 * {"node100000","SAASCenterInfo","100000","CALIS管理中心","CALIS管理中心","北京","北京市"}
 */
provider.getSaasCenter = function() {
	return provider.rcsDataQueryService.listSaasCenter();
};
/**
 *获取内存中某一共享域下所有成员馆信息
 * */
provider.getNodeInfoData = function(saasCode) {
	return provider.rcsDataQueryService.getLibInfoData(saasCode);
};
// 根据代码返回中文名字，代码可以为共享域或者机构
provider.getNameOfCode = function(code) {
	if(!provider.codeNameMap) {
		provider.codeNameMap = provider.rcsDataQueryService.getNameOfCodeMap();
	}
	return provider.codeNameMap.get(code);
}

// 返回菜单功能支持的应用系统类型的映射表
provider.getFuncMap = function() {
	if(!provider.funcMap) {
		provider.funcMap = provider.menuDataQueryService.getFunctionMap();
	}
	return provider.funcMap;
}

//返回菜单功能父类和子类菜单的关系
provider.getFuncPchildMap = function() {
	
		provider.funcPchildMap = provider.menuDataQueryService.getFuncPchildMap();
		
		return provider.funcPchildMap;
}


//返回各项菜单的配置参数，映射表，key为功能id，value为String[]类型的参数，依次为：
//[0]功能名称，[1]图表指标最大选择数,[2]表格指标最大选择数，[3]是否显示图表，[4]是否显示表格
//[5]处理方式，[6]是否显示时间刻度，[7]默认选择的应用系统类型，[8]图表指标显示名称，[9]支持的站点列表
provider.getFuncInfoMap = function() {
	if(!provider.funcInfoMap) {
		provider.funcInfoMap = provider.menuDataQueryService.getFuncInfoMap();
	}
	return provider.funcInfoMap;
}


//返回oat的name和filter的映射表
provider.getOatInfoNameMap = function() {
	if(!provider.oatInfoMap) {
		provider.oatInfoMap = provider.menuDataQueryService.getOatInfoMap();
	}
	return provider.oatInfoMap;
}




// 返回全站下拥有的应用系统类型
provider.getGlobalOatList = function() {
	return provider.rcsDataQueryService.getGlobalOatList();
}

// 返回某个机构下拥有的应用系统类型
provider.getOatListWithSite = function(site) {
	if(site == "") {
		if(!provider.globalOatList)
			globalOatList =  provider.rcsDataQueryService.getGlobalOatList();
		return globalOatList;
	}
	else {
		if(!provider.codeOatListMap)
			codeOatListMap = provider.rcsDataQueryService.getCodeOatListMap();
		return codeOatListMap.get(site);
	}
}

provider.getTypeOfCode = function(code) {
	return provider.rcsDataQueryService.getTypeOfCode(code);
};

provider.getNodeInfo = function(nodeCode) {
	return provider.rcsDataQueryService.getNodeInfo(nodeCode);
};

provider.getPVSeriesData = function(oatenOascID, oat) {
	return provider.realTimeService.getPVSeriesData(oatenOascID, oat);
};

provider.getVSeriesData = function(oatenOascID, oat) {
	return provider.realTimeService.getVSeriesData(oatenOascID, oat);
};

provider.getPVXY = function(oatenOascID, qtMinute , oat) {
	return provider.realTimeService.getPVXY(oatenOascID, qtMinute , oat);
};

provider.getVXY = function(oatenOascID, qtMinute , oat) {
	return provider.realTimeService.getVXY(oatenOascID, qtMinute , oat);
};

provider.getPVTodayData = function(oatenOascID, oat) {
	return provider.realTimeService.getPVTodayData(oatenOascID, oat);
};

provider.getVTodayData = function(oatenOascID, oat) {
	return provider.realTimeService.getVTodayData(oatenOascID, oat);
};

provider.getPVTodayY = function(oatenOascID, oat) {
	return provider.realTimeService.getPVTodayY(oatenOascID, oat);
};

provider.getVTodayY = function(oatenOascID, oat) {
	return provider.realTimeService.getVTodayY(oatenOascID, oat);
};

provider.SubNameOfCodeMap = new java.util.HashMap();
provider.SubNameOfCodeMap.putAdd("01", "哲学");
provider.SubNameOfCodeMap.putAdd("02", "经济学");
provider.SubNameOfCodeMap.putAdd("03", "法学");
provider.SubNameOfCodeMap.putAdd("04", "教育学");
provider.SubNameOfCodeMap.putAdd("05", "文学");
provider.SubNameOfCodeMap.putAdd("06", "历史学");
provider.SubNameOfCodeMap.putAdd("07", "理学");
provider.SubNameOfCodeMap.putAdd("08", "工学");
provider.SubNameOfCodeMap.putAdd("09", "农学");
provider.SubNameOfCodeMap.putAdd("10", "医学");
provider.SubNameOfCodeMap.putAdd("11", "军事学");
provider.SubNameOfCodeMap.putAdd("12", "管理学");
provider.SubNameOfCodeMap.putAdd("13", "艺术学");
provider.getSubNameOfCode = function(code) {
	return provider.SubNameOfCodeMap.get(code);
}