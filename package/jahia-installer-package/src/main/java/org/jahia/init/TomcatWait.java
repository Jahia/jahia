/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
//

package org.jahia.init;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.izforge.izpack.util.AbstractUIProcessHandler;

/**
 * <p>Title: Tomcat waiting utility</p>
 * <p>Description: Small standalone utility to wait until the Tomcat web server
 * has finished initializing before going on with the startup script.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Serge Huber
 * @version 1.0
 */

public class TomcatWait {
    private static final String DEFAULT_TARGET_URL = "http://localhost:8080/cms/html/startup/startjahia.html";

    public static void main(String args[]) throws MalformedURLException {
        execute(args.length == 0 ? DEFAULT_TARGET_URL : args[0]);
    }

    /**
     * @param handler The UI handler for user interaction and to send output to.
     * @return true on success, false if processing should stop
     */
    public boolean run(AbstractUIProcessHandler handler, String[] args) {
        try {
            execute(args.length == 0 ? DEFAULT_TARGET_URL : args[0]);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static void execute(String pingUrl) throws MalformedURLException {
        URL url = new URL(pingUrl);

        boolean flag = false;
        System.out.print("Waiting for Web Server to become available at " + url);

        try {
            Thread.sleep(1000); // sleep 1 second before making the connection
        } catch (InterruptedException ie) {
            // ignore it
        }
        while (!flag) {
            System.out.print(".");
            try {
                Thread.sleep(500); // sleep 500 ms between tries.
            } catch (InterruptedException ie) {
                // ignore it
            }

            try {
                InputStream inputstream = url.openStream();
                flag = true;
                inputstream.close();
            } catch (Exception throwable1) {
                flag = false;
            }
        }
        System.out.println("\nWeb Server now available.");
    }
}