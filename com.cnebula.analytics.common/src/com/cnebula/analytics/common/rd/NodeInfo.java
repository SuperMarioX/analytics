package com.cnebula.analytics.common.rd;

import java.util.ArrayList;
import java.util.List;


public class NodeInfo {

	private String _type_;
	private String nodeId;	
	private String name;
	private String shortName;
	private String type;
	private String code;
	private String centerCode;
	private String provinceOfLocation;
	private String cityOfLocation;
	
	List<RCSAppInfo> listRCSAppInfo = new ArrayList<RCSAppInfo>();
	List<NodeInfo> listNodeInfoOfLibrary = new ArrayList<NodeInfo>();
	
	
	public List<RCSAppInfo> getListRCSAppInfo() {
		return listRCSAppInfo;
	}
	public void setListRCSAppInfo(List<RCSAppInfo> listRCSAppInfo) {
		this.listRCSAppInfo = listRCSAppInfo;
	}
	public List<NodeInfo> getListNodeInfoOfLibrary() {
		return listNodeInfoOfLibrary;
	}
	public void setListNodeInfoOfLibrary(List<NodeInfo> listNodeInfoOfLibrary) {
		this.listNodeInfoOfLibrary = listNodeInfoOfLibrary;
	}
	public String get_type_() {
		return _type_;
	}
	public void set_type_(String _type_) {
		this._type_ = _type_;
	}
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getCenterCode() {
		return centerCode;
	}
	public void setCenterCode(String centerCode) {
		this.centerCode = centerCode;
	}
	public String getProvinceOfLocation() {
		return provinceOfLocation;
	}
	public void setProvinceOfLocation(String provinceOfLocation) {
		this.provinceOfLocation = provinceOfLocation;
	}
	public String getCityOfLocation() {
		return cityOfLocation;
	}
	public void setCityOfLocation(String cityOfLocation) {
		this.cityOfLocation = cityOfLocation;
	}	
}
