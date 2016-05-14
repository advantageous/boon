package io.advantageous.boon.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;

/**
 * Created by rick on 5/13/16.
 */
public class TypeTypeTest {
    @Test
    public void isCollection() throws Exception {

        assertTrue(TypeType.getType(ArrayList.class).isCollection());
        assertTrue(TypeType.getType(List.class).isCollection());

        assertFalse(TypeType.getType(Map.class).isCollection());
    }

    @Test
    public void isMap() throws Exception {

        assertFalse(TypeType.getType(ArrayList.class).isMap());
        assertFalse(TypeType.getType(List.class).isMap());

        assertTrue(TypeType.getType(Map.class).isMap());
        assertTrue(TypeType.getType(TreeMap.class).isMap());

    }
}