package org.test4j.testng.jmockit;

import java.util.Calendar;
import java.util.Date;

import mockit.Mock;

import org.test4j.module.spring.annotations.AutoBeanInject;
import org.test4j.module.spring.annotations.SpringBeanByName;
import org.test4j.module.spring.annotations.SpringContext;
import org.test4j.testng.Test4J;
import org.test4j.tools.commons.DateHelper;
import org.testng.annotations.Test;

/**
 * 验证new MockUp<T> 的作用域
 * 
 * @author darui.wudr
 */
@Test(groups = "test4j")
@SpringContext("org/test4j/module/spring/testedbeans/xml/data-source.xml")
@AutoBeanInject
public class MockUpTest_Depends extends Test4J {
    @SpringBeanByName(claz = MyImpl.class)
    MyIntf myIntf;

    @Test
    public void testStaticMethod_mock() {
        new MockUp<DateHelper>() {
            @Mock
            public Date now() {
                Calendar cal = mockCalendar(2012, 1, 28);
                return cal.getTime();
            }
        };
        String str = DateHelper.currDateTimeStr("MM/dd/yy hh:mm:ss");
        want.string(str).isEqualTo("01/28/12 07:58:55");
    }

    @Test(dependsOnMethods = "testStaticMethod_mock", expectedExceptions = AssertionError.class)
    public void testStaticMethod_unmock() {
        String str = DateHelper.currDateTimeStr("MM/dd/yy hh:mm:ss");
        want.string(str).isEqualTo("01/28/12 07:58:55");
    }

    public void testMehtod_unmock_beforeMock() {
        String hello = myIntf.hello();
        want.string(hello).isEqualTo("hello");
    }

    @Test(dependsOnMethods = "testMehtod_unmock_beforeMock")
    public void testMethod_mock() {
        new MockUp<MyImpl>() {
            @Mock
            public String hello() {
                return "hello mock!";
            }
        };
        String hello = myIntf.hello();
        want.string(hello).isEqualTo("hello mock!");
    }

    @Test(dependsOnMethods = "testMethod_mock")
    public void testMehtod_unmock_afterMock() {
        String hello = myIntf.hello();
        want.string(hello).isEqualTo("hello");
    }

    public static interface MyIntf {
        String hello();
    }

    public static class MyImpl implements MyIntf {
        @Override
        public String hello() {
            return "hello";
        }
    }

    public static Calendar mockCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.set(2010, 1, 12, 19, 58, 55);
        return cal;
    }

    public static Calendar mockCalendar(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, 19, 58, 55);
        return cal;
    }
}
