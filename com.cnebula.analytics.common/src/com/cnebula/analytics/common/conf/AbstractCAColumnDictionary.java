package com.cnebula.analytics.common.conf;

import java.util.Locale;

/**
 * 
 * 数据字典是为了解决数据冗余带来的容量问题。 一般的使用情况： 1)在入库前,
 * 向字典里put一个value，字典返回一个key，这个key代表了这个value 2)在出库时，
 * 需要用key值得到真正的value值
 * 
 * 在多线程环境下，应当保证线程安全，因此它不像hashMap有更多的方法，入库时有一个
 * containsKey再put(key,value)的过程，其中key是由数据字典自动管理并保证唯一性。
 * 
 * @author sandor
 * 
 * @param <K>
 * @param <V>
 */
public abstract class AbstractCAColumnDictionary<K, V> extends CAColumnDictionary {

	public abstract V getValue(K key, Locale locale);

	public abstract V getValue(K key);

	public abstract K put(V value);

}
