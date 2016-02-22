/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.felix.http.base.internal.context;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.http.base.internal.logger.SystemLogger;
import org.apache.felix.http.base.internal.util.MimeTypes;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;

public final class ServletContextImpl
    implements ExtServletContext
{
    private final Bundle bundle;
    private final ServletContext context;
    private final HttpContext httpContext;
    private final Map<String, Object> attributes;
    private final ServletContextAttributeListener attributeListener;

    public ServletContextImpl(Bundle bundle, ServletContext context, HttpContext httpContext,
        ServletContextAttributeListener attributeListener, boolean sharedAttributes)
    {
        this.bundle = bundle;
        this.context = context;
        this.httpContext = httpContext;
        this.attributeListener = attributeListener;
        this.attributes = sharedAttributes ? null : new ConcurrentHashMap<String, Object>();
    }

    public String getContextPath()
    {
        return this.context.getContextPath();
    }

    public ServletContext getContext(String uri)
    {
        return this.context.getContext(uri);
    }

    public int getMajorVersion()
    {
        return this.context.getMajorVersion();
    }

    public int getMinorVersion()
    {
        return this.context.getMinorVersion();
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getEffectiveMinorVersion() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Set getResourcePaths(String path)
    {
        Enumeration paths = this.bundle.getEntryPaths(normalizePath(path));
        if ((paths == null) || !paths.hasMoreElements()) {
            return null;
        }

        Set<String> set = new HashSet<String>();
        while (paths.hasMoreElements()) {
            set.add((String) paths.nextElement());
        }

        return set;
    }

    public URL getResource(String path)
    {
        return this.httpContext.getResource(normalizePath(path));
    }

    public Bundle getBundle() {
        return bundle;
    }

    public InputStream getResourceAsStream(String path)
    {
        URL res = getResource(path);
        if (res != null) {
            try {
                return res.openStream();
            } catch (IOException e) {
                // Do nothing
            }
        }

        return null;
    }

    private String normalizePath(String path)
    {
        if (path == null) {
            return null;
        }

        String normalizedPath = path.trim().replaceAll("/+", "/");
        if (normalizedPath.startsWith("/") && (normalizedPath.length() > 1)) {
            normalizedPath = normalizedPath.substring(1);
        }

        return normalizedPath;
    }

    public RequestDispatcher getRequestDispatcher(String uri)
    {
        return null;
    }

    public RequestDispatcher getNamedDispatcher(String name)
    {
        return null;
    }

    public String getInitParameter(String name)
    {
        return context.getInitParameter(name);
    }

    public Enumeration getInitParameterNames()
    {
        return context.getInitParameterNames();
    }

    @Override
    public boolean setInitParameter(String s, String s2) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getAttribute(String name)
    {
        return (this.attributes != null) ? this.attributes.get(name) : this.context.getAttribute(name);
    }

    public Enumeration getAttributeNames()
    {
        return (this.attributes != null) ? Collections.enumeration(this.attributes.keySet()) : this.context
            .getAttributeNames();
    }

    public void setAttribute(String name, Object value)
    {
        if (value == null)
        {
            this.removeAttribute(name);
        }
        else if (name != null)
        {
            Object oldValue;
            if (this.attributes != null)
            {
                oldValue = this.attributes.put(name, value);
            }
            else
            {
                oldValue = this.context.getAttribute(name);
                this.context.setAttribute(name, value);
            }

            if (oldValue == null)
            {
                attributeListener.attributeAdded(new ServletContextAttributeEvent(this, name, value));
            }
            else
            {
                attributeListener.attributeReplaced(new ServletContextAttributeEvent(this, name, oldValue));
            }
        }
    }

    public void removeAttribute(String name)
    {
        Object oldValue;
        if (this.attributes != null)
        {
            oldValue = this.attributes.remove(name);
        }
        else
        {
            oldValue = this.context.getAttribute(name);
            this.context.removeAttribute(name);
        }

        if (oldValue != null)
        {
            attributeListener.attributeRemoved(new ServletContextAttributeEvent(this, name, oldValue));
        }
    }

    @SuppressWarnings("deprecation")
    public Servlet getServlet(String name)
        throws ServletException
    {
        return null;
    }

    @SuppressWarnings("deprecation")
    public Enumeration getServlets()
    {
        return Collections.enumeration(Collections.emptyList());
    }

    @SuppressWarnings("deprecation")
    public Enumeration getServletNames()
    {
        return Collections.enumeration(Collections.emptyList());
    }

    public void log(String message)
    {
        SystemLogger.info(message);
    }

    public void log(Exception cause, String message)
    {
        SystemLogger.error(message, cause);
    }

    public void log(String message, Throwable cause)
    {
        SystemLogger.error(message, cause);
    }

    public String getServletContextName()
    {
        return this.context.getServletContextName();
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String s, String s2) {
        return this.context.addServlet(s, s2);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String s, Servlet servlet) {
        return this.context.addServlet(s, servlet);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String s, Class<? extends Servlet> aClass) {
        return this.context.addServlet(s, aClass);
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> tClass) throws ServletException {
        return this.context.createServlet(tClass);
    }

    @Override
    public ServletRegistration getServletRegistration(String s) {
        return this.context.getServletRegistration(s);
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return this.context.getServletRegistrations();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String s, String s2) {
        return this.context.addFilter(s, s2);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String s, Filter filter) {
        return this.context.addFilter(s, filter);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String s, Class<? extends Filter> aClass) {
        return this.context.addFilter(s, aClass);
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> tClass) throws ServletException {
        return this.context.createFilter(tClass);
    }

    @Override
    public FilterRegistration getFilterRegistration(String s) {
        return this.context.getFilterRegistration(s);
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return this.context.getFilterRegistrations();
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return this.context.getSessionCookieConfig();
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
        this.context.setSessionTrackingModes(sessionTrackingModes);
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return this.context.getDefaultSessionTrackingModes();
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return this.context.getEffectiveSessionTrackingModes();
    }

    @Override
    public void addListener(String s) {
        this.context.addListener(s);
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        this.context.addListener(t);
    }

    @Override
    public void addListener(Class<? extends EventListener> aClass) {
        this.context.addListener(aClass);
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> tClass) throws ServletException {
        return this.context.createListener(tClass);
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return this.context.getJspConfigDescriptor();
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.context.getClassLoader();
    }

    @Override
    public void declareRoles(String... strings) {
        this.context.declareRoles(strings);
    }

    public String getRealPath(String name)
    {
        return null;
    }

    public String getServerInfo()
    {
        return this.context.getServerInfo();
    }

    public String getMimeType(String file)
    {
        String type = this.httpContext.getMimeType(file);
        if (type != null) {
            return type;
        }

        return MimeTypes.get().getByFile(file);
    }

    public boolean handleSecurity(HttpServletRequest req, HttpServletResponse res)
        throws IOException
    {
        return this.httpContext.handleSecurity(req, res);
    }
}
