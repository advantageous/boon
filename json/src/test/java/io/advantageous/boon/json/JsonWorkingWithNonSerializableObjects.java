package io.advantageous.boon.json;

import io.advantageous.boon.core.IO;
import org.junit.Test;

import java.io.File;

import static io.advantageous.boon.core.IO.puts;
import static org.junit.Assert.assertEquals;

public class JsonWorkingWithNonSerializableObjects {

    @Test
    public void test() throws Exception {
        JsonWorkingWithNonSerializableObjects.main("none");
    }

    public static class Sales {

        final int price;
        final String name;


        public Sales(int price, String name) {
            this.price = price;
            this.name = name;
        }

        @Override
        public String toString() {
            return "Sales{" +
                    "price=" + price +
                    ", name='" + name + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Sales)) return false;

            Sales sales = (Sales) o;

            if (price != sales.price) return false;
            if (name != null ? !name.equals(sales.name) : sales.name != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = price;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }

    public static void main(String... args) throws Exception {


        File file = File.createTempFile("foo", "json");

        final JsonParserAndMapper jsonMapper =
                new JsonParserFactory()
                        .createFastObjectMapperParser();

        final JsonSerializer jsonSerializer = new JsonSerializerFactory()
                .create();

        final Sales salesOut = new Sales(1, "foo");
        final String json = jsonSerializer.serialize(salesOut).toString();

        puts(json);


        IO.write(file.toPath(), json);

        final String jsonIn = IO.read(file);


        puts("OUT", salesOut, jsonIn);





        final Sales salesIn = jsonMapper.parse(Sales.class, jsonIn);

        puts("OUT", salesIn, salesOut);


        assertEquals(salesOut, salesIn);



    }
}
