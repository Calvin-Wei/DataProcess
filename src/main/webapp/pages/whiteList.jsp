<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>
<!DOCTYPE html>
<html>

<head>
<meta charset="UTF-8">
<title>白名单数据</title>
<script src="<%=basePath%>/js/jquery.min.js"></script>
<script type="text/javascript" src="<%=basePath%>/date/js/laydate.js"></script>
<link rel="stylesheet" type="text/css"
	href="<%=basePath%>/bootstrap-3.3.7-dist/css/bootstrap.css" />
<script type="text/javascript"
	src="<%=basePath%>/bootstrap-3.3.7-dist/js/bootstrap.min.js"></script>
<!-- bootstrap-table -->
<link rel="stylesheet" type="text/css"
	href="<%=basePath%>/bootstrap-3.3.7-dist/bootstrap-table/bootstrap-table.min.css" />
<script type="text/javascript"
	src="<%=basePath%>/bootstrap-3.3.7-dist/bootstrap-table/bootstrap-table-locale-all.min.js"></script>
<script type="text/javascript"
	src="<%=basePath%>/bootstrap-3.3.7-dist/bootstrap-table/bootstrap-table.min.js"></script>
<script src="<%=basePath%>/js/ajaxfileupload.js" type="text/javascript"></script>
<style type="text/css">
body {
	padding-bottom: 40px;
}

.sidebar-nav {
	padding: 9px 0;
}

@media ( max-width : 980px) {
	/* Enable use of floated navbar text */
	.navbar-text.pull-right {
		float: none;
		padding-left: 5px;
		padding-right: 5px;
	}
}

@media ( max-width : 980px) {
	/* Enable use of floated navbar text */
	.navbar-text.pull-right {
		float: none;
		padding-left: 5px;
		padding-right: 5px;
	}
	body {
		width: 1365px;
	}
}
</style>
</head>

