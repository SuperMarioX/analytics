package com.cnebula.analytics.reportservice;

public class ReportException extends RuntimeException {

	private static final long serialVersionUID = 7509694151051918180L;
	
	public ReportException(){
		super();
	}
	
	public ReportException(String msg){
		super(msg);
	}
	
	public ReportException(Throwable t){
		super(t);
	}
	
	public ReportException(String msg,Throwable t){
		super(msg, t);
	}
	
}
