/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;


/**
 * Prints XML output of the test to a specified Writer.
 *
 * @see FormatterElement
 */

public class SurefireJUnitXMLResultFormatter extends RunListener {

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

    /** name attribute for property, testcase and testsuite elements */
    private static final String ATTR_NAME = "name";

    /** time attribute for testcase and testsuite elements */
    private static final String ATTR_TIME = "time";

    /** errors attribute for testsuite elements */
    private static final String ATTR_ERRORS = "errors";

    /** failures attribute for testsuite elements */
    private static final String ATTR_FAILURES = "failures";

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
    private Map<Description, Element> testElements = new HashMap<>();
    /**
     * tests that failed.
     */
    private Set<Description> failedTests = new HashSet<>();
    /**
     * Timing helper.
     */
    private Map<Description, Long> testStarts = new HashMap<>();
    /**
     * Where to write the log to.
     */
    private OutputStream out;

    /** No arg constructor. */
    public SurefireJUnitXMLResultFormatter() {
    }

    /** No arg constructor. */
    public SurefireJUnitXMLResultFormatter(OutputStream out) {
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
     * @param suite the testsuite.
     */
    @Override
    public void testRunStarted(Description description) throws Exception {
        doc = getDocumentBuilder().newDocument();
        rootElement = doc.createElement(TESTSUITE);
        String n = description.getDisplayName();
        rootElement.setAttribute(ATTR_NAME, n == null ? UNKNOWN : n);

        //add the timestamp
        final String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
        rootElement.setAttribute(TIMESTAMP, timestamp);
        //and the hostname.
        rootElement.setAttribute(HOSTNAME, getHostname());
    }

    /**
     * get the local hostname
     * @return the name of the local host, or "localhost" if we cannot work it out
     */
    private String getHostname()  {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    /**
     * The whole testsuite ended.
     * @param suite the testsuite.
     * @throws BuildException on error.
     */
    @Override
    public void testRunFinished(Result result) throws Exception {
        rootElement.setAttribute(ATTR_TESTS, "" + result.getRunCount());
        rootElement.setAttribute(ATTR_FAILURES, "" + result.getFailureCount());
        rootElement.setAttribute(ATTR_ERRORS, "" + result.getFailures().size());
        rootElement.setAttribute(
            ATTR_TIME, "" + (result.getRunTime() / ONE_SECOND));
        if (out != null) {
            try (OutputStreamWriter outWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                    Writer wri = new BufferedWriter(outWriter)) {
                wri.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
                (new DOMElementWriter()).write(rootElement, wri, 0, "  ");
                wri.flush();
            }
        }
    }

    /**
     * Interface RunListener.
     *
     * <p>A new Test is started.
     * @param t the test.
     */
    @Override
    public void testStarted(Description test) throws Exception {
        testStarts.put(test, System.currentTimeMillis());
    }

    /**
     * Interface RunListener.
     *
     * <p>A Test is finished.
     * @param test the test.
     */
    @Override
    public void testFinished(Description test) throws Exception {
        // Fix for bug #5637 - if a junit.extensions.TestSetup is
        // used and throws an exception during setUp then startTest
        // would never have been called
        if (!testStarts.containsKey(test)) {
            testStarted(test);
        }
        Element currentTest = null;
        if (!failedTests.contains(test)) {
            currentTest = doc.createElement(TESTCASE);
            String n = test.getDisplayName();
            currentTest.setAttribute(ATTR_NAME,
                                     n == null ? UNKNOWN : n);
            // a TestSuite can contain Tests from multiple classes,
            // even tests with the same name - disambiguate them.
            currentTest.setAttribute(ATTR_CLASSNAME,
                    test.getClassName());
            rootElement.appendChild(currentTest);
            testElements.put(test, currentTest);
        } else {
            currentTest = testElements.get(test);
        }

        Long l = testStarts.get(test);
        currentTest.setAttribute(ATTR_TIME,
            "" + ((System.currentTimeMillis()
                   - l.longValue()) / ONE_SECOND));
    }

    /**
     * Interface RunListener for JUnit &lt;= 3.4.
     *
     * <p>A Test failed.
     * @param test the test.
     * @param t the exception.
     */
    @Override
    public void testFailure(Failure failure) throws Exception {
        if (failure.getDescription() != null) {
            testFinished(failure.getDescription());
            failedTests.add(failure.getDescription());
        }

        Element nested = doc.createElement(FAILURE);
        Element currentTest = failure.getDescription() != null ? testElements.get(failure.getDescription()) : rootElement;

        currentTest.appendChild(nested);

        String message = failure.getMessage();
        if (message != null && message.length() > 0) {
            nested.setAttribute(ATTR_MESSAGE, failure.getMessage());
        }
        nested.setAttribute(ATTR_TYPE, failure.getClass().getName());

        String strace = failure.getTrace();
        Text trace = doc.createTextNode(strace);
        nested.appendChild(trace);
    }

    private void formatOutput(String type, String output) {
        Element nested = doc.createElement(type);
        rootElement.appendChild(nested);
        nested.appendChild(doc.createCDATASection(output));
    }

}
