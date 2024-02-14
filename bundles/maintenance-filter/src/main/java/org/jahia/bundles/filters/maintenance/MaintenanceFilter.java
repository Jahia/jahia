/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
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
        logger.debug("Activating Service...");
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
            logger.debug("Service activated with allowedResource: {}", this.allowedResources);
        }
    }

    @Override public void updated(Dictionary<String, ?> dictionary) {
        logger.debug("Updating service...");
        if (dictionary != null) {
            this.allowedResources = Collections.list(dictionary.keys()).stream().filter(k -> k.startsWith("allowedResource"))
                    .map(k -> dictionary.get(k).toString()).map(Pattern::compile).collect(Collectors.toSet());
        }
        if (allowedResources.isEmpty()) {
            logger.warn("Maintenance Filter allowedResource configuration is empty, this may cause an unreachable system if "
                    + "maintenance mode is activated.");
        } else {
            logger.debug("Service updated with allowedResource: {}", this.allowedResources);
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        logger.debug("Filter called");
        if (Jahia.isMaintenance()) {
            logger.debug("Filter is active, allowing only whitelisted resources");
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
