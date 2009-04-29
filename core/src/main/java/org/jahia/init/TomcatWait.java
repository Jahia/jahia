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
//

package org.jahia.init;

import java.io.InputStream;
import java.net.URL;

/**
 * <p>Title: Tomcat waiting utility</p>
 * <p>Description: Small standalone utility to wait until the Tomcat web server
 * has finished initializing before going on with the startup script.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Serge Huber
 * @version 1.0
 */

public class TomcatWait
{
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(TomcatWait.class);

    private static final String DEFAULT_TARGET_URL = "http://localhost:8080/cms/html/startup/startjahia.html";

    public static void main(String args[])
    {
        String targetURL = DEFAULT_TARGET_URL;
        if (args.length == 0) {
            logger.error("Missing targetURL, defaulting to " + DEFAULT_TARGET_URL);
        } else {
            targetURL = args[0];
        }
        URL url = null;
        try
        {
            url = new URL(targetURL);
        }
        catch(Throwable throwable)
        {
            logger.error("Error in URL parameter : " + targetURL, throwable);
            return;
        }
        boolean flag = false;
        System.out.print("Waiting for Web Server to become available at " + targetURL + ".");
        try {
            Thread.sleep(1000); // sleep 1 second before making the connection
        } catch (InterruptedException ie) {
            logger.error(ie.getMessage(), ie);
        }
        while(!flag) {
            System.out.print(".");
            try {
                Thread.sleep(500); // sleep 500 ms between tries.
            } catch (InterruptedException ie) {
                logger.error(ie.getMessage(), ie);
            }

            try
            {
                InputStream inputstream = url.openStream();
                flag = true;
                inputstream.close();
            }
            catch(Exception throwable1)
            {
                flag = false;
            }
        }
        System.out.println("");
        System.out.println("Web Server now available.");
    }
}
