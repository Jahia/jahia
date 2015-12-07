/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.bin;

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.templates.ModuleState;
import org.jahia.data.templates.ModuleState.State;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

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
        
        finishInit();
        
        long initializationTime = System.currentTimeMillis() - JahiaContextLoaderListener.getStartupTime() ;
        StringBuilder out = new StringBuilder(256);
        if (SettingsBean.getInstance().isDevelopmentMode()) {
            out.append("\n--------------------------------------------------------------------------------------------------" +
            "\n  D E V E L O P M E N T   M O D E   A C T I V E" +
            "\n" +
            "\n  In development mode, Digital Experience Manager will allow JSPs to be modified, modules to be re-deployed and" +
            "\n  other modifications to happen immediately, but these DO have a performance impact. It is strongly " +
            "\n  recommended to switch to production mode when running performance tests or going live. The " +
            "\n  setting to change modes is called operatingMode in the jahia.properties configuration file.");
        } else if (SettingsBean.getInstance().isDistantPublicationServerMode()) {
            out.append("\n--------------------------------------------------------------------------------------------------" +
            "\n  D I S T A N T  P U B L I C A T I O N  S E R V E R  M O D E   A C T I V E");
        } else {
            out.append("\n--------------------------------------------------------------------------------------------------" +
            "\n  P R O D U C T I O N   M O D E   A C T I V E");
        }
        out.append("\n--------------------------------------------------------------------------------------------------\n");
        appendModulesInfo(out);
        out.append("\n--------------------------------------------------------------------------------------------------"+
        "\n  ").append(Jahia.getFullProductVersion()).append(" is now ready. Initialization completed in ").append((initializationTime/1000)).append(" seconds");
        out.append("\n--------------------------------------------------------------------------------------------------");
        logger.info(out.toString());
        initialized = true;
    }

    private void finishInit() {
        try {
            // start schedulers
            ServicesRegistry.getInstance().getSchedulerService().startSchedulers();
        } catch (JahiaInitializationException e) {
            logger.error(e.getMessage(), e);
            throw new JahiaRuntimeException(e);
        }
    }

    private void appendModulesInfo(StringBuilder out) {
        JahiaTemplateManagerService templateService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
        out.append("  Modules:");
        for (State state : ModuleState.State.values()) {
            List<Bundle> modules = templateService.getModulesByState(state);
            if (modules.isEmpty()) {
                continue;
            }
            out.append("\n      ").append(state).append(": ").append(modules.size());
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        logger.info("Digital Experience Manager is shutting down, please wait...");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!initialized) {
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

}
