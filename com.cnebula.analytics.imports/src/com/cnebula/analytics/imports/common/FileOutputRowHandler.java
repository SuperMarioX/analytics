package com.cnebula.analytics.imports.common;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class FileOutputRowHandler implements IRowHandler {

	public static final String ROW_SEPARATOR = "\n";

	protected String dir = null;

	protected int rowCount = 0;

	protected StringBuilder header = new StringBuilder();
	
	protected Map<String, List<StringBuilder>> dayLogCache = new HashMap<String, List<StringBuilder>>();
	
	protected static final ConcurrentHashMap<String,ReentrantReadWriteLock> dayLockers = new ConcurrentHashMap<String, ReentrantReadWriteLock>();

	public FileOutputRowHandler(String dir) {
		this.dir = dir;
		if (dir.indexOf("\\") > 0) {
			dir = dir.replaceAll("\\\\", "/");
		}
		if (dir.endsWith("/")) {
			dir = dir.substring(0, dir.length() - 1);
		}
		File f = new File(dir);
		if (f.exists()) {
			if (f.isFile()) {
				throw new RuntimeException("请指定目录，而不是文件");
			}
		} else {
			f.mkdir();
		}
	}

	@Override
	public void handle(String[] colNames, String[] colValues) {
		
	}

	@Override
	public void flush() {
		Set<String> days = dayLogCache.keySet();
		int count = 0;
		for (String day : days) {
			List<StringBuilder> buffs = dayLogCache.get(day);
			StringBuilder buff = new StringBuilder();
			for(StringBuilder sb : buffs){
				if (sb.length() > 0) {
					buff.append(sb).append(ROW_SEPARATOR);
					count++;
				}
			}
			save2File(day, buff);
		}
		System.out.println("flush " + count);
		dayLogCache.clear();
	}

	public void save2File(String fileName, StringBuilder buff) {
		FileChannel out = null;
		ByteBuffer bb = null;
		ReentrantReadWriteLock lock = dayLockers.get(fileName);
		try {
			lock.writeLock().lock();
			RandomAccessFile file = new RandomAccessFile(dir + "/" + fileName, "rw");
			out = file.getChannel();
			if(out.size() == 0){
				out.write(ByteBuffer.wrap(header.toString().getBytes(Charset.forName("UTF-8"))));
			}
			byte[] b = buff.toString().getBytes(Charset.forName("UTF-8"));
			bb = ByteBuffer.wrap(b);
			out.position(out.size());
			out.write(bb);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try{
				lock.writeLock().unlock();
			}catch (Exception e) {
			}
			if (out != null) {
				try {
					out.close();
					bb.clear();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
