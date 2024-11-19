/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.bin;

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.templates.ModuleState;
import org.jahia.data.templates.ModuleState.State;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(EndInit.class);
    private static final String LINE = "--------------------------------------------------------------------------------------------------";

    private static final long serialVersionUID = -2221764992780224013L;

    private boolean initialized = false;

    private void appendModulesInfo() {
        JahiaTemplateManagerService templateService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
        logger.info("  Modules:");
        for (State state : ModuleState.State.values()) {
            List<Bundle> modules = templateService.getModulesByState(state);
            if (modules.isEmpty()) {
                continue;
            }
            logger.info("      {}: {}", state, modules.size());
        }
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

    @Override
    public void init() throws ServletException {
        super.init();

        logger.info("Got into EndInit");

        JahiaContextLoaderListener.endContextInitialized();

        printEndMessage();

        initialized = true;
    }

    private void printEndMessage() {
        long initializationTime = System.currentTimeMillis() - JahiaContextLoaderListener.getStartupTime() ;
        logger.info(LINE);
        if (SettingsBean.getInstance().isDevelopmentMode()) {
            logger.info("  D E V E L O P M E N T   M O D E   A C T I V E");
            logger.info("  In development mode, Jahia will allow JSPs to be modified, modules to be");
            logger.info("  re-deployed and other modifications to happen immediately, but these DO have a performance impact.");
            logger.info("  It is strongly recommended to switch to production mode when running performance tests or going live.");
            logger.info("  The setting to change modes is called operatingMode in the jahia.properties configuration file.");
        } else if (SettingsBean.getInstance().isDistantPublicationServerMode()) {
            logger.info("  D I S T A N T  P U B L I C A T I O N  S E R V E R  M O D E   A C T I V E");
        } else {
            logger.info("  P R O D U C T I O N   M O D E   A C T I V E");
        }
        logger.info(LINE);
        appendModulesInfo();
        logger.info(LINE);
        logger.info("  {} is now ready. Initialization completed in {} seconds", Jahia.getFullProductVersion(), initializationTime/1000);
        logger.info(LINE);
    }

}
