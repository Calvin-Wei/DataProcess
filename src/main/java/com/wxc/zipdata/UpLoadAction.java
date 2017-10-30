package com.wxc.zipdata;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.ProgressListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.wxc.db.ImportExcel;
import com.wxc.db.ImportTC;
import com.wxc.util.Progress;
import com.wxc.util.TimeUtils;


@Controller

@RequestMapping("upLoad")
public class UpLoadAction implements ProgressListener {

	private HttpSession session;
	@Autowired
	ImportExcel importExcel;
	@Autowired
	ImportTC importTC;

	@RequestMapping(value = "upFile", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> upLoadFile(@RequestParam("file_input") MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) {
		HttpSession session=request.getSession();
		Map<String, Object> map = new HashMap<>();
		int port = Integer.parseInt(request.getParameter("dport"));
		String sign = request.getParameter("dsign");
		if (!file.isEmpty()) {
			String name = file.getOriginalFilename();
			name = (name.substring(name.lastIndexOf(".") + 1, name.length())).toLowerCase();
			if (name.equals("xlsx") || name.equals("xls")) {
				try {
					long costTime = System.currentTimeMillis();
					map = importExcel.importExcel(file, port, sign);
					System.out.println("------------------------------>" + TimeUtils.costTime(costTime));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (name.equals("txt") || name.equals("csv")) {
				long costTime = System.currentTimeMillis();
				map=importTC.importTC(file, port, sign);
				System.out.println("---------------------------------->" + TimeUtils.costTime(costTime));
			}
		} else {
			map.put("code", "Error");
		}
		session.setAttribute("port", map.get("port"));
		session.setAttribute("upTime", map.get("upTime"));
		return map;
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
