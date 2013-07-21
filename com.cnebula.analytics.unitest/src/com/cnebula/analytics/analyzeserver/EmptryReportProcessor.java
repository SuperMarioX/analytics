package com.cnebula.analytics.analyzeserver;

import java.util.Date;

import com.cnebula.analytics.analyzeserver.processor.AbstractProcessor;
import com.cnebula.analytics.analyzeservice.AnalyzeException;
import com.cnebula.analytics.analyzeservice.CAProcessCtx;

public class EmptryReportProcessor extends AbstractProcessor {

	public EmptryReportProcessor() {
		super();
	}

	@Override
	public void process(CAProcessCtx ctx) throws AnalyzeException {
		System.out.println("EmptryReportProcessor.process(CAProcessCtx) called  " + ctx);
	}

	@Override
	public void rollBack(CAProcessCtx ctx, Date date) throws AnalyzeException {
		System.out.println("EmptryReportProcessor.process(CAProcessCtx,Date) called  " + date + ctx);
	}
}