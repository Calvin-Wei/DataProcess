/**
 * @Title: ImportExcel.java
 * @Package com.gdgz.whitelist.db
 * @Description: TODO(用一句话描述该文件做什么)
 * @author wxc
 * @date 2017年10月18日 下午5:10:34
 * @version V1.0   
 */
package com.wxc.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.gdgz.whitelist.util.TimeUtils;

/**
 * @author wxc
 * @version 创建时间：2017年10月18日 下午5:10:34
 */
public class ImportExcel {

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private DataSource dataSource;

	private int port;
	private String sign;

	public Map<String, Object> importExcel(MultipartFile file, int port, String sign) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			this.port = port;
			this.sign = sign;
			map = read(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * 
	 * @描述：根据文件名读取excel文件
	 * 
	 */
	public Map<String, Object> read(MultipartFile file) throws Exception {
		InputStream is = null;
		String filePath = file.getOriginalFilename();
		Map<String, Object> map;
		try {
			/** 判断文件的类型，是2003还是2007 */
			boolean isExcel2003 = true;
			if (isExcel2007(filePath)) {
				isExcel2003 = false;
			}
			/** 调用本类提供的根据流读取的方法 */
			is = file.getInputStream();
			map = read(is, isExcel2003);
			is.close();
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw new Exception("读取Excel文件出错!");
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					is = null;
					logger.error(e.getMessage(), e);
				}
			}
		}
		/** 返回最后读取的结果 */
		return map;
	}

	/**
	 * 
	 * @描述：根据流读取Excel文件
	 * 
	 */
	public Map<String, Object> read(InputStream inputStream, boolean isExcel2003) throws Exception {
		try {
			/** 根据版本选择创建Workbook的方式 */
			Workbook wb = null;
			if (isExcel2003) {
				wb = new HSSFWorkbook(inputStream);
			} else {
				wb = new XSSFWorkbook(inputStream);
			}
			return read(wb);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new Exception("读取Excel文件出错!");
		}
	}

	private String insertPort = "insert into Port (port,Sign) values(?,?)";
	private String insertMobile = "insert into Mobile (InPort,mobile,upLoadTime) values (?,?,?)";
	private String insertPM = "insert into PM(pId,mId,upLoadTime) values(?,?,?)";
	private String selectPort = "select pId from Port where Port=@p";
	private String selectMId = "select mId from Mobile where mobile=@m";

	/**
	 * 1.插入端口号，端口号存在则查出pId（端口号主键） 2.插入手机号，手机号存在则查出mId(手机号主键)
	 * 3.pm表关联表，关联端口号和手机号，通过pId和mId进行关联，
	 * 
	 * @描述：读取第一列数据
	 */
	private Map<String, Object> read(Workbook wb) {

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
		// 端口号的主键id
		int pId = 0;
		int totalRows = 0;// 总行数
		// 插入时间
		Timestamp timestamp = TimeUtils.now();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("port", port);
		map.put("upTime", timestamp);
		try {
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
					pId = pIdResultSet.getInt(1);
			} catch (SQLException e) {
				try {
					selectPortSt = conn.prepareStatement(selectPort.replace("@p", port + ""));
					resultSet = selectPortSt.executeQuery();
					if (resultSet.next())
						pId = resultSet.getInt(1);
				} catch (SQLException e1) {
					logger.error(e1.getMessage(), e1);
				}
			} finally {
				JdbcUtil.closeResultSet(pIdResultSet);
				JdbcUtil.closeResultSet(resultSet);
				JdbcUtil.closeStatement(insertPortSt);
				JdbcUtil.closeStatement(selectPortSt);
			}
			logger.info(">>>>> into Port Table port is {}, in {} , Import file type is excel .",port,timestamp);
			String data = new String();
			/** 得到第一个shell */
			Sheet sheet = wb.getSheetAt(0);
			/** 得到Excel的行数 */
			totalRows = sheet.getPhysicalNumberOfRows();
			for (int r = 0; r < totalRows; r++) {
				Row row = sheet.getRow(r);
				if (row == null) {
					continue;
				}
				Cell cell = row.getCell(0);
				String cellValue = "";
				if (null != cell) {
					// 以下是判断数据的类型
					switch (cell.getCellType()) {
					case HSSFCell.CELL_TYPE_NUMERIC: // 数字
						cellValue = cell.getNumericCellValue() + "";
						break;
					case HSSFCell.CELL_TYPE_STRING: // 字符串
						cellValue = cell.getStringCellValue();
						break;
					case HSSFCell.CELL_TYPE_BOOLEAN: // Boolean
						cellValue = cell.getBooleanCellValue() + "";
						break;
					case HSSFCell.CELL_TYPE_FORMULA: // 公式
						cellValue = cell.getCellFormula() + "";
						break;
					case HSSFCell.CELL_TYPE_BLANK: // 空值
						cellValue = "";
						break;
					case HSSFCell.CELL_TYPE_ERROR: // 故障
						cellValue = "非法字符";
						break;
					default:
						cellValue = "未知类型";
						break;
					}
				}
				/** 保存第r行的数据 */
				data = cellValue;
				if (data.length() != 11) {
					continue;
				}
				String mId = "";
				try {
					insertMobileSt.setInt(1, pId);// 第一次插入时的端口号
					insertMobileSt.setString(2, data);
					insertMobileSt.setTimestamp(3, timestamp);
					insertMobileSt.executeUpdate();
					mIdResultSet = insertMobileSt.getGeneratedKeys();
					if (mIdResultSet.next())
						mId = mIdResultSet.getString(1);
				} catch (SQLException e) {
					selectMIdSt = conn.prepareStatement(selectMId.replace("@m", data));
					resultFor = selectMIdSt.executeQuery();
					if (resultFor.next()) {
						mId = resultFor.getString("mId");
					}
				}
				try {
					insertPMSt.setInt(1, pId);
					insertPMSt.setString(2, mId);
					insertPMSt.setTimestamp(3, timestamp);
					insertPMSt.executeUpdate();
				} catch (Exception e) {
					continue;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
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

	/**
	 * 
	 * @描述：是否是2003的excel，返回true是2003
	 * 
	 * @返回值：boolean
	 */
	public static boolean isExcel2003(String filePath) {
		return filePath.matches("^.+\\.(?i)(xls)$");
	}

	/**
	 * 
	 * @描述：是否是2007的excel，返回true是2007
	 * 
	 * @返回值：boolean
	 */

	public static boolean isExcel2007(String filePath) {
		return filePath.matches("^.+\\.(?i)(xlsx)$");
	}

}
