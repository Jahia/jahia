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
package org.jahia.services.atmosphere;

import static org.atmosphere.cpr.ApplicationConfig.PROPERTY_COMET_SUPPORT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.util.ServletContextFactory;
import org.jahia.settings.SettingsBean;

import com.google.common.collect.Lists;

/**
 * Jahia specific servlet for Atmosphere framework that allows to configure asynchronous support implementation using
 * <code>jahia.properties</code>.
 *
 * @author Sergiy Shyrkov
 */
public class AtmosphereServlet extends org.atmosphere.cpr.AtmosphereServlet {

    private static final String DEFAULT_ASYNC_SUPPORT = "org.atmosphere.container.Servlet30CometSupport";
    private static final String HEARTBEAT_FREQUENCY = "org.atmosphere.interceptor.HeartbeatInterceptor.heartbeatFrequencyInSeconds";

    private static final long serialVersionUID = 7618272237237696835L;

    /**
     * Looks up an instance of the {@link BroadcasterFactory}.
     *
     * @return an instance of the {@link BroadcasterFactory},
     * or null if broadcasterFactory can be accessed because ServletContext not ready
     */
    public static BroadcasterFactory getBroadcasterFactory() {
        ServletContext servletContext = ServletContextFactory.getDefault().getServletContext();
        if(servletContext != null) {
            return (BroadcasterFactory)  servletContext.getAttribute(BroadcasterFactory.class.getName());
        }
        return null;
    }

    @Override
    public void init(final ServletConfig sc) throws ServletException {
        ServletConfig scFacade;

        String asyncSupport = SettingsBean.getInstance().getAtmosphereAsyncSupport();
        String heartbeatFrequency = SettingsBean.getInstance().getAtmosphereHeartbeatFrequency();
        // override asyncSupport only if explicitly set via jahia.properties or not set at all
        if (StringUtils.isNotEmpty(asyncSupport) || sc.getInitParameter(PROPERTY_COMET_SUPPORT) == null) {
            final String implName = StringUtils.defaultIfBlank(asyncSupport, DEFAULT_ASYNC_SUPPORT);
            scFacade = new ServletConfig() {
                @Override
                public String getInitParameter(String name) {
                    switch (name) {
                        case PROPERTY_COMET_SUPPORT:
                            return implName;
                        case HEARTBEAT_FREQUENCY:
                            return heartbeatFrequency;
                        default:
                            return sc.getInitParameter(name);
                    }
                }

                @Override
                public Enumeration<String> getInitParameterNames() {
                    ArrayList<String> names = Lists.newArrayList(PROPERTY_COMET_SUPPORT);
                    CollectionUtils.addAll(names, sc.getInitParameterNames());
                    return Collections.enumeration(names);
                }

                @Override
                public ServletContext getServletContext() {
                    return sc.getServletContext();
                }

                @Override
                public String getServletName() {
                    return sc.getServletName();
                }
            };
        } else {
            scFacade = sc;
        }

        super.init(scFacade);
    }

}
