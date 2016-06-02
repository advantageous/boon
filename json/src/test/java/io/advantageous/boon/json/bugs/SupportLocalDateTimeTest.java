package io.advantageous.boon.json.bugs;

import io.advantageous.boon.json.JsonFactory;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class SupportLocalDateTimeTest {


    public static class Foo {
        LocalDateTime localDateTime = LocalDateTime.of(2016, 5, 5, 0, 0);
    }

    @Test
    public void test() {
        final String json = JsonFactory.toJson(new Foo());
        System.out.println(json);

        final Foo foo = JsonFactory.fromJson(json, Foo.class);

        assertEquals(new Foo().localDateTime, foo.localDateTime);
    }

}
