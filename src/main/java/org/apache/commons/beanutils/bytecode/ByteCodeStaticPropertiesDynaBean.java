/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.beanutils.bytecode;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;

/**
 * Internal parent class for all DynaBean
 * wrappers created by the {@link ByteCodeStaticPropertiesDynaClass}.
 * 
 * Considered internal and should have been package-local,
 * but has to be public because PropertyUtilsBean's isDynaBean(Object bean)
 * needs access to it.
 * 
 * @see ByteCodeStaticPropertiesDynaClass
 * @see PropertyUtilsBean#isDynaBean(Object bean)
 *
 * @author Michael Vorburger
 */
public abstract class ByteCodeStaticPropertiesDynaBean implements DynaBean {

	// The implementation here is not really optimal, but it works...
	//
	// The thing is that, for something like Object get(String name),
	// we'll do Reflection via BeanUtils... but strictly speaking, that's a
	// little silly... and requires an if (o instanceof ByteCodeStaticPropertiesDynaBean)
	// in PropertyUtilsBean, to avoid recursion.
	//
	// Generating something like this would of course be more efficient:
	//     if ("field1".equals(name)) return field1;
	//     else if ("field2".equals(name)) return field2;
	//     else throw new IllegalArgumentException(...);
	//
	// The net.sf.cglib.transform.impl.FieldProviderTransformer could be used to generate that...
	// May be some time later! ;-)

	/**
	 * This is intentionally package-local, and is set in {@link ByteCodeStaticPropertiesDynaClass#newInstance()}. 
	 */
	DynaClass dynaClass;

	//@Override
	public DynaClass getDynaClass() {
		return dynaClass;
	}

	//@Override
	public Object get(String name) {
		try {
			return PropertyUtils.getSimpleProperty(this, name);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		}
	}

	//@Override
	public Object get(String name, int index) {
		try {
			return PropertyUtils.getIndexedProperty(this, name, index);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		}
	}

	//@Override
	public Object get(String name, String key) {
		try {
			return PropertyUtils.getMappedProperty(this, name, key);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		}
	}

	//@Override
	public void set(String name, Object value) {
		try {
			PropertyUtils.setProperty(this, name, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		}
	}

	//@Override
	public void set(String name, int index, Object value) {
		try {
			PropertyUtils.setIndexedProperty(this, name, index, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		}
	}

	//@Override
	public void set(String name, String key, Object value) {
		try {
			PropertyUtils.setMappedProperty(this, name, key, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		}
	}
	
	//@Override
	public boolean contains(String name, String key) {
        throw new UnsupportedOperationException("ByteCodeStaticPropertiesDynaClass DynaBeans do not yet support contains()");
/*        
		try {
			// TODO Is this correct?! Probably not... how to implement this? Or rewrite to access DynaBean directly, instead of reflection...
			return PropertyUtils.getMappedProperty(this, name, key) != null;
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Unexpected reflect exception from PropertyUtils", e);
		}
*/		
	}

	//@Override
	public void remove(String name, String key) {
        throw new UnsupportedOperationException("ByteCodeStaticPropertiesDynaClass DynaBeans do not yet support remove()");
	}


}
