package com.cnebula.analytics.common.conf;

/**
 * 
 * 这中字典是一一对应的(不强制要求唯一，但不同键有相同值应当是小概率事件)，<br/>
 * 即，小数据量的字典应当保证一一对应，大数据量可以保证大多数情况一一对应，<br/>
 * 同时不限定value的类型。<br/>
 * 
 * 例如，HashMap、一张数据字典表，key，value两列均是唯一键等
 * 
 * @author sandor
 */
public class CAColumnDictionary {

	protected int id = 0;

	protected String className = "";

	protected String dictarget = "";

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getDictarget() {
		return dictarget;
	}

	public void setDictarget(String dictarget) {
		this.dictarget = dictarget;
	}
}
