package com.cnebula.analytics.reportservice;

import java.util.ArrayList;
import java.util.List;

import com.cnebula.analytics.reportserver.aas.Role;
import com.cnebula.common.annotations.xml.CollectionStyleType;
import com.cnebula.common.annotations.xml.XMLMapping;

public class StaticRolesConfig {
	List<Role> roles = new ArrayList<Role>();

	@XMLMapping(collectionStyle=CollectionStyleType.FLAT,childTag="Role")
	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
	
}
