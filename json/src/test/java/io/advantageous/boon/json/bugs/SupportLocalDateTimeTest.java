package io.advantageous.boon.json.bugs;

import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.json.JsonFactory;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SupportLocalDateTimeTest {


    public static class Foo {
        LocalDateTime localDateTime;
    }

    @Test
    public void test() {

        final Foo foo1 = new Foo();
        foo1.localDateTime = LocalDateTime.of(2016, 5, 5, 0, 0);
        final String json = JsonFactory.toJson(foo1);
        System.out.println(json);

        final Foo foo = JsonFactory.fromJson(json, Foo.class);

        assertEquals(LocalDateTime.of(2016, 5, 5, 0, 0), foo.localDateTime);
    }



    @Test
    public void test2() {

        final Foo foo1 = new Foo();
        foo1.localDateTime = LocalDateTime.of(2016, 5, 5, 0, 0, 1);
        final String json = JsonFactory.toJson(foo1);
        System.out.println(json);

        final Foo foo = JsonFactory.fromJson(json, Foo.class);

        assertEquals(LocalDateTime.of(2016, 5, 5, 0, 0, 1), foo.localDateTime);
    }


    @Test
    public void test3() {

        final Foo foo1 = new Foo();
        foo1.localDateTime = LocalDateTime.of(2016, 5, 5, 2, 2, 1, 1);
        final String json = JsonFactory.toJson(foo1);
        System.out.println(json);

        final Foo foo = JsonFactory.fromJson(json, Foo.class);

        assertEquals(LocalDateTime.of(2016, 5, 5, 2, 2, 1, 1), foo.localDateTime);
    }


    @Test
    public void test4() {

        System.out.println(Conversions.coerce(LocalDateTime.class, "2016-06-21T21:10:45.3271564Z"));
        System.out.println(Conversions.coerce(LocalDateTime.class, "2016-06-21T21:11:45.5356272Z"));
        System.out.println();
        System.out.println(Conversions.coerce(LocalDateTime.class, "2016-06-21T21:10:45Z"));
        System.out.println(Conversions.coerce(LocalDateTime.class, "2016-06-21T21:11:45Z"));

    }

    class DateFecker {
        public LocalDateTime startTime;
        public LocalDateTime endTime;
    }



}
