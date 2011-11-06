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
package org.apache.commons.beanutils;


/**
 * DynaBean which delegates (typically, most of) its
 * functionality to another DynaBean implementation.
 * 
 * @author Michael Vorburger
 */
public class DelegatingDynaBean implements DynaBean {

	protected DynaBean delegate;

	protected DelegatingDynaBean(DynaBean delegate) {
		this.delegate = delegate;
	}

	//@Override
	public boolean contains(String name, String key) {
		return delegate.contains(name, key);
	}

	//@Override
	public Object get(String name) {
		return delegate.get(name);
	}

	//@Override
	public Object get(String name, int index) {
		return delegate.get(name, index);
	}

	//@Override
	public Object get(String name, String key) {
		return delegate.get(name, key);
	}

	//@Override
	public DynaClass getDynaClass() {
		return delegate.getDynaClass();
	}

	//@Override
	public void remove(String name, String key) {
		delegate.remove(name, key);
	}

	//@Override
	public void set(String name, Object value) {
		delegate.set(name, value);
	}

	//@Override
	public void set(String name, int index, Object value) {
		delegate.set(name, index, value);
	}

	//@Override
	public void set(String name, String key, Object value) {
		delegate.set(name, key, value);
	}

}
