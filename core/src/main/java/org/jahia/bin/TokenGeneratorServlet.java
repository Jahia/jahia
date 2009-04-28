package org.jahia.bin;

import org.jahia.operations.valves.FormValve;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Apr 28, 2009
 * Time: 10:13:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class TokenGeneratorServlet extends HttpServlet {

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String token = req.getParameter("formToken");
        try {
            String userToken = FormValve.createNewUserToken(token);
            PrintWriter out = resp.getWriter();
            out.print(userToken);
        } catch (Exception e) {
             e.printStackTrace();
        }
    }
}
