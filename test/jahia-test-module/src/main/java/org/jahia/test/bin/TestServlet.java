/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.test.bin;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.test.SurefireJUnitXMLResultFormatter;
import org.jahia.test.SurefireTestNGXMLResultFormatter;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.junit.internal.requests.FilterRequest;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Filter;
import org.springframework.core.io.Resource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.TestNG;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * JUnit test runner servlet.
 * User: toto
 * Date: Feb 11, 2009
 * Time: 4:07:40 PM
 */
public class TestServlet extends BaseTestController {
    
    private transient static Logger logger = LoggerFactory.getLogger(TestServlet.class);
    
    protected void handleGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {

            String pathInfo = StringUtils.substringAfter(httpServletRequest.getPathInfo(), "/test");
            String xmlTest = httpServletRequest.getParameter("xmlTest");
            if (pathInfo.contains("selenium") || !StringUtils.isEmpty(xmlTest)) {
                TestNG myTestNG = new TestNG();
                SurefireTestNGXMLResultFormatter xmlResultFormatter = new SurefireTestNGXMLResultFormatter(
                        httpServletResponse.getOutputStream());
                myTestNG.addListener((ISuiteListener) xmlResultFormatter);                
                myTestNG.addListener((ITestListener) xmlResultFormatter);
               
                Resource[] resources = null;
                if (!StringUtils.isEmpty(xmlTest)) {
                    resources = SpringContextSingleton
                            .getInstance()
                            .getResources(
                                    "classpath*:"
                                            + xmlTest
                                            + (!xmlTest.contains("org/jahia") ? ";classpath*:org/jahia/selenium/scripts/"
                                                    + xmlTest
                                                    : "")
                                            + (StringUtils.endsWith(pathInfo,
                                                    ".xml") ? ";classpath*:"
                                                    + pathInfo : ""));
                }
                if (resources != null) {
                    List<XmlSuite> allSuites = new ArrayList<XmlSuite>();
                    for (Resource re : resources) {
                        InputStream is = re.getInputStream();
                        try {
                            Parser parser = new Parser(is);
                            List<XmlSuite> suites = parser.parseToList();

                            for (XmlSuite suite : suites) {
                                suite.setPreserveOrder("true");
                                suite.setConfigFailurePolicy("continue");
                                for (XmlTest test : suite.getTests()) {
                                    test.setPreserveOrder("true");
                                }
                            }

                            allSuites.addAll(suites);
                        } catch (Exception e) {
                            logger.error("Error executing test", e);
                        } finally {
                            IOUtils.closeQuietly(is);
                        }
                    }
                    myTestNG.setXmlSuites(allSuites);
                } else {
                    String className = pathInfo.substring(pathInfo
                            .lastIndexOf('/') + 1);
                    try {
                        Class<?> testClass = Class.forName(className);
                        List<Class<?>> classes = getTestClasses(testClass,
                                new ArrayList<Class<?>>());
                        if (!classes.isEmpty()) {
                            myTestNG.setTestClasses(classes
                                    .toArray(new Class[classes.size()]));
                        }
                    } catch (Exception e) {
                        logger.error("Error executing test", e);
                    }
                }
                // myTestNG.setOutputDirectory(outputDirectory);
                myTestNG.setConfigFailurePolicy("continue");
                myTestNG.setPreserveOrder(true);
                myTestNG.run();
            } else if (StringUtils.isNotEmpty(pathInfo) && !pathInfo.contains("*")) {

                final Set<String> ignoreTests = getIgnoreTests();
                // Execute one test
                String className = pathInfo.substring(pathInfo.lastIndexOf('/')+1);
                try {
                    JUnitCore junitcore = new JUnitCore();
                    SurefireJUnitXMLResultFormatter xmlResultFormatter = new SurefireJUnitXMLResultFormatter(httpServletResponse.getOutputStream());
                    junitcore.addListener(xmlResultFormatter);
                    JahiaTemplatesPackage testPackage = findPackageForTestCase(className);
                    Class<?> testClass = testPackage != null ? testPackage.getClassLoader().loadClass(className) : Class.forName(className);
                    if (testClass == null) {
                        throw new Exception("Couldn't find origin module for test " + className);
                    }                    
                    List<Class<?>> classes = getTestClasses(testClass, new ArrayList<Class<?>>());
                    if (classes.isEmpty()) {
                        Description description = Description.createSuiteDescription(testClass);
                        xmlResultFormatter.testRunStarted(description);
                        xmlResultFormatter.testRunFinished(new Result());
                    } else {
                        junitcore.run(new FilterRequest(Request.classes(classes
                                .toArray(new Class[classes.size()])), new Filter() {

                            @Override
                            public boolean shouldRun(Description description) {
                                return !ignoreTests.contains(description.getDisplayName());
                            }

                            @Override
                            public String describe() {
                                return "Filter out Jahia configured methods";
                            }
                        }));
                    }
                } catch (Exception e) {
                    logger.error("Error executing test", e);
                }
            } else {
                Pattern testNamePattern = StringUtils.isNotEmpty(pathInfo) ? Pattern
                        .compile(pathInfo.length() > 1 && pathInfo.startsWith("/") ? pathInfo
                                .substring(1) : pathInfo) : null;
                Set<String> testCases = getAllTestCases();


                PrintWriter pw = httpServletResponse.getWriter();
                // Return the lists of available tests
                List<String> tests = new LinkedList<String>();
                    for (String o : testCases) {
                        if (testNamePattern == null || testNamePattern.matcher(o).matches()) {
                            tests.add(o);
                        }
                    }

                for (String c : tests) {
                    pw.println(c);
                }
            }
    }

