/**
 * @Title: CustomMultipartResolver.java
 * @Package com.gdgz.whitelist.util
 * @Description: TODO(用一句话描述该文件做什么)
 * @author wxc
 * @date 2017年10月17日 下午2:23:48
 * @version V1.0   
 */
package com.wxc.util;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.wxc.zipdata.UpLoadAction;


/**
 * @author wxc
 * @version 创建时间：2017年10月17日 下午2:23:48
 */
public class CustomMultipartResolver extends CommonsMultipartResolver {
	@Autowired
	private UpLoadAction progressListener;

	public void setUpLoadAction(UpLoadAction progressListener) {
		this.progressListener = progressListener;
	}

	@Override
	protected MultipartParsingResult parseRequest(HttpServletRequest request) throws MultipartException {
		String encoding = determineEncoding(request);
		FileUpload fileUpload = prepareFileUpload(encoding);
		progressListener.setSession(request.getSession());
		fileUpload.setProgressListener(progressListener);
		try {
			List<FileItem> fileItems = ((ServletFileUpload) fileUpload).parseRequest(request);
			return parseFileItems(fileItems, encoding);
		} catch (FileUploadBase.SizeLimitExceededException ex) {
			throw new MaxUploadSizeExceededException(fileUpload.getSizeMax(), ex);
		} catch (FileUploadException ex) {
			throw new MultipartException("Could not parse multipart servlet request", ex);
		}
	}

}
