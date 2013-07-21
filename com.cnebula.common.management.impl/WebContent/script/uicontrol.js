var nameTreeTplt = ejs.getNodeText("./tplt/nametree.tplt");
var attrtableTplt = ejs.getNodeText("./tplt/attrtable.tplt");
var introduceTplt = ejs.getNodeText("./tplt/introduce.tplt");

function readerNames() {
	var ctx = EasyServiceClient.newContext();
	ctx.domainMap = ES.jmx.getDomainMap();
	ctx.domains = ES.jmx.getAllDomains();
	ejs.renderComplexNode(nameTreeTplt, "#names", ctx);
	$(function() {
		$("#names").accordion({
			collapsible : true,
			autoHeight : false
		});
	});
}

function renderAttributes(mbname) {
	var mbinfo = ES.jmx.getMBInfo(mbname);
	if (mbinfo) {
		var ctx = EasyServiceClient.newContext();
		ctx.mbname = mbname;
		ctx.attributes = mbinfo.attributes;
		ctx.operations = mbinfo.operations;
		ctx.className = mbinfo.className;
		var attrnames = new Array(ctx.attributes.size());
		for ( var i = 0; i < ctx.attributes.size(); i++) {
			var a = ctx.attributes.get(i);
			attrnames[i] = a.name;
		}
		ctx.attrnames = attrnames;
		var attrvalues = ES.jmx.getAttributes(mbname, attrnames);
		if (attrvalues) {
			if (attrvalues.values) {
				ctx.attrvalues = new Array(attrvalues.values.length);
				for ( var i = 0; i < attrvalues.values.length; i++) {
					ctx.attrvalues[i] = ES.jmx.toDisplay(attrvalues.values[i]);
				}
			} else {
				ctx.attrvalues = null;
			}
		} else {
			ctx.attrvalues = null;
		}
		ejs.renderComplexNode(attrtableTplt, "#attrs", ctx);
	}
}

function readerIntroduce() {
	var ctx = EasyServiceClient.newContext();
	ejs.renderComplexNode(introduceTplt, "#introduce", ctx);
}

function invoke(mbname, opname, form) {
	var inputs = null;
	var is = [];
	if (form instanceof Array) {
		is = form;
	} else if (form) {
		inputs = form.getElementsByTagName("input");
		for ( var i = 0; i < inputs.length; i++) {
			var input = inputs[i];
			if (input.type == "text") {
				is[i] = input.value;
			}
		}
	}
	var r = ES.jmx.invoke(mbname, opname, is);
	var rst = ES.jmx.toDisplay(r);
	if (!rst) {
		rst = "<结果为空>";
	}
	ES.jmx.openDialog(rst);
	return false;
};

var Monitor = function() {
	var _this = this;
	this.monitorMap = new java.util.HashMap();
	this.monitorTimer = null;
	this.monitorInterval = 20000;
	this.tplt = ejs.getNodeText('./tplt/monitor.tplt');

	this.setInterval = function(interval) {
		_this.monitorInterval = interval;
	};
	this.confirmTimeInterval = function() {
		var ti = document.getElementById("timeInterval").value;
		try {
			ti = parseInt(ti);
			if (ti < 10000) {
				alert("Too small to accept.");
				return;
			}
		} catch (e) {
			alert("Wrong number.");
			return;
		}
		_this.stop();
		_this.monitorInterval = ti;
		_this.start();
		_this.readerMonitor();
	};
	this.start = function() {
		_this.monitorTimer = setInterval(_this.readerMonitor, _this.monitorInterval);
		_this.readerMonitor();
	};
	this.stop = function() {
		clearInterval(_this.monitorTimer);
		_this.monitorTimer = null;
	};
	this.readerMonitor = function() {
		var ctx = EasyServiceClient.newContext();
		if (_this.monitorMap.size() != 0) {
			var bs = new EasyServiceClient.BatchCall(ctx);
			var service = "com.cnebula.common.management.IJMXQueryService";
			var method = "getAttributes";

			var mbnames = _this.monitorMap.keySet();
			var mbnattrmap = new java.util.HashMap();
			var mbnvaluemap = new java.util.HashMap();
			for ( var i = 0; i < mbnames.length; i++) {
				var mbname = mbnames[i];
				var attrnames = _this.monitorMap.get(mbname).map.keySet();
				mbnattrmap.put(mbname, attrnames);
				bs.add(i, service, method, [ mbname, attrnames ]);
			}
			bs.execute();
			for ( var i = 0; i < mbnames.length; i++) {
				var mbname = mbnames[i];
				var attrvalues = ctx[i];
				if (attrvalues) {
					if (attrvalues.values) {
						var avs = new Array(attrvalues.values.length);
						for ( var j = 0; j < attrvalues.values.length; j++) {
							avs[j] = ES.jmx.toDisplay(attrvalues.values[j]);
						}
						mbnvaluemap.put(mbname, avs);
					}
				}
			}
			ctx.mbnattrmap = mbnattrmap;
			ctx.mbnvaluemap = mbnvaluemap;
			ejs.renderComplexNode(_this.tplt, "#monitor", ctx);
		} else {
			ctx.mbnattrmap = new java.util.HashMap();
			ctx.mbnvaluemap = new java.util.HashMap();
			ejs.renderComplexNode(_this.tplt, "#monitor", ctx);
		}
	};
	this.remove = function(mbname, attrname) {
		var attrs = _this.monitorMap.get(mbname);
		if (attrs) {
			attrs.remove(attrname);
		}
		_this.confirmTimeInterval();
		return false;
	};
	this.add = function(mbname, attrname) {
		if (!_this.monitorMap.containsKey(mbname)) {
			var attrs = new java.util.Set();
			attrs.add(attrname);
			_this.monitorMap.put(mbname, attrs);
		} else {
			var attrs = _this.monitorMap.get(mbname);
			if (!attrs.contains(attrname)) {
				attrs.add(attrname);
			}
		}
	};
}

