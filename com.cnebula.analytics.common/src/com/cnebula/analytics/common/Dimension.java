package com.cnebula.analytics.common;

import com.cnebula.analytics.common.conf.CAColumn;

public class Dimension {
	
	String name = "";
	
	String description = "";
	
	String type = "";
	
	int length = 0;

	public Dimension(){
		
	}
	
	public Dimension(CAColumn cac){
		super();
		setName(cac.getName());
		setDescription(cac.getDescription());
		setType(cac.getType().getString());
		setLength((int)cac.getLength());
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dimension other = (Dimension) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Dimension [name=" + name + ", type=" + type + ", length=" + length + "]";
	}
}
