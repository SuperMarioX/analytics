package com.cnebula.analytics.common.conf;

import com.cnebula.common.annotations.xml.XMLMapping;

public class CAColumn {

	protected int id = 0;

	protected String name = "";

	protected boolean prime = false;

	protected boolean nullAble = true;

	protected double length = 0;

	protected String formatPattern = "";

	protected CAColumnTypes type = CAColumnTypes.UNKNOWN;

	protected String colName = "";

	protected String description = "";
	
	
	protected String shortDesc;

	protected CAColumnDictionary dictionary = null;
	
	protected String defaultValue = null;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isPrime() {
		return prime;
	}

	public void setPrime(boolean prime) {
		this.prime = prime;
	}

	public boolean isNullAble() {
		return nullAble;
	}

	public void setNullAble(boolean nullAble) {
		this.nullAble = nullAble;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public String getFormatPattern() {
		return formatPattern;
	}

	public void setFormatPattern(String formatPattern) {
		this.formatPattern = formatPattern;
	}

	public CAColumnTypes getType() {
		return type;
	}

	public void setType(CAColumnTypes type) {
		this.type = type;
	}

	public String getColName() {
		return colName;
	}

	public void setColName(String colName) {
		this.colName = colName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public CAColumnDictionary getDictionary() {
		return dictionary;
	}

	public void setDictionary(CAColumnDictionary dictionary) {
		this.dictionary = dictionary;
	}
	
	@XMLMapping(tag="default")
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public String getShortDesc() {
		return shortDesc;
	}
	
	public void setShortDesc(String shortDesc) {
		this.shortDesc = shortDesc;
	}
}
