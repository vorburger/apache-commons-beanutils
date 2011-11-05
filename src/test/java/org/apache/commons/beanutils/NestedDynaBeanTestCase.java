package org.apache.commons.beanutils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

/**
 * Test cases for nested DynaBeans.
 * 
 * @author Michael Vorburger
 */
public class NestedDynaBeanTestCase extends TestCase {

	public void testNestedDynaBean() throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
    	DynaProperty[] adrProps = new DynaProperty[]{
            new DynaProperty("zip", Long.class)
          };
        BasicDynaClass adrDynaClass = new BasicDynaClass("Address", adrProps);

        DynaProperty[] empProps = new DynaProperty[]{
            new DynaProperty("address",     java.util.Map.class,  adrDynaClass),
            new DynaProperty("subordinate", java.util.List.class, DynaClass.class),
            new DynaProperty("firstName",   String.class),
            new DynaProperty("lastName",    String.class),
            new DynaProperty("mainAddress", adrDynaClass),
            new DynaProperty("boss",        DynaClass.class)
          };
        BasicDynaClass empDynaClass = new BasicDynaClass("Employee", empProps);
        empDynaClass.getDynaProperty("boss").setDynaType(empDynaClass);
        empDynaClass.getDynaProperty("subordinate").setDynaType(empDynaClass);
        
        assertTrue(empDynaClass.getDynaProperty("address").isMapped());
        assertTrue(empDynaClass.getDynaProperty("subordinate").isIndexed());
        
        // ---
        
        DynaBean address = adrDynaClass.newInstance();
        address.set("zip", new Long(9016));
        DynaBean subordinate = empDynaClass.newInstance();
        subordinate.set("firstName", "Dino");
        DynaBean boss = empDynaClass.newInstance();
        boss.set("firstName", "Wilma");
        DynaBean employee = empDynaClass.newInstance();
        employee.set("firstName", "Fred");
        employee.set("lastName", "Flintstone");
        employee.set("mainAddress", address);
        employee.set("boss", boss);
        PropertyUtils.setProperty(employee, "boss.lastName", "Flintstone");
        employee.set("address", new HashMap());
        PropertyUtils.setProperty(employee, "address(home)", address);
        employee.set("subordinate", new ArrayList());
        ((List)employee.get("subordinate")).add(subordinate);
       
        assertEquals(new Long(9016), address.get("zip"));
        assertEquals(new Long(9016), PropertyUtils.getProperty(employee, "mainAddress.zip"));
        assertEquals(new Long(9016), PropertyUtils.getProperty(employee, "address(home).zip"));
        assertEquals("Wilma", PropertyUtils.getProperty(employee, "boss.firstName"));
        assertEquals("Flintstone", PropertyUtils.getProperty(employee, "boss.lastName"));
        assertEquals("Dino", PropertyUtils.getProperty(employee, "subordinate[0].firstName"));
        
        // TODO What a shame DynaBean get/set do not support nested names directly like SDO, and we have to use PropertyUtils
        
    }
}
