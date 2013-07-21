package com.cnebula.analytics.logserver;

import java.util.concurrent.atomic.AtomicInteger;

import com.cnebula.analytics.common.conf.CAColumn;
import com.cnebula.analytics.common.conf.GeneralCATable;
import com.cnebula.analytics.common.rd.IRCSDataQueryService;

public class GlobalCacheObject {

	//IP  索引，含IP到机构（含省市信息）
	
	//应用系统索引，
	
	//列描述信息
	public GeneralCATable caRecord;
	
	public String[] cols;
	
	public String[] colsDefaultVal;
	
	public String logServerId = "cas";
	
	//log buf A初始用来采集，和B轮换
	public  String[][] bufA;
	
	//log buf B初始用来写库，和A轮换
	public  String[][] bufB;
	
	public AtomicInteger bufACusor;
	public AtomicInteger bufBCursor;
	
	public IRCSDataQueryService dictionService;

	
	private static GlobalCacheObject single;
	
	public GlobalCacheObject() {
	}
	
	public GlobalCacheObject(GeneralCATable caRecord, int bufSize, IRCSDataQueryService dictionService) {
		this.caRecord = caRecord;
		cols = new String[caRecord.colNameSet().size()];
		colsDefaultVal = new String[cols.length];
		int i = 0;
		for (CAColumn c : caRecord.getColumns()){
			cols[i] = c.getColName();
			colsDefaultVal[i] = c.getDefaultValue();
			i++;
		}
		bufA = new String[bufSize][];
		bufB = new String[bufSize][];
		bufACusor = new AtomicInteger(-1);
		bufBCursor = new AtomicInteger(-1);
		this.dictionService = dictionService;
	}
	
	public static GlobalCacheObject getInstance(){
		return single;
	}
	
	public static GlobalCacheObject initSingle(GlobalCacheObject gco){
		return single = gco;
	}
	
	
}
