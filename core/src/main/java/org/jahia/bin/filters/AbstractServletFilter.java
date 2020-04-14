/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.BeanNameAware;

import javax.servlet.DispatcherType;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.*;

/**
 * Base servlet filter implementation
 *
 * @author kevan
 */
public abstract class AbstractServletFilter implements javax.servlet.Filter, Comparable<AbstractServletFilter>, BeanNameAware {

    private String[] urlPatterns;
    private boolean matchAllUrls = false;
    private float order = 0.f;
    private String beanName;
    private String filterName;
    private Set<String> dispatcherTypes = Collections.singleton(DispatcherType.REQUEST.name());
    private Map<String, String> parameters = new HashMap<>();

    public void init(ServletContext servletContext) throws ServletException {
        init(new FilterConfigImpl(servletContext));
    }

    public String[] getUrlPatterns() {
        return urlPatterns;
    }

    public void setUrlPatterns(String[] urlPatterns) {
        this.urlPatterns = urlPatterns;
    }

    public boolean isMatchAllUrls() {
        return matchAllUrls;
    }

    public void setMatchAllUrls(boolean matchAllUrls) {
        this.matchAllUrls = matchAllUrls;
    }

    public float getOrder() {
        return order;
    }

    public void setOrder(float order) {
        this.order = order;
    }

    public String getFilterName() {
        return filterName != null ? filterName : beanName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getBeanName() {
        return beanName;
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Set<String> getDispatcherTypes() {
        return dispatcherTypes;
    }

    public void setDispatcherTypes(Set<String> dispatcherTypes) {
        this.dispatcherTypes = dispatcherTypes;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int compareTo(AbstractServletFilter other) {
        int result = Float.compare(getOrder(), other.getOrder());
        return result != 0 ? result : getClass().getName().compareTo(other.getClass().getName());
    }

    private class FilterConfigImpl implements FilterConfig {
        private ServletContext servletContext;

        public FilterConfigImpl(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        @Override
        public String getFilterName() {
            return AbstractServletFilter.this.getFilterName();
        }

        @Override
        public ServletContext getServletContext() {
            return servletContext;
        }

        @Override
        public String getInitParameter(String s) {
            return parameters.get(s);
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return new Vector<>(parameters.keySet()).elements();
        }
    }

}
