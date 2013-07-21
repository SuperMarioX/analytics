package com.cnebula.analytics.reportserver.bean;

import java.util.ArrayList;
import java.util.List;

import com.cnebula.common.annotations.xml.CollectionStyleType;
import com.cnebula.common.annotations.xml.XMLMapping;

/**
 *一级功能菜单实体
 */
public class TopMenu implements Cloneable{

	public String id;
	public String name;
	
	List<Menu> menus = new ArrayList<Menu>();
	
	@XMLMapping(collectionStyle=CollectionStyleType.FLAT,childTag="Menu")
	public List<Menu> getMenus() {
		return menus;
	}

	public void setMenus(List<Menu> menus) {
		this.menus = menus;
	}

	public Object clone(){
		TopMenu o=null;
        try
        {
            o=(TopMenu)super.clone();
        }
        catch(CloneNotSupportedException e)
        {
            System.out.println(e.toString());
        }
        return o;
    }
	
}
