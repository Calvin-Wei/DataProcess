/**
 * @Title: CompareAction.java
 * @Package com.gdgz.whitelist.controller.upload
 * @Description: TODO(用一句话描述该文件做什么)
 * @author wxc
 * @date 2017年10月26日 下午8:09:50
 * @version V1.0   
 */
package com.wxc.zipdata;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.ProgressListener;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.wxc.db.CompareExcel;
import com.wxc.db.CompareTC;
import com.wxc.db.JdbcUtil;
import com.wxc.util.Progress;
import com.wxc.util.TimeUtils;



/**
 * @author wxc
 * @version 创建时间：2017年10月26日 下午8:09:50
 */
@Controller
@RequestMapping("compare")
public class CompareAction implements ProgressListener {
	private HttpSession session;
	@Autowired
	CompareExcel compareExcel;
	@Autowired
	CompareTC compareTC;

	@RequestMapping(value = "compareData", method = RequestMethod.POST)
	public String compare(/*@RequestParam("file_input") MultipartFile file,*/
			MultipartFile file_compare,String compare_port, HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
//		int port = Integer.parseInt(request.getParameter("cport"));
		int port=Integer.parseInt(compare_port);
		File comparefile=new File("c:/比对数据/"+port+"/");
		if (!file_compare.isEmpty()) {
			String name = file_compare.getOriginalFilename();
			name = (name.substring(name.lastIndexOf(".") + 1, name.length())).toLowerCase();
			if (name.equals("xlsx") || name.equals("xls")) {
				try {
					long costTime = System.currentTimeMillis();
					map = compareExcel.compareExcel(file_compare, port);
					logger.info("------------------------------>" + TimeUtils.costTime(costTime));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (name.equals("txt") || name.equals("csv")) {
				long costTime = System.currentTimeMillis();
				map=compareTC.compareTC(file_compare, port);
				logger.info("---------------------------------->" + TimeUtils.costTime(costTime));
			}
			
			if(!comparefile.exists())
				comparefile.mkdirs();
			
			comparefile=compareData(port);
			
		} else {
			map.put("code", "Error");
		}
		downloadFile(comparefile, response, true);
		return null;
	}
	@Autowired
	private DataSource dataSource;
	private Logger logger=LoggerFactory.getLogger(getClass());
	
	private String compareSql="select mobile from temp" + 
			" except " + 
			" select m.mobile from PM pm,Mobile m,Port p" + 
			" where m.mId=pm.mId and p.Port=@P and p.pId=pm.pId";
	/**
	 * @param port
	 */
	private File compareData(int port) {
		Connection conn=null;
		PreparedStatement sqlST=null;
		ResultSet resultSet=null;
		String data=null;
		File file=null;
		Workbook wb=null;
		Sheet sheet=null;
		String excelPath = "c:/导白数据/" + port + "/";
		try {
			conn=dataSource.getConnection();
			sqlST=conn.prepareStatement(compareSql.replace("@P", port+""));
			resultSet=sqlST.executeQuery();
			wb=new XSSFWorkbook();
			sheet=wb.createSheet("Sheet1");
			Row row=null;
			int index=0;
			file=new File(excelPath+port+".xlsx");
			while(resultSet.next()) {
				data=resultSet.getString("mobile");
				row=sheet.createRow(index);
				row.setHeight((short) 500);
				row.createCell(0).setCellValue(data);
				index++;
			}
			OutputStream outputStream = new FileOutputStream(excelPath + port  + ".xlsx");
			wb.write(outputStream);
			outputStream.flush();
			outputStream.close();
		} catch (SQLException | IOException e) {
			logger.error(e.getMessage(),e);
		} finally {
			JdbcUtil.closeResultSet(resultSet);
			JdbcUtil.closeStatement(sqlST);
			JdbcUtil.closeConnection(conn);
		}
		return file;
	}

	public void downloadFile(File file,HttpServletResponse response,boolean isDelete) {
		Connection conn=null;
		PreparedStatement pStatement=null;
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
            conn=dataSource.getConnection();
            pStatement=conn.prepareStatement("delete from temp");
            pStatement.executeUpdate();
         }catch (IOException ex) {
        	logger.error(ex.getMessage(),ex);
         }catch (SQLException e) {
			logger.error(e.getMessage(),e);
		 }finally {
			JdbcUtil.closeStatement(pStatement);
			JdbcUtil.closeConnection(conn);
		 }
        
    }
	
	public void setSession(HttpSession session) {
		this.session = session;
		Progress status = new Progress();
		session.setAttribute("status", status);
	}

	@Override
	public void update(long byteRead, long contentLength, int items) {
		Progress status = (Progress) session.getAttribute("status");
		status.setByteRead(byteRead);
		status.setContentLength(contentLength);
		status.setItems(items);
	}
}
