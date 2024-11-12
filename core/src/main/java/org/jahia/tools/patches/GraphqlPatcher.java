/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.tools.patches;

import org.jahia.osgi.BundleLifecycleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;

import static org.jahia.tools.patches.Patcher.SUFFIX_FAILED;
import static org.jahia.tools.patches.Patcher.SUFFIX_INSTALLED;

/**
 * Execute graphql mutations in files ending with .graphql
 */
public class GraphqlPatcher implements PatchExecutor {
    private static final Logger logger = LoggerFactory.getLogger(GraphqlPatcher.class);
    private static final int TIMEOUT = 60000;

    @Override
    public boolean canExecute(String name, String lifecyclePhase) {
        return name.endsWith(lifecyclePhase + ".graphql");
    }

    @Override
    public String executeScript(String name, String scriptContent) {
        try {
            BundleContext context = FrameworkService.getBundleContext();
            String filter = "(component.name=graphql.kickstart.servlet.OsgiGraphQLHttpServlet)";
            ServiceTracker<?, ?> configurationAdminTracker = new ServiceTracker<>(context, context.createFilter(filter), null);
            configurationAdminTracker.open();
            Servlet servlet = (Servlet) configurationAdminTracker.waitForService(TIMEOUT);
            configurationAdminTracker.close();
            if (servlet == null) {
                logger.error("Cannot find OSGi graphql servlet to execute patch");
                return SUFFIX_FAILED;
            }

            JCRSessionFactory.getInstance().setCurrentUser(JahiaUserManagerService.getInstance().lookupRootUser().getJahiaUser());
            String[] mutations = (String[]) servlet.getClass().getMethod("getMutations").invoke(servlet);
            if (mutations == null || mutations.length == 0) {
                logger.warn("No mutations found in graphql servlet, attempting to rewire graphql dxm provider.");
                ServiceReference<?>[] serviceReferences = context.getServiceReferences("graphql.kickstart.servlet.osgi.GraphQLProvider", "(component.name=org.jahia.modules.graphql.provider.dxm.DXGraphQLProvider)");
                if (serviceReferences == null || serviceReferences.length == 0) {
                    logger.error("Cannot find OSGi Jahia graphql provider to rewire mutations");
                    return SUFFIX_FAILED;
                } else {
                    logger.info("Refreshing Jahia graphql provider to rewire mutations, will perform patch once refreshed.");
                    BundleLifecycleUtils.refreshBundle(serviceReferences[0].getBundle());
                    logger.info("Refreshed Jahia graphql provider will perform patch now.");
                }
            } else {
                logger.info("Found {} mutations in graphql servlet, will execute patch.", mutations.length);
            }
            String json = (String) servlet.getClass().getMethod("executeQuery", String.class).invoke(servlet, scriptContent);
            logger.info("Graphql execution result : {}", json);
            JSONObject object = new JSONObject(json);
            if (object.has("errors") && object.getJSONArray("errors").length() > 0) {
                return SUFFIX_FAILED;
            }
            return SUFFIX_INSTALLED;
        } catch (Exception e) {
            logger.error("Execution of script failed with error: {}", e.getMessage(), e);
            return SUFFIX_FAILED;
        } finally {
            JCRSessionFactory.getInstance().setCurrentUser(null);
        }
    }
}
