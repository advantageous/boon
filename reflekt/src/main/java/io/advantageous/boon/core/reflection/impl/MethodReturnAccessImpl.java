package io.advantageous.boon.core.reflection.impl;

import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.reflection.MethodReturnAccess;

import java.util.List;

public class MethodReturnAccessImpl extends BaseTypeInfo implements MethodReturnAccess {

    public MethodReturnAccessImpl(boolean generic, TypeType typeEnum, Class<?> type, List<Class<?>> genericClasses) {
        super(generic, typeEnum, type, genericClasses);
    }
}