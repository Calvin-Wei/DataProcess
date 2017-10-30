/**
 * @Title: ImportTC.java
 * @Package com.gdgz.whitelist.db
 * @Description: TODO(用一句话描述该文件做什么)
 * @author wxc
 * @date 2017年10月18日 下午5:10:57
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
 * @version 创建时间：2017年10月18日 下午5:10:57
 */
public class ImportTC {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private DataSource dataSource;

	private int port;
	private String sign;

	private String insertPort = "insert into Port (port,Sign) values(?,?)";
	private String insertMobile = "insert into Mobile (InPort,mobile,upLoadTime) values (?,?,?)";
	private String insertPM = "insert into PM(pId,mId,upLoadTime) values(?,?,?)";
	private String selectPort = "select pId from Port where Port=@p";
	private String selectMId = "select mId from Mobile where mobile=@m";

	/**
	 * 1.插入端口号，端口号存在则查出pId（端口号主键） 2.插入手机号，手机号存在则查出mId(手机号主键)
	 * 3.pm表关联表，关联端口号和手机号，通过pId和mId进行关联，
	 * 
	 * @param file
	 */
	public Map<String, Object> importTC(MultipartFile file, int p, String s) {
		Map<String, Object> map = new HashMap<String, Object>();
		Connection conn = null;
		// 用来插入端口号
		PreparedStatement insertPortSt = null;
		PreparedStatement insertMobileSt = null;
		PreparedStatement insertPMSt = null;
		// 端口号存在查询到端口号的id
		PreparedStatement selectPortSt = null;
		// 查询手机号码已经存在的id
		PreparedStatement selectMIdSt = null;
		ResultSet resultSet = null, pIdResultSet = null, mIdResultSet = null;
		ResultSet resultFor = null;

		this.port = p;
		this.sign = s;

		map.put("port", p);
		// 插入时间
		Timestamp timestamp = TimeUtils.now();
		map.put("upTime", timestamp);

		String pId = "";
		try {
			conn = dataSource.getConnection();
			insertMobileSt = conn.prepareStatement(insertMobile);
			insertPMSt = conn.prepareStatement(insertPM);
			insertPortSt = conn.prepareStatement(insertPort, Statement.RETURN_GENERATED_KEYS);
			insertPortSt.setInt(1, port);
			insertPortSt.setString(2, sign);
			insertPortSt.executeUpdate();
			pIdResultSet = insertPortSt.getGeneratedKeys();
			if (pIdResultSet.next())
				pId = String.valueOf(pIdResultSet.getInt(1));
		} catch (SQLException e) {
			try {
				selectPortSt = conn.prepareStatement(selectPort.replace("@p", port + ""));
				resultSet = selectPortSt.executeQuery();
				if (resultSet.next())
					pId = String.valueOf(resultSet.getInt(1));
			} catch (SQLException e1) {
				logger.error(e1.getMessage(), e1);
			}
		} finally {
			JdbcUtil.closeResultSet(pIdResultSet);
			JdbcUtil.closeResultSet(resultSet);
			JdbcUtil.closeStatement(selectPortSt);
			JdbcUtil.closeStatement(selectPortSt);
		}
		logger.info(">>>>> into Port Table port is {}, in {} , Import file type is txt or csv .",port,timestamp);
		try {
			BufferedInputStream bis = new BufferedInputStream(file.getInputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(bis, "utf-8"), 10 * 1024 * 1024);
			while (in.ready()) {
				String mobile = in.readLine();
				if (mobile.length() != 11)
					continue;
				String mId = "";
				try {
					mId = insertMobile(mIdResultSet, insertMobileSt, pId, mobile, timestamp);
				} catch (SQLException e) {
//					e.printStackTrace();
					selectMIdSt = conn.prepareStatement(selectMId.replace("@m", mobile));
					resultFor = selectMIdSt.executeQuery();
					if (resultFor.next()) {
						mId = resultFor.getString("mId");
					}
				}
				try {
					insertPMSt.setString(1, pId);
					insertPMSt.setString(2, mId);
					insertPMSt.setTimestamp(3, timestamp);
					insertPMSt.executeUpdate();
				} catch (Exception e) {
//					e.printStackTrace();
					continue;
				}
			}
			bis.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.closeResultSet(pIdResultSet);
			JdbcUtil.closeResultSet(resultFor);
			JdbcUtil.closeResultSet(mIdResultSet);
			JdbcUtil.closeStatement(selectMIdSt);
			JdbcUtil.closeStatement(insertPMSt);
			JdbcUtil.closeStatement(insertMobileSt);
			JdbcUtil.closeStatement(insertPortSt);
			JdbcUtil.closeConnection(conn);
		}
		return map;
	}

	public String insertMobile(ResultSet mIdResultSet, PreparedStatement insertMobileSt, String pId, String mobile,
			Timestamp timestamp) throws SQLException {
		String mId = "";
		insertMobileSt.setString(1, pId);// 第一次插入时的端口号
		insertMobileSt.setString(2, mobile);
		insertMobileSt.setTimestamp(3, timestamp);
		insertMobileSt.executeUpdate();
		mIdResultSet = insertMobileSt.getGeneratedKeys();
		if (mIdResultSet.next())
			mId = mIdResultSet.getString(1);
		return mId;
	}

}
