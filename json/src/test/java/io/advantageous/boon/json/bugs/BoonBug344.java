package io.advantageous.boon.json.bugs;

import io.advantageous.boon.json.JsonFactory;
import io.advantageous.boon.json.JsonSerializer;
import io.advantageous.boon.json.JsonSerializerFactory;
import io.advantageous.boon.json.ObjectMapper;
import org.junit.Test;

import java.util.HashMap;

import static junit.framework.Assert.assertEquals;

public class BoonBug344 {

    @Test
    public void test() {
        ObjectMapper mapper = JsonFactory.create();
        HashMap<String, Object> data = new HashMap<>();
        data.put("\"hi\"", Double.parseDouble("1.2312312"));
        assertEquals("{\"\"hi\"\":1.2312312}", mapper.writeValueAsString(data));
    }


    @Test
    public void test2() {
        final JsonSerializerFactory jsonSerializerFactory = new JsonSerializerFactory();
        final JsonSerializer jsonSerializer = jsonSerializerFactory.setSerializeMapKeys(true).setEncodeStrings(true).create();

        HashMap<String, Object> data = new HashMap<>();
        data.put("\"hi\"", Double.parseDouble("1.2312312"));

        assertEquals("{\"\\\"hi\\\"\":1.2312312}", jsonSerializer.serialize(data).toString());
    }

}
