package com.cnebula.analytics.common.conf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.cnebula.analytics.common.IJDBCManipulateAble;
import com.cnebula.common.annotations.xml.XMLIgnore;
import com.cnebula.common.annotations.xml.XMLMapping;

public class GeneralCATable extends CARecord implements IJDBCManipulateAble {

	public static String DEFAULT_DATE_FORMAT = "yyyyMMdd";

	public static int DEFAULT_NUM_PRECISION = 10;

	public static int DEFAULT_NUM_SCALE = 0;

	public static String DEFAULT_VARCHAR_LENGTH = "32";

	protected String tableName = "";
	
	protected String indexSeq = "";

	private String defaultInsertSql = null;

	@XMLMapping(tag = "table")
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	@XMLMapping(tag = "index")
	public String getIndexSeq() {
		return indexSeq;
	}
	
	public void setIndexSeq(String indexSeq) {
		this.indexSeq = indexSeq;
	}
	
	@Override
	@XMLIgnore
	public String getIndexCreateSQL() {
		if(indexSeq == null || indexSeq == "") {
			return "";
		}
		StringBuilder sqlBuilder = new StringBuilder("CREATE INDEX IF NOT EXISTS ").append(tableName).append("_").append(indexSeq).append(" ON ").append(tableName).append(" ").append("( ");
		String[] col = indexSeq.split("_");
		for(int i = 0; i < col.length; i++) {
			if(i == 0)
				sqlBuilder.append(col[i]);
			else
				sqlBuilder.append(", ").append(col[i]);
		}
		sqlBuilder.append(")");
		return sqlBuilder.toString();
	}

	@Override
	@XMLIgnore
	public String getDefinationSQL() {
		if (tableName == null || "".equals(tableName)) {
			throw new RuntimeException("表名为空，不能产生创建表的脚本。");
		}
		StringBuilder sqlBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" ").append("( ");
		CAColumn priColumn = null;
		for (int i = 0; i < size(); i++) {
			CAColumn col = getColumn(i);
			if (col.isPrime()) {
				priColumn = col;
			}
			String colName = col.getColName();
			CAColumnTypes type = col.getType();
			if (type.equals(CAColumnTypes.INT)) {
				sqlBuilder.append(colName).append(" ").append(" INT ");
				if (col.getDefaultValue() != null) {
					sqlBuilder.append(" default ").append(col.getDefaultValue());
				}
			} else if (type.equals(CAColumnTypes.DATE)) {
				sqlBuilder.append(colName).append(" ").append(" DATE ");
			} else if (type.equals(CAColumnTypes.NUMBER)) {
				String numberDefine = String.valueOf(col.getLength());
				String[] ps = numberDefine.split("\\.");
				int p = DEFAULT_NUM_PRECISION;
				int s = DEFAULT_NUM_SCALE;
				if (ps.length == 1 || ps.length == 2) {
					p = Integer.parseInt(ps[0]);
				}
				if (ps.length == 2) {
					s = Integer.parseInt(ps[1]);
				}
				sqlBuilder.append(colName).append(" ").append("NUMBER (" + p + "," + s + ") ");
				if (col.getDefaultValue() != null) {
					sqlBuilder.append(" default ").append(col.getDefaultValue());
				}
			} else if (type.equals(CAColumnTypes.TEXT)) {
				int len = (int) col.getLength();
				sqlBuilder.append(colName).append(" ").append("VARCHAR(" + len + ") ");
				if (col.getDefaultValue() != null) {
					sqlBuilder.append(" default ").append("'").append(col.getDefaultValue()).append("'");
				}
			} else {
				throw new RuntimeException("不支持的数据类型");
			}
			if (col.isNullAble()) {
				sqlBuilder.append("NULL ");
			} else {
				sqlBuilder.append("NOT NULL ");
			}
			if (i != getColumns().size() - 1)
				sqlBuilder.append(", ");
		}
		if (priColumn != null) {
			sqlBuilder.append(",PRIMARY KEY (").append(priColumn.getColName()).append("))");
		} else {
			sqlBuilder.append(")");
		}
		return sqlBuilder.toString();
	}

	@Override
	@XMLIgnore
	public String getDropSql() {
		return "DROP TABLE " + tableName;
	}

	/**
	 * 如果动态修改了表名，需要重新生成 默认的insertSql语句
	 */
	public void refreshDefaultInsertSql() {
		defaultInsertSql = getDefualtPreparamentInsertSql();
	}

	/**
	 * @param conn
	 *            不能关闭connection
	 * @param values
	 */
	public void persistValuesSortedByColumnId(Connection conn, Object[] values) throws SQLException {
		if (values == null || values.length == 0) {
			return;
		}
		if (defaultInsertSql == null) {
			defaultInsertSql = getDefualtPreparamentInsertSql();
		}
		PreparedStatement stmt = conn.prepareStatement(defaultInsertSql);
		int index = 1;
		for (Object v : values) {
			Object o = null;
			if (getColumn(index - 1).getType().equals(CAColumnTypes.NUMBER)) {
				if (v != null) {
					try {
						o = Long.valueOf(String.valueOf(v));
					} catch (NumberFormatException e) {
						o = null;
					}
				}
			} else if (v instanceof Date) {
				Date d = (Date) v;
				o = new java.sql.Date(d.getTime());
			} else {
				o = v;
			}
			stmt.setObject(index, o);
			index++;
		}
		stmt.execute();
		stmt.close();
	}

	@Override
	@XMLIgnore
	public String getDefualtPreparamentInsertSql() {
		if (size() == 0) {
			return null;
		}
		StringBuilder insertNames = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
		StringBuilder insertParam = new StringBuilder("(");
		for (int id = 0; id < size(); id++) {
			String colName = getColumn(id).getColName();
			insertNames.append(colName);
			insertParam.append("?");
			if (id != (size() - 1)) {
				insertNames.append(",");
				insertParam.append(",");
			}
			if (id == (size() - 1)) {
				insertNames.append(")");
				insertParam.append(")");
			}
		}
		insertNames.append(" VALUES ").append(insertParam);
		return insertNames.toString();
	}

	@Override
	public String getDefualtSelectAllSql() {
		StringBuilder sql = new StringBuilder("SELECT * ");
		sql.append("FROM ").append(tableName);
		return sql.toString();
	}

	@Override
	public String getClearSql() {
		StringBuilder sql = new StringBuilder("delete  ");
		sql.append("FROM ").append(tableName);
		return sql.toString();
	}

	@Override
	public String getCSVColumnHeader() {
		StringBuilder colNameCSV = new StringBuilder();
		List<CAColumn> cs = getColumns();
		for (CAColumn c : cs) {
			String cn = c.getColName();
			colNameCSV.append(cn).append(",");
		}
		colNameCSV.replace(colNameCSV.length() - 1, colNameCSV.length(), "");
		return colNameCSV.toString();
	}
}
