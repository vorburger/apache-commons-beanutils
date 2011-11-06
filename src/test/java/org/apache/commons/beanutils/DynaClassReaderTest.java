package org.apache.commons.beanutils;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Tests for {@link DynaClassReader}.
 *
 * @author Michael Vorburger
 */
public class DynaClassReaderTest extends TestCase {

	/**
	 * Tests {@link org.apache.commons.beanutils.DynaClassReader#readClasspathResource(String))},
	 * {@link org.apache.commons.beanutils.DynaClassReader#getDynaClasses()}
	 * {@link org.apache.commons.beanutils.DynaClassReader#getDynaClass(java.lang.String)}.
	 */
	public final void testDynaClassReader() throws Exception {
		DynaClassReader r = new DynaClassReader();
		r.readClasspathResource("/DynaClassReaderTest.domain.txt");
		assertEquals(2, r.getDynaClasses().length);
		DynaClass klass = r.getDynaClass("Employee");
		
		assertEquals("Employee", klass.getName());
		assertEquals(6, klass.getDynaProperties().length);
		
		DynaProperty p = klass.getDynaProperty("lastName");
		assertEquals("lastName", p.getName());
		assertEquals(null, p.getContentType());
		assertEquals(String.class, p.getType());
		assertEquals(null, p.getDynaType());
		assertEquals(false, p.isIndexed());
		assertEquals(false, p.isMapped());
		
		p = klass.getDynaProperty("mainAddress");
		assertEquals(null, p.getContentType());
		assertEquals(DynaClass.class, p.getType());
		assertEquals("Address", p.getDynaType().getName());
		assertEquals(false, p.isIndexed());
		assertEquals(false, p.isMapped());

		p = klass.getDynaProperty("boss");
		assertEquals(null, p.getContentType());
		assertEquals(DynaClass.class, p.getType());
		assertEquals("Employee", p.getDynaType().getName());
		assertEquals(false, p.isIndexed());
		assertEquals(false, p.isMapped());

		p = klass.getDynaProperty("subordinates");
		assertEquals(DynaClass.class, p.getContentType());
		assertEquals(List.class, p.getType());
		assertEquals("Employee", p.getDynaType().getName());
		assertEquals(true, p.isIndexed());
		assertEquals(false, p.isMapped());

		p = klass.getDynaProperty("address");
		assertEquals(Map.class, p.getType());
		assertEquals("Address", p.getDynaType().getName());
		assertEquals(false, p.isIndexed());
		assertEquals(true, p.isMapped());
		assertEquals(DynaClass.class, p.getContentType());
	}
}
