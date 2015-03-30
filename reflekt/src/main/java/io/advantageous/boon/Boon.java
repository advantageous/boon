/*
 * Copyright 2013-2014 Richard M. Hightower
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * __________                              _____          __   .__
 * \______   \ ____   ____   ____   /\    /     \ _____  |  | _|__| ____    ____
 *  |    |  _//  _ \ /  _ \ /    \  \/   /  \ /  \\__  \ |  |/ /  |/    \  / ___\
 *  |    |   (  <_> |  <_> )   |  \ /\  /    Y    \/ __ \|    <|  |   |  \/ /_/  >
 *  |______  /\____/ \____/|___|  / \/  \____|__  (____  /__|_ \__|___|  /\___  /
 *         \/                   \/              \/     \/     \/       \//_____/
 *      ____.                     ___________   _____    ______________.___.
 *     |    |____ ___  _______    \_   _____/  /  _  \  /   _____/\__  |   |
 *     |    \__  \\  \/ /\__  \    |    __)_  /  /_\  \ \_____  \  /   |   |
 * /\__|    |/ __ \\   /  / __ \_  |        \/    |    \/        \ \____   |
 * \________(____  /\_/  (____  / /_______  /\____|__  /_______  / / ______|
 *               \/           \/          \/         \/        \/  \/
 */

package io.advantageous.boon;


import io.advantageous.boon.core.reflection.*;
import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.core.Sys;
import io.advantageous.boon.core.Typ;
import io.advantageous.boon.primitive.CharBuf;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class contains some utility methods and acts as facade
 * over the most popular Boon features.
 */
public class Boon {




    /**
     * Turns debugging on.
     */
    private static AtomicBoolean debug = new AtomicBoolean(false);



