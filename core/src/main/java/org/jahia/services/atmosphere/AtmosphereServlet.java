/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
 * Digital Experience Manager specific servlet for Atmosphere framework that allows to configure asynchronous support implementation using
 * <code>jahia.properties</code>.
 * 
 * @author Sergiy Shyrkov
 */
public class AtmosphereServlet extends org.atmosphere.cpr.AtmosphereServlet {

    private static final String DEFAULT_ASYNC_SUPPORT = "org.atmosphere.container.Servlet30CometSupport";

    private static final long serialVersionUID = 7618272237237696835L;

    /**
     * Looks up an instance of the {@link BroadcasterFactory}.
     * 
     * @return an instance of the {@link BroadcasterFactory}
     */
    public static BroadcasterFactory getBroadcasterFactory() {
        return (BroadcasterFactory) ServletContextFactory.getDefault().getServletContext()
                .getAttribute(BroadcasterFactory.class.getName());
    }

    @Override
    public void init(final ServletConfig sc) throws ServletException {
        ServletConfig scFacade;

        String asyncSupport = SettingsBean.getInstance().getAtmosphereAsyncSupport();
        // override asyncSupport only if explicitly set via jahia.properties or not set at all
        if (StringUtils.isNotEmpty(asyncSupport) || sc.getInitParameter(PROPERTY_COMET_SUPPORT) == null) {
            final String implName = StringUtils.defaultIfBlank(asyncSupport, DEFAULT_ASYNC_SUPPORT);
            scFacade = new ServletConfig() {
                @Override
                public String getInitParameter(String name) {
                    return PROPERTY_COMET_SUPPORT.equals(name) ? implName : sc.getInitParameter(name);
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
