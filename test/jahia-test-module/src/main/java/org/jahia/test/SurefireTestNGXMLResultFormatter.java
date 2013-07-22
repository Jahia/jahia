/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.util.DOMElementWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private transient static Logger logger = LoggerFactory
            .getLogger(SurefireTestNGXMLResultFormatter.class);

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
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
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
    private Map<String, Element> testElements = new HashMap<String, Element>();
    /**
     * tests that failed.
     */
    private Set<String> failedTests = new HashSet<String>();
    /**
     * Timing helper.
     */
    private Map<String, Long> testStarts = new HashMap<String, Long>();
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
        rootElement.setAttribute(ATTR_TIME, ""
                + ((endDate.getTime() - startDate.getTime()) / ONE_SECOND));
        if (out != null) {
            Writer wri = null;
            try {
                wri = new BufferedWriter(new OutputStreamWriter(out, "UTF8"));
                wri.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
                (new DOMElementWriter()).write(rootElement, wri, 0, "  ");
            } catch (IOException exc) {
                logger.error("Unable to write log file", exc);
            } finally {
                if (wri != null) {
                    try {
                        wri.flush();
                    } catch (IOException ex) {
                        // ignore
                    }
                }
                if (out != System.out && out != System.err) {
                    IOUtils.closeQuietly(wri);
                }
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
        testStarts.put(result.getName(), new Long(System.currentTimeMillis()));
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
            String n = getUserFriendlyTestName(result);
            currentTest.setAttribute(ATTR_NAME, n == null ? UNKNOWN : n);
            // a TestSuite can contain Tests from multiple classes,
            // even tests with the same name - disambiguate them.
            currentTest.setAttribute(ATTR_CLASSNAME, result.getTestClass()
                    .getName());
            rootElement.appendChild(currentTest);
            testElements.put(getUserFriendlyTestName(result), currentTest);
        } else {
            currentTest = (Element) testElements
                    .get(getUserFriendlyTestName(result));
        }

        Long l = (Long) testStarts.get(result.getName());
        currentTest.setAttribute(ATTR_TIME, ""
                + ((System.currentTimeMillis() - l.longValue()) / ONE_SECOND));
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
        Element currentTest = null;
        if (getUserFriendlyTestName(result) != null) {
            currentTest = (Element) testElements
                    .get(getUserFriendlyTestName(result));
        } else {
            currentTest = rootElement;
        }

        currentTest.appendChild(nested);

        String message = result.getThrowable().getMessage();
        if (message != null && message.length() > 0) {
            nested.setAttribute(ATTR_MESSAGE, result.getThrowable()
                    .getMessage());
        }
        nested.setAttribute(ATTR_TYPE, result.getClass().getName());
        StringWriter out = new StringWriter();
        result.getThrowable().printStackTrace(new PrintWriter(out));
        String strace = out.toString();
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
        Element currentTest = null;
        if (getUserFriendlyTestName(result) != null) {
            currentTest = (Element) testElements
                    .get(getUserFriendlyTestName(result));
        } else {
            currentTest = rootElement;
        }

        currentTest.appendChild(nested);

        String message = "skipped";
        if (message != null && message.length() > 0) {
            nested.setAttribute(ATTR_MESSAGE, message);
        }
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

    /**
     * Invoked after the test class is instantiated and before any configuration method is called.
     */
    public void onStart(ITestContext context) {

    }

    /**
     * Invoked after all the tests have run and all their Configuration methods have been called.
     */
    public void onFinish(ITestContext context) {

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

}
