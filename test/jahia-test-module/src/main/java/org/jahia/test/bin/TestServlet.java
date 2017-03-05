/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.test.bin;

import org.jahia.registries.ServicesRegistry;
import org.jahia.test.SurefireJUnitXMLResultFormatter;
import org.jahia.test.SurefireTestNGXMLResultFormatter;
import org.jahia.utils.ClassLoaderUtils;
import org.jahia.utils.ClassLoaderUtils.Callback;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.junit.internal.requests.FilterRequest;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
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
import java.net.URL;
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
            boolean isHtmlOutput = ".html".equals(pathInfo);
            String xmlTest = httpServletRequest.getParameter("xmlTest");
            if (pathInfo.contains("selenium") || !StringUtils.isEmpty(xmlTest)) {
                JahiaTemplatesPackage seleniumModule = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById("selenium");
                if (seleniumModule == null) {
                    throw new ServletException("Selenium module not found (or not started)");
                }
                TestNG myTestNG = new TestNG();
                SurefireTestNGXMLResultFormatter xmlResultFormatter = new SurefireTestNGXMLResultFormatter(
                        httpServletResponse.getOutputStream());
                myTestNG.addListener((ISuiteListener) xmlResultFormatter);                
                myTestNG.addListener((ITestListener) xmlResultFormatter);
               
                final Enumeration<URL> resources = seleniumModule.getBundle().findEntries("testng", StringUtils.defaultIfBlank(xmlTest, "*.xml"), false);
                if (resources != null && resources.hasMoreElements()) {
                    final List<XmlSuite> allSuites = new ArrayList<XmlSuite>();
                    ClassLoaderUtils.executeWith(seleniumModule.getClassLoader(), new Callback<Boolean>() {
                        @Override
                        public Boolean execute() {
                            while (resources.hasMoreElements()) {
                                InputStream is = null;
                                try {
                                    is = resources.nextElement().openStream();
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
                            return Boolean.TRUE;
                        }
                    });
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

                String testOutputDirectory = httpServletRequest.getParameter("testOutputDirectory");
                if (!StringUtils.isEmpty(testOutputDirectory)) {
                    myTestNG.setOutputDirectory(testOutputDirectory);
                    logger.info("Output directory set to " + testOutputDirectory);
                }
                myTestNG.setConfigFailurePolicy("continue");
                myTestNG.setPreserveOrder(true);
                myTestNG.run();
            } else if (StringUtils.isNotEmpty(pathInfo) && !pathInfo.contains("*") && !pathInfo.trim().equals("/") && !isHtmlOutput) {
                runTest(httpServletRequest, httpServletResponse, pathInfo);
            } else {
                Pattern testNamePattern = !isHtmlOutput && StringUtils.isNotEmpty(pathInfo) && !pathInfo.trim().equals("/") ? Pattern
                        .compile(pathInfo.length() > 1 && pathInfo.startsWith("/") ? pathInfo
                                .substring(1) : pathInfo) : null;
                Set<String> testCases = getAllTestCases(Boolean.valueOf(httpServletRequest.getParameter("skipCoreTests")));


                PrintWriter pw = httpServletResponse.getWriter();
                // Return the lists of available tests
                List<String> tests = new LinkedList<String>();
                    for (String o : testCases) {
                        if (testNamePattern == null || testNamePattern.matcher(o).matches()) {
                            tests.add(o);
                        }
                    }

                if (isHtmlOutput) {
                    outputHtml(tests, httpServletRequest, httpServletResponse);
                } else {
                    for (String c : tests) {
                        pw.println(c);
                    }
                }
            }
    }

    private void outputHtml(List<String> tests, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=utf-8");
        PrintWriter pw = response.getWriter();
        pw.println("<!doctype html>");
        pw.println("");
        pw.println("<html lang=\"en\">");
        pw.println("<head>");
        pw.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>");
        pw.print("<link rel=\"stylesheet\" href=\"");
        pw.print(request.getContextPath());
        pw.println("/modules/tools/css/tools.css\" type=\"text/css\" />");
        pw.println("<title>DX Tests</title>");
        pw.println("</head>");
        pw.println("<body>");
        pw.println("<h1>");
        pw.println(tests.size());
        pw.println(" tests found");
        pw.println("</h1>");
        pw.println("<ul>");
        for (String test : tests) {
            pw.print("<li><a href=\"");
            pw.print(request.getContextPath());
            pw.print("/cms/test/");
            pw.print(test);
            pw.print("\">");
            pw.print(test);
            pw.println("</a></li>");
        }
        pw.println("</ul>");
        pw.println("</body>");
    }

    private void runTest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String pathInfo) {
        // Execute one test
        String className = pathInfo.substring(pathInfo.lastIndexOf('/')+1);
        String methodName = httpServletRequest.getParameter("test");
        try {
            JUnitCore junitcore = new JUnitCore();
            SurefireJUnitXMLResultFormatter xmlResultFormatter = new SurefireJUnitXMLResultFormatter(httpServletResponse.getOutputStream());
            junitcore.addListener(xmlResultFormatter);
            JahiaTemplatesPackage testPackage = findPackageForTestCase(className);
            Class<?> testClass = testPackage != null ? testPackage.getClassLoader().loadClass(className) : Class.forName(className);
            if (testClass == null) {
                throw new Exception("Couldn't find origin module for test " + className);
            }
            logger.info("Will use test class {}", testClass.getName());
            List<Class<?>> classes = getTestClasses(testClass, new ArrayList<Class<?>>());;
            if (classes.isEmpty()) {
                Description description = Description.createSuiteDescription(testClass);
                xmlResultFormatter.testRunStarted(description);
                xmlResultFormatter.testRunFinished(new Result());
            } else {
                if (methodName != null) {
                    logger.info("Executing test method {}.{}()", testClass.getName(), methodName);
                    long start = System.currentTimeMillis();
                    junitcore.run(Request.method(testClass, methodName));
                    logger.info("Done executing test method {}.{}() in {} ms", new Object[] { testClass.getName(),
                            methodName, System.currentTimeMillis() - start });
                } else {
                    logger.info("Executing test classes {}", classes.toArray());
                    long start = System.currentTimeMillis();
                    final Set<String> ignoreTests = getIgnoreTests();
                    Runner runner = new FilterRequest(Request.classes(classes
                            .toArray(new Class[classes.size()])), new Filter() {
    
                        @Override
                        public boolean shouldRun(Description description) {
                            return !ignoreTests.contains(description.getDisplayName());
                        }
    
                        @Override
                        public String describe() {
                            return "Filter out Jahia configured methods";
                        }
                    }).getRunner();
                    
                    if (runner instanceof ErrorReportingRunner) {
                        logger.warn("No tests remain after applying ignoreTests filter {} in {}", ignoreTests,
                                classes.toArray());
                    } else {
                        junitcore.run(runner);
                        logger.info("Done executing test classes {} in {} ms", classes.toArray(),
                                System.currentTimeMillis() - start);                        
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error executing test", e);
        }
    }

    private Set<String> getAllTestCases(boolean skipCore) {
        Set<String> testCases = new TreeSet<String>();
        for (JahiaTemplatesPackage aPackage : ServicesRegistry.getInstance().getJahiaTemplateManagerService().getAvailableTemplatePackages()) {
            if (aPackage.getContext() != null) {
                Map<String,TestBean> packageTestBeans = aPackage.getContext().getBeansOfType(TestBean.class);
                if (packageTestBeans.size() > 0) {
                    for (TestBean testBean : packageTestBeans.values()) {
                        if (!skipCore || !testBean.isCoreTests()) {
                            testCases.addAll(testBean.getTestCases());
                        }
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
