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
package org.jahia.bundles.filters.maintenance;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.bin.errors.ErrorServlet;
import org.jahia.bin.filters.AbstractServletFilter;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Allows temporary disabling request serving and switching to a maintenance mode.
 * Some URLs can be allowed in maintenance mode by adding regexp in the bundle config file :
 * - karaf/etc/org.jahia.bundles.filters.maintenance.yml
 * (allowed url patterns are reloaded at runtime)
 *
 * @author Sergiy Shyrkov
 * @author Jerome Blanchard
 */
@Component(service = { AbstractServletFilter.class, ManagedService.class }, property = {
        "pattern=/*" }, configurationPid = "org.jahia.bundles.filters.maintenance", immediate = true)
public class MaintenanceFilter extends AbstractServletFilter implements ManagedService {

    public static final String FILTER_NAME = "MaintenanceFilter";
    public static final float FILTER_ORDER = -4f;
    public static final boolean FILTER_MATCH_ALL_URLS = true;
    private static final Logger logger = LoggerFactory.getLogger(MaintenanceFilter.class);
    private Set<Pattern> allowedResources = new HashSet<>();

    @Activate public void activate(Map<String, ?> properties) {
        logger.info("Activating Service...");
        this.setOrder(FILTER_ORDER);
        this.setFilterName(FILTER_NAME);
        this.setMatchAllUrls(FILTER_MATCH_ALL_URLS);
        if (properties != null) {
            this.allowedResources = properties.entrySet().stream().filter(e -> e.getKey().startsWith("allowedResource"))
                    .map(e -> e.getValue().toString()).map(Pattern::compile).collect(Collectors.toSet());
        }
        if (allowedResources.isEmpty()) {
            logger.warn("Maintenance Filter allowedResource configuration is empty, this may cause an unreachable system if "
                    + "maintenance mode is activated.");
        } else {
            logger.info("Service activated with allowedResource: {}", this.allowedResources);
        }
    }

    @Override public void updated(Dictionary<String, ?> dictionary) {
        logger.info("Updating service...");
        if (dictionary != null) {
            this.allowedResources = Collections.list(dictionary.keys()).stream().filter(k -> k.startsWith("allowedResource"))
                    .map(k -> dictionary.get(k).toString()).map(Pattern::compile).collect(Collectors.toSet());
        }
        if (allowedResources.isEmpty()) {
            logger.warn("Maintenance Filter allowedResource configuration is empty, this may cause an unreachable system if "
                    + "maintenance mode is activated.");
        } else {
            logger.info("Service updated with allowedResource: {}", this.allowedResources);
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        logger.debug("Filter called");
        if (Jahia.isMaintenance()) {
            logger.debug("MaintenanceFilter is active, allowing only whitelisted resources");
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            final var uri = StringUtils.substringAfter(httpRequest.getRequestURI(), httpRequest.getContextPath());
            if (allowedResources.stream().noneMatch(pattern -> pattern.matcher(uri).matches())) {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, ErrorServlet.MAINTENANCE_MODE);
            }
        }
        chain.doFilter(request, response);
    }

    public void init(FilterConfig filterConfig) {
    }

    public void destroy() {
    }

}
