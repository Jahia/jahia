/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.bin;

import org.jahia.params.ProcessingContextFactory;
import org.jahia.params.ProcessingContext;
import org.jahia.params.BasicSessionState;
import org.jahia.params.ParamBean;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.services.applications.ServletIncludeResponseWrapper;
import org.jahia.exceptions.JahiaException;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner;
import org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.ServletException;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.Enumeration;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 11, 2009
 * Time: 4:07:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestServlet  extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {

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
            ctx = pcf.getContext(httpServletRequest, httpServletResponse, getServletContext());
        } catch (JahiaException e) {
            ctx = pcf.getContext(new BasicSessionState("123"));
        }

        try {
            ctx.setOperationMode(ParamBean.EDIT);
            JahiaUser admin = JahiaAdminUser.getAdminUser(0);
            ctx.setTheUser(admin);
        } catch (JahiaException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        String pathInfo = httpServletRequest.getPathInfo();
        if (pathInfo != null) {
            // Execute one test
            String className = pathInfo.substring(pathInfo.lastIndexOf('/')+1);
            try {
                XMLJUnitResultFormatter unitResultFormatter = new XMLJUnitResultFormatter();
                unitResultFormatter.setOutput(httpServletResponse.getOutputStream());
                JUnitTestRunner runner =  new JUnitTestRunner(new JUnitTest(className,false,false,false), false,false,false);
                runner.addFormatter(unitResultFormatter);
                runner.run();
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } else {
            PrintWriter pw = httpServletResponse.getWriter();
            // Return the lists of available tests
            URLConnection url = getClass().getClassLoader().getResource("Test.properties").openConnection();
            if (url instanceof JarURLConnection) {
                JarFile file = ((JarURLConnection) getClass().getClassLoader().getResource("Test.properties").openConnection()).getJarFile();
                Enumeration<JarEntry> en = file.entries();
                while (en.hasMoreElements()) {
                    JarEntry jarEntry = en.nextElement();
                    String n = jarEntry.getName();
                    if (n.endsWith(".class")) {
                        try {
                            Class c = Class.forName(n.replace('/','.').substring(0, n.lastIndexOf('.')));
                            if (TestCase.class.isAssignableFrom(c)) {
                                pw.println(c.getName());
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }

                    }
                }
            }
        }

    }

}
