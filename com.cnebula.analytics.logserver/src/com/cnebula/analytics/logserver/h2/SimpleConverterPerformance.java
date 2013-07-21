package com.cnebula.analytics.logserver.h2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleConverterPerformance {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String str = "210";
		int c = 100000;
		long s = System.currentTimeMillis();
		int intv = 0;
		for (int i = 0; i < c; i++){
			intv = Integer.parseInt(str);
		}
		
		System.out.println("int str cost " + (System.currentTimeMillis() - s));
		
		String ddtr = "2010-01-20";
		
		
		s = System.currentTimeMillis();
		Date d = new Date();
		for (int i = 0; i < c; i++){
			try {
				SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
				d = sf.parse(ddtr);
				d.setTime(System.currentTimeMillis());
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("date str cost " + (System.currentTimeMillis() - s));
	}

}
