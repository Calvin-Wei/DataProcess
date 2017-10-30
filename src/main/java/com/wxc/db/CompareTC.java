/**
 * @Title: CompareTC.java
 * @Package com.gdgz.whitelist.db
 * @Description: TODO(用一句话描述该文件做什么)
 * @author wxc
 * @date 2017年10月26日 下午8:15:31
 * @version V1.0   
 */
package com.wxc.db;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.gdgz.whitelist.util.TimeUtils;

/**
 * @author wxc
 * @version 创建时间：2017年10月26日 下午8:15:31
 */
public class CompareTC {

	@Autowired
	private DataSource dataSource;
	
	private Logger logger=LoggerFactory.getLogger(getClass());

	private String insertMobile = "insert into temp (mobile) values (?)";

	/**
	 * 1.插入端口号，端口号存在则查出pId（端口号主键） 2.插入手机号，手机号存在则查出mId(手机号主键)
	 * 3.pm表关联表，关联端口号和手机号，通过pId和mId进行关联，
	 * 
	 * @param file
	 */
	public Map<String, Object> compareTC(MultipartFile file, int p) {
		Map<String, Object> map = new HashMap<String, Object>();
		Connection conn = null;
		PreparedStatement insertMobileSt = null;

		map.put("port", p);
		// 插入时间
		Timestamp timestamp = TimeUtils.now();
		map.put("upTime", timestamp);

		String pId = "";
		try {
			conn = dataSource.getConnection();
			insertMobileSt = conn.prepareStatement(insertMobile);
			BufferedInputStream bis = new BufferedInputStream(file.getInputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(bis, "utf-8"), 10 * 1024 * 1024);
			while (in.ready()) {
				String mobile = in.readLine();
				if (mobile.length() != 11)
					continue;
				
				insertMobileSt.setString(1, pId);// 第一次插入时的端口号
				insertMobileSt.setString(2, mobile);
				insertMobileSt.setTimestamp(3, timestamp);
				insertMobileSt.executeUpdate();				
			}
			logger.info(">>>>> into Port Table port is {}, in {} , Import file type is txt or csv .",p,timestamp);
			bis.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.closeStatement(insertMobileSt);
			JdbcUtil.closeConnection(conn);
		}
		return map;
	}

}
