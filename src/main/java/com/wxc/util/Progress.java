/**
 * @Title: Progress.java
 * @Package com.gdgz.whitelist.util
 * @Description: TODO(用一句话描述该文件做什么)
 * @author wxc
 * @date 2017年10月17日 下午2:11:17
 * @version V1.0   
 */
package com.wxc.util;

/**
 * 上传文件进度条
 * @author wxc
 * 
 */
public class Progress {
	private long byteRead;
	private long contentLength;
	private long items;
	/**
	 * @return byteRead
	 */
	public long getByteRead() {
		return byteRead;
	}
	/**
	 * @param byteRead 要设置的 byteRead
	 */
	public void setByteRead(long byteRead) {
		this.byteRead = byteRead;
	}
	/**
	 * @return contentLength
	 */
	public long getContentLength() {
		return contentLength;
	}
	/**
	 * @param contentLength 要设置的 contentLength
	 */
	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}
	/**
	 * @return items
	 */
	public long getItems() {
		return items;
	}
	/**
	 * @param items 要设置的 items
	 */
	public void setItems(long items) {
		this.items = items;
	}
	
}
