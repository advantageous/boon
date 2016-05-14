package io.advantageous.boon.core.reflection.impl;

import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.reflection.TypeInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by rick on 5/14/16.
 */
public class BaseTypeInfo implements TypeInfo{

    private final TypeType typeEnum;
    private final Class<?> type;
    private final List<Class<?>> genericClasses;
    private final List<TypeType> genericTypes;
    private final boolean generic;


    public BaseTypeInfo(final boolean generic, TypeType typeEnum, Class<?> type, List<Class<?>> genericClasses) {
        this.typeEnum = typeEnum;
        this.generic = generic;
        this.type = type;
        this.genericClasses = genericClasses;
        genericTypes = this.genericClasses.stream().map(TypeType::getType).collect(Collectors.toList());
    }

    @Override
    public TypeType typeEnum() {
        return typeEnum;
    }

    @Override
    public TypeType componentType() {
        return genericTypes.get(0);
    }

    @Override
    public TypeType componentKeyType() {
        return genericTypes.get(0);
    }

    @Override
    public TypeType componentValueType() {
        return genericTypes.get(1);
    }

    @Override
    public Class<?> getComponentClass() {
        return genericClasses.get(0);
    }

    @Override
    public Class<?> getComponentKeyClass() {
        return genericClasses.get(0);
    }

    @Override
    public Class<?> getComponentValueClass() {
        return genericClasses.get(1);
    }

    @Override
    public Class<?> getType() {
        return type;
    }


    @Override
    public Class<?> getGenericClass0() {
        return genericClasses.get(0);
    }

    @Override
    public Class<?> getGenericClass1() {
        return genericClasses.get(1);
    }

    @Override
    public Class<?> getGenericClass2() {
        return genericClasses.get(2);
    }

    @Override
    public Class<?> getGenericClass3() {
        return genericClasses.get(3);
    }

    @Override
    public Class<?> getGenericClass4() {
        return genericClasses.get(4);
    }

    @Override
    public Class<?> getGenericClass5() {
        return genericClasses.get(5);
    }

    @Override
    public Class<?> getGenericClass6() {
        return genericClasses.get(6);
    }

    @Override
    public Class<?> getGenericClassByPosition(int index) {
        return genericClasses.get(index);
    }


    @Override
    public TypeType genericType0() {
        return genericTypes.get(0);
    }

    @Override
    public TypeType genericType1() {
        return genericTypes.get(1);
    }

    @Override
    public TypeType genericType2() {
        return genericTypes.get(2);
    }

    @Override
    public TypeType genericType3() {
        return genericTypes.get(3);
    }

    @Override
    public TypeType genericType4() {
        return genericTypes.get(4);
    }

    @Override
    public TypeType genericType5() {
        return genericTypes.get(5);
    }

    @Override
    public TypeType genericType6() {
        return genericTypes.get(6);
    }

    @Override
    public TypeType genericTypeByPosition(int index) {
        return genericTypes.get(index);
    }

    @Override
    public boolean generic() {
        return generic;
    }
}
