package com.cnebula.analytics.analyzeservice;

public class AnalyzeException extends RuntimeException {

	private static final long serialVersionUID = 3012403150830685727L;

	public AnalyzeException(String msg, Throwable e) {
		super(msg + "-->\n" + e.getCause());
		setStackTrace(e.getStackTrace());
	}

	public AnalyzeException(String msg) {
		super(msg);
	}

	public AnalyzeException(Throwable t) {
		super(t);
	}
}