    private Set<String> getAllTestCases() {
        Set<String> testCases = new TreeSet<String>();
        for (JahiaTemplatesPackage aPackage : ServicesRegistry.getInstance().getJahiaTemplateManagerService().getAvailableTemplatePackages()) {
            if (aPackage.getContext() != null) {
                Map<String,TestBean> packageTestBeans = aPackage.getContext().getBeansOfType(TestBean.class);
                if (packageTestBeans.size() > 0) {
                    for (TestBean testBean : packageTestBeans.values()) {
                        testCases.addAll(testBean.getTestCases());
                    }
                }
            }
        }
        return testCases;
    }

    private JahiaTemplatesPackage findPackageForTestCase(String testCase) {
        for (JahiaTemplatesPackage aPackage : ServicesRegistry.getInstance().getJahiaTemplateManagerService().getAvailableTemplatePackages()) {
            if (aPackage.getContext() != null) {
                Map<String,TestBean> packageTestBeans = aPackage.getContext().getBeansOfType(TestBean.class);
                if (packageTestBeans.size() > 0) {
                    for (TestBean testBean : packageTestBeans.values()) {
                        for (String beanTestCase : testBean.getTestCases()) {
                            if (beanTestCase.equals(testCase)) {
                                return aPackage;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private List<Class<?>> getTestClasses(Class<?> testClass, List<Class<?>> classes) {
        Method suiteMethod = null;
        try {
            // check if there is a suite method
            suiteMethod = testClass.getMethod("suite", new Class[0]);
        } catch (NoSuchMethodException e) {
            // no appropriate suite method found. We don't report any
            // error here since it might be perfectly normal.
        }

        if (suiteMethod != null) {
            // if there is a suite method available, then try
            // to extract the suite from it. If there is an error
            // here it will be caught below and reported.
            try {
                classes = getTestClasses((Test)suiteMethod.invoke(null, new Class[0]), classes);
                
            } catch (Exception e) {
                logger.error("Error getting classes of suite", e);
            }
        } else {
            classes.add(testClass);
        }
        return classes;
    }
    
    private List<Class<?>> getTestClasses(Test test, List<Class<?>> classes) {
        if (test instanceof TestSuite) {
            // if there is a suite method available, then try
            // to extract the suite from it. If there is an error
            // here it will be caught below and reported.
            Set<Class<?>> tempClasses = new HashSet<Class<?>>();
            for (Enumeration<Test> tests = ((TestSuite)test).tests(); tests.hasMoreElements(); ) {
                Test currentTest = tests.nextElement();
                if (currentTest instanceof TestSuite || !tempClasses.contains(currentTest.getClass())) {
                    classes = getTestClasses(currentTest, classes);
                    tempClasses.add(currentTest.getClass());
                }
            }
        } else {
            classes.add(test.getClass());
        }
        return classes;
    }
    
    private Set<String> getIgnoreTests() {
        // Return the lists of available tests
        Set<String> ignoreTests = new HashSet<String>();

        for (JahiaTemplatesPackage aPackage : ServicesRegistry.getInstance().getJahiaTemplateManagerService().getAvailableTemplatePackages()) {
            if (aPackage.getContext() != null) {
                Map<String,TestBean> packageTestBeans = aPackage.getContext().getBeansOfType(TestBean.class);
                if (packageTestBeans.size() > 0) {
                    for (TestBean testBean : packageTestBeans.values()) {
                        if (testBean.getIgnoredTests() != null) {
                            ignoreTests.addAll(testBean.getIgnoredTests());
                        }
                    }
                }
            }
        }

        return ignoreTests;
    }
}