var SamplesRender = function() {
	var _this = this;
	var poolTotalSizeGraphData = [];
	var poolAvailableSizeGraphData = [];
	var sessionGraphData = [];
	var threadsGraphData = [];
	var connectionsGraphData = [];
	var requestsGraphData = [];
	var connectionsOpenMaxGraphData = [];

	var timer = null;

	var sampleObj = {
		'java.lang:type=OperatingSystem' : [ 'Name', 'TotalPhysicalMemorySize', 'TotalSwapSpaceSize', 'FreePhysicalMemorySize', 'FreeSwapSpaceSize','AvailableProcessors' ],
		'java.lang:type=Memory' : [ 'HeapMemoryUsage', 'NonHeapMemoryUsage' ],
		'com.atomikos.datasource.pool:type=ConnectionPoolMBean' : [ 'totalSize', 'availableSize' ],
		'com.cnebula.common.remote.jmx:type=RemoteCallMBean' : [ 'methodCallCosts', 'methodCalledTimes', 'remoteClientCallTimes', 'runningThreads' ],
		'org.mortbay.jetty.servlet:id=0,type=hashsessionmanager' : [ 'sessions' ],
		'org.mortbay.jetty.nio:id=0,type=selectchannelconnector' : [ 'connections', 'requests', 'connectionsOpenMax' ],
		'org.mortbay.thread:id=0,type=boundedthreadpool' : [ 'maxThreads', 'idleThreads', 'threads' ],
		'com.atomikos.icatch.jta:type=Transactions' : [ 'Transactions' ]
	};
	var freshCount = 0;

	this.interv = 5000;/* 5秒刷新一次 */
	this.tmpl = ejs.getNodeText("./tplt/samples.tplt");

	this.refresh = function() {
		var ctx = EasyServiceClient.newContext();
		var bs = new EasyServiceClient.BatchCall(ctx);
		var service = "com.cnebula.common.management.IJMXQueryService";
		var method = "getAttributes";
		for ( var mbname in sampleObj) {
			bs.add(mbname, service, method, [ mbname, sampleObj[mbname] ]);
		}
		bs.execute();
		freshCount++;
		
		//JVM
		var co = ctx['java.lang:type=OperatingSystem'];
		var os = {};
		os.Name = co.get(0);
		os.TotalPhysicalMemorySize = co.get(1);
		os.TotalSwapSpaceSize = co.get(2);
		os.FreePhysicalMemorySize = co.get(3);
		os.FreeSwapSpaceSize = co.get(4);
		os.AvailableProcessors = co.get(5);
		
		var me = ctx['java.lang:type=Memory'];
		var m = {};
		m.heapMax = me.get(0).keyValue.get('max');
		m.heapUse = me.get(0).keyValue.get('used');
		m.heapInit = me.get(0).keyValue.get('init');
		m.noneHeapMax = me.get(1).keyValue.get('max');
		m.noneHeapUse = me.get(1).keyValue.get('used');
		m.noneHeapInit = me.get(1).keyValue.get('init');
		
		// Atomikos
		var p = ctx['com.atomikos.datasource.pool:type=ConnectionPoolMBean'];
		var trans = ctx['com.atomikos.icatch.jta:type=Transactions'];
		var pool = {};
		pool.totalSize = p.get(0);
		pool.availableSize = p.get(1);
		pool.connections = pool.totalSize - pool.availableSize;
		pool.transactions = trans == null || trans.get(0) == null ? 0 : trans.get(0).size();
		poolAvailableSizeGraphData.push([ freshCount, pool.availableSize ]);
		poolTotalSizeGraphData.push([ freshCount, pool.totalSize ]);

		// Remote Call
		var r = ctx['com.cnebula.common.remote.jmx:type=RemoteCallMBean'];
		var remote = {};
		remote.costs = r.get(0);
		remote.times = r.get(1);
		sort(r.get(2));
		remote.clients = r.get(2).table;
		remote.runningThreads = r.get(3).table;
		var avgCosts = new java.util.HashMap();
		var totalCostsSet = remote.costs.keySet();
		for ( var k = 0; k < totalCostsSet.length; k++) {
			var key = totalCostsSet[k];
			var value = remote.costs.get(key) / remote.times.get(key);
			avgCosts.put(key, value);
		}
		sort(avgCosts);
		remote.avgcosts = avgCosts.table;

		// Jetty Session
		var s = ctx['org.mortbay.jetty.servlet:id=0,type=hashsessionmanager'];
		sessionGraphData.push([ freshCount, s.get(0) ]);

		// Jetty Connection
		var c = ctx['org.mortbay.jetty.nio:id=0,type=selectchannelconnector'];
		connectionsGraphData.push([ freshCount, c.get(0) ]);
		requestsGraphData.push([ freshCount, c.get(1) ]);
		connectionsOpenMaxGraphData.push([ freshCount, c.get(2) ]);

		// Jetty Thread
		var t = ctx['org.mortbay.thread:id=0,type=boundedthreadpool'];
		threadsGraphData.push([ freshCount, t.get(2) ]);
		var jetty = {};
		jetty.session = s.get(0);
		jetty.connections = c.get(0);
		jetty.requests = c.get(1);
		jetty.connectionsOpenMax = c.get(2);
		jetty.maxThreads = t.get(0);
		jetty.idleThreads = t.get(1);
		jetty.threads = t.get(2);

		ctx.memery = m;
		ctx.os = os;
		ctx.pool = pool;
		ctx.remote = remote;
		ctx.jetty = jetty;
		try {
			if ($('#pool'))
				$('#pool').remove();
			if ($('#session-g'))
				$('#session-g').remove();
			if ($("#connection-g"))
				$("#connection-g").remove();
		} catch (e) {
		}
		(function() {
			ejs.renderComplexNode(_this.tmpl, "#samples", ctx);

			$.plot($("#pool-g"), [ {
				label : "available",
				data : poolAvailableSizeGraphData
			}, {
				label : "total",
				data : poolTotalSizeGraphData
			} ]);

			$.plot($("#session-g"), [ {
				label : "session",
				data : sessionGraphData
			}, {
				label : "threads",
				data : threadsGraphData
			} ]);

			$.plot($("#connection-g"), [ {
				label : "connections",
				data : connectionsGraphData
			}, {
				label : "requests",
				data : requestsGraphData
			}, {
				label : "connectionsOpenMax",
				data : connectionsOpenMaxGraphData
			} ]);

		})();
		ES.jmx.breakCtxRef(ctx);
		ctx = null;
	};
	this.start = function() {
		timer = setInterval(_this.refresh, _this.interv);
		_this.refresh();
	};
}

