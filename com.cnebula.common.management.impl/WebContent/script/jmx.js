var ES;
if (!ES) {
	ES = {};
};

(function() {
	if (!$) {
		// need jquery
		throw "need jquery";
	}
	if (!$.plot) {
		throw "need fplot";
	}
	if (!ES.jmx) {
		ES.jmx = function() {
			var _this = this;
			this.jmxService = null;
			this.domains = null;
			this.mbNames = null;
			this.domainMap = null;
			this.init = function() {
				try {
					_this.jmxService = _this.getJMXQueryService();
				} catch (e) {
					_this.openDialog(e);
				}
				if (_this.jmxService != null) {
					_this.domainMap = _this.jmxService.queryAllMBean();
					_this.domains = _this.domainMap.keySet();
					_this.mbNames = new java.util.HashMap();
					for ( var i = 0; i < _this.domains.length; i++) {
						var names = _this.domainMap.get(_this.domains[i]);
						for ( var j = 0; j < names.size(); j++) {
							var n = names.get(j);
							_this.mbNames.put(n.canonicalName, n);
						}
					}
				}
			};
			this.init();
		}
		ES.jmx.prototype.$$ = function(tag, id) {
			var e = document.createElement(tag);
			if (id)
				e.id = id;
			return e;
		};
		ES.jmx.prototype.openDialog = function(msg) {
			if (msg.message) {
				msg = msg.message;
			}
			if (!document.getElementById("dialog")) {
				var ddiv = this.$$("div", "dialog");
				ddiv.style.display = 'none';
				document.body.appendChild(ddiv);
			}
			$('#dialog').html(msg);
			$.fx.speeds._default = 500;
			$(function() {
				$("#dialog").dialog({
					autoOpen : false,
					title : "Dialog",
					show : "blind",
					hide : "explode",
					modal : true,
					height : 400,
					width : 600
				});
			});
			$("#dialog").dialog("open");
		};

		ES.jmx.prototype.getJMXQueryService = function() {
			return EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.common.management.IJMXQueryService");
		};
		ES.jmx.prototype.getMBInfo = function(mbname) {
			var mbinfo = null;
			try {
				mbinfo = this.jmxService.queryMBeanInfo(mbname);
			} catch (e) {
				this.openDialog(e);
				return null;
			}
			return mbinfo;
		};
		ES.jmx.prototype.getAttributes = function(mbname, attrnames) {
			var rst = null;
			try {
				rst = this.jmxService.getAttributes(mbname, attrnames);
			} catch (e) {
				this.openDialog(e);
				return rst;
			}
			return rst;
		};
		ES.jmx.prototype.invoke = function(mbname, opname, params) {
			var rst = null;
			try {
				rst = this.jmxService.invoke(mbname, opname, params);
			} catch (e) {
				rst = e
			}
			return rst;
		};
		ES.jmx.prototype.toDisplay = function(obj) {
			if (obj) {
				if (obj._t_) {
					if (obj._t_ == "java.util.List" || obj._t_ == "java.util.ArrayList") {
						var rst = new Array(obj.size());
						for ( var i = 0; i < obj.size(); i++) {
							rst[i] = this.toDisplay(obj.get(i));
						}
						return this.toDisplay(rst);
					}
					if (obj._t_ == "java.util.Map" || obj._t_ == "java.util.HashMap") {
						var rst = new Array(obj.size());
						var keys = obj.keySet();
						for ( var i = 0; i < keys.length; i++) {
							var t = this.toDisplay(obj.get(keys[i]));
							rst[i] = keys[i] + '=' + t
						}
						return this.toDisplay(rst);
					}
					if (obj._t_ == "com.cnebula.common.management.OpenMBCompositeData") {
						return this.toDisplay(obj.keyValue);
					}
					if (obj._t_ == "com.cnebula.common.management.GcInfoCompositeData") {
						return this.toDisplay(obj.gcInfo);
					}
					if (obj._t_ == "com.cnebula.common.management.GcInfo") {
						var a = new Array(4)
						a[0] = 'id=' + obj.id;
						a[1] = 'startTime=' + obj.startTime;
						a[2] = 'endTime=' + obj.endTime;
						a[3] = 'duration=' + obj.duration;
						return this.toDisplay(a);
					}
					if (obj._t_ == "com.cnebula.common.management.MBName") {
						var a = new Array(4);
						a[0] = "canonicalName=" + obj.canonicalName;
						a[1] = "domain=" + obj.domain;
						a[2] = "canonicalKeyPropertyListString=" + obj.canonicalKeyPropertyListString;
						a[3] = "keyPropertyListString=" + obj.keyPropertyListString;
						return this.toDisplay(a);
					}
					if (obj._t_ == "com.cnebula.common.management.OpenMBTabularDataSupport") {
						var a = obj.tabularType;
						return this.toDisplay(a);
					}
					if (obj._t_ == "com.cnebula.common.management.OpenMBTabularType") {
						var a = new Array(4);
						a[0] = "className=" + obj.className;
						a[1] = "description=" + obj.description;
						a[2] = "typeName=" + obj.typeName;
						a[3] = "indexNames=" + this.toDisplay(obj.indexNames);
						return this.toDisplay(a);
					}
					if (obj._t_ == "com.cnebula.common.management.OpenMBCompositeType") {
						var a = new Array(3);
						a[0] = "className=" + obj.className;
						a[1] = "description=" + obj.description;
						a[2] = "typeName=" + obj.typeName;
						return this.toDisplay(a);
					}
				}
				if(obj instanceof Date){
					return obj.toLocaleString();
				}
				if (obj instanceof Array) {
					return '[' + obj.join('<br/>') + ']';
				}
				if(obj instanceof Object){
					var objMap = new java.util.HashMap();
					for(k in obj){
						if(k != 'values' && k.charAt(0) != '_'){
							objMap.put(k,obj[k]);
						}
					}
					return this.toDisplay(objMap);
				}
				return obj;
			} else {
				return null;
			}
		};
		ES.jmx.prototype.getAllDomains = function() {
			return this.domains;
		};
		ES.jmx.prototype.getAllMBNames = function() {
			return this.mbNames;
		};
		ES.jmx.prototype.getDomainMap = function() {
			return this.domainMap;
		};
		ES.jmx.prototype.breakCtxRef = function(obj) {
			for ( var k in obj) {
				obj[k] = null;
			}
			obj = null;
		};
		ES.jmx = new ES.jmx();
	}
})();