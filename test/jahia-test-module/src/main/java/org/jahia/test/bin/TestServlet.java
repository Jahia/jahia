/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.services.history.NodeVersionHistoryListener;
import org.jahia.test.SurefireJUnitXMLResultFormatter;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaNotFoundException;
import org.junit.internal.requests.FilterRequest;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * JUnit test runner servlet.
 * User: toto
 * Date: Feb 11, 2009
 * Time: 4:07:40 PM
 */
public class TestServlet extends BaseTestController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestServlet.class);
    private static final String TEST_ERROR_MSG = "Error executing test";

    static {
        // if this class is deployed, we disable the listener for deleting node version histories of nodes after a site is deleted
        NodeVersionHistoryListener.setDisabled(true);
    }

    protected void handleGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {

        String pathInfo = StringUtils.substringAfter(httpServletRequest.getPathInfo(), "/test");
        boolean isHtmlOutput = ".html".equals(pathInfo);
        if (StringUtils.isNotEmpty(pathInfo) && !pathInfo.contains("*") && !pathInfo.trim().equals("/") && !isHtmlOutput) {
            runIntegrationTests(httpServletRequest, httpServletResponse, pathInfo.substring(pathInfo.lastIndexOf('/') + 1));
        } else {
            Set<String> testCases = getAllTestCases(Boolean.valueOf(httpServletRequest.getParameter("skipCoreTests")));
            List<String> selectedTests = new LinkedList<>(); 
            if (!isHtmlOutput && StringUtils.isNotEmpty(pathInfo) && !pathInfo.trim().equals("/")) {
                Pattern testNamePattern = Pattern
                        .compile(pathInfo.length() > 1 && pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo);
                selectedTests = testCases.stream().filter(testCase -> testNamePattern.matcher(testCase).matches())
                        .collect(Collectors.toList());
            } else {
                selectedTests.addAll(testCases);
            }

            if (Boolean.parseBoolean(httpServletRequest.getParameter("run"))) {
                runIntegrationTests(httpServletRequest, httpServletResponse, selectedTests.stream().toArray(String[]::new));
            } else if (isHtmlOutput) {
                outputHtml(selectedTests, httpServletRequest, httpServletResponse);
            } else {
                PrintWriter pw = httpServletResponse.getWriter();
                selectedTests.forEach(pw::println);
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

    private void runIntegrationTests(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String... classNames)
            throws IOException {
        // Execute one test
        JUnitCore junitcore = new JUnitCore();
        Map<Class<?>, List<Class<?>>> testClassesPerSuites = getTestClassesPerSuites(classNames);
        SurefireJUnitXMLResultFormatter xmlResultFormatter = new SurefireJUnitXMLResultFormatter(httpServletResponse.getOutputStream());
        junitcore.addListener(xmlResultFormatter);
        List<Class<?>> testClassesToRun = new ArrayList<>();
        for (Map.Entry<Class<?>, List<Class<?>>> testClassesPerSuite : testClassesPerSuites.entrySet()) {
            try {
                Class<?> suiteOrTestClass = testClassesPerSuite.getKey();
                List<Class<?>> testClasses = testClassesPerSuite.getValue();
                if (testClasses.isEmpty()) {
                    Description description = Description.createSuiteDescription(suiteOrTestClass);
                    xmlResultFormatter.testRunStarted(description);
                    xmlResultFormatter.testRunFinished(new Result());
                } else {
                    testClassesToRun.addAll(testClasses);
                }
            } catch (Exception e) {
                logger.error(TEST_ERROR_MSG, e);
            }
        }
        if (!testClassesToRun.isEmpty()) {
            String methodName = httpServletRequest.getParameter("test");
            logger.info("Executing test classes {}", testClassesToRun.toArray());
            long start = System.currentTimeMillis();
            final Set<String> ignoreTests = getIgnoreTests();
            Runner runner = new FilterRequest(Request.classes(testClassesToRun.toArray(new Class[testClassesToRun.size()])), new Filter() {

                @Override
                public boolean shouldRun(Description description) {
                    return !ignoreTests.contains(description.getDisplayName())
                            && (methodName == null || methodName.equalsIgnoreCase(description.getMethodName()));
                }

                @Override
                public String describe() {
                    return "Filter out Jahia configured methods";
                }
            }).getRunner();

            if (runner instanceof ErrorReportingRunner) {
                logger.warn("No tests remain after applying ignoreTests filter {} in {}", ignoreTests, testClassesToRun.toArray());
            } else {
                junitcore.run(runner);
                logger.info("Done executing test classes {} in {} ms", testClassesToRun.toArray(), System.currentTimeMillis() - start);
            }
        }

    }
    
    private Map<Class<?>, List<Class<?>>> getTestClassesPerSuites(String... classNames) {
        Map<Class<?>, List<Class<?>>> testClassesPerSuites = new HashMap<>();
        for (String className : classNames) {
            try {
                JahiaTemplatesPackage testPackage = findPackageForTestCase(className);
                Class<?> testClass = testPackage != null ? testPackage.getClassLoader().loadClass(className) : Class.forName(className);
                if (testClass == null) {
                    throw new JahiaNotFoundException("Couldn't find origin module for test " + className);
                }
                logger.info("Will use test class {}", testClass.getName());
                testClassesPerSuites.put(testClass, getTestClasses(testClass, new ArrayList<Class<?>>()));
            } catch (Exception e) {
                logger.error("Error getting tests", e);
            }
        }
        return testClassesPerSuites;
    }

    private Set<String> getAllTestCases(boolean skipCore) {
        Set<String> testCases = new TreeSet<>();
        for (JahiaTemplatesPackage aPackage : ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                .getAvailableTemplatePackages()) {
            if (aPackage.getContext() != null) {
                Map<String, TestBean> packageTestBeans = aPackage.getContext().getBeansOfType(TestBean.class);
                for (TestBean testBean : packageTestBeans.values()) {
                    if (!skipCore || !testBean.isCoreTests()) {
                        testCases.addAll(testBean.getTestCases());
                    }
                }
            }
        }
        return testCases;
    }

    private JahiaTemplatesPackage findPackageForTestCase(String testCase) {
        for (JahiaTemplatesPackage aPackage : ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                .getAvailableTemplatePackages()) {
            if (aPackage.getContext() != null) {
                Map<String, TestBean> packageTestBeans = aPackage.getContext().getBeansOfType(TestBean.class);
                for (TestBean testBean : packageTestBeans.values()) {
                    for (String beanTestCase : testBean.getTestCases()) {
                        if (beanTestCase.equals(testCase)) {
                            return aPackage;
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
            suiteMethod = testClass.getMethod("suite", (Class[])null);
        } catch (NoSuchMethodException e) {
            // no appropriate suite method found. We don't report any
            // error here since it might be perfectly normal.
        }

        if (suiteMethod != null) {
            // if there is a suite method available, then try
            // to extract the suite from it. If there is an error
            // here it will be caught below and reported.
            try {
                classes = getTestClasses((Test)suiteMethod.invoke(null,  (Object[])null), classes);
                
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
            Set<Class<?>> tempClasses = new HashSet<>();
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
        Set<String> ignoreTests = new HashSet<>();

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
