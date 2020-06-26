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
package org.jahia.bundles.extender.jahiamodules.jsp;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

/**
 * Simple wrapper that adds -pax at the end of attribute names, to avoid mixing up with main tomcat context attributes
 */
public class ServletContextWrapper implements ServletContext {
    private static final String PAX_NAME_SUFFIX = "-pax";

    private ServletContext servletContext;

    public ServletContextWrapper(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public String getContextPath() {
        return servletContext.getContextPath();
    }

    @Override
    public ServletContext getContext(String s) {
        return servletContext.getContext(s);
    }

    @Override
    public int getMajorVersion() {
        return servletContext.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return servletContext.getMinorVersion();
    }

    @Override
    public int getEffectiveMajorVersion() {
        return servletContext.getEffectiveMajorVersion();
    }

    @Override
    public int getEffectiveMinorVersion() {
        return servletContext.getEffectiveMinorVersion();
    }

    @Override
    public String getMimeType(String s) {
        return servletContext.getMimeType(s);
    }

    @Override
    public Set<String> getResourcePaths(String s) {
        return servletContext.getResourcePaths(s);
    }

    @Override
    public URL getResource(String s) throws MalformedURLException {
        return servletContext.getResource(s);
    }

    @Override
    public InputStream getResourceAsStream(String s) {
        return servletContext.getResourceAsStream(s);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return servletContext.getRequestDispatcher(s);
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String s) {
        return servletContext.getNamedDispatcher(s);
    }

    @Override
    public Servlet getServlet(String s) throws ServletException {
        return servletContext.getServlet(s);
    }

    @Override
    public Enumeration<Servlet> getServlets() {
        return servletContext.getServlets();
    }

    @Override
    public Enumeration<String> getServletNames() {
        return servletContext.getServletNames();
    }

    @Override
    public void log(String s) {
        servletContext.log(s);
    }

    @Override
    public void log(Exception e, String s) {
        servletContext.log(e, s);
    }

    @Override
    public void log(String s, Throwable throwable) {
        servletContext.log(s, throwable);
    }

    @Override
    public String getRealPath(String s) {
        return servletContext.getRealPath(s);
    }

    @Override
    public String getServerInfo() {
        return servletContext.getServerInfo();
    }

    @Override
    public String getInitParameter(String s) {
        return servletContext.getInitParameter(s);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return servletContext.getInitParameterNames();
    }

    @Override
    public boolean setInitParameter(String s, String s1) {
        return servletContext.setInitParameter(s, s1);
    }

    @Override
    public Object getAttribute(String s) {
        s += PAX_NAME_SUFFIX;
        return servletContext.getAttribute(s);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return servletContext.getAttributeNames();
    }

    @Override
    public void setAttribute(String s, Object o) {
        s += PAX_NAME_SUFFIX;
        servletContext.setAttribute(s, o);
    }

    @Override
    public void removeAttribute(String s) {
        s += PAX_NAME_SUFFIX;
        servletContext.removeAttribute(s);
    }

    @Override
    public String getServletContextName() {
        return servletContext.getServletContextName();
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String s, String s1) {
        return servletContext.addServlet(s, s1);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String s, Servlet servlet) {
        return servletContext.addServlet(s, servlet);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String s, Class<? extends Servlet> aClass) {
        return servletContext.addServlet(s, aClass);
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> aClass) throws ServletException {
        return servletContext.createServlet(aClass);
    }

    @Override
    public ServletRegistration getServletRegistration(String s) {
        return servletContext.getServletRegistration(s);
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return servletContext.getServletRegistrations();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String s, String s1) {
        return servletContext.addFilter(s, s1);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String s, Filter filter) {
        return servletContext.addFilter(s, filter);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String s, Class<? extends Filter> aClass) {
        return servletContext.addFilter(s, aClass);
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> aClass) throws ServletException {
        return servletContext.createFilter(aClass);
    }

    @Override
    public FilterRegistration getFilterRegistration(String s) {
        return servletContext.getFilterRegistration(s);
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return servletContext.getFilterRegistrations();
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return servletContext.getSessionCookieConfig();
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> set) {
        servletContext.setSessionTrackingModes(set);
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return servletContext.getDefaultSessionTrackingModes();
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return servletContext.getEffectiveSessionTrackingModes();
    }

    @Override
    public void addListener(String s) {
        servletContext.addListener(s);
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        servletContext.addListener(t);
    }

    @Override
    public void addListener(Class<? extends EventListener> aClass) {
        servletContext.addListener(aClass);
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> aClass) throws ServletException {
        return servletContext.createListener(aClass);
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return servletContext.getJspConfigDescriptor();
    }

    @Override
    public ClassLoader getClassLoader() {
        return servletContext.getClassLoader();
    }

    @Override
    public void declareRoles(String... strings) {
        servletContext.declareRoles(strings);
    }

    @Override
    public String getVirtualServerName() {
        return servletContext.getVirtualServerName();
    }
}
