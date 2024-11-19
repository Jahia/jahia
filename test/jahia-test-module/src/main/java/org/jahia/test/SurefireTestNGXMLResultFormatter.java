/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;

import org.apache.tools.ant.util.DOMElementWriter;
import org.jahia.utils.xml.JahiaDocumentBuilderFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Prints XML output of the test to a specified Writer.
 *
 * @see FormatterElement
 */

public class SurefireTestNGXMLResultFormatter implements ISuiteListener,
        ITestListener {

    private static final double ONE_SECOND = 1000.0;

    /** constant for unnnamed testsuites/cases */
    private static final String UNKNOWN = "unknown";

    /** the system-err element */
    private static final String SYSTEM_ERR = "system-err";

    /** the system-out element */
    private static final String SYSTEM_OUT = "system-out";

    /** the testsuite element */
    private static final String TESTSUITE = "testsuite";

    /** the testcase element */
    private static final String TESTCASE = "testcase";

    /** the failure element */
    private static final String FAILURE = "failure";

    /** the skipped element */
    private static final String SKIPPED = "skipped";

    /** name attribute for property, testcase and testsuite elements */
    private static final String ATTR_NAME = "name";

    /** time attribute for testcase and testsuite elements */
    private static final String ATTR_TIME = "time";

    /** errors attribute for testsuite elements */
    private static final String ATTR_ERRORS = "errors";

    /** failures attribute for testsuite elements */
    private static final String ATTR_FAILURES = "failures";

    /** skipped attribute for testsuite elements */
    private static final String ATTR_SKIPPED = "failures";

    /** tests attribute for testsuite elements */
    private static final String ATTR_TESTS = "tests";

    /** type attribute for failure and error elements */
    private static final String ATTR_TYPE = "type";

    /** message attribute for failure elements */
    private static final String ATTR_MESSAGE = "message";

    /** classname attribute for testcase elements */
    private static final String ATTR_CLASSNAME = "classname";

    /**
     * timestamp of test cases
     */
    private static final String TIMESTAMP = "timestamp";

    /**
     * name of host running the tests
     */
    private static final String HOSTNAME = "hostname";

    private static DocumentBuilder getDocumentBuilder() {
        try {
            return JahiaDocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception exc) {
            throw new ExceptionInInitializerError(exc);
        }
    }

    /**
     * The XML document.
     */
    private Document doc;
    /**
     * The wrapper for the whole testsuite.
     */
    private Element rootElement;
    /**
     * Element for the current test.
     */
    private Map<String, Element> testElements = new HashMap<>();
    /**
     * tests that failed.
     */
    private Set<String> failedTests = new HashSet<>();
    /**
     * Timing helper.
     */
    private Map<String, Long> testStarts = new HashMap<>();
    /**
     * Where to write the log to.
     */
    private OutputStream out;

    /** No arg constructor. */
    public SurefireTestNGXMLResultFormatter() {
    }

    /** No arg constructor. */
    public SurefireTestNGXMLResultFormatter(OutputStream out) {
        setOutput(out);
    }

    /** {@inheritDoc}. */
    public void setOutput(OutputStream out) {
        this.out = out;
    }

    /** {@inheritDoc}. */
    public void setSystemOutput(String out) {
        formatOutput(SYSTEM_OUT, out);
    }

    /** {@inheritDoc}. */
    public void setSystemError(String out) {
        formatOutput(SYSTEM_ERR, out);
    }

    /**
     * The whole testsuite started.
     *
     * @param suite
     *            the testsuite.
     */
    public void onStart(ISuite suite) {
        doc = getDocumentBuilder().newDocument();
        rootElement = doc.createElement(TESTSUITE);
        String n = suite.getName();
        rootElement.setAttribute(ATTR_NAME, n == null ? UNKNOWN : n);

        // add the timestamp
        final String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .format(new Date());
        rootElement.setAttribute(TIMESTAMP, timestamp);
        // and the hostname.
        rootElement.setAttribute(HOSTNAME, getHostname());
    }

    /**
     * get the local hostname
     *
     * @return the name of the local host, or "localhost" if we cannot work it out
     */
    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    /**
     * The whole testsuite ended.
     *
     * @param suite
     *            the testsuite.
     * @throws BuildException
     *             on error.
     */
    public void onFinish(ISuite suite) {
        int tests = 0;
        int failures = 0;
        int skipped = 0;
        Date startDate = null;
        Date endDate = null;
        for (ISuiteResult suiteResult : suite.getResults().values()) {
            ITestContext context = suiteResult.getTestContext();
            tests += context.getAllTestMethods().length;
            failures += context.getFailedButWithinSuccessPercentageTests()
                    .size() + context.getFailedTests().size();
            skipped += context.getSkippedTests().size();
            if (startDate == null || startDate.after(context.getStartDate())) {
                startDate = context.getStartDate();
            }
            if (endDate == null || endDate.before(context.getEndDate())) {
                endDate = context.getEndDate();
            }
        }
        rootElement.setAttribute(ATTR_TESTS, "" + tests);
        rootElement.setAttribute(ATTR_FAILURES, "" + failures);
        rootElement.setAttribute(ATTR_ERRORS, "" + failures);
        rootElement.setAttribute(ATTR_SKIPPED, "" + skipped);
        rootElement.setAttribute(ATTR_TIME, endDate != null ? ""
                + ((endDate.getTime() - startDate.getTime()) / ONE_SECOND) : "0");
        if (out != null) {
            try (OutputStreamWriter outWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                    Writer wri = new BufferedWriter(outWriter)) {
                wri.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
                (new DOMElementWriter()).write(rootElement, wri, 0, "  ");
                wri.flush();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /**
     * Invoked each time before a test will be invoked. The <code>ITestResult</code> is only partially filled with the references to class,
     * method, start millis and status.
     *
     * @param result
     *            the partially filled <code>ITestResult</code>
     * @see ITestResult#STARTED
     */
    public void onTestStart(ITestResult result) {
        testStarts.put(result.getName(), System.currentTimeMillis());
    }

    /**
     * Interface RunListener.
     *
     * <p>
     * A Test is finished.
     *
     * @param test
     *            the test.
     */
    private void testFinished(ITestResult result) {
        Element currentTest = null;
        if (!failedTests.contains(result.getName())) {
            currentTest = doc.createElement(TESTCASE);
            currentTest.setAttribute(ATTR_NAME, getUserFriendlyTestName(result));
            // a TestSuite can contain Tests from multiple classes,
            // even tests with the same name - disambiguate them.
            currentTest.setAttribute(ATTR_CLASSNAME, result.getTestClass()
                    .getName());
            rootElement.appendChild(currentTest);
            testElements.put(getUserFriendlyTestName(result), currentTest);
        } else {
            currentTest = testElements
                    .get(getUserFriendlyTestName(result));
        }

        Long l = testStarts.get(result.getName());
        if (l != null) {
            currentTest.setAttribute(ATTR_TIME, ""
                + ((System.currentTimeMillis() - l.longValue()) / ONE_SECOND));
        } else {
            currentTest.setAttribute(ATTR_TIME, "0");
        }
    }

    /**
     * Invoked each time a method fails but has been annotated with successPercentage and this failure still keeps it within the success
     * percentage requested.
     *
     * @param result
     *            <code>ITestResult</code> containing information about the run test
     * @see ITestResult#SUCCESS_PERCENTAGE_FAILURE
     */
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        onTestFailure(result);
    }

    /**
     * Invoked each time a test fails.
     *
     * @param result
     *            <code>ITestResult</code> containing information about the run test
     * @see ITestResult#FAILURE
     */
    public void onTestFailure(ITestResult result) {
        if (result.getName() != null) {
            testFinished(result);
            failedTests.add(result.getName());
        }

        Element nested = doc.createElement(FAILURE);
        Element currentTest = testElements.get(getUserFriendlyTestName(result));

        currentTest.appendChild(nested);

        String message = result.getThrowable().getMessage();
        if (message != null && message.length() > 0) {
            nested.setAttribute(ATTR_MESSAGE, result.getThrowable()
                    .getMessage());
        }
        nested.setAttribute(ATTR_TYPE, result.getClass().getName());
        StringWriter writer = new StringWriter();
        result.getThrowable().printStackTrace(new PrintWriter(writer));
        String strace = writer.toString();
        Text trace = doc.createTextNode(strace);
        nested.appendChild(trace);
    }

    /**
     * Invoked each time a test is skipped.
     *
     * @param result
     *            <code>ITestResult</code> containing information about the run test
     * @see ITestResult#SKIP
     */
    public void onTestSkipped(ITestResult result) {
        if (result.getName() != null) {
            testFinished(result);
            failedTests.add(result.getName());
        }

        Element nested = doc.createElement(SKIPPED);
        Element currentTest = testElements.get(getUserFriendlyTestName(result));

        currentTest.appendChild(nested);

        nested.setAttribute(ATTR_MESSAGE, SKIPPED);
    }

    /**
     * Invoked each time a test succeeds.
     *
     * @param result
     *            <code>ITestResult</code> containing information about the run test
     * @see ITestResult#SUCCESS
     */
    public void onTestSuccess(ITestResult result) {
        testFinished(result);
    }

    private void formatOutput(String type, String output) {
        Element nested = doc.createElement(type);
        rootElement.appendChild(nested);
        nested.appendChild(doc.createCDATASection(output));
    }

    private static String getUserFriendlyTestName(ITestResult result) {
        // This is consistent with the JUnit output
        return result.getName() + "(" + result.getTestClass().getName() + ")";
    }

    @Override
    public void onStart(ITestContext context) {
        // No action yet
    }

    @Override
    public void onFinish(ITestContext context) {
        // No action yet
    }

}
