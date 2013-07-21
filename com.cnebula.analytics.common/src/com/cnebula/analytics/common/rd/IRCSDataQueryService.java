package com.cnebula.analytics.common.rd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cnebula.common.annotations.es.ParamList;
import com.cnebula.common.annotations.xml.CollectionStyleType;
import com.cnebula.common.annotations.xml.XMLMapping;

public interface IRCSDataQueryService {
	
	/**
	 * 使用IP查找成员馆信息
	 * @param ip
	 * @return
	 */
	@XMLMapping(tag = "node", childTag = "properties")
	@ParamList({ "ip" })
	public List<String> locateNodeInfo(String ip);
	
	/**
	 * 使用馆代码获取成员馆信息
	 * rcs_nid,rcs_n_t, rcs_nn,rcs_nsn, rcs_nt,,rcs_nc, rcs_ncc,rcs_npl,rcs_ncl<br/>
	 * 0) nodeId
	 * 1) nodeEntityType
	 * 2) nodeName
	 * 3)nodeShortName,
	 * 4)nodeType
	 * 5)node code
	 * 6)node'sSaasCenterCode
	 * 7)node'ProvinceLocation
	 * 8)nodes'CityLocation
	 * @param nodeCode
	 * @return
	 */
	@ParamList({ "code" })
	@XMLMapping(tag = "node", childTag = "properties")
	public List<String> getNodeInfo(String nodeCode);

	@XMLMapping(tag = "nodes", childTag = "node", collectionStyle = CollectionStyleType.EMBED, itemTypes = { Map.Entry.class, String.class, List.class })
	public Map<String, List<String>> listNodeInfo();
	
	/**
	 * 
	 * @return 所有共享域中心列表，String[]表示共享域中心的部分属性
	 * 
	 * rcs_nid,rcs_n_t,rcs_nc,rcs_nn,rcs_nsn,rcs_ncc,rcs_npl,rcs_ncl<br/>
	 * nodeId,nodeEntityType,nodeCode,nodeName,nodeShortName,node'sSaasCenterCode,node'ProvinceLocation,nodes'CityLocation
	 * 
	 */
	@XMLMapping(tag = "centers", childTag = "center")
	public List<String[]> listSaasCenter();
	
	/**
	 * appInfo的部分属性
	 *         rcs_a_t,rcs_aid,rcs_an,rcs_av,rcs_atn,rcs_atc,rcs_att,rcs_atv,rcs_as,rcs_nc,rcs_ncc<br/>
	 *         appEntityType,appId,appName,appVersion,appTypeName,appTypeCode,appTypeType,appTypeVersion,appStatus,app'sNodeCode,app'sNodeCenterCode
	 * @return
	 */
	@XMLMapping(tag = "apps", childTag = "app", collectionStyle = CollectionStyleType.EMBED, itemTypes = { Map.Entry.class, String.class, List.class })
	public Map<String,List<String>> listAppInfo();
	
	/**
	 * 获取某个应用系统的信息
	 * appInfo的部分属性
	 *         rcs_a_t,rcs_aid,rcs_an,rcs_av,rcs_atn,rcs_atc,rcs_att,rcs_atv,rcs_as,rcs_nc,rcs_ncc<br/>
	 *         appEntityType,appId,appName,appVersion,appTypeName,appTypeCode,appTypeType,appTypeVersion,appStatus,app'sNodeCode,app'sNodeCenterCode
	 * 
	 * @param appId
	 * @return
	 */
	@XMLMapping(tag = "app", childTag = "properties")
	@ParamList({"appId"})
	public List<String> getAppInfo(String appId);

	/**
	 * 获取某个共享域中心包含的所有应用系统类型(AppType.Type)
	 * 
	 * @return List<String>表示该共享域中包含的所有的应用系统类型
	 */
	@XMLMapping(tag = "apptypes", childTag = "type")
	@ParamList({ "code" })
	public List<String> getAppTypeOfSaasCenter(String saasCenterCode);

	/**
	 * 获取某个共享域中应用类类型所属的应用系统类型
	 * 
	 * @param saasCenterCode
	 * 
	 * @return 以应用系统类型为key,List<String[]>表示该应用系统类型中所有应用系统 String[]
	 *         中是appInfo的部分属性
	 *         rcs_a_t,rcs_aid,rcs_an,rcs_av,rcs_atn,rcs_atc,rcs_att,rcs_atv,rcs_as,rcs_nc,rcs_ncc<br/>
	 *         appEntityType,appId,appName,appVersion,appTypeName,appTypeCode,appTypeType,appTypeVersion,appStatus,app'sNodeCode,app'sNodeCenterCode
	 */
	@XMLMapping(tag = "type", childTag = "app", collectionStyle = CollectionStyleType.EMBED, itemTypes = { Map.Entry.class, String.class, List.class })
	@ParamList({ "code" })
	public Map<String, List<String[]>> getAppOfAppType(String saasCenterCode);
	
	public Map<String,NodeInfo> getNodeInfoMap();
	public Map<String,NodeInfo> getSaasNodeInfoMap();
	public Map<String,NodeInfo> getVSaasNodeInfoMap();

}
