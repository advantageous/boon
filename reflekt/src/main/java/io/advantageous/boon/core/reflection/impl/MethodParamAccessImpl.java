package io.advantageous.boon.core.reflection.impl;

import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.reflection.MethodParamAccess;

import java.util.List;

public class MethodParamAccessImpl extends BaseTypeInfo implements MethodParamAccess {

    public MethodParamAccessImpl(boolean generic, TypeType typeEnum, Class<?> type, List<Class<?>> genericClasses) {
        super(generic, typeEnum, type, genericClasses);
    }
}