    /**
     * Checks to see if two objects are equal.
     *
     * @param a a
     * @param b b
     * @return eq?
     */
    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }


    public static boolean isArray(Object obj) {
        return Typ.isArray(obj);
    }

    public static boolean isStringArray(Object obj) {
        return Typ.isStringArray(obj);
    }

    public static int len(Object obj) {
        return Conversions.len(obj);
    }


    public static Iterator iterator(final Object o) {
        return Conversions.iterator(o);
    }

    /**
     * Join by for array.
     */
    public static String joinBy(char delim, Object... args) {
        CharBuf builder = CharBuf.create(256);
        int index = 0;
        for (Object arg : args) {
            builder.add(arg.toString());
            if (!(index == args.length - 1)) {
                builder.add(delim);
            }
            index++;
        }
        return builder.toString();
    }

    /**
     * Join by for collection.
     */
    public static String joinBy(char delim, Collection<?> collection) {
        CharBuf builder = CharBuf.create(256);
        int index = 0;
        int size = collection.size();
        for (Object arg : collection) {
            builder.add(arg.toString());
            if (!(index == size - 1)) {
                builder.add(delim);
            }
            index++;
        }
        return builder.toString();
    }


    /**
     * Join by for iterable.
     */
    public static String joinBy(char delim, Iterable<?> iterable) {
        CharBuf builder = CharBuf.create(256);
        int index = 0;
        for (Object arg : iterable) {
            builder.add(arg.toString());
            builder.add(delim);
            index++;
        }
        if (index > 1) {
            builder.removeLastChar();
        }
        return builder.toString();
    }


    /**
     * Map by which is really contained in Lists
     *
     * @param objects  objects to map
     * @param function function to use for mapping
     * @return list
     */
    public static List<?> mapBy(Iterable<?> objects, Object function) {
        return Lists.mapBy(objects, function);
    }


    /**
     * Does path lookupWithDefault.
     * Facade over BeanUtils.
     *
     * @param value value to read
     * @param path  property path to read from value
     * @return value from property path
     */
    public static Object atIndex(Object value, String path) {
        return BeanUtils.idx(value, path);
    }


    /**
     * Gets input from console.
     *
     * @return String from console.
     */
    public static String gets() {
        Scanner console = new Scanner(System.in);
        String input = console.nextLine();
        return input.trim();
    }

    /**
     * Adds a bunch of Strings together.
     */
    public static String add(String... args) {
        return Str.add(args);
    }

    /**
     * Gets the string value of an object path.
     *
     * @param value object value
     * @param path  property path to read from value
     * @return string version of results
     */
    public static String stringAtIndex(Object value, String path) {
        return Conversions.toString(BeanUtils.idx(value, path));
    }


    /**
     * Facade method over Boon invoker system.
     * Allow you to easily invoke methods from Java objects using reflection.
     *
     * TODO change this to invoke missingMethod if the method is not found.
     * First arg is the name of the missing method.
     * (If missingMethod is implemented on the object value).
     *
     * @param value  object value
     * @param method method you want to invoke on the object value
     * @return results of object invocation.
     */
    public static Object call(Object value, String method) {
        if (value instanceof Class) {
            return Invoker.invoke((Class) value, method);
        } else {
            return Invoker.invoke(value, method);
        }
    }


    /**
     * Common helper method for string slice.
     *
     * @param string string you want to slice
     * @param start  start location
     * @param stop   end location
     * @return new sliced up string.
     */
    public static String sliceOf(String string, int start, int stop) {
        return Str.sliceOf(string, start, stop);
    }


    public static String sliceOf(String string, int start) {
        return Str.sliceOf(string, start);
    }


    public static String endSliceOf(String string, int end) {
        return Str.endSliceOf(string, end);
    }


    /**
     * Quickly grab a system property.
     *
     * @param propertyName property value
     * @param defaultValue default value if not found.
     * @return value of system property
     */
    public static String sysProp(String propertyName, Object defaultValue) {
        return Sys.sysProp(propertyName, defaultValue);
    }


    public static boolean hasSysProp(String propertyName) {
        return Sys.hasSysProp(propertyName);
    }


    public static void putSysProp(String propertyName, Object value) {
        Sys.putSysProp(propertyName, value);
    }


    /**
     * Press enter to continue. Used for console apps.
     *
     * @param pressEnterKeyMessage message
     */
    public static void pressEnterKey(String pressEnterKeyMessage) {
        Str.puts(pressEnterKeyMessage);
        gets();
    }


    /**
     * Used by console apps.
     */
    public static void pressEnterKey() {
        Str.puts("Press enter key to continue");
        gets();
    }

    /**
     * Checks to see if an object responds to a method.
     * Helper facade over Reflection library.
     *
     * @param object object in question
     * @param method method name in question.
     * @return true or false
     */
    public static boolean respondsTo(Object object, String method) {
        if (object instanceof Class) {
            return Reflection.respondsTo((Class) object, method);
        } else {
            return Reflection.respondsTo(object, method);
        }
    }


    /**
     * Loads a resource from the file system or classpath if not found.
     * This allows you to have resources that exist in the jar
     * and that can be configured outside of the jar easily.
     *
     * Classpath is only used if file system resource is not found.
     *
     * @param path path to resource
     * @return resource returned.
     */
    public static String resource(String path) {
        if (!IO.exists(IO.path(path))) {
            path = add("classpath:/", path);
        }

        String str = IO.read(path);
        return str;
    }


    /**
     * Loads a resource from the file system or classpath if not found.
     * This allows you to have resources that exist in the jar
     * and that can be configured outside of the jar easily.
     *
     * Classpath is only used if file system resource is not found.
     *
     * @param path path to resource
     * @return resource returned.
     */
    public static String resource(Path path) {
        String str = IO.read(path);
        return str;
    }











    /**
     * Gets class name from object.
     * It is null safe.
     *
     * @param object class name
     * @return class name of object
     */
    public static String className(Object object) {
        return object == null ? "CLASS<NULL>" : object.getClass().getName();
    }

    /**
     * Gets class name from object.
     * It is null safe.
     *
     * @param object class name
     * @return class name of object
     */
    public static Class<?> cls(Object object) {
        return object == null ? null : object.getClass();
    }

    /**
     * Gets simple class name from object.
     *
     * @param object object to get class name from
     * @return returns the class name
     */
    public static String simpleName(Object object) {
        return object == null ? "CLASS<NULL>" : object.getClass().getSimpleName();
    }



    /**
     * Checks to see if debugging is turned on.
     *
     * @return on?
     */
    public static boolean debugOn() {
        return debug.get();
    }


    /**
     * Turns debugging on.
     */
    public static void turnDebugOn() {
        debug.set(true);
    }


    /**
     * Turns debugging off.
     */
    public static void turnDebugOff() {
        debug.set(false);
    }



    public static boolean equalsOrDie(Object expected, Object got) {

        if (expected == null && got == null) {
            return true;
        }

        if (expected == null && got != null) Exceptions.die();
        if (!expected.equals(got)) Exceptions.die("Expected was", expected, "but we got", got);

        return true;
    }


    public static boolean equalsOrDie(String message, Object expected, Object got) {

        if (expected == null && got != null) Exceptions.die(message, "Expected was", expected, "but we got", got);
        if (!expected.equals(got)) Exceptions.die(message, "Expected was", expected, "but we got", got);

        return true;
    }

    public static String toPrettyJson(Object object) {

        CharBuf buf = CharBuf.createCharBuf();
        return buf.prettyPrintObject(object, false, 0).toString();
    }


    public static String toPrettyJsonWithTypes(Object object) {

        CharBuf buf = CharBuf.createCharBuf();
        return buf.prettyPrintObject(object, true, 0).toString();
    }

    public static boolean isEmpty(Object object) {
        return len(object) == 0;
    }
}