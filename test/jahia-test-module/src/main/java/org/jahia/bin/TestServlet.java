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

package org.jahia.bin;

import org.jahia.params.ProcessingContextFactory;
import org.jahia.params.ProcessingContext;
import org.jahia.params.BasicSessionState;
import org.jahia.params.ParamBean;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.test.JahiaAdminUser;
import org.jahia.test.SurefireJUnitXMLResultFormatter;
import org.jahia.exceptions.JahiaException;
import org.junit.internal.requests.FilterRequest;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Filter;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit test runner servlet.
 * User: toto
 * Date: Feb 11, 2009
 * Time: 4:07:40 PM
 */
@SuppressWarnings("serial")
public class TestServlet extends HttpServlet implements Controller, ServletContextAware {
    
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(TestServlet.class);
    
    private ServletContext servletContext;
    
    protected void handleGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {

//    should be protected in production
//        if (System.getProperty("org.jahia.selftest") == null) {
//            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
//            return;
//        }

        final ProcessingContextFactory pcf = (ProcessingContextFactory) SpringContextSingleton.
                getInstance().getContext().getBean(ProcessingContextFactory.class.getName());
        ProcessingContext ctx = null;

        try {
            // should send response wrapper !
            ctx = pcf.getContext(httpServletRequest, httpServletResponse, servletContext);
        } catch (JahiaException e) {
            logger.error("Error while trying to build ProcessingContext", e);
            return;
        }

        try {
            ctx.setOperationMode(ParamBean.EDIT);            
//            ctx.setEntryLoadRequest(new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0, ctx.getLocales()));

            JahiaUser admin = JahiaAdminUser.getAdminUser(0);
            JCRSessionFactory.getInstance().setCurrentUser(admin);
            ctx.setTheUser(admin);
        } catch (JahiaException e) {
            logger.error("Error getting user", e);
        }

        try {
            String pathInfo = StringUtils.substringAfter(httpServletRequest.getPathInfo(), "/test");
            if (StringUtils.isNotEmpty(pathInfo)) {
                final Set<String> ignoreTests = getIgnoreTests();
                // Execute one test
                String className = pathInfo.substring(pathInfo.lastIndexOf('/')+1);
                try {
                    JUnitCore junitcore = new JUnitCore();
                    SurefireJUnitXMLResultFormatter xmlResultFormatter = new SurefireJUnitXMLResultFormatter(httpServletResponse.getOutputStream());
                    junitcore.addListener(xmlResultFormatter);
                    Class testClass = Class.forName(className);
                    List<Class> classes = getTestClasses(testClass, new ArrayList<Class>());
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
                WebApplicationContext webApplicationContext = (WebApplicationContext) servletContext.getAttribute(WebApplicationContext.class.getName() + ".jahiaModules");
                Map<String,TestBean> testBeans = webApplicationContext.getBeansOfType(TestBean.class);

                PrintWriter pw = httpServletResponse.getWriter();
                // Return the lists of available tests
                List<String> tests = new LinkedList<String>();
                SortedSet<TestBean> s = new TreeSet<TestBean>(testBeans.values());
                for (TestBean testBean : s) {
                    for (String o : testBean.getTestCases()) {
                        tests.add(o);
                    }
                }

                for (String c : tests) {
					pw.println(c);
				}
            }
        } finally {
            try {
                ctx.setUserGuest();
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
        }

    }
    
    private List<Class> getTestClasses(Class testClass, List<Class> classes) {
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
    
    private List<Class> getTestClasses(Test test, List<Class> classes) {
        if (test instanceof TestSuite) {
            // if there is a suite method available, then try
            // to extract the suite from it. If there is an error
            // here it will be caught below and reported.
            Set<Class> tempClasses = new HashSet<Class>();
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
        WebApplicationContext webApplicationContext = (WebApplicationContext) servletContext.getAttribute(WebApplicationContext.class.getName() + ".jahiaModules");
        Map<String,TestBean> testBeans = webApplicationContext.getBeansOfType(TestBean.class);

        // Return the lists of available tests
        Set<String> ignoreTests = new HashSet<String>();

        SortedSet<TestBean> s = new TreeSet<TestBean>(testBeans.values());
        for (TestBean testBean : s) {
            if (testBean.getIgnoredTests() != null) {
                ignoreTests.addAll(testBean.getIgnoredTests());
            }
        }

        return ignoreTests;
    }

    private boolean isMethodAnnotationPresent(Class<?> checkedClass, Class<? extends Annotation> annotationClass) {
        boolean isPresent = false;
        for (Method method : checkedClass.getMethods()) {
            if (method.isAnnotationPresent(annotationClass)) {
                isPresent = true;
                break;
            }
        }
        return isPresent;
    }
    
    private Class<?> getTestClass(String line) {
        Class<?> clazz = null;
        if (StringUtils.isNotBlank(line) && !line.trim().startsWith("#") && !line.trim().startsWith("//")) {
            String clazzName = null;
            if (line.contains("/")) {
                // assume that it is a path for .class file
                clazzName = line.trim();
                clazzName = clazzName.replace('/','.').substring(0, clazzName.lastIndexOf('.'));
            } else {
                // assume it is fully qualified class name
                clazzName = line.trim();
            }
            try {
                Class<?> c = Class.forName(clazzName);
                if (TestCase.class.isAssignableFrom(c) || isMethodAnnotationPresent(c, org.junit.Test.class)) {
                    clazz = c;
                }
            } catch (ClassNotFoundException e) {
                logger.error("Error finding class for name " + line, e);
            }
        }
        
        return clazz;
    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (request.getMethod().equalsIgnoreCase("get")) {
            handleGet(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        return null;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    private List<String> readTests(String resource, boolean verify) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resource);
        List<String> tests = Collections.emptyList();
        try {
	        if (is != null) {
	        	tests = new LinkedList<String>();
	            @SuppressWarnings("unchecked")
                List<String> lines = IOUtils.readLines(is);
	            for (String line : lines) {
	            	if (verify) {
	            		// check the class
		                Class<?> c = getTestClass(line);
		                if (c != null) {
		                    tests.add(c.getName());
		                }
	            	} else if (StringUtils.isNotBlank(line) && !line.trim().startsWith("#") && !line.trim().startsWith("//")){
	            		// just add an non-empty line
	            		tests.add(line);
	            	}
	            }
	        }
        }
        catch (Exception e) {
        	logger.warn("Unable to read class names from the resource " + resource);
        } finally {
            IOUtils.closeQuietly(is);
        }
        
        return tests;
    }
}
