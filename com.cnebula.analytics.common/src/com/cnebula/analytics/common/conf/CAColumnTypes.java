package com.cnebula.analytics.common.conf;

/**
 * 
 * 一条日志记录的，对唯一约束，
 * 外键约束较弱，因此不支持外键类型。
 * <p>
 * 外键类型的可以由数据字典描述
 * {@link com.cnebula.analytics.common.conf.CAColumnDictionary}
 * </p>
 * @author sandor
 */
public enum CAColumnTypes {

	UNKNOWN(-1,"UNKNOWN"), TEXT(0,"TEXT"), NUMBER(1,"NUMBER"), DATE(2,"DATE"), INT(3, "INT");

	private final int index;
	private final String string;

	private CAColumnTypes(int ti,String ts) {
		this.index = ti;
		this.string = ts;
	}

	public static int valueOf(CAColumnTypes type){
		for(CAColumnTypes clct: values()){
			if(clct.equals(type)){
				return clct.index;
			}
		}
		return UNKNOWN.index;
	}
	
	public static String stringOf(CAColumnTypes type){
		for(CAColumnTypes clct: values()){
			if(clct.equals(type)){
				return clct.string;
			}
		}
		return UNKNOWN.string;
	}
	
	public int getIndex(){
		return index;
	}
	
	public String getString(){
		return string;
	}

	public String toString() {
		return string;
	}
}