function sort(arr) {
	this._this = this;
	this.len = 0;
	if (!arr) {
		return arr;
	}
	if (arr instanceof Array) {
		this.len = arr.length;
	} else if (typeof arr == 'object') {
		this.len = arr.size();
	}
	this.getValue = function(a, i) {
		if (a instanceof Array) {
			return a[i];
		}
		if (a._t_) {
			if (a._t_ == "java.util.Map" || a._t_ == "java.util.HashMap") {
				return a.table[i];
			}
		}
	};
	this.setValue = function(a, i, o) {
		if (a instanceof Array) {
			a[i] = o;
		}
		if (a._t_) {
			if (a._t_ == "java.util.Map" || a._t_ == "java.util.HashMap") {
				a.table[i] = o;
			}
		}
	};
	this.compareTo = function(a, b) {
		if (a.key) {
			a = a.value;
			b = b.value;
		}
		if (a > b) {
			return 1;
		} else {
			return -1;
		}
	};
	this.quickSort = function(arr, low, high) {
		var i = low;
		var j = high;
		if (low < high) {
			var key = _this.getValue(arr, low);
			while (i < j) {
				while (i < j && _this.compareTo(_this.getValue(arr, j), key) > 0) {
					j--;
				}
				if (i < j) {
					_this.setValue(arr, i, _this.getValue(arr, j));
					i++;
				}
				while (i < j && _this.compareTo(_this.getValue(arr, i), key) < 0) {
					i++;
				}
				if (i < j) {
					_this.setValue(arr, j, _this.getValue(arr, i));
					j--;
				}
			}
			_this.setValue(arr, i, key);
			_this.quickSort(arr, low, i - 1);
			_this.quickSort(arr, i + 1, high);
		}
	};
	quickSort(arr, 0, len - 1);
}