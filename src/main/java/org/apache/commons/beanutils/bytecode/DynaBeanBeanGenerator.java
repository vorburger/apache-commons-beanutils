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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.cglib.core.AbstractClassGenerator;
import net.sf.cglib.core.ClassEmitter;
import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Constants;
import net.sf.cglib.core.EmitUtils;
import net.sf.cglib.core.KeyFactory;
import net.sf.cglib.core.ReflectUtils;
import net.sf.cglib.core.Signature;
import net.sf.cglib.core.TypeUtils;

import org.apache.commons.beanutils.DynaBean;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

/**
 * CGLib ClassGenerator for ByteCodeWrappingDynaBeanFactory.
 * 
 * This code "strongly inspired" from the {@link net.sf.cglib.beans.BeanGenerator}... ;-)
 * Just the actual byte-code generation inside {@link #generateClass(ClassVisitor)} is different.
 * 
 * @see ByteCodeWrappingDynaBeanFactory
 * 
 * @author Michael Vorburger
 */
//Intentionally package local, probably no need to expose this
//@SuppressWarnings({"rawtypes", "unchecked"})
class DynaBeanBeanGenerator extends AbstractClassGenerator {

	private static final Source SOURCE = new Source(DynaBeanBeanGenerator.class.getName());
	private static final BeanGeneratorKey KEY_FACTORY = (BeanGeneratorKey) KeyFactory.create(BeanGeneratorKey.class);
	
	private static final Type TYPE_DYNABEAN = TypeUtils.parseType(DynaBean.class.getName());
	private static final Signature GET = TypeUtils.parseSignature("Object get(String)");
	private static final Signature SET = TypeUtils.parseSignature("void set(String, Object)");

	interface BeanGeneratorKey {
		public Object newInstance(String superclass, Map props);
	}

	private Class superclass;
	private Map/*<String, Type>*/ props = new HashMap();

	protected DynaBeanBeanGenerator() {
		super(SOURCE);
	}

    public void setSuperclass(Class superclass) {
        this.superclass = superclass;
    }
    
	public void addProperty(String name, Class type) {
        props.put(name, Type.getType(type));
	}

	//@Override
	public void generateClass(ClassVisitor v) throws Exception {
		int size = props.size();
		String[] names = (String[]) props.keySet().toArray(new String[size]);
		Type[] types = new Type[size];
		for (int i = 0; i < size; i++) {
			types[i] = (Type) props.get(names[i]);
		}
		
		ClassEmitter ce = new ClassEmitter(v);
		ce.begin_class(Constants.V1_2, Constants.ACC_PUBLIC, getClassName(),
				superclass != null ? Type.getType(superclass) : Constants.TYPE_OBJECT, null, null);
		EmitUtils.null_constructor(ce);
		
		Set/*<Map.Entry<String, Type>>*/ entries = props.entrySet();
		for (Iterator iterator = entries.iterator(); iterator.hasNext();) {
			Entry/*<String, Type>*/ entry = (Entry/*<String, Type>*/) iterator.next();
			String fieldName = (String) entry.getKey();
			String upperName = TypeUtils.upperFirst(fieldName);
			Type type = (Type) entry.getValue();
			
			CodeEmitter e;
	        e = ce.begin_method(Constants.ACC_PUBLIC,
	                            new Signature("get" + upperName,
	                                          type,
	                                          Constants.TYPES_EMPTY),
	                            null);
	        e.load_this();
	        e.push(fieldName);
	        e.invoke_interface(TYPE_DYNABEAN, GET);
            e.checkcast(type);
	        e.return_value();
	        e.end_method();

	        e = ce.begin_method(Constants.ACC_PUBLIC,
	                            new Signature("set" + upperName,
	                                          Type.VOID_TYPE,
	                                          new Type[]{ type }),
	                            null);
	        e.load_this();
	        e.push(fieldName);
	        e.load_arg(0);
	        e.invoke_interface(TYPE_DYNABEAN, SET);
	        e.return_value();
	        e.end_method();
		}
        
		ce.end_class();
	}

	//@Override
	protected ClassLoader getDefaultClassLoader() {
        if (superclass != null) {
            return superclass.getClassLoader();
        } else {
            return null;
        }
	}

	public Object create() {
        if (superclass != null) {
            setNamePrefix(superclass.getName());
        }
        String superName = (superclass != null) ? superclass.getName() : "java.lang.Object";
        Object key = KEY_FACTORY.newInstance(superName, props);
        return super.create(key);
	}

	//@Override
	protected Object firstInstance(Class type) throws Exception {
        return ReflectUtils.newInstance(type);
	}

	//@Override
	protected Object nextInstance(Object instance) throws Exception {
        Class protoclass = (instance instanceof Class) ? (Class)instance : instance.getClass();
        return ReflectUtils.newInstance(protoclass);
	}

}
