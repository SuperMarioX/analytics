
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


java.util.ArrayList.prototype.indexOf = function(obj) {
	for(var i=0; i<this.values.length; i++)
		if(this.values[i] == obj)
			return i;
	return -1;
}

java.util.Map.prototype.putAdd = function(k, v) {
	var i = this.find(k);
	var table = this.table;
	if (i == -1) {
		table.push(new java.util.Map.Entry(k,v));
	}
	else {
		this.table[i].value += v;
	}
}


/**通用数据结构和函数**/
var common = {};
common.util = function() {
	this.initialize.apply(this, arguments);
};
//common.report保存和报表相关的数据结构和函数
common.report = function() {
	this.initialize.apply(this, arguments);
};

common.report.newDataExportRequest = function() {
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
		groups : new java.util.ArrayList(),
		id : ''
	};
};

//全局的观察者监听模块，可用于对象之间的消息传递
var ObsEvent = (function(){
    var events = {},
    registerEvent = function(eName, scope, handler, params){
        events[eName] = events[eName] || [];
        events[eName].push({
            scope: scope || this,
            handler: handler,
            params:	params
        });
    },
    removeEvent = function(eName, scope, handler){
    	var fns = events[eName];
        scope = scope || this;
        if(!fns) return;
        events[eName] = events[eName].filter(function(fn){
            return fn.scope!=scope || fn.handler!=handler
        });
    },
    removeAllEvent = function(eName){
    	var fns = events[eName];
        if(!fns) return;
        events[eName] = [];
    },
    triggerEvent = function(eventName,params){
        var fns = events[eventName],i,fn;
        if(!fns) return;
        for(i=0;fn=fns[i];i++){
        	if(fn.params){
        		var dates = [];
        		for(var j=0;j<fn.params.length;j++){
        			dates[j] = fn.params[j];
        			if(jQuery.isFunction( dates[j] )){
        				var v = dates[j].call();//function类型调用
        				dates[j] = v;
        			}
        		}
        		fn.handler.apply(fn.scope,dates||[]);
        	}else{
        		fn.handler.apply(fn.scope,params||[]);
        	}
            
        }
    };
    return {
        listen: registerEvent,
        ignore: removeEvent,
        trigger: triggerEvent,
        ignoreAll : removeAllEvent
    }
})();
/**
 * 数组去除重复的元素,支持各种格式的重复的数组成员
 */
common.distinctArrayAll = function(arr) {
	var isEqual=function(obj1,obj2){
	//两个对象地址相等，必相等
	if(obj1===obj2){
	return true;
	}
	if(typeof(obj1)==typeof(obj2)){
	if(typeof(obj1)=="object"&&typeof(obj2)=="object"){
	var pcount=0;
	for(var p in obj1){
	pcount++;
	if(!isEqual(obj1[p],obj2[p])){
	return false;
	}
	}
	for(var p in obj2){
	pcount--;
	}
	return pcount==0;
	}else if(typeof(obj1)=="function"&&typeof(obj2)=="function"){
	if(obj1.toString()!=obj2.toString()){
	return false;
	}
	}else {
	if(obj1!=obj2){
	return false;
	}
	}
	}else{
	return false;
	}
	return true;
	}
	var temp=arr.slice(0);//数组复制一份到temp
	for(var i=0;i<temp.length;i++){
	for(j=i+1;j<temp.length;j++){
	if(isEqual(temp[j],temp[i])){
	temp.splice(j,1);//删除该元素
	j--;
	}
	}
	}
	return temp;
	};
