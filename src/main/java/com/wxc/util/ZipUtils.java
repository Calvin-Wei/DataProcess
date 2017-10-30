/**
 * @Title: ZipUtils.java
 * @Package com.gdgz.whitelist.util
 * @Description: TODO(用一句话描述该文件做什么)
 * @author wxc
 * @date 2017年10月26日 下午2:29:05
 * @version V1.0   
 */
package com.wxc.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

/**
 * @author wxc
 * @version 创建时间：2017年10月26日 下午2:29:05
 */
public class ZipUtils {
	/**
	 * 对Excel文件进行压缩
	 * 
	 * @param wb
	 */
	public static void zip(File targetFile, HSSFWorkbook... wbs) {
		if (wbs == null || wbs.length == 0) {
			return;
		}
		ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(new FileOutputStream(targetFile));
			for (int i = 0; i < wbs.length; i++) {
				ZipEntry entry = new ZipEntry("文件" + i + ".xls");
				// 设置压缩包的入口
				zos.putNextEntry(entry);
				wbs[i].write(zos);
				zos.flush();
			}
			zos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
