package org.jahia.bin;

import org.jahia.params.ProcessingContextFactory;
import org.jahia.params.ProcessingContext;
import org.jahia.params.BasicSessionState;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner;
import org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.*;

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
        ProcessingContext ctx = pcf.getContext(new BasicSessionState("123"));

        JahiaUser admin = JahiaAdminUser.getAdminUser(0);
        ctx.setTheUser(admin);

        if (httpServletRequest.getPathInfo() != null) {
            // Execute one test
            String className = httpServletRequest.getPathInfo().substring(1);
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
            // Return the lists of available tests
            InputStream is = getClass().getClassLoader().getResourceAsStream("Test.properties");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            PrintWriter pw = httpServletResponse.getWriter();
            try {
                while ((line = reader.readLine()) != null) {
                    pw.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }

}
