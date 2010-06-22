/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.bin;

import org.jahia.params.ProcessingContextFactory;
import org.jahia.params.ProcessingContext;
import org.jahia.params.BasicSessionState;
import org.jahia.params.ParamBean;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.exceptions.JahiaException;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner;
import org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import junit.framework.TestCase;

/**
 * JUnit test runner servlet.
 * User: toto
 * Date: Feb 11, 2009
 * Time: 4:07:40 PM
 */
@SuppressWarnings("serial")
public class TestServlet extends HttpServlet implements Controller, ServletContextAware {
    
    private transient static Logger logger = Logger.getLogger(TestServlet.class);
    
    private ServletContext servletContext;
    
    @SuppressWarnings("unchecked")
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
            ctx = pcf.getContext(new BasicSessionState("123"));
        }

        try {
            ctx.setOperationMode(ParamBean.EDIT);            
            ctx.setEntryLoadRequest(new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0, ctx.getLocales()));

            JahiaUser admin = JahiaAdminUser.getAdminUser(0);
            JCRSessionFactory.getInstance().setCurrentUser(admin);
            ctx.setTheUser(admin);
        } catch (JahiaException e) {
            logger.error("Error getting user", e);
        }

        try {
            String pathInfo = StringUtils.substringAfter(httpServletRequest.getPathInfo(), "/test");
            if (StringUtils.isNotEmpty(pathInfo)) {
                // Execute one test
                String className = pathInfo.substring(pathInfo.lastIndexOf('/')+1);
                try {
                    JUnitResultFormatter unitResultFormatter = new XMLJUnitResultFormatter();
                    unitResultFormatter.setOutput(httpServletResponse.getOutputStream());
                    JUnitTestRunner runner =  new JUnitTestRunner(new JUnitTest(className,false,false,false), false,false,false);
                    runner.addFormatter(unitResultFormatter);
                    runner.run();
                } catch (Exception e) {
                    logger.error("Error executing test", e);
                }
            } else {
                PrintWriter pw = httpServletResponse.getWriter();
                // Return the lists of available tests
                InputStream is = getClass().getClassLoader().getResourceAsStream("Test.properties");

                if (is != null) {
                    List<String> lines = IOUtils.readLines(is);
                    IOUtils.closeQuietly(is);
                    for (String line : lines) {
                        Class<?> c = getTestClass(line);
                        if (c != null) {
                            pw.println(c.getName());
                        }
                    }
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
}
