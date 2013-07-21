package com.cnebula.analytics.logserver.h2;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Date;

public class TestH2BinaryStream {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		 try {
			Class.forName("org.h2.Driver");
			Connection conn = DriverManager. getConnection("jdbc:h2:tcp://localhost/~/test", "sa", "");
//			Connection conn = DriverManager. getConnection("jdbc:h2:mem:test", "sa", "");
			conn.setAutoCommit(true);
//			conn.createStatement().execute("create table testtype (tstr varchar(20), tint int, tdate date)");
			
			PreparedStatement pstmt = conn.prepareStatement("insert into testtype(tstr, tdate, tint) values(?, ?, ?)");
//			pstmt.setBytes(1, "及时str".getBytes("utf-8"));
//			pstmt.setBytes(2, "1999-02-21".getBytes());
//			pstmt.setBytes(3, "200".getBytes());
//			pstmt.setString(1, "及时str");
//			pstmt.setString(2, "1999-02-21");
//			pstmt.setString(3, "200");
//			long s = System.currentTimeMillis();
//			StringReader[] sra = new StringReader[] {new StringReader("2及时str"), new StringReader("1999-02-21"), new StringReader("210")};
//			int c = 10000;
//			for (int i = 0; i < c; i++){
//				pstmt.setCharacterStream(1,  sra[0]);
//				pstmt.setCharacterStream(2, sra[1]);
//				pstmt.setCharacterStream(3, sra[2]);
//				pstmt.addBatch();
//				sra[0].reset();sra[1].reset();sra[2].reset();
//			}
//			pstmt.executeBatch();
//			System.out.println("reader cost " + (System.currentTimeMillis() - s));
			
//			long s = System.currentTimeMillis();
//			Object[] sr = new Object[] {("2及时str"), new java.sql.Date(new Date().getTime()), 210};
//			int c = 10000;
//			for (int i = 0; i < c; i++){
//				pstmt.setString(1,  (String)sr[0]);
//				pstmt.setDate(2, (java.sql.Date)sr[1]);
//				pstmt.setInt(3, (Integer)sr[2]);
//				pstmt.addBatch();
////				sra[0].reset();sra[1].reset();sra[2].reset();
//			}
//			pstmt.executeBatch();
//			System.out.println("reader cost " + (System.currentTimeMillis() - s));
			
			long s = System.currentTimeMillis();
			String[] sr = new String[] {("2及时str"), "2011-09-21", "210"};
			int c = 100000;
			for (int i = 0; i < c; i++){
				pstmt.setString(1, sr[0]);
				pstmt.setString(2, sr[1]);
				pstmt.setString(3, sr[2]);
				pstmt.addBatch();
//				sra[0].reset();sra[1].reset();sra[2].reset();
			}
			pstmt.executeBatch();
			System.out.println("string cost " + (System.currentTimeMillis() - s));
//			long s = System.currentTimeMillis();
//			Object[] sra = new Object[] {("2及时str").getBytes("utf-8"),   "2010-02-22",  new byte[]{0, 0,0, 1} };
//			int c = 10000;
//			for (int i = 0; i < c; i++){
//				pstmt.setBytes(1,  (byte[])sra[0]);
//				pstmt.setString(2, (String)sra[1]);
//				pstmt.setBytes(3, (byte[])sra[2]);
//				pstmt.addBatch();
//			}
//			pstmt.executeBatch();
//			System.out.println("reader cost " + (System.currentTimeMillis() - s));
			pstmt.close();
			conn.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	       
	}

}
