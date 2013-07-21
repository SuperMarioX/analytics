package com.cnebula.analytics.common.conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cnebula.common.annotations.xml.XMLIgnore;
import com.cnebula.common.annotations.xml.XMLMapping;

public class CARecord {

	protected List<CAColumn> columns = new ArrayList<CAColumn>();
	protected Map<String, CAColumn> colNameColMap = new HashMap<String, CAColumn>();
	protected Map<String, CAColumn> nameColMap = new HashMap<String, CAColumn>();
	protected Map<Integer, String> idNameMap = new HashMap<Integer, String>();
	protected Map<String, Integer> nameIdMap = new HashMap<String, Integer>();

	@XMLMapping(childTag = "column")
	public List<CAColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<CAColumn> columns) {
		if (columns == null) {
			return;
		}
		this.columns = columns;
		/**
		 * CAColumn ID 是自动生成，在XML中配置无效
		 * */
		int i = 0;
		for (CAColumn c : columns) {
			c.setId(i);
			colNameColMap.put(c.getColName().toLowerCase(), c);
			nameColMap.put(c.getName().toLowerCase(), c);
			idNameMap.put(i, c.getName().toLowerCase());
			nameIdMap.put(c.getName().toLowerCase(), i);
			i++;
		}
	}

	public void addColumn(CAColumn column) {
		int id = size();
		column.setId(id);
		columns.add(column);
		colNameColMap.put(column.getColName().toLowerCase(), column);
		if(column.getName() == null || "".equals(column.getName())){
			column.setName(column.getColName());
		}
		nameColMap.put(column.getName().toLowerCase(), column);
		idNameMap.put(id, column.getName().toLowerCase());
		nameIdMap.put(column.getName().toLowerCase(), id);
	}

	@XMLIgnore
	public CAColumn getColumn(int id) {
		if (columns == null || columns.size() == 0) {
			return null;
		} else {
			String name = idNameMap.get(id);
			if (name == null) {
				return null;
			}
			return nameColMap.get(name);
		}
	}

	public CAColumn columnOf(String name) {
		if (name == null) {
			return null;
		} else {
			return nameColMap.get(name.toLowerCase());
		}
	}

	@XMLIgnore
	public CAColumn getColumn(String colName) {
		if (colName == null) {
			return null;
		} else {
			return colNameColMap.get(colName.toLowerCase());
		}
	}

	public int idOfColumnName(String name) {
		if (name == null) {
			return -1;
		} else {
			Integer id = nameIdMap.get(name.toLowerCase());
			if (id == null) {
				return -1;
			} else {
				return id;
			}
		}
	}

	public Set<String> nameSet() {
		return nameColMap.keySet();
	}

	public Set<String> colNameSet() {
		return colNameColMap.keySet();
	}

	public int size() {
		return columns.size();
	}
}
