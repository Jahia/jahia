package org.jahia.bin;

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.slf4j.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This servlet is used to catch the end of the initialization of the web application, as the order of
 * initialization of a web application is :
 * - all listeners
 * - all filters
 * - all servlets
 * Using this servlet we can perform actions once the web application has completed, such as perform an
 * HTTP request to it to check if the web application has completed initialization or not.
 */
public class EndInit extends HttpServlet {

    private static final long serialVersionUID = -2221764992780224013L;

	private static Logger logger = org.slf4j.LoggerFactory.getLogger(EndInit.class);

    private boolean initialized = false;

    @Override
    public void init() throws ServletException {
        super.init();
        long initializationTime = System.currentTimeMillis() - JahiaContextLoaderListener.getStartupTime() ;
        logger.info("------------------------------------------------------------------------------------------------");
        logger.info("Jahia is now ready. Initialization completed in " + (initializationTime/1000) + " seconds");
        logger.info("------------------------------------------------------------------------------------------------");
        initialized = true;
    }

    @Override
    public void destroy() {
        super.destroy();
        logger.info("Jahia is shutting down, please wait...");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!initialized) {
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

}
