package com.amsu.test;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void MyTest(){
        try {
            Date date = (Date) Class.forName(Date.class.getName()).newInstance();
            System.out.println(date);

            int a;
            List<Integer> integers = new ArrayList<>();
            integers.add(2);

            Integer integer = integers.get(a = 0);
            System.out.println(integer);
            System.out.println(a);


        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}