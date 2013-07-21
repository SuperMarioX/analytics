package com.cnebula.common.management.dmbean;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

public class ObjectMBean implements DynamicMBean {

	private Object manageObj;
	private String mClassName;
	private Class<?> mClass;
	private String desc;
	private MBeanConstructorInfo[] mbConstructors;
	private MBeanAttributeInfo[] mbAttributes;
	private MBeanOperationInfo[] mbOperations;
	private MBeanNotificationInfo[] mbNotificationInfos;

	private Map<String, Method> geters = new HashMap<String, Method>();
	private Map<String, Method> seters = new HashMap<String, Method>();
	private Map<String, Method> opers = new HashMap<String, Method>();

	public static DynamicMBean wrap2DMBean(Object obj){
		return new ObjectMBean(obj);
	}
	
	public ObjectMBean(Object obj) {
		this.manageObj = obj;
		this.mClass = obj.getClass();
		this.mClassName = mClass.getName();
		this.desc = "ES Management Object: " + mClassName;
		init();
	}

	@Override
	public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
		if (geters.containsKey(attribute)) {
			Method m = geters.get(attribute);
			Object v = null;
			try {
				v = m.invoke(manageObj, new Object[0]);
			} catch (IllegalArgumentException e) {
				throw new AttributeNotFoundException(e.getMessage());
			} catch (IllegalAccessException e) {
				throw new MBeanException(e);
			} catch (InvocationTargetException e) {
				throw new MBeanException(e);
			}
			return v;
		}
		return null;
	}

	@Override
	public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException,
			ReflectionException {
		String attrName = attribute.getName();
		Object attrValue = attribute.getValue();
		if (seters.containsKey(attrName)) {
			Method m = seters.get(attrName);
			try {
				m.invoke(manageObj, attrValue);
			} catch (IllegalArgumentException e) {
				throw new AttributeNotFoundException(e.getMessage());
			} catch (IllegalAccessException e) {
				throw new MBeanException(e);
			} catch (InvocationTargetException e) {
				throw new MBeanException(e);
			}
		}
	}

	@Override
	public AttributeList getAttributes(String[] attributes) {
		if (attributes == null || attributes.length == 0) {
			return null;
		}
		AttributeList alist = new AttributeList();
		for (String attribute : attributes) {
			Object o = null;
			try {
				o = getAttribute(attribute);
			} catch (Exception e) {
				continue;
			}
			Attribute a = new Attribute(attribute, o);
			alist.add(a);
		}
		return alist;
	}

	@Override
	public AttributeList setAttributes(AttributeList attributes) {
		if (attributes == null) {
			return null;
		}
		AttributeList results = new AttributeList(attributes.size());
		while (attributes.iterator().hasNext()) {
			try {
				Attribute attribute = (Attribute) attributes.iterator().next();
				setAttribute(attribute);
				results.add(new Attribute(attribute.getName(), attribute.getValue()));
			} catch (Exception e) {
			}
		}
		return results;
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
		int paramLen = params == null ? 0 : params.length;
		Method m = opers.get(actionName + "_" + paramLen);
		if (m == null) {
			throw new NoSuchMethodError(actionName);
		}
		Object o = null;
		try {
			o = m.invoke(manageObj, params);
		} catch (IllegalArgumentException e) {
			throw new ReflectionException(e);
		} catch (IllegalAccessException e) {
			throw new MBeanException(e);
		} catch (InvocationTargetException e) {
			throw new ReflectionException((Exception) e.getTargetException());
		}
		return o;
	}

	@Override
	public MBeanInfo getMBeanInfo() {
		defineAttributeInfos();
		defineOperationInfos();
		defineNotifications();
		MBeanInfo mbi = new MBeanInfo(mClassName, desc, mbAttributes, mbConstructors, mbOperations, mbNotificationInfos);
		return mbi;
	}

	@SuppressWarnings("rawtypes")
	private void init() {
		Constructor[] cs = mClass.getConstructors();
		mbConstructors = new MBeanConstructorInfo[cs.length];
		for (int i = 0; i < cs.length; i++) {
			Constructor c = cs[i];
			MBeanConstructorInfo mc = new MBeanConstructorInfo(c.getName(), c);
			mbConstructors[i] = mc;
		}

		Method[] ms = mClass.getDeclaredMethods();
		for (Method m : ms) {
			String mn = m.getName();
			if (isGetter(mn)) {
				String readAtrr = mn.substring(3);
				geters.put(Character.toLowerCase(readAtrr.charAt(0)) + readAtrr.substring(1), m);
			} else if (isIsGetter(mn)) {
				String readAtrr = mn.substring(2);
				geters.put(Character.toLowerCase(readAtrr.charAt(0)) + readAtrr.substring(1), m);
			} else if (isSetter(mn)) {
				String writeAtrr = mn.substring(3);
				seters.put(Character.toLowerCase(writeAtrr.charAt(0)) + writeAtrr.substring(1), m);
			} else {
				int argsLen = m.getParameterTypes().length;
				opers.put(mn + "_" + argsLen, m);
			}
		}

	}

	private boolean isSetter(String methodName) {
		if (methodName.startsWith("set")) {
			if (methodName.length() > 4) {
				char c = methodName.charAt(3);
				int ci = (int) c;
				if (ci < 90) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private boolean isGetter(String methodName) {
		if (methodName.startsWith("get")) {
			if (methodName.length() > 4) {
				char c = methodName.charAt(3);
				int ci = (int) c;
				if (ci < 90) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private boolean isIsGetter(String methodName) {
		if (methodName.startsWith("is")) {
			if (methodName.length() > 3) {
				char c = methodName.charAt(2);
				int ci = (int) c;
				if (ci < 90) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private void defineAttributeInfos() {

		HashSet<String> resolved = new HashSet<String>();
		HashSet<String> toResolv = new HashSet<String>();
		toResolv.addAll(geters.keySet());
		toResolv.addAll(seters.keySet());

		List<MBeanAttributeInfo> mbais = new ArrayList<MBeanAttributeInfo>();
		Iterator<String> iter = toResolv.iterator();
		while (iter.hasNext()) {
			String name = iter.next();
			if (resolved.contains(name)) {
				continue;
			}
			String type = null;
			String getType = null;
			String setType = null;

			String desc = "";
			boolean isReadable = geters.containsKey(name);
			boolean isWritable = seters.containsKey(name);
			boolean isIs = false;
			if (isReadable) {
				Method rm = geters.get(name);
				getType = rm.getReturnType().getName();
				isReadable = isReadable && Modifier.isPublic(rm.getModifiers());
				isIs = isIsGetter(rm.getName());
			}
			if (isWritable) {
				Method wm = seters.get(name);
				Class<?>[] paramtypes = wm.getParameterTypes();
				if(paramtypes.length > 1){
					continue;
				}
				setType = paramtypes[0].getName();
				isWritable = isWritable && Modifier.isPublic(wm.getModifiers());
			}

			if (getType == null && setType == null) {
				continue;
			} else {
				if (getType != null && setType != null) {
					if (!getType.equals(setType)) {
						continue;
					} else {
						type = getType;
					}
				} else {
					type = getType == null ? setType : getType;
				}
			}
			MBeanAttributeInfo mbai = new MBeanAttributeInfo(name, type, desc, isReadable, isWritable, isIs);
			mbais.add(mbai);
			resolved.add(name);
		}
		mbAttributes = new MBeanAttributeInfo[mbais.size()];
		for (int i = 0; i < mbAttributes.length; i++) {
			mbAttributes[i] = mbais.get(i);
		}
	}

	private void defineOperationInfos() {
		List<MBeanOperationInfo> mbois = new ArrayList<MBeanOperationInfo>();
		Iterator<String> iter = opers.keySet().iterator();
		while (iter.hasNext()) {
			Method m = opers.get(iter.next());
			if (m == null) {
				continue;
			}
			if (!Modifier.isPublic(m.getModifiers())) {
				continue;
			}
			MBeanOperationInfo mboi = new MBeanOperationInfo("", m);
			mbois.add(mboi);
		}
		mbOperations = new MBeanOperationInfo[mbois.size()];
		for (int i = 0; i < mbOperations.length; i++) {
			mbOperations[i] = mbois.get(i);
		}

	}

	private void defineNotifications() {
		// TODO;
	}
}
