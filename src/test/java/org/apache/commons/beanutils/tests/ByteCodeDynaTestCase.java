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
package org.apache.commons.beanutils.tests;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.sf.cglib.beans.BeanMap;

import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.bytecode.ByteCodeStaticPropertiesDynaClass;
import org.apache.commons.beanutils.bytecode.ByteCodeWrappingDynaBeanFactory;

/**
 * Tests for ByteCodeDynaClass & Co.
 * 
 * This class is intentionally in a different package than what it tests,
 * to make sure that the package-local stuff in what it tests is not accessible here.
 *
 * @author Michael Vorburger
 */
public class ByteCodeDynaTestCase extends TestCase {
	
	/**
	 * Tests ByteCodeDynaClass.
	 * 
	 * This approach is useful if you can "wrap" the original DynaClass
	 * and use this new wrapper instead of the original.
	 */
	public void testFunkyDynaClass() throws IllegalAccessException, InstantiationException {
		DynaClass normalDynaClass = new BasicDynaClass("TestDynaClass", null, 
				new DynaProperty[] { new DynaProperty("name", String.class) });
		
		DynaClass funkyDynaClass = new ByteCodeStaticPropertiesDynaClass(normalDynaClass);
		DynaBean dynaBean = funkyDynaClass.newInstance();
		BeanMap map = BeanMap.create(dynaBean);
		
		map.put("name", "Michael");
		Assert.assertEquals("Michael", map.get("name"));
		
		dynaBean.set("name", "Divvya");
		Assert.assertEquals("Divvya", map.get("name"));
	}

	/**
	 * Tests Wrapper.
	 * 
	 * This approach is useful if you want to only wrap a DynaBean.
	 */
	public void testBeanWrapper() throws IllegalAccessException, InstantiationException {
		DynaClass normalDynaClass = new BasicDynaClass("TestDynaClass", null, 
				new DynaProperty[] { new DynaProperty("name", String.class) });
		
		DynaBean normalDynaBean = normalDynaClass.newInstance();
		normalDynaBean.set("name", "Michael");

		DynaBean funkyBean = ByteCodeWrappingDynaBeanFactory.wrapAsRealJavaBean(normalDynaBean);
		BeanMap map = BeanMap.create(funkyBean);
		
		Assert.assertEquals("Michael", map.get("name"));
		
		map.put("name", "Divvya");
		Assert.assertEquals("Divvya", funkyBean.get("name"));
	}

}
