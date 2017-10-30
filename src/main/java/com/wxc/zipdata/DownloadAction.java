/**
 * @Title: DownloadAct.java
 * @Package com.gdgz.whitelist.controller.upload
 * @Description: TODO(用一句话描述该文件做什么)
 * @author wxc
 * @date 2017年10月23日 上午9:42:36
 * @version V1.0   
 */
package com.wxc.zipdata;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.wxc.db.JdbcUtil;


/**
 * @author wxc
 * @version 创建时间：2017年10月23日 上午9:42:36
 */
@Controller
@RequestMapping("downLoad")
public class DownloadAction {
	@Autowired
	private DataSource dataSource;

	@RequestMapping(value = "exportFile")
	public String download(
			String export_type,String export_name,String export_time,
			HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("type", export_type);
		map.put("name", export_name);
		map.put("time", export_time);
		HttpSession session = request.getSession();
		int port = (int) session.getAttribute("port");
		
		Timestamp upTime = (Timestamp) session.getAttribute("upTime");
		
		String serverPath = "c:/导白数据/" + port + "/";// 压缩包的文件
		File file = new File(serverPath);
		if (!file.exists())
			file.mkdirs();
		List<File> srcfile = getExcelData(map, port, upTime);
		
		file=new File("c:/导白数据/" + port + "/"+port+".zip");
		FileOutputStream outStream=new FileOutputStream(file);
		ZipOutputStream toClient=new ZipOutputStream(outStream);
		zipFile(srcfile, toClient);
		toClient.close();
		outStream.close();
//		zipFiles(srcfile, file);
		downloadFile(file, response, true);
		for(int i=0;i<srcfile.size();i++) {
			File f=srcfile.get(i);
			f.delete();
		}
		return null;
	}

	private String sql = "select top 10000  m.mobile from ( "
			+ "	select ROW_NUMBER() OVER(order by pm.upLoadTime asc) oi,"
			+ " pm.mId,pm.pId,pm.upLoadTime from PM ) pm,Port p,Mobile m "
			+ " where pm.mId=m.mId and p.pId=pm.pId and oi>@begin ";

	/**
	 * 
	 * @param param
	 * @param port
	 * @param upTime
	 * @return
	 */
	private List<File> getExcelData(Map<String, Object> param, int port, Timestamp upTime) {
		String fileName = (String) param.get("name");
		String type = (String) param.get("type");
		String forceTime = (String) param.get("time");
		List<File> listFile = new ArrayList<>();
		Connection conn = null;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;

		Workbook wb = null;
		Sheet sheet = null;
		String excelPath = "c:/导白数据/" + port + "/";
		String[] title = { "手机号码", "操作类型", "生效时间", "失效时间" };
		try {
			conn = dataSource.getConnection();
			int total = countInfo(port, upTime);// 导出数据的数量
			int yu = total % 10000;
			int num = total / 10000;
			String s = requireSql(port, upTime, sql).toString();
			if (yu > 0)
				num += 1;
			for (int i = 1; i <= num; i++) {
				int begin = 0;
				File f = new File(excelPath + fileName + "_" + i + ".xlsx");
				wb = new XSSFWorkbook();
				sheet = wb.createSheet("Sheet1");
//				DataFormat format=wb.createDataFormat();
				Row row = sheet.createRow(0);
				row.setHeight((short) 500);
				Cell cell = row.createCell(0);
//				CellStyle style = wb.createCellStyle();// 样式对象
//				style.setFillForegroundColor(HSSFColor.PALE_BLUE.index);
//				style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
//				style.setAlignment(CellStyle.ALIGN_CENTER);
//				style.setDataFormat(format.getFormat("yyyy/m/d"));
//				style.setWrapText(true);// 显示不下时自动换行
				CellStyle style=wb.createCellStyle();
				DataFormat fmat=wb.createDataFormat();
				style.setDataFormat(fmat.getFormat("yyyy/m/d"));
				cell.setCellStyle(style);
				for (int j = 0; j < title.length; j++) {
					cell = row.createCell(j);
					cell.setCellValue(title[j]);
					if(j==0)
						cell.setCellType(HSSFCell.CELL_TYPE_STRING);
					if(j==2||j==3) {
						cell.setCellStyle(style);
					}
					sheet.setColumnWidth(j, 20 * 256);
				}

				pStatement = conn.prepareStatement(s.replace("@begin", begin + ""));
				resultSet = pStatement.executeQuery();
				int index = 0;
				while (resultSet.next()) {
					String mobile = resultSet.getString("mobile");
					row = (Row) sheet.createRow(index + 1);
					row.setHeight((short) 500);
					Cell c0=row.createCell(0);
					c0.setCellType(HSSFCell.CELL_TYPE_STRING);
					c0.setCellValue(mobile);
//					row.createCell(0).setCellValue(mobile);
					row.createCell(1).setCellValue(type);
					Cell c2=row.createCell(2);
					c2.setCellStyle(style);
					c2.setCellValue(forceTime);
//					row.createCell(2).setCellValue(forceTime);
					Cell c3=row.createCell(3);
					c3.setCellStyle(style);
					c3.setCellValue("2110/2/2");
//					row.createCell(3).setCellValue("2110/2/2");
					index++;
				}

				OutputStream outputStream = new FileOutputStream(excelPath + fileName + "_" + i + ".xlsx");
				wb.write(outputStream);
				outputStream.flush();
				outputStream.close();
				listFile.add(f);
				begin += 10000;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.closeResultSet(resultSet);
			JdbcUtil.closeStatement(pStatement);
			JdbcUtil.closeConnection(conn);
		}
		return listFile;
	}

	/**
	 * 符合查询条件下，一共有多少条记录
	 * 
	 * @param param
	 * @return
	 */
	private int countInfo(int port, Timestamp upTime) {
		StringBuilder sql = requireSql(port, upTime, countSql);
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
			e.printStackTrace();
		} finally {
			JdbcUtil.closeResultSet(resultSet);
			JdbcUtil.closeStatement(countSt);
			JdbcUtil.closeConnection(conn);
		}
		return count;
	}

