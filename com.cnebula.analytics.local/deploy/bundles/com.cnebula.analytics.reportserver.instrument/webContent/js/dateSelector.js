var _DS = (function(){
	var Class = {
	  create: function() {
		return function() {
		  this.initialize.apply(this, arguments);
		};
	  }
	};
	var DateSelector = Class.create();
	DateSelector.prototype = {
		yesterday:null,
		$$: function (id) {
			return "string" == typeof id ? document.getElementById(id) : id;
		},
		Extend: function(destination, source) {
			for (var property in source) {
				destination[property] = source[property];
			}
			return destination;
		},
		addEventHandler: function (oTarget, sEventType, fnHandler) {
			if (oTarget.addEventListener) {
				oTarget.addEventListener(sEventType, fnHandler, false);
			} else if (oTarget.attachEvent) {
				oTarget.attachEvent("on" + sEventType, fnHandler);
			} else {
				oTarget["on" + sEventType] = fnHandler;
			}
		},	
	  	initialize: function(oYear, oMonth, oDay, options) {
			this.SelYear = this.$$(oYear);//年选择对象
			this.SelMonth = this.$$(oMonth);//月选择对象
			this.SelDay = this.$$(oDay);//日选择对象
			this.setOptions(options);
			var dt = new Date(), 
				iMonth = parseInt(this.options.Month), 
				iDay = parseInt(this.options.Day), 
				iMinYear = parseInt(this.options.MinYear),
				iMaxYear = parseInt(this.options.MaxYear);
			var yes = this.getYesterday();
			this.Year = yes.getFullYear();
			this.Month =  yes.getMonth() + 1;
			this.Day = yes.getDate();
			
			this.MinYear = iMinYear && iMinYear < this.Year ? iMinYear : this.Year;
			this.MaxYear = iMaxYear && iMaxYear > this.Year ? this.Year : iMaxYear;
			this.onChange = this.options.onChange;
			
			//年设置
			this.setYear();
			//月设置
			this.setMonth();
			//日设置
			this.setDay();
			
			var oThis = this;
			//日期改变事件
			this.addEventHandler(this.SelYear, "change", function(){
				oThis.Year = oThis.SelYear.value; 
				oThis.setMonth();
				oThis.setDay(); 
				oThis.onChange();
			});
			this.addEventHandler(this.SelMonth, "change", function(){
				oThis.Month = oThis.SelMonth.value; 
				oThis.setDay(); 
				oThis.onChange();
			});
			this.addEventHandler(this.SelDay, "change", function(){ 
				oThis.Day = oThis.SelDay.value;
				oThis.onChange(); 
			});
	  	},
	  	getYesterday:function(){
	  		if(this.yesterday == null){
	  			var d = new Date();
	  			this.yesterday = new Date(d.getFullYear(),d.getMonth(),d.getDate() - 1);
	  		}
	  		return this.yesterday;
	  	}, 
	  	//设置默认属性
	  	setOptions: function(options) {
			this.options = {//默认值
				Year:		0,//年
				Month:		0,//月
				Day:		0,//日
				MinYear:	0,//最小年份
				MaxYear:	0,//最大年份
				onChange:	function(){}//日期改变时执行
			};
			this.Extend(this.options, options || {});
	  	},
	  	setYear:function(){
	  		this.setSelect(this.SelYear, this.MinYear, 
	  				this.MaxYear - this.MinYear + 1, this.Year - this.MinYear);
	  	},
	  	setMonth:function(){
	  		var d = this.getYesterday();
	  		if(d.getFullYear() == this.Year){
	  			var curMonth = d.getMonth() + 1;
	  			if(curMonth < this.Month){
	  				this.Month = curMonth;
	  			}
	  			this.setSelect(this.SelMonth, 1, curMonth, this.Month - 1);
	  		}else{
	  			this.setSelect(this.SelMonth, 1, 12, this.Month - 1);
	  		}
	  	},
	  	//日设置
	  	setDay: function() {
			var d = new Date();
			if(d.getFullYear() == this.Year && parseInt(d.getMonth()) + 1 == this.Month){
				var today = d.getDate();
				if(today < parseInt(this.Day) + 1 ){
					this.Day = this.getYesterday().getDate();
				}
				this.setSelect(this.SelDay, 1, today, this.Day - 1);
			}else{
				//取得月份天数
				var daysInMonth = new Date(this.Year, this.Month, 0).getDate();
				if (this.Day > daysInMonth) { 
					this.Day = daysInMonth; 
				};
				this.setSelect(this.SelDay, 1, daysInMonth, this.Day - 1);
			}
	  	},
	  	
	  	/**
	  	 * select设置
	  	 * oSelect,页面元素ID
	  	 * iStart,select里面的起始值：第一个元素的值
	  	 * iLength,select长度
	  	 * iIndex,select中的选中元素的index
	  	 */
	  	setSelect: function(oSelect, iStart, iLength, iIndex) {
			//添加option
			oSelect.options.length = iLength;
			for (var i = 0; i < iLength; i++) { 
				oSelect.options[i].text = oSelect.options[i].value = iStart + i; 
			}
			//设置选中项
			oSelect.selectedIndex = iIndex;
	  	}
	};
	return DateSelector;
}());