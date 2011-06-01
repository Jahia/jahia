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
        StringBuilder out = new StringBuilder(256);
        if (SettingsBean.getInstance().isDevelopmentMode()) {
            out.append("\n--------------------------------------------------------------------------------------------------" +
            "\n  D E V E L O P M E N T   M O D E   A C T I V E" +
            "\n" +
            "\n  In development mode, Jahia will allow JSPs to be modified, modules to be re-deployed and other" +
            "\n  modifications to happen immediately, but these DO have a performance impact. It is strongly " +
            "\n  recommended to switch to production mode when running performance tests or going live. The " +
            "\n  setting to change modes is called developmentMode in the jahia.properties configuration file.");
        } else if (SettingsBean.getInstance().isDistantPublicationServerMode()) {
            out.append("\n--------------------------------------------------------------------------------------------------" + 
            "\n  D I S T A N T  P U B L I C A T I O N  S E R V E R  M O D E   A C T I V E");
        } else {
            out.append("\n--------------------------------------------------------------------------------------------------" + 
            "\n  P R O D U C T I O N   M O D E   A C T I V E");
        }
        out.append("\n--------------------------------------------------------------------------------------------------"+
        "\n  Jahia is now ready. Initialization completed in ").append((initializationTime/1000)).append(" seconds");
        out.append("\n--------------------------------------------------------------------------------------------------");
        logger.info(out.toString());
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
