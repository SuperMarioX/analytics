<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="SHORTCUT ICON" href="icon.png"/>
<title>#{site.uiname}</title>
<style type="text/css">
body {margin:0px; font-size:12px; line-height:30px;}
.table {
	border: 1px solid #cecece;
}

.font1 {
	font-size:16px;
	font-weight: bold;
	color: #FFFFFF;
}
.font2 {
	font-size: 12px;
	color: #333333;
}
.font3 {
	font-size: 12px;
	font-weight: bold;
	color: #333333;
}
.foot_logo {	background-image: url(ext/foot-logo.jpg);
	background-repeat: no-repeat;
	background-position: left;
}
.userinfo{
	font-size:12px;
	font-weight: bold;
	color: #FFFFFF;
}
.userinfo a{
	color:#FFFFFF;
}
</style>
<script type="text/javascript" src="/easyservice-util/EasyServiceClientFullZ.js"></script>
<script type="text/javascript" src="/easyservice/com.cnebula.common.security.auth.ILoginService?json&v=1"></script>
<script type="text/javascript" src="/easyservice/com.cnebula.platform.admin.IPlatformStatusService?json&v=1"></script>
<script type="text/javascript">
var uasInteType = "#{uasInteType}";
var uasHost = "#{uashost}";
var uasPort = "#{uasport}";
var artifact;
var platformService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.platform.admin.IPlatformStatusService");
var loginService = EasyServiceClient.getRemoteProxy("/easyservice/com.cnebula.common.security.auth.ILoginService");
var recLastLoginType = null;


function getRequestParam(name) {
	var search = location.search;
	var patt = /^\?/g;
	search = search.replace(patt, "");
	var paramPaired = search.split('&');
	for (i = 0; i < paramPaired.length; i++) {
		var aParam = paramPaired[i].split('=');
		if (aParam[0] == name)
			return aParam[1];
	}
	return null;
}

function addToFavour(){
	var location = window.location;
	var protocol = location.protocol;
	var host = location.host;
	var url = protocol + "//" + host + "/update/";
	if (window.sidebar) { 
		window.sidebar.addPanel(document.title,url,""); 
	} else if( document.all ) {
		window.external.AddFavorite(url,document.title);
	} else if( window.opera && window.print ) {
		alert("不能识别的浏览器，请用户自行添加本页到收藏夹！");
	}
}

function openClient(){
	if(!artifact || ""==artifact){
		alert("请先登录!");
		return;
	}
    	var url = location.protocol + "//" + location.host + "/update/acnebulaUI.boot?artifact=" + artifact;
	window.open(url);
}

function login(type){
  var location = window.location;
  var protocol = location.protocol;
  var host = location.host;
  var baseURL = protocol + "//" + host + "/";
  var clientArtifactLoginURL = baseURL + "update/default.st?lastLoginType="  + type;
  var uasLoginURL = baseURL + "amconsole/AuthServices?verb=login&goto=" + encodeURIComponent(clientArtifactLoginURL);
  if(type == 1)
  	uasLoginURL += "&syslogin=1&forcelogin=true";
  if(type == 2)
  	uasLoginURL += "&syslogin=2";
  if(recLastLoginType != null && type != recLastLoginType)
	uasLoginURL = uasLoginURL.replace(/verb=login/,"verb=logout");	
  window.location.href = uasLoginURL;
}

function init(){

 var appVersion=platformService.getPlatformEnv("APP_FULL_VER");
           var label=document.getElementById('appFullVersion')
	   label.innerHTML ="version:"+appVersion;

	recLastLoginType = getRequestParam("lastLoginType");
	var url = window.location.href;
	if(url.indexOf("syslogin=1")>0)
		login(1);
	if(url.indexOf("syslogin=0")>0)
		login(0); 
	artifact = getRequestParam("artifact");
	displayLoginInfo();
	
	if( "#{normalAdminLoginButton}" == "none" )
	{
		document.getElementById ("normalAdminLoginButton").style.display = "none";
		document.getElementById ("normalAdminLoginText").style.display = "none";
		document.getElementById ("normalAdminLoginEtc").style.display = "none";
	}
	
}
function displayLoginInfo(){
	var user = loginService.getUser();
	if(user.id != "$anonymous"){
		showWelcomeInfo(user);
	}else{
		var artifact = getRequestParam('artifact');
		if(artifact != null){
			var user = loginService.loginByCredential(artifact) ;
			showWelcomeInfo(user);
		}
	}
}

function showWelcomeInfo(user){
		document.getElementById('userinfo').innerHTML = "欢迎," + user.name + "&nbsp;&nbsp;";
		document.getElementById('userinfo').className = "userinfo";
		var link = document.createElement("a");
		link.innerHTML = "退出";
		link.href = "#";
		//link.className = "font1 fontLogout";
		if(document.addEventListener){
			link.addEventListener("click",logout,false);
		}
		if(document.attachEvent){
			link.attachEvent("onclick",logout);
		}
		document.getElementById('userinfo').appendChild(link);
}

