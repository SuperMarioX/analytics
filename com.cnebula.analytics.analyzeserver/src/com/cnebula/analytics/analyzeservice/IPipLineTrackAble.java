package com.cnebula.analytics.analyzeservice;

import java.util.List;
import java.util.Map;

import com.cnebula.analytics.analyzeserver.ReportPipLineTrack;

public interface IPipLineTrackAble {

	public List<ReportPipLineTrack> listLast30DayTrack();

	public List<ReportPipLineTrack> listTrack(String dateInyyyyMMdd);

	public List<ReportPipLineTrack> listFailTrack(String startDateInyyyyMMdd, String endDateInyyyyMMdd);

	public long countSizeOfMatrix(String matrixName);

	public Map<String, Long> listCountSizeOfAllMatrix();
}