	private String countSql = "select count(0) from PM pm,Port p,Mobile m where pm.mId=m.mId and p.pId=pm.pId ";

	private StringBuilder requireSql(int port, Timestamp upTime, String sql) {
		StringBuilder sbuilder = new StringBuilder(sql);
		sbuilder.append(" and p.Port=" + port);
		sbuilder.append(" and pm.upLoadTime='" + upTime.toString()+"'");
		return sbuilder;
	}

	public static void zipFile(List<File> files, ZipOutputStream outputStream) throws IOException, ServletException {
		try {
			int size = files.size();
			// 压缩列表中的文件
			for (int i = 0; i < size; i++) {
				File file = (File) files.get(i);
				zipFile(file, outputStream);
			}
		} catch (IOException e) {
			throw e;
		}
	}

	public static void zipFile(File inputFile, ZipOutputStream outputstream) throws IOException, ServletException {
		try {
			if (inputFile.exists()) {
				if (inputFile.isFile()) {
					FileInputStream inStream = new FileInputStream(inputFile);
					BufferedInputStream bInStream = new BufferedInputStream(inStream);
					ZipEntry entry = new ZipEntry(inputFile.getName());
					outputstream.putNextEntry(entry);

					final int MAX_BYTE = 50 * 1024 * 1024; // 最大的流为10M
					long streamTotal = 0; // 接受流的容量
					int streamNum = 0; // 流需要分开的数量
					int leaveByte = 0; // 文件剩下的字符数
					byte[] inOutbyte; // byte数组接受文件的数据

					streamTotal = bInStream.available(); // 通过available方法取得流的最大字符数
					streamNum = (int) Math.floor(streamTotal / MAX_BYTE); // 取得流文件需要分开的数量
					leaveByte = (int) streamTotal % MAX_BYTE; // 分开文件之后,剩余的数量

					if (streamNum > 0) {
						for (int j = 0; j < streamNum; ++j) {
							inOutbyte = new byte[MAX_BYTE];
							// 读入流,保存在byte数组
							bInStream.read(inOutbyte, 0, MAX_BYTE);
							outputstream.write(inOutbyte, 0, MAX_BYTE); // 写出流
						}
					}
					// 写出剩下的流数据
					inOutbyte = new byte[leaveByte];
					bInStream.read(inOutbyte, 0, leaveByte);
					outputstream.write(inOutbyte);
					outputstream.closeEntry(); // Closes the current ZIP entry
					// and positions the stream for
					// writing the next entry
					bInStream.close(); // 关闭
					inStream.close();
				}
			} else {
				throw new ServletException("文件不存在！");
			}
		} catch (IOException e) {
			throw e;
		}
	}

	public void downloadFile(File file,HttpServletResponse response,boolean isDelete) {
        try {
            // 以流的形式下载文件。
            BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file.getPath()));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            // 清空response
            response.reset();
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(file.getName().getBytes("UTF-8"),"ISO-8859-1"));
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
            if(isDelete)
            {
                file.delete();        //是否将生成的服务器端文件删除
            }
         } 
         catch (IOException ex) {
            ex.printStackTrace();
        }
    }
	
}
