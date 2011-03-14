package org.jahia.bin;

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.settings.SettingsBean;
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
        if (SettingsBean.getInstance().isDevelopmentMode()) {
            logger.info("------------------------------------------------------------------------------------------------");
            logger.info("D E V E L O P M E N T   M O D E   A C T I V E");
            logger.info("");
            logger.info("In development mode, Jahia will allow JSPs to be modified, modules to be re-deployed and other");
            logger.info("modifications to happen immediately, but these DO have a performance impact. It is strongly ");
            logger.info("recommended to switch to production mode when running performance tests or going live. The ");
            logger.info("setting to change modes is called developmentMode in the jahia.properties configuration file.");
        } else {
            logger.info("------------------------------------------------------------------------------------------------");
            logger.info("P R O D U C T I O N   M O D E   A C T I V E");
        }
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
