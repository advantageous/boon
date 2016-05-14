package io.advantageous.boon.core.reflection;

import io.advantageous.boon.core.TypeType;

public interface TypeInfo {

    TypeType typeEnum();
    TypeType componentType();
    TypeType componentKeyType();
    TypeType componentValueType();

    TypeType genericType0();
    TypeType genericType1();
    TypeType genericType2();
    TypeType genericType3();
    TypeType genericType4();
    TypeType genericType5();
    TypeType genericType6();
    TypeType genericTypeByPosition(int index);

    boolean generic();

    default boolean isCollection() {
        return typeEnum().isCollection();
    }


    default boolean isArray() {
        return typeEnum().isArray();
    }

    default boolean isMap() {
        return typeEnum() == TypeType.MAP || typeEnum().baseType() == TypeType.MAP;
    }


    Class<?> getType();
    Class<?> getComponentClass();
    Class<?> getComponentKeyClass();
    Class<?> getComponentValueClass();
    Class<?> getGenericClass0();
    Class<?> getGenericClass1();
    Class<?> getGenericClass2();
    Class<?> getGenericClass3();
    Class<?> getGenericClass4();
    Class<?> getGenericClass5();
    Class<?> getGenericClass6();
    Class<?> getGenericClassByPosition(int index);

}