//退出
function logout(){
	var uasLoginService;
	if(parseInt(uasInteType) != 3 ){
		uasLoginService = EasyServiceClient.cloneWithNewTarget(loginService, "http://" + uasHost + ":" + uasPort + "/easyservice/com.cnebula.common.security.auth.ILoginService");
		uasLoginService.logout(function(){
  		loginService.logout();
  		refreshPage();
  	});
	}else{
		loginService.logout();
		refreshPage();
	}
}

//刷新页面
function refreshPage(){
	var url = window.location.protocol + "\/\/" + window.location.host + "\/" + "update/";
  	window.location = url;
}
</script>
</head>

<body onload="init();return false;">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td align="center"><table width="1000" border="0" cellspacing="0" cellpadding="0" class="table">
      <tr>
        <td width="83" height="80" align="left" style="background:url(ext/bg-banner.gif);" bgcolor="#145393" class="font1"><img src="ext/logo.gif" width="83" height="80" /></td>
        <td width="915" align="left" style="background:url(ext/bg-banner.gif);" bgcolor="#145393" class="font1">#{site.uiname} 智能客户端</td>
      	<td width="600" align="right" style="background:url(ext/bg-banner.gif);" bgcolor="#145393" class="font1" id="userinfo"></td>
      </tr>
    </table></td>
  </tr>
  <tr>
    <td align="center"><table width="1000" border="0" cellspacing="0" cellpadding="0" class="table">
      <tr>
        <td align="center"><table width="92%" border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td align="left">&nbsp;</td>
          </tr>
          <tr>
            <td align="left" class="font3" colspan="2">一、准备JRE6或JDK6</td>
          </tr>
          <tr>
            <td align="top" valign="top" width="10%">1）</td><td align="left" class="font2">在启动智能客户端前，请确定您已安装JRE6或JDK6及以上版本。</td>
          </tr>
          <tr>
            <td align="top" valign="top" width="10%">2）</td><td align="left" class="font2">如果您尚未安装JRE6或JDK6级以上版本，请点击Oracle公司链接 <a href="" onclick="window.open('#{site.remoteJRE}');return false;">JRE6.0</a>下载最新版的JRE，或从本站下载 <a href="" onclick="window.open('#{site.localJRE}');return false;">JRE6.0</a>。</td>
          </tr>
          <tr>
            <td align="top" valign="top" width="10%">3）</td><td align="left" class="font2">无法访问国际网的用户（如某些教育网用户）请注意，在进运行安装JRE程序的时候，该安装程序将会尝试连接国际网检测更新，所以请耐心等待数分钟其安装界面才会出现。</td>
          </tr>
          <tr>
            <td align="left" class="font3" height="40" colspan="2">&nbsp;</td>
          </tr>
          <tr>
            <td align="left" class="font3" colspan="2">二、启动智能客户端</td>
          </tr>
          <tr>
            <td align="top" valign="top" width="10%">1）</td><td align="left" class="font2">若您已经登录，请点击右侧系统图标&nbsp; <img src="icon.png" align="top" border="0" onclick="openClient();return false;" style="cursor:pointer;"/>&nbsp;启动客户端。</td>
          </tr>
          <tr>
            <td align="top" valign="top" width="10%">2）</td><td align="left" class="font2">若您还没有登录，请点击 <span id="normalAdminLoginText" style="color:red;">"普通管理员登录"</span><span id="normalAdminLoginEtc">按钮或</span><span style="color:red;">"超级管理员登录"</span>按钮选择您的登录方式并登录，登录成功后将会回到该页面，然后点击<span style="color:red;"> "启动客户端"</span>按钮启动客户端。</td>
          </tr>
          <tr>
            <td align="top" valign="top" width="10%">3）</td><td align="left" class="font2">今后您需要从此页面登录客户端。您可以<a href="javascript:void(0);" onclick="addToFavour();">收藏</a>此页。</td>
          </tr>
          <tr>
            <td align="left" height="40" colspan="2">&nbsp;</td>
          </tr>
          <tr>
            <td align="left" colspan="2"><table width="40%" border="0" cellspacing="0" cellpadding="0">
              <tr>
                <td><input id="normalAdminLoginButton" type="image" onclick="login(2);return false;" src="ext/b1.gif" value="普通管理员登录"></input></td>
                <td><input type="image" onclick="login(1);return false;" src="ext/b2.gif" value="超级管理员登录"></input></td>
                <td><input type="image" onclick="openClient();return false;" src="ext/b3.gif" value="启动客户端"></input></td>
              </tr>
            </table></td>
          </tr>
          <tr>
            <td align="left" height="40" colspan="2">&nbsp;</td>
          </tr>
        </table></td>
      </tr>
    </table></td>
  </tr>
  <tr>
    <td align="center">
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
