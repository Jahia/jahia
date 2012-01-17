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
    private Map<Description, Element> testElements = new HashMap<Description, Element>();
    /**
     * tests that failed.
     */
    private Set<Description> failedTests = new HashSet<Description>();
    /**
     * Timing helper.
     */
    private Map<Description, Long> testStarts = new HashMap<Description, Long>();
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
    public void testRunFinished(Result result) throws Exception {
        rootElement.setAttribute(ATTR_TESTS, "" + result.getRunCount());
        rootElement.setAttribute(ATTR_FAILURES, "" + result.getFailureCount());
        rootElement.setAttribute(ATTR_ERRORS, "" + result.getFailures().size());
        rootElement.setAttribute(
            ATTR_TIME, "" + (result.getRunTime() / ONE_SECOND));
        if (out != null) {
            Writer wri = null;
            try {
                wri = new BufferedWriter(new OutputStreamWriter(out, "UTF8"));
                wri.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
                (new DOMElementWriter()).write(rootElement, wri, 0, "  ");
            } catch (IOException exc) {
                throw new Exception("Unable to write log file", exc);
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
     * Interface RunListener.
     *
     * <p>A new Test is started.
     * @param t the test.
     */
    public void testStarted(Description test) throws Exception {
        testStarts.put(test, new Long(System.currentTimeMillis()));
    }

    /**
     * Interface RunListener.
     *
     * <p>A Test is finished.
     * @param test the test.
     */
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
                    test.getTestClass().getName());
            rootElement.appendChild(currentTest);
            testElements.put(test, currentTest);
        } else {
            currentTest = (Element) testElements.get(test);
        }

        Long l = (Long) testStarts.get(test);
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
    public void testFailure(Failure failure) throws Exception {
        if (failure.getDescription() != null) {
            testFinished(failure.getDescription());
            failedTests.add(failure.getDescription());
        }

        Element nested = doc.createElement(FAILURE);
        Element currentTest = null;
        if (failure.getDescription() != null) {
            currentTest = (Element) testElements.get(failure.getDescription());
        } else {
            currentTest = rootElement;
        }

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