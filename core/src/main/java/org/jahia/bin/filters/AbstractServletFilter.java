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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
