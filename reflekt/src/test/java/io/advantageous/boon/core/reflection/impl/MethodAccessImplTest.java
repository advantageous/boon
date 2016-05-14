package io.advantageous.boon.core.reflection.impl;

import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.boon.core.reflection.MethodParamAccess;
import io.advantageous.boon.core.reflection.MethodReturnAccess;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MethodAccessImplTest {

    @Test
    public void testParams() {
        final ClassMeta<TestService> testServiceClassMeta = ClassMeta.classMeta(TestService.class);
        final MethodAccess someMethod = testServiceClassMeta.method("someMethod");
        final List<MethodParamAccess> parameters = someMethod.parameters();

        //Test List
        final MethodParamAccess methodParamAccess1 = parameters.get(0);
        assertTrue(methodParamAccess1.isCollection());
        assertEquals(Employee.class, methodParamAccess1.getComponentClass());
        assertEquals(TypeType.INSTANCE, methodParamAccess1.componentType());


        //Test Map
        final MethodParamAccess methodParamAccess2 = parameters.get(1);
        assertTrue(methodParamAccess2.isMap());
        assertEquals(String.class, methodParamAccess2.getComponentKeyClass());
        assertEquals(Employee.class, methodParamAccess2.getComponentValueClass());
        assertEquals(TypeType.STRING, methodParamAccess2.componentKeyType());
        assertEquals(TypeType.INSTANCE, methodParamAccess2.componentValueType());


        //Test Tuple
        final MethodParamAccess methodParamAccess3 = parameters.get(2);
        assertEquals(String.class, methodParamAccess3.getGenericClass0());
        assertEquals(Employee.class, methodParamAccess3.getGenericClass1());
        assertEquals(List.class, methodParamAccess3.getGenericClass2());
        assertEquals(TypeType.STRING, methodParamAccess3.genericType0());
        assertEquals(TypeType.INSTANCE, methodParamAccess3.genericType1());
        assertEquals(TypeType.LIST, methodParamAccess3.genericType2());


    }


    @Test
    public void testReturnMap() {
        final ClassMeta<TestService> testServiceClassMeta = ClassMeta.classMeta(TestService.class);
        final MethodAccess methodAccess = testServiceClassMeta.method("getMap");
        final MethodReturnAccess methodReturnAccess = methodAccess.returnAccess();
        assertTrue(methodReturnAccess.isMap());
        assertEquals(String.class, methodReturnAccess.getComponentKeyClass());
        assertEquals(Employee.class, methodReturnAccess.getComponentValueClass());
        assertEquals(TypeType.STRING, methodReturnAccess.componentKeyType());
        assertEquals(TypeType.INSTANCE, methodReturnAccess.componentValueType());

    }


    @Test
    public void testReturnList() {
        final ClassMeta<TestService> testServiceClassMeta = ClassMeta.classMeta(TestService.class);
        final MethodAccess methodAccess = testServiceClassMeta.method("getList");
        final MethodReturnAccess methodReturnAccess = methodAccess.returnAccess();
        assertTrue(methodReturnAccess.isCollection());
        assertEquals(Employee.class, methodReturnAccess.getComponentClass());
        assertEquals(TypeType.INSTANCE, methodReturnAccess.componentType());
    }


    @Test
    public void testReturnTuple() {
        final ClassMeta<TestService> testServiceClassMeta = ClassMeta.classMeta(TestService.class);
        final MethodAccess methodAccess = testServiceClassMeta.method("getTuple");
        final MethodReturnAccess methodReturnAccess = methodAccess.returnAccess();
        assertEquals(String.class, methodReturnAccess.getGenericClass0());
        assertEquals(Employee.class, methodReturnAccess.getGenericClass1());
        assertEquals(List.class, methodReturnAccess.getGenericClass2());
        assertEquals(TypeType.STRING, methodReturnAccess.genericType0());
        assertEquals(TypeType.INSTANCE, methodReturnAccess.genericType1());
        assertEquals(TypeType.LIST, methodReturnAccess.genericType2());

    }


    @Test
    public void testInvoke() {
        Map<String, Employee> map = new HashMap<>();
        map.put("key", new Employee("rick"));
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("geoff"));
        Tuple<String, Employee, List> tuple = new Tuple<>();

        TestService testService = new TestService();

        ClassMeta classMeta = ClassMeta.classMeta(TestService.class);
        Boolean returnValue = (Boolean) classMeta.invoke(testService, "someMethod", Arrays.asList(employees, map, tuple));

        assertTrue(returnValue);
        assertTrue(testService.someMethodCalled.get());

    }


    @Test
    public void testInvokeDynamicObject() {
        Map<String, Employee> map = new HashMap<>();
        map.put("key", new Employee("rick"));
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("geoff"));
        Tuple<String, Employee, List> tuple = new Tuple<>();

        TestService testService = new TestService();

        ClassMeta classMeta = ClassMeta.classMeta(TestService.class);

        final MethodAccess someMethod = classMeta.method("someMethod");

        Object returnValue = someMethod.invokeDynamicObject(testService, Arrays.asList(employees, map, tuple));

        assertTrue((Boolean) returnValue);
        assertTrue(testService.someMethodCalled.get());

    }


    @Test
    public void testInvokeDynamicObjectWithMap() {
        Map<String, String> geoff = new HashMap<>();
        geoff.put("name", "geoff");

        Map<String, String> rick = new HashMap<>();
        geoff.put("name", "rick");

        Map<String, Object> map = new HashMap<>();
        map.put("key", rick);

        List<Object> employees = new ArrayList<>();
        employees.add(geoff);
        Tuple<String, Employee, List> tuple = new Tuple<>();

        TestService testService = new TestService();

        ClassMeta classMeta = ClassMeta.classMeta(TestService.class);

        final MethodAccess someMethod = classMeta.method("someMethod");

        Object returnValue = someMethod.invokeDynamicObject(testService, Arrays.asList(employees, map, tuple));

        assertTrue((Boolean) returnValue);
        assertTrue(testService.someMethodCalled.get());

    }


    @Test
    public void testInvokeDynamic() {
        Map<String, Employee> map = new HashMap<>();
        map.put("key", new Employee("rick"));
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("geoff"));
        Tuple<String, Employee, List> tuple = new Tuple<>();

        TestService testService = new TestService();

        ClassMeta classMeta = ClassMeta.classMeta(TestService.class);

        final MethodAccess someMethod = classMeta.method("someMethod");

        Object returnValue = someMethod.invokeDynamic(testService, new Object[]{employees, map, tuple});

        assertTrue((Boolean) returnValue);
        assertTrue(testService.someMethodCalled.get());

    }

    public static class Tuple<A, B, C> {

    }

    public static class Employee {

        private final String name;

        public Employee(String name) {
            this.name = name;
        }
    }

    public static class TestService {

        AtomicBoolean someMethodCalled = new AtomicBoolean();

        public boolean someMethod(List<Employee> employees, Map<String, Employee> empMap, Tuple<String, Employee, List> tuple) {

            Employee employee = employees.get(0);
            Employee employee2 = empMap.get("key");
            someMethodCalled.set(true);
            return true;
        }

        public Map<String, Employee> getMap() {
            return null;
        }


        public List<Employee> getList() {
            return null;
        }


        public Tuple<String, Employee, List> getTuple() {
            return null;
        }
    }
}