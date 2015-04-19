package io.advantageous.boon.json.bugs;

import io.advantageous.boon.json.JsonFactory;

import static io.advantageous.boon.core.IO.puts;

/**
 * Created by rhightower on 4/18/15.
 */
public class Bug311 {


    public static class SimpleObject {
        float f1 = Float.NEGATIVE_INFINITY;
    }
    public static void main (String... args) {


        puts(JsonFactory.toJson(new SimpleObject()));

        final String json = JsonFactory.toJson(new SimpleObject());

        final SimpleObject simpleObject = JsonFactory.fromJson(json, SimpleObject.class);

        puts (simpleObject.f1);

    }
}