<body>
	<form style="text-align: center;" class="form-inline definewidth m20"
		method="post">
		端口号： <input type="text" id="port" name="port"
			class="abc input-default" placeholder="" value=""> 手机号： <input
			name="mobile" id="mobile" placeholder="" class="abc input-default">
		导入时间： <input name="upTime" id="upTime" placeholder="请输入日期"
			class="laydate-icon"
			onClick="laydate({istime: true, format: 'YYYY-MM-DD hh:mm:ss'})">
		<button id="search" name="search" type="button" onclick="btnclick()"
			class="btn btn-primary">查询</button>
		&nbsp;&nbsp;
	</form>
	<div style="text-align: center">

		<button class="btn btn-primary" data-toggle='modal'
			data-target='#batchImportModal' id='import_btn'>导入文件</button>
		<button class="btn btn-primary" id="export_btn" data-toggle='modal'
			data-target="#exportModal">导出文件</button>
		<button class="btn btn-primary" id="compare_btn" data-toggle='modal'
			data-target="#compareModal">数据比对</button>
	</div>
	<table id="table" style="overflow-x: auto; margin-right: 20px；"
		class="table table-bordered table-hover definewidth m10">
	</table>

	<!-- Modal -->
	<div id="batchImportModal" class="modal fade" role="dialog"
		aria-labelledby="myModalLabel" data-backdrop="static"
		aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button id="close_Dialog" type="button" class="close"
						data-dismiss="modal" aria-hidden="true">x</button>
					<h4 class="modal-title" id="myModalLabel">导入白名单</h4>
				</div>
				<div class="modal-body">
					<form id="uploadForm" method="post" enctype="multipart/form-data">
						<div class="form-group" id="passwordDiv">
							<label>数据文件支持：csv,txt,excel,文件大小最好控制在25m以下</label> <input
								type="file" id="file_input" name="file_input"> <br>
							<br> 端口号： <input name="dialog_port" id="dialog_port"
								class="abc input-default" type="text"
								onkeyup="(this.v=function(){this.value=this.value.replace(/[^0-9-]+/,'');}).call(this)"
								onblur="this.v();" placeholder="" value=""> 签名： <input
								name="dialog_sign" id="dialog_sign" placeholder=""
								class="abc input-default" value="">
						</div>
						<div class="progress progress-striped active"
							style="display: none">
							<div id="progressBar" name="progressBar"
								class="progress-bar progress-bar-info" role="progressbar"
								aria-valuemin="0" aria-valuenow="0" aria-valuemax="100"
								style="width: 0%"></div>
						</div>
						<div class="form-group">
							<input id="uploadBtn" type="submit" name="uploadBtn"
								class="btn btn-success" onclick="upLoadClick()" value="上传" />
						</div>
					</form>
				</div>
			</div>
			<!-- /.modal-content -->
		</div>
		<!-- /.modal-dialog -->
	</div>
	<!-- /.modal -->

	<!-- Modal -->
	<div id="exportModal" class="modal fade" role="dialog"
		aria-labelledby="myModalLabel" data-backdrop="static"
		aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button id="close_Dialog" type="button" class="close"
						data-dismiss="modal" aria-hidden="true">x</button>
					<h4 class="modal-title" id="myModalLabel">导出白名单文件</h4>
				</div>
				<div class="modal-body">
					<form id="exportForm"
						action="${pageContext.request.contextPath}/downLoad/exportFile.action"
						onsubmit="return download();" method="post">
						<div class="form-group" id="passwordDiv">
							<label>数据文件支持:excel.</label><br> <br> 操作类型:<input
								name="export_type" id="export_type" placeholder=""
								class="abc input-default" value=""> <br> 文件命名规则:<input
								name="export_name" id="export_name" placeholder=""
								class="abc input-default" value=""> 生效时间:<input
								name="export_time" id="export_time" placeholder="请输入日期"
								class="laydate-icon"
								onClick="laydate({istime: true, format: 'YYYY/MM/DD'})">
						</div>
						<div class="progress progress-striped active"
							style="display: none">
							<div id="progressBar_export" name="progressBar_export"
								class="progress-bar progress-bar-info" role="progressbar"
								aria-valuemin="0" aria-valuenow="0" aria-valuemax="100"
								style="width: 0%"></div>
						</div>
						<div class="form-group">
							<input id="exportBtn" type="submit" name="exportBtn"
								class="btn btn-success" value="导出文件" />
						</div>
					</form>
				</div>
			</div>
			<!-- /.modal-content -->
		</div>
		<!-- /.modal-dialog -->
	</div>
	<!-- /.modal -->

	<!-- Modal -->
	<div id="compareModal" class="modal fade" role="dialog"
		aria-labelledby="myModalLabel" data-backdrop="static"
		aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button id="close_cDialog" type="button" class="close"
						data-dismiss="modal" aria-hidden="true">x</button>
					<h4 class="modal-title" id="myModalLabel">比对白名单数据,数据将以excel的格式进行导出</h4>
				</div>
				<div class="modal-body">
					<form id="compareForm"
						action="${pageContext.request.contextPath}/compare/compareData.action"
						enctype="multipart/form-data" method="post"
						onsubmit="return compareClick();">
						<div class="form-group" id="passwordDiv">
							<label>数据文件支持：csv,txt,excel,文件大小最好控制在25m以下</label> <input
								type="file" id="file_compare" name="file_compare"> <br>
							<br> 端口号： <input name="compare_port" id="compare_port"
								class="abc input-default" type="text"
								onkeyup="(this.v=function(){this.value=this.value.replace(/[^0-9-]+/,'');}).call(this)"
								onblur="this.v();" placeholder="" value="">
						</div>
						<div class="progress progress-striped active"
							style="display: none">
							<div id="cprogressBar" name="cprogressBar"
								class="progress-bar progress-bar-info" role="progressbar"
								aria-valuemin="0" aria-valuenow="0" aria-valuemax="100"
								style="width: 0%"></div>
						</div>
						<div class="form-group">
							<input id="compareBtn" type="submit" name="compareBtn"
								class="btn btn-success" value="上传目标文件" />
						</div>
					</form>
				</div>
			</div>
			<!-- /.modal-content -->
		</div>
		<!-- /.modal-dialog -->
	</div>
	<!-- /.modal -->

	<script type="text/javascript">
			class BstpTable {
				constructor(obj) {
					this.obj = obj;
				}
				inint(searchArgs) {
					this.obj.bootstrapTable('destroy');
					this.obj.bootstrapTable({
						url: '${pageContext.request.contextPath}/selectInfo/selectList.action',
						method: 'post',
						dataType: 'json',
						queryParamsType: '',
						queryParams: function queryParams(params) {
							var param = {
								pageNumber: params.pageNumber,
								pageSize: params.pageSize
							};
							for(var key in searchArgs) {
								param[key] = searchArgs[key];
							}
							return param;
						},
						locale: 'zh-CN',
						pagination: true,
						pageNumber: 1,
						pageSize: 10000,
						pageList: [10000, 10000],
						sidePagination: "server",
						showRefresh: true,
						height: 600,
						rowStyle: function(row, index) {
							var style = "";
							return {
								classes: style
							}
						},
						columns: [
						    {
								field:'Port',
								title:'端口号',
								width:'90'
							},
						    {
								field: 'Sign',
								title: '签名',
								width: '90'
							},
							{
								field: 'Mobile',
								title: '手机号',
								width: '90'
							},
							{
								field: 'UpTime',
								title: '导入时间',
								width: '120',
								formatter:resultFormatter
							}
						]
					});
				}
			}
			window.operateEvents = {
				
			}
			function getzf(num) {
				if(parseInt(num) < 10) {
					num = '0' + num;
				}
				return num;
			}

			function resultFormatter(cellvalue, timestamp, rowObject) {
				if(cellvalue == null) {
					return null;
				}
				var oDate = new Date(cellvalue),
					oYear = oDate.getFullYear(),
					oMonth = oDate.getMonth() + 1,
					oDay = oDate.getDate(),
					oHour = oDate.getHours(),
					oMin = oDate.getMinutes(),
					oSen = oDate.getSeconds(),
					oTime = oYear + '-' + getzf(oMonth) + '-' + getzf(oDay) + ' ' + getzf(oHour) + ':' + getzf(oMin) + ':' + getzf(oSen); //最后拼接时间    
				return oTime;
			}
			var bstpTable = new BstpTable($("#table"));
			bstpTable.inint({});
			
			function btnclick(){
				
				//js获取当前系统时间
				var date=new Date();
				var month=date.getMonth()+1;
				var strDate=date.getDate();
				if(month>=1&&month<=9){
					month="0"+month;
				}
				if(strDate>=0&&strDate<=9){
					strDate="0"+strDate;
				}
				var currentdate=date.getFullYear()+"-"+month+"-"+strDate
					+" "+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds();
				
				var searchArgs={
						port:$("#port").val(),
						upTime:$("#upTime").val(),
						mobile:$("#mobile").val()
					};
				
				var m=$("#mobile").val();
				if(m!=''){
					if(!(/^1[34578]\d{9}$/.test(m))){
						alert("手机号码有误，请重填");
						return ;
					}	
				}
				var upTime=$("#upTime").val();
				upTime=(new Date(upTime.replace(/-/g,"\/")));
				var currentTime=(new Date(currentdate.replace(/-/g,"\/")));
				if(upTime>currentTime){
					alert("上传文件的时间不能超过当前的时间!");
					return ;
				}
				else{
					bstpTable.inint(searchArgs);
				}		
			}
			
			function upLoadClick(){
				var fileName=$("#file_input").val();
				if(fileName==''){
					alert('请选择文件');
					return ;
				}
				var dialog_port=$("#dialog_port").val();
				var dialog_sign=$("#dialog_sign").val();
				if(dialog_port==''){
					alert("端口号不能为空");
					return ;
				}
				if(dialog_sign==''){
					alert('签名不能为空');
					return ;
				}
					
				var fileType=(fileName.substring(fileName.lastIndexOf(".")+1,fileName.length)).toLowerCase();
				if(fileType=='txt'||fileType=='csv'||fileType=='xls'||fileType=='xlsx'){
					//上传文件按钮				
					// 进度条归零
					$("#progressBar").width("0%");
					// 上传按钮禁用
					$("#uploadBtn").attr("disabled", true);
					//关闭模态框按钮禁用
					$("#close_Dialog").attr("disabled",true);
					// 进度条显示
					$("#progressBar").parent().show();
					$("#progressBar").parent().addClass("active");
					$("#uploadBtn").val("请稍候，文件上传并数据去重中！");
					upload("带进度条的文件上传");
				}else{
					alert("不支持该文件上传!");
				}
			}
			
			function refreshBtn(){
				setTimeout(function() {
					$("#uploadBtn").val("上传");
					$("#uploadBtn").removeAttr("disabled");
					$("#close_Dialog").removeAttr("disabled");
				}, 1500);
			}
			function upload(name) {
				var dialog_port=$("#dialog_port").val();
				var dialog_sign=$("#dialog_sign").val();
				
				var formData = new FormData();
				formData.append('file_input', $('#file_input')[0].files[0]);
				formData.append('dport',dialog_port);
				formData.append('dsign',dialog_sign);
				function onprogress(evt) {
					// 写要实现的内容
					var progressBar = $("#progressBar");
					if (evt.lengthComputable) {
						var completePercent = Math.round(evt.loaded / evt.total * 100);
						progressBar.width(completePercent + "%");
						$("#progressBar").text(completePercent + "%");
					}
				}
				var xhr_provider = function() {
					var xhr = jQuery.ajaxSettings.xhr();
					if (onprogress && xhr.upload) {
						xhr.upload.addEventListener('progress', onprogress, false);
					}
					return xhr;
				};
				
				$.ajax({
					url : '${pageContext.request.contextPath}/upLoad/upFile.action',
					type : 'POST',
					cache : false,
					data : formData,
					contentType:'application/json',
					processData : false,
					contentType : false,
					xhr : xhr_provider,
					success : function(result) {
						$("#uploadBtn").val("上传成功");
						setTimeout(function() {
							$("#uploadBtn").val("上传成功");
						}, 1000);
						alert("上传文件成功");
						$("#batchImportModal").modal('hide');						
						// 进度条隐藏
						$("#progressBar").parent().hide();
						refreshBtn();
						$("#uploadForm")[0].reset();
						bstpTable.inint(result);
					},
					error : function() {
						alert("上传文件失败,文件为空或者文件格式不正确!");
						refreshBtn();
						$("#uploadBtn").val('上传失败');
					}
				});
			}
			
			function download(){
				
				var name=$("#export_name").val();
				if(name==''){
					alert("文件的命名规则不能为空!");
					return false;
				}
				var type=$("#export_type").val();
				if(type==''){
					alert("操作类型不能为空!")
					return false;
				}
				var time=$("#export_time").val();
				if(time==''){
					alert("生效时间不能为空");
					return false;
				}
				
				function onprogress(evt) {
					// 写要实现的内容
					var progressBar = $("#progressBar_export");
					if (evt.lengthComputable) {
						var completePercent = Math.round(evt.loaded / evt.total * 100);
						progressBar.width(completePercent + "%");
						$("#progressBar_export").text(completePercent + "%");
					}
				}
				var xhr_provider = function() {
					var xhr = jQuery.ajaxSettings.xhr();
					if (onprogress && xhr.upload) {
						xhr.upload.addEventListener('progressBar_export', onprogress, false);
					}
					return xhr;
				};
				
				var data={
					name:name,
					type:type,
					time:time
				};
				
				return true;
				
				
			}
			
			
			
			function compareClick(){
				var fileName=$("#file_compare").val();
				if(fileName==''){
					alert('请选择文件');
					return false;
				}
				var compare_port=$("#compare_port").val();
				if(compare_port==''){
					alert("端口号不能为空");
					return false;
				}
				var fileType=(fileName.substring(fileName.lastIndexOf(".")+1,fileName.length)).toLowerCase();
				if(fileType=='txt'||fileType=='csv'||fileType=='xls'||fileType=='xlsx'){
					$("#compareBtn").val("请不要关闭窗口，文件上传并数据比对中！");
					compare("带进度条的文件上传");
					return true;
				}else{
					alert("不支持该文件上传!");
					return false;
				}
			}
			function refreshCBtn(){
				setTimeout(function() {
					$("#compareBtn").val("上传");
					$("#compareBtn").removeAttr("disabled");
					$("#close_cDialog").removeAttr("disabled");
				}, 15000);
			}
			function compare(name) {
				function onprogress(evt) {
					// 写要实现的内容
					var progressBar = $("#cprogressBar");
					if (evt.lengthComputable) {
						var completePercent = Math.round(evt.loaded / evt.total * 100);
						progressBar.width(completePercent + "%");
						$("#cprogressBar").text(completePercent + "%");
					}
				}
				var xhr_provider = function() {
					var xhr = jQuery.ajaxSettings.xhr();
					if (onprogress && xhr.upload) {
						xhr.upload.addEventListener('progress', onprogress, false);
					}
					return xhr;
				};
			}
			
	</script>
</body>

</html>