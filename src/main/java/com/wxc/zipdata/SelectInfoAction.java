/**
 * @Title: SelectInfoAction.java
 * @Package com.gdgz.whitelist.controller.upload
 * @Description: TODO(用一句话描述该文件做什么)
 * @author wxc
 * @date 2017年10月20日 上午9:22:39
 * @version V1.0   
 */
package com.wxc.zipdata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wxc.db.JdbcUtil;


/**
 * @author wxc
 * @version 创建时间：2017年10月20日 上午9:22:39
 */
@Controller
@RequestMapping("selectInfo")
public class SelectInfoAction {

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private DataSource dataSource;

	@RequestMapping(value = "selectList", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> selectList(@RequestBody(required = true) Map<String, Object> param) {
		Map<String, Object> map = new HashMap<>();
		int pageNumber = (int) param.get("pageNumber");
		int pageSize = (int) param.get("pageSize");
		int begin = (pageNumber - 1) * pageSize;
		param.put("begin", begin);
		if (param.get("port") != null || param.get("upTime") != null || param.get("mobile") != null)
			map = selectInfo(param);
		return map;
	}

	private String queryInfo = "select top @pagesize  p.Port,p.Sign,m.mobile,pm.upLoadTime from ( "
			+ "	select ROW_NUMBER() OVER(order by pm.upLoadTime asc) oi,"
			+ " pm.mId,pm.pId,pm.upLoadTime from PM ) pm,Port p,Mobile m "
			+ " where pm.mId=m.mId and p.pId=pm.pId and oi>@begin ";

	public Map<String, Object> selectInfo(Map<String, Object> param) {
		Map<String, Object> target = new HashMap<>();
		int begin = (int) param.get("begin");
		int total = countInfo(param);
		List<Map<String, Object>> listInfo = new ArrayList<>();
		StringBuilder sql = requireSql(param, queryInfo);
		Connection conn = null;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		try {
			conn = dataSource.getConnection();
			String s = sql.toString();
			int pagesize = (int) param.get("pageSize");
			System.out.println(s.replace("@pagesize", pagesize + "").replace("@begin", begin + ""));
			pStatement = conn.prepareStatement(s.replace("@pagesize", pagesize + "").replace("@begin", begin + ""));
			resultSet = pStatement.executeQuery();
			while (resultSet.next()) {
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("Port", resultSet.getInt("Port"));
				m.put("Sign", resultSet.getString("Sign"));
				m.put("Mobile", resultSet.getString("mobile"));
				m.put("UpTime", resultSet.getTimestamp("upLoadTime"));
				listInfo.add(m);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			JdbcUtil.closeResultSet(resultSet);
			JdbcUtil.closeStatement(pStatement);
			JdbcUtil.closeConnection(conn);
		}
		target.put("total", total);
		target.put("rows", listInfo);
		target.put("begin", begin);
		return target;
	}

	/**
	 * 符合查询条件下，一共有多少条记录
	 * 
	 * @param param
	 * @return
	 */
	private int countInfo(Map<String, Object> param) {
		StringBuilder sql = requireSql(param, countSql);
		Connection conn = null;
		PreparedStatement countSt = null;
		ResultSet resultSet = null;
		int count = 0;
		try {
			conn = dataSource.getConnection();
			System.out.println(">>>>>>>>>>>>> " + sql.toString());
			countSt = conn.prepareStatement(sql.toString());
			resultSet = countSt.executeQuery();
			if (resultSet.next())
				count = resultSet.getInt(1);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			JdbcUtil.closeResultSet(resultSet);
			JdbcUtil.closeStatement(countSt);
			JdbcUtil.closeConnection(conn);
		}
		return count;
	}

	private String countSql = "select count(0) from PM pm,Port p,Mobile m where pm.mId=m.mId and p.pId=pm.pId ";

	private StringBuilder requireSql(Map<String, Object> param, String sql) {
		StringBuilder sbuilder = new StringBuilder(sql);
		String port = String.valueOf(param.get("port"));
		if (port != null && port.length() > 0) {
			sbuilder.append(" and p.Port=" + port);
		}
		String mobile = (String) param.get("mobile");
		if (mobile != null && mobile.length() > 0) {
			sbuilder.append(" and m.mobile=" + mobile);
		}
		if (param.get("upTime") instanceof String) {
			String temp=(String) param.get("upTime");
			if(temp!=null&&temp.length()>0)
			sbuilder.append(" and pm.upLoadTime>" + param.get("upTime"));
		} else if (param.get("upTIme") instanceof Long) {
			long src = (long) param.get("upTime");
			Timestamp t = new Timestamp(src);
			if (t != null) {
				sbuilder.append(" and pm.upLoadTime>" + t.toString().split(" ")[0]);
			}
		}
		return sbuilder;
	}

}
