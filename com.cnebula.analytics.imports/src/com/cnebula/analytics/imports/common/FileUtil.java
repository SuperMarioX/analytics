package com.cnebula.analytics.imports.common;

import java.io.File;

public class FileUtil {
	
	public static void deleteDir(File aFile) {
		if (aFile.exists()) {
			if (aFile.isDirectory()) {
				File[] subFileList = aFile.listFiles();
				for (File subFile : subFileList) {
					if (subFile.isDirectory()) {
						deleteDir(subFile);
					}
					if (subFile.isFile()) {
						System.out.println("删除" + aFile + "成功？" + subFile.delete());
					}
				}
				System.out.println("删除" + aFile + "成功？" + aFile.delete());
			}
			if (aFile.isFile()) {
				System.out.println("删除" + aFile + "成功？" + aFile.delete());
			}
		} else {
			return;
		}
	}
	
}
