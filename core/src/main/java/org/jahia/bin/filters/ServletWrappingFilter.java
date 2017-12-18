/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bin.filters;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.beans.factory.BeanNameAware;

import com.google.common.collect.Iterators;

/**
 * Servlet filter that wraps the calls to the configured servlet class.<br>
 * Inspired by Spring's ServletWrappingController.
 * 
 * @author Sergiy Shyrkov
 */
public class ServletWrappingFilter extends AbstractServletFilter implements BeanNameAware {

    private class DelegatingServletConfig implements ServletConfig {

        public String getInitParameter(String paramName) {
            return initParameters.get(paramName);
        }

        public Enumeration<String> getInitParameterNames() {
            return Iterators.asEnumeration(initParameters.keySet().iterator());
        }

        public ServletContext getServletContext() {
            return filterConfig.getServletContext();
        }

        public String getServletName() {
            return servletName;
        }
    }

    private String beanName;

    private FilterConfig filterConfig;

    private Map<String, String> initParameters = Collections.emptyMap();

    private Class<?> servletClass;

    private Servlet servletInstance;

    private String servletName;

    @Override
    public void destroy() {
        if (servletInstance != null) {
            servletInstance.destroy();
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        servletInstance.service(servletRequest, servletResponse);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        if (this.servletClass == null) {
            throw new IllegalArgumentException("servletClass is required");
        }
        if (!Servlet.class.isAssignableFrom(this.servletClass)) {
            throw new IllegalArgumentException("servletClass [" + this.servletClass.getName()
                    + "] needs to implement interface [javax.servlet.Servlet]");
        }
        if (this.servletName == null) {
            this.servletName = this.beanName;
        }
        try {
            this.servletInstance = (Servlet) this.servletClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ServletException(e);
        }
        this.servletInstance.init(new DelegatingServletConfig());
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    public void setInitParameters(Map<String, String> initParameters) {
        this.initParameters = initParameters != null ? initParameters : Collections.<String, String> emptyMap();
    }

    /**
     * Set the class of the servlet to wrap. Needs to implement {@code javax.servlet.Servlet}.
     * 
     * @see javax.servlet.Servlet
     */
    public void setServletClass(Class<?> servletClass) {
        this.servletClass = servletClass;
    }

    /**
     * Set the name of the servlet to wrap. Default is the bean name of this controller.
     */
    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

}
