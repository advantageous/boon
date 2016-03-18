package io.advantageous.boon.json.bugs;

import io.advantageous.boon.json.JsonFactory;
import org.junit.Test;

import static io.advantageous.boon.core.IO.puts;

/**
 * Created by rick on 3/18/16.
 */
public class ABug14 {

    public static class Employee {
        long id;
        int age;
        String name;

        public long getId() {
            return id;
        }

        public Employee setId(long id) {
            this.id = id;
            return this;
        }

        public int getAge() {
            return age;
        }

        public Employee setAge(int age) {
            this.age = age;
            return this;
        }

        public String getName() {
            return name;
        }

        public Employee setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public String toString() {
            return "Employee{" +
                    "id=" + id +
                    ", age=" + age +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    @Test(expected = Exception.class)
    public void test() {
        Employee employee = JsonFactory.fromJson("{\"id\":\"a\",\"name\":\"Rick\"}", Employee.class);
        puts(employee);
    }
}
