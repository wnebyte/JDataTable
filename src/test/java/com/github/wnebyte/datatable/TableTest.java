package com.github.wnebyte.datatable;

import org.junit.Test;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.github.wnebyte.datatable.io.TextReader;

public class TableTest {

    private static final String path = TableTest.class.getResource("/names.txt").getPath();

    static List<Person> getTestData(int size) {
        List<String> names = TextReader.read(new File(path));
        assert names != null;
        int upper = names.size() - 1;
        Random rand = new Random();
        List<Person> data = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            String firstName = names.get(rand.nextInt(upper));
            String lastName = names.get(rand.nextInt(upper));
            int age = randInt(5, 99);
            data.add(new Person(firstName, lastName, age));
        }

        return data;
    }


    static int randInt(int lower, int upper) {
        Random rand = new Random();
        int i = rand.nextInt(upper);
        while (i < lower) {
            i = rand.nextInt(upper);
        }
        return i;
    }

    public static void main(String[] args) {
        List<Person> data = getTestData(25);
        Table<Person> table = new Table<>();
        table.addHeader(new Table.Cell("FIRST NAME", Alignment.CENTER));
        table.addHeader("LAST NAME", Alignment.CENTER);
        table.addHeader("AGE", Alignment.CENTER);
        table.addColumn(Person::getFirstName, Alignment.RIGHT);
        table.addColumn(Person::getLastName, Alignment.RIGHT);
        table.addColumn(new Table.Column<>(p -> String.valueOf(p.getAge()), Alignment.CENTER, 2));
        table.setAutoGrowColumnSize(true);
        table.addAllRows(data);
        table.sync();
        System.out.println(table);
    }

    @Test
    public void test00() {
        Table.Cell cell = new Table.Cell("hello", Alignment.CENTER);
        System.out.println(cell.getLeftPadding(20));
    }

    private static class Person {

        static int counter = 0;

        String firstName, lastName;
        int age, index;

        Person(String firstName, String lastName, int age) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
            setIndex(counter++);
        }

        String getFirstName() { return firstName; }

        String getLastName() { return lastName; }

        int getAge() { return age; }

        int getIndex() { return index; }

        void setIndex(int index) { this.index = index; }
    }
}
