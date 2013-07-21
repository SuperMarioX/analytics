package com.cnebula.analytics.analyzeservice;

import java.util.HashMap;
import java.util.Map;

import com.cnebula.common.annotations.xml.XMLIgnore;
import com.cnebula.common.annotations.xml.XMLMapping;

public class CAProcessCtx {

	private String fromMatrix = "";

	private String toMatrix = "";

	private String processor = "";

	private Map<String, Object> properties = new HashMap<String, Object>();

	private CADataMatrix matrixFrom = null;

	private CADataMatrix matrixTo = null;

	public String getFromMatrix() {
		return fromMatrix;
	}

	public void setFromMatrix(String fromMatrix) {
		this.fromMatrix = fromMatrix;
	}

	public String getToMatrix() {
		return toMatrix;
	}

	public void setToMatrix(String toMatrix) {
		this.toMatrix = toMatrix;
	}

	public String getProcessor() {
		return processor;
	}

	public void setProcessor(String processor) {
		this.processor = processor;
	}

	@XMLMapping(childTag = "property", keyTag = "key", valueTag = "value", itemTypes = { com.cnebula.common.xml.MapEntry.class,
			String.class, Object.class })
	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	@XMLIgnore
	public CADataMatrix getMatrixFrom() {
		return matrixFrom;
	}

	public void setMatrixFrom(CADataMatrix matrixFrom) {
		this.matrixFrom = matrixFrom;
	}

	@XMLIgnore
	public CADataMatrix getMatrixTo() {
		return matrixTo;
	}

	@Override
	public String toString() {
		return "[fromMatrix=" + fromMatrix + ", toMatrix=" + toMatrix + ", processor=" + processor + "]";
	}

	public void setMatrixTo(CADataMatrix matrixTo) {
		this.matrixTo = matrixTo;
	}
}
