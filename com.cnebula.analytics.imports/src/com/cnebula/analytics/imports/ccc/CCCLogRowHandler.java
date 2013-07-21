package com.cnebula.analytics.imports.ccc;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.cnebula.analytics.common.conf.CAColumn;
import com.cnebula.analytics.common.conf.GeneralCATable;
import com.cnebula.analytics.common.rd.IRCSDataQueryService;
import com.cnebula.analytics.imports.common.FileOutputRowHandler;

public class CCCLogRowHandler extends FileOutputRowHandler {

	private static final int LIMIT_COUNT = 20000;

	public static final String FIELD_SEPARATOR = "\t";

	protected IRCSDataQueryService rd = null;
	protected GeneralCATable table = null;
	protected String colNameCSVHeader = null;
	private int limitCount = 0;
	private long limitStart = -1L;

	public CCCLogRowHandler(String dir, IRCSDataQueryService rd, GeneralCATable table) {
		super(dir);
		this.rd = rd;
		this.table = table;
		if (table != null) {
			colNameCSVHeader = table.getCSVColumnHeader();
			colNameCSVHeader = colNameCSVHeader.replaceAll(",", FIELD_SEPARATOR);
			this.header = new StringBuilder(colNameCSVHeader);
			header.append(ROW_SEPARATOR);
		}
	}

	@Override
	public void handle(String[] colNames, String[] colValues) {
		if (rd == null) {
			throw new RuntimeException("需要RCS数据的支持");
		}
		if (limitCount == 0) {
			limitStart = System.currentTimeMillis();
		}

		String dayOfLog = "";
		String lip = "";
		int total = colNames.length > colValues.length ? colValues.length : colNames.length;
		Map<String, String> rInDb = new HashMap<String, String>();
		for (int i = 0; i < total; i++) {
			String colName = colNames[i].toLowerCase();
			String colValue = "".equals(colValues[i]) || colValues[i] == null ? "" : colValues[i];
			colValue = colValue.replaceAll("\\s+[nN][uU][lL][lL]\\s+", "");
			colValue = colValue.replaceAll("\\s+", " ").trim();
			byte[] b = colValue.getBytes(Charset.forName("UTF-8"));
			int bLen = b.length;
			if (bLen > 2000) {
				byte[] db = new byte[2000];
				System.arraycopy(b, 0, db, 0, 2000);
				colValue = new String(db);
				colValue = colValue.substring(0, colValue.length() - 1);
			}
			//有冒号的H2认为是一列，因此必须处理
			colValue = colValue.replaceAll("\\\"", "'");
			if (colName.equals("topy") || colName.equals("topm") || colName.equals("topd")) {
				dayOfLog = dayOfLog + colValue;
			}
			if (colName.equals("rsc")) {
				// 会话记录数的
				if (colValue == null || colValue.trim().equals("")) {
					colValue = "0";
				}
			}
			if (colName.equals("rvc")) {
				if (colValue == null || colValue.trim().equals("")) {
					colValue = "0";
				} else if (colValue.length() > 1) {
					colValue = "1";
				}

			}
			if (colName.equals("lip")) {
				lip = colValue;
			}
			rInDb.put(colName, colValue);
		}
		List<String> nodeInfo = rd.locateNodeInfo(lip);
		if (nodeInfo != null) {
			// lorg
			String v = nodeInfo.get(5) == null ? "" : nodeInfo.get(5);
			rInDb.put("lorg", v);
			// lst
			v = nodeInfo.get(7) == null ? "" : nodeInfo.get(7);
			rInDb.put("lst", v);
		} else {
			// lorg
			rInDb.put("lorg", "");
			// lst
			rInDb.put("lst", "");
		}

		if (!rInDb.containsKey("rvc")) {
			rInDb.put("rvc", "1");
		}

		if (!rInDb.containsKey("rsc")) {
			rInDb.put("rsc", "0");
		}

		StringBuilder sb = new StringBuilder();
		List<CAColumn> cs = table.getColumns();
		for (CAColumn c : cs) {
			String cn = c.getColName();
			if (rInDb.containsKey(cn)) {
				sb.append(rInDb.get(cn)).append(FIELD_SEPARATOR);
			} else {
				sb.append("").append(FIELD_SEPARATOR);
			}
		}
		sb.replace(sb.length() - 1, sb.length(), "");

		String k = "lv" + dayOfLog;
		if (!dayLockers.containsKey(k)) {
			ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
			dayLockers.putIfAbsent(k, lock);
		}

		List<StringBuilder> buffed = dayLogCache.get(k);
		if (buffed == null) {
			List<StringBuilder> buff = new ArrayList<StringBuilder>();
			dayLogCache.put(k, buff);
			buffed = buff;
		}
		buffed.add(sb);

		rowCount++;
		limitCount++;
		if (limitCount >= LIMIT_COUNT) {
			System.out.println("export " + limitCount + " cost: " + (System.currentTimeMillis() - limitStart));
			flush();
			limitCount = 0;
			limitStart = 0;
		}
	}

	@Override
	public int getRowCount() {
		return rowCount;
	}
}
