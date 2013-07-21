<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<LINK REL="SHORTCUT ICON"
       HREF="icon.png">
<title>智能客户端界面启动</title>
<script type="text/javascript" src="/easyservice-util/EasyServiceClientFullZ.js"></script>
<script type="text/javascript" src="/easyservice/com.cnebula.platform.admin.IPlatformStatusService?json&v=1"></script>
<script type="text/javascript">
var platformService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.platform.admin.IPlatformStatusService");
function setLoginTitle() {
   try{
	   var appVersion=platformService.getPlatformEnv("APP_FULL_VER");
       var label=document.getElementById('appFullVersion');
	   label.innerHTML ="version:"+appVersion;
   } catch(e) {
   	   // TODO
   	   alert(e);
   }
}
</script>

<style type="text/css">
.foot_logo {	background-image: url(ext/foot-logo.jpg);
	background-repeat: no-repeat;
	background-position: left;
}
<!--
a:link	{ text-decoration: underline; color: blue; }
body			{scrollbar-base-color: #E4EAF2; scrollbar-arrow-color: #648EB2;  font-size: 12px; background-color: #FFFFFC }
table			{ font-family: tahoma,MS Shell Dlg,Arial,Helvetica,verd; color: #323456; font-size: 12px; 			  
}
.cellstyle {border : 1px solid #648EB2 }
.mybutton { border : 1px solid #648EB2; font-family: tahoma,MS Shell Dlg,Arial,Helvetica,verd; font-size: 12px;  color: #323456; font-weight: normal; background-color: #E4EAF2 }
select			{ font-family: 宋体; font-size: 12px;  color: #323456; font-weight: normal; background-color: #E4EAF2 }
.header			{ font-family: tahoma,MS Shell Dlg,Arial,Helvetica,verd; font-size: 12px; color: #323456; font-weight: bold; background-color: #BCD0ED }
-->
</style>
</head>

<body onload="setLoginTitle();return false;"style = "scrollbar-shadow-color :#648EB2;"  bgcolor="#e6e6e6" class="generyFont" leftmargin="10"  rightmargin="10" topmargin="0"  bottommargin="0"  style="overflow-x:hidden;overflow-y:auto"> 
<table cellspacing="1" cellpadding="1"   width="95%">
  <tr class="cellstyle"> 
    <td colspan="2" class="header" align="center">#{site.uiname}智能客户端界面启动步骤 </td>
  </tr>
  <tr> 
    <td width="7%"><img src="ext/warning64.gif"></td>
    <td width="93%">一、准备JRE6或JDK6</td>
  </tr>
  <tr class="cellstyle"> 
    <td align="center" valign="middle">1）</td>
    <td><p>在启动智能客户端前，请确定您已安装JRE6或JDK6以及以上版本。</p></td>
  </tr>
  <tr> 
    <td align="center">2）</td>
    <td>如果您尚未安装JRE6或JDK6以及以上版本，请点击从SUN公司的Java.com下载<a href="#{site.remoteJRE}">JRE6.0</a>，<br/>
	或者从本站下载<a href="#{site.localJRE}">JRE6.0</a><font  color="#009966"> 
       </td>
  </tr>
  <tr> 
    <td align="center" valign="center">3）</td>
    <td>无法访问国际网的用户（如某些教育网用户）请注意，在您运行JRE安装程序时候，<br>该安装程序将会尝试连国际网检测更新，所以请耐心等待数分钟其安装界面才会出现。</td>
  </tr>
  <tr> 
    <td align="center" valign="center"><img src="ext/ipregister.gif" width="67" height="67"></td>
    <td>二、启动智能客户端</td>
  </tr>
  <tr> 
    <td align="center" valign="bottom">1）</td>
    <td> 点击#{site.uiname}<a href="#{curpath}/cnebulaUI.boot?110331.jnlp"><img src="icon.png"></a>直接启动</td>
  </tr>
  <tr> 
    <td align="center" valign="bottom">2）</td>
    <td>首次启动成功后，将会在您的桌面上创建一个快捷启动图标<img src="icon.png">，以后您可以选择直接双击此桌面图标启动客户端，
	也可仍然使用本页面上面图标启动客户端</td>
 </tr>

  <tr> 
 <td align="left" height="120" colspan="2">&nbsp;</td>
  </tr>
  
  <tr>
    <td align="center" colspan="2">
 	 <table id="logoBlock" width="410" border="0" align="center" cellpadding="0" cellspacing="0" style="height:100px;" class="foot_logo">
	      <tr>
	        <td align="center" valign="center">中国高等教育文献保障系统（CALIS）管理中心　版权所有</td>
			<td></td>
             </tr>
              <tr>
			<td align="center" valign="center">Copyright(C) 1996-2011 CALIS All rights reserved.</td>
			<td></td>
             </tr>
              <tr>
			 <td id="appFullVersion" align="center" valign="center"></td>
         <td></td>
	      </tr>
	    </table>
    </td>
  </tr>
</table>
</body>
</html>
