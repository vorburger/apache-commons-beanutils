package org.apache.commons.beanutils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility to read DynaClass definitions from a textual description ("DSL").
 * 
 * The expected syntax is described in DynaClassReaderTest.domain.txt in src/test/resources.
 *
 * @author Michael Vorburger
 */
public class DynaClassReader {

	private static final Charset UTF8 = Charset.forName("UTF-8");
	private Collection/*<MissingCrossReference>*/ unresolvedDynaClassNames = new LinkedList();
	private Map/*<String, DynaClass>*/ dynaClassNameMap = new HashMap();
	private String currentClassName = null;
	private List/*<DynaProperty>*/ currentProperties = new LinkedList();
	
	public void readReader(Reader reader) throws IOException {
		LineNumberReader lnr = new LineNumberReader(reader);
		String line;
		
		while ((line = lnr.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty() || line.startsWith("#") || line.startsWith("//"))
				continue;
			else if (line.contains("{")) {
				String[] split = line.split("\\s+");
				if (split.length != 2 || !split[1].equals("{"))
					error("Syntax Error, class definition must only contain name followed by {", lnr, line);
				if (currentClassName != null)
					error("Syntax Error, have not closed class definition: " + currentClassName, lnr, line);
				currentClassName = split[0];
			}
			else if (line.equals("}")) {
				if (currentClassName == null)
					error("Unexpected '}'", lnr, line);
				DynaClass newClass = new BasicDynaClass(currentClassName, (DynaProperty[]) currentProperties.toArray(new DynaProperty[currentProperties.size()]));
				dynaClassNameMap.put(currentClassName, newClass);
				currentClassName = null;
				currentProperties = new LinkedList();
			}
			else if (currentClassName != null) {
				String[] split = line.split("\\s*\\:\\s*");
				if (split.length != 2)
					error("Syntax Error, property definition must only contain name followed by : and then type", lnr, line);
				String name = split[0];
				String typeDef = split[1].trim();
				boolean isMultiple= false;
				boolean isMap = false;
				if (typeDef.endsWith("*")) {
					isMultiple = true;
					typeDef = typeDef.substring(0, typeDef.indexOf('*')).trim();
				} else if (typeDef.endsWith("<>")) {
					isMap = true;
					typeDef = typeDef.substring(0, typeDef.indexOf('<')).trim();
				}
				Class javaType = null;
				String dynaTypeName = null;
				DynaClass dynaTypeClass = null;
				if (typeDef.contains(".")) {
					try {
						javaType = Class.forName(typeDef);
					} catch (ClassNotFoundException e) {
						error("Error, expected " + typeDef + " to be a Java class name, but ClassNotFound", lnr, line, e);
					}
				} else {
					dynaTypeName = typeDef;
					dynaTypeClass = (DynaClass) dynaClassNameMap.get(dynaTypeName);
				}
				DynaProperty p = null;
				if       (dynaTypeClass == null && javaType != null && !isMultiple && !isMap)
					p = new DynaProperty(name, javaType);
				else if  (dynaTypeClass == null && javaType != null &&  isMultiple && !isMap)
					p = new DynaProperty(name, List.class, javaType);
				else if  (dynaTypeClass == null && javaType != null && !isMultiple && isMap)
					p = new DynaProperty(name, Map.class, javaType);
				
				else if  (dynaTypeClass != null && javaType == null && !isMultiple && !isMap)
					p = new DynaProperty(name, dynaTypeClass);
				else if  (dynaTypeClass != null && javaType == null &&  isMultiple && !isMap)
					p = new DynaProperty(name, List.class, dynaTypeClass);
				else if  (dynaTypeClass != null && javaType == null && !isMultiple && isMap)
					p = new DynaProperty(name, Map.class, dynaTypeClass);

				else if  (dynaTypeName != null && javaType == null && !isMultiple && !isMap)
					p = new DynaProperty(name, DynaClass.class);
				else if  (dynaTypeName != null && javaType == null &&  isMultiple && !isMap)
					p = new DynaProperty(name, List.class, DynaClass.class);
				else if  (dynaTypeName != null && javaType == null && !isMultiple && isMap)
					p = new DynaProperty(name, Map.class, DynaClass.class);

				else
					error("Internal error, I'm having trouble deciding how to create DynaProperty", lnr, line);
				
				if (dynaTypeName != null && dynaTypeClass == null) {
					MissingCrossReference r = new MissingCrossReference();
					r.dynaClassName = currentClassName;
					r.dynaPropertyName = name;
					r.dynaClassNameReference = dynaTypeName;
					unresolvedDynaClassNames.add(r);
				}
				
				if (p != null)
					currentProperties.add(p);
			}
			else {
				error("Syntax Error", lnr, line);
			}
		}
	}
	
	private void error(String msg, LineNumberReader lnr, String line, ClassNotFoundException e) {
		String fullMsg = msg + " at line " + lnr.getLineNumber() + " : " + line;
		if (e == null)
			throw new IllegalArgumentException(fullMsg);
		else 
			throw new IllegalArgumentException(fullMsg, e);
	}

	private void error(String msg, LineNumberReader lnr, String line) {
		error(msg, lnr, line, null);
	}

	public DynaClass getDynaClass(String name) throws IllegalStateException {
		attemptToCompleteMissingReferencesAndCheckIfReadyForUsage();
		return (DynaClass) dynaClassNameMap.get(name);
	}

	public DynaClass[] getDynaClasses() throws IllegalStateException {
		attemptToCompleteMissingReferencesAndCheckIfReadyForUsage();
		Collection/*<DynaClass>*/ ro = Collections.unmodifiableCollection(dynaClassNameMap.values());
		return (DynaClass[]) ro.toArray(new DynaClass[ro.size()]);
	}

	private void attemptToCompleteMissingReferencesAndCheckIfReadyForUsage() throws IllegalStateException {
		for (Iterator iterator = unresolvedDynaClassNames.iterator(); iterator.hasNext();) {
			MissingCrossReference r = (MissingCrossReference) iterator.next();
			DynaClass dynaClassRef = (DynaClass) dynaClassNameMap.get(r.dynaClassNameReference);
			if (dynaClassRef == null)
				throw new IllegalStateException("Not ready, unresolved DynaClass: " + r.dynaClassNameReference);
			iterator.remove();
			DynaClass dynaClass = (DynaClass) dynaClassNameMap.get(r.dynaClassName);
			dynaClass.getDynaProperty(r.dynaPropertyName).setDynaType(dynaClassRef);
		}
		if (!unresolvedDynaClassNames.isEmpty()) {
			throw new IllegalStateException("Not ready, unresolved DynaClasses: "
					+ unresolvedDynaClassNames.toString());
		}
	}
	
	public void readString(String lines) throws IOException {
		readReader(new StringReader(lines));
	}
	
	public void readFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		readInputStream(bis);
	}

	private void readInputStream(InputStream is) throws IOException {
		InputStreamReader inputStreamReader = new InputStreamReader(is, UTF8);
		readReader(inputStreamReader);
	}

	public void readClasspathResource(String name) throws IOException {
		InputStream is = getClass().getResourceAsStream(name);
		if (is == null) {
			throw new FileNotFoundException("Not found ClasspathResource: " + name);
		}
		readInputStream(is);
	}
	
	private static class MissingCrossReference {
		String dynaClassName;
		String dynaPropertyName;
		String dynaClassNameReference;
	}

}
