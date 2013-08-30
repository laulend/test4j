package org.test4j.testng.spring;

import org.test4j.module.spring.annotations.SpringContext;
import org.test4j.testng.JTester;
import org.testng.annotations.Test;

@Test(groups = { "jtester", "mockbean" })
@SpringContext( { "org/jtester/module/spring/testedbeans/xml/beans.xml",
		"org/jtester/module/spring/testedbeans/xml/data-source.xml" })
public class MockedBeanByNameTest_Base extends JTester {

}