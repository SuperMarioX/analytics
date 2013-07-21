package com.cnebula.analytics.common.rd;


/***
 * 
 * 	<column colName="rcs_a_t"   name="_type_"           type="TEXT"    length="32" ></column>
	<column colName="rcs_aid"   name="appId"            type="TEXT"    length="128"></column>
	<column colName="rcs_an"    name="appName"          type="TEXT"    length="255"></column>
	<column colName="rcs_av"    name="appVersion"       type="TEXT"    length="32" ></column>
	<column colName="rcs_ah"    name="host"             type="TEXT"    length="255"></column>
	<column colName="rcs_ap"    name="port"             type="TEXT"    length="12" ></column>
	<column colName="rcs_atn"   name="appType.name"     type="TEXT"    length="255"></column>
	<column colName="rcs_atfn"  name="appType.fullName" type="TEXT"    length="255"></column>
	<column colName="rcs_atc"   name="appType.code"     type="TEXT"    length="64" ></column>
	<column colName="rcs_att"   name="appType.type"     type="TEXT"    length="32" ></column>
	<column colName="rcs_atv"   name="appType.version"  type="TEXT"    length="32" ></column>
	<column colName="rcs_as"    name="status"           type="TEXT"    length="32" ></column>
	<column colName="rcs_acrt"  name="createTime"       type="TEXT"    length="24" ></column>
	<column colName="rcs_almt"  name="lastModifyTime"   type="TEXT"    length="24" ></column>
	<column colName="rcs_n_t"   name="nodeInfo._type_"  type="TEXT"    length="32" ></column>
	<column colName="rcs_nid"   name="nodeInfo.nodeId"  type="TEXT"    length="128"></column>
	<column colName="rcs_nn"    name="nodeInfo.name"    type="TEXT"    length="255"></column>
	<column colName="rcs_nsn"   name="nodeInfo.shortName"           type="TEXT"    length="255"></column>
	<column colName="rcs_nt"    name="nodeInfo.type"                type="TEXT"    length="128"></column>
	<column colName="rcs_nc"    name="nodeInfo.code"                type="TEXT"    length="128"></column>
	<column colName="rcs_ncc"   name="nodeInfo.centerCode"          type="TEXT"    length="128"></column>
	<column colName="rcs_npl"   name="nodeInfo.provinceOfLocation"  type="TEXT"    length="128"></column>
	<column colName="rcs_ncl"   name="nodeInfo.cityOfLocation"      type="TEXT"    length="128"></column>
	
	
	AppInfo表示应用信息，该应用表示1个抽象的应用。如一个SAAS系统中的每1个租用实例均为1个应用； 1个应用信息可以有1到多个服务信息（ServiceInfo）； 任何1条应用信息一定对应着一种应用类型；
	AppType表示应用类型，应用类型是用来描述应用信息的元数据；每1个开发出来的应用都可以为其创建相应的应用类型； 1个应用类型对应着多个服务类型（ServiceType）
	NodeInfo表示会员信息，1个会员机构可以有多个应用（AppInfo）；
	ServiceType表示服务类型，服务类型是用来描述服务信息的元数据；每1种服务均可以定义1种服务类型来描述它。
	ServiceInfo表示服务信息，一个应用可以有多条服务信息；任何1条服务信息一定对应着1种服务类型（ServiceType）；
 */
public class RCSInfo {
	
	String _type_;
	String appId;
	String appName;
	String appVersion;
	String host;
	String port;
	String status;
	String createTime;
	String lastModifyTime;
	
	String appTypeName;
	String appTypeFullName;
	String appTypeCode;
	String appTypeType;
	String appTypeVersion;

	String nodeInfo_type_;
	String nodeInfoNodeId;
	String nodeInfoName;
	String nodeInfoShortName;
	String nodeInfoType;
	String nodeInfoCode;
	String nodeInfoCenterCode;
	String nodeInfoProvinceOfLocation;
	String nodeInfoCityOfLocation;
	
	public String get_type_() {
		return _type_;
	}
	public void set_type_(String _type_) {
		this._type_ = _type_;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getAppVersion() {
		return appVersion;
	}
	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getLastModifyTime() {
		return lastModifyTime;
	}
	public void setLastModifyTime(String lastModifyTime) {
		this.lastModifyTime = lastModifyTime;
	}
	public String getAppTypeName() {
		return appTypeName;
	}
	public void setAppTypeName(String appTypeName) {
		this.appTypeName = appTypeName;
	}
	public String getAppTypeFullName() {
		return appTypeFullName;
	}
	public void setAppTypeFullName(String appTypeFullName) {
		this.appTypeFullName = appTypeFullName;
	}
	public String getAppTypeCode() {
		return appTypeCode;
	}
	public void setAppTypeCode(String appTypeCode) {
		this.appTypeCode = appTypeCode;
	}
	public String getAppTypeType() {
		return appTypeType;
	}
	public void setAppTypeType(String appTypeType) {
		this.appTypeType = appTypeType;
	}
	public String getAppTypeVersion() {
		return appTypeVersion;
	}
	public void setAppTypeVersion(String appTypeVersion) {
		this.appTypeVersion = appTypeVersion;
	}
	public String getNodeInfo_type_() {
		return nodeInfo_type_;
	}
	public void setNodeInfo_type_(String nodeInfo_type_) {
		this.nodeInfo_type_ = nodeInfo_type_;
	}
	public String getNodeInfoNodeId() {
		return nodeInfoNodeId;
	}
	public void setNodeInfoNodeId(String nodeInfoNodeId) {
		this.nodeInfoNodeId = nodeInfoNodeId;
	}
	public String getNodeInfoName() {
		return nodeInfoName;
	}
	public void setNodeInfoName(String nodeInfoName) {
		this.nodeInfoName = nodeInfoName;
	}
	public String getNodeInfoShortName() {
		return nodeInfoShortName;
	}
	public void setNodeInfoShortName(String nodeInfoShortName) {
		this.nodeInfoShortName = nodeInfoShortName;
	}
	public String getNodeInfoType() {
		return nodeInfoType;
	}
	public void setNodeInfoType(String nodeInfoType) {
		this.nodeInfoType = nodeInfoType;
	}
	public String getNodeInfoCode() {
		return nodeInfoCode;
	}
	public void setNodeInfoCode(String nodeInfoCode) {
		this.nodeInfoCode = nodeInfoCode;
	}
	public String getNodeInfoCenterCode() {
		return nodeInfoCenterCode;
	}
	public void setNodeInfoCenterCode(String nodeInfoCenterCode) {
		this.nodeInfoCenterCode = nodeInfoCenterCode;
	}
	public String getNodeInfoProvinceOfLocation() {
		return nodeInfoProvinceOfLocation;
	}
	public void setNodeInfoProvinceOfLocation(String nodeInfoProvinceOfLocation) {
		this.nodeInfoProvinceOfLocation = nodeInfoProvinceOfLocation;
	}
	public String getNodeInfoCityOfLocation() {
		return nodeInfoCityOfLocation;
	}
	public void setNodeInfoCityOfLocation(String nodeInfoCityOfLocation) {
		this.nodeInfoCityOfLocation = nodeInfoCityOfLocation;
	}
	
	
}
