package io.advantageous.boon.json.bugs;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.json.JsonSerializer;
import io.advantageous.boon.json.JsonSerializerFactory;
import io.advantageous.boon.primitive.Arry;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// https://github.com/advantageous/boon/issues/7
public class ABug7 {

    @Test
    public void testListComplex() {



        JsonSerializer jsonSerializer = new JsonSerializerFactory().setUseAnnotations(true)
                .addFilter((parent, fieldAccess) -> !fieldAccess.name().equals("metaClass")).create();

        final String json = jsonSerializer.serialize(Lists.list(1, 2, null, 3)).toString();

        assertNotNull(json);

        assertEquals("[1,2,null,3]", json);

    }


    @Test
    public void testArrayComplex() {



        JsonSerializer jsonSerializer = new JsonSerializerFactory().setUseAnnotations(true)
                .addFilter((parent, fieldAccess) -> !fieldAccess.name().equals("metaClass")).create();

        final String json = jsonSerializer.serialize(Arry.array(1, 2, null, 3)).toString();

        assertNotNull(json);

        assertEquals("[1,2,null,3]", json);

    }


    @Test
    public void testListSimple() {



        JsonSerializer jsonSerializer = new JsonSerializerFactory().create();

        final String json = jsonSerializer.serialize(Lists.list(1, 2, null, 3)).toString();

        assertNotNull(json);

        assertEquals("[1,2,null,3]", json);

    }


    @Test
    public void testArraySimple() {



        JsonSerializer jsonSerializer = new JsonSerializerFactory().create();

        final String json = jsonSerializer.serialize(Arry.array(1, 2, null, 3)).toString();

        assertNotNull(json);

        assertEquals("[1,2,null,3]", json);

    }
}
