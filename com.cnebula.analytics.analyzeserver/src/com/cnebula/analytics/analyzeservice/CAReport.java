package com.cnebula.analytics.analyzeservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cnebula.analytics.common.Metrics;
import com.cnebula.common.annotations.xml.XMLIgnore;
import com.cnebula.common.annotations.xml.XMLMapping;
import com.cnebula.common.xml.XMLParseException;

@XMLMapping(tag = "report")
public class CAReport {

	public static final String DEFAULT_PROCESS_TIME = "01:00";

	String reportName = "";

	/**
	 * 默认每天凌晨1点执行报表任务
	 */
	String processTime = DEFAULT_PROCESS_TIME;

	List<Metrics> metrics = new ArrayList<Metrics>();

	List<CADataMatrix> matrixes = new ArrayList<CADataMatrix>();

	List<CAProcessCtx> processors = new ArrayList<CAProcessCtx>();

	private HashSet<String> metricsNameSet = new HashSet<String>();

	private Map<String, CADataMatrix> nameMatixMap = new HashMap<String, CADataMatrix>();

	private Map<CADataMatrix, Set<String>> dimensionMap = new HashMap<CADataMatrix, Set<String>>();

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public String getProcessTime() {
		return processTime;
	}

	public void setProcessTime(String processTime) {
		this.processTime = processTime;
	}

	@XMLMapping(tag = "metricsList")
	public List<Metrics> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<Metrics> metrics) {
		this.metrics = metrics;
		metricsNameSet.clear();
		if (metrics != null) {
			for (Metrics m : metrics) {
				metricsNameSet.add(m.getName().toLowerCase());
			}
		}
	}

	@XMLMapping(tag = "matrixes", childTag = "matrix")
	public List<CADataMatrix> getMatrixes() {
		return matrixes;
	}

	public void setMatrixes(List<CADataMatrix> matrixes) {
		this.matrixes = matrixes;
		dimensionMap.clear();
		nameMatixMap.clear();
		if (matrixes != null) {
			for (CADataMatrix m : matrixes) {
				if (m.isReportMatrix) {
					dimensionMap.put(m, m.nameSet());
				}
				nameMatixMap.put(m.getName(), m);
			}
		}
	}

	@XMLIgnore
	public CADataMatrix getMatrix(String name) {
		return nameMatixMap.get(name);
	}

	public boolean containMatrix(String name) {
		return nameMatixMap.containsKey(name);
	}

	@XMLIgnore
	public boolean containsAllMetrics(List<String> metrics) {
		return metricsNameSet.containsAll(metrics);
	}

	public List<CADataMatrix> matrixContainsAllDimension(List<String> dimensions) {
		List<CADataMatrix> rst = new ArrayList<CADataMatrix>();
		Set<Map.Entry<CADataMatrix, Set<String>>> entrySet = dimensionMap.entrySet();
		for (Map.Entry<CADataMatrix, Set<String>> entry : entrySet) {
			if (!entry.getKey().isReportMatrix) {
				continue;
			}
			if (entry.getValue().containsAll(dimensions)) {
				rst.add(entry.getKey());
			}
		}
		return rst;
	}

	@XMLMapping(tag = "pipline", childTag = "processor")
	public List<CAProcessCtx> getProcessors() {
		return processors;
	}

	public void setProcessors(List<CAProcessCtx> processors) {
		this.processors = processors;
	}

	public static void main(String[] args) throws XMLParseException {
	}
}
