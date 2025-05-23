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
package org.springframework.mock.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.WebUtils;

import javax.activation.FileTypeMap;
import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Mock implementation of the {@link javax.servlet.ServletContext} interface.
 *
 * <p>Compatible with Servlet 2.5 and partially with Servlet 3.0. Can be configured to
 * expose a specific version through {@link #setMajorVersion}/{@link #setMinorVersion};
 * default is 2.5. Note that Servlet 3.0 support is limited: servlet, filter and listener
 * registration methods are not supported; neither is cookie or JSP configuration.
 * We generally do not recommend to unit-test your ServletContainerInitializers and
 * WebApplicationInitializers which is where those registration methods would be used.
 *
 * <p>Used for testing the Spring web framework; only rarely necessary for testing
 * application controllers. As long as application components don't explicitly
 * access the {@code ServletContext}, {@code ClassPathXmlApplicationContext} or
 * {@code FileSystemXmlApplicationContext} can be used to load the context files
 * for testing, even for {@code DispatcherServlet} context definitions.
 *
 * <p>For setting up a full {@code WebApplicationContext} in a test environment,
 * you can use {@code AnnotationConfigWebApplicationContext},
 * {@code XmlWebApplicationContext}, or {@code GenericWebApplicationContext},
 * passing in an appropriate {@code MockServletContext} instance. You might want
 * to configure your {@code MockServletContext} with a {@code FileSystemResourceLoader}
 * in that case to ensure that resource paths are interpreted as relative filesystem
 * locations.
 *
 * <p>A common setup is to point your JVM working directory to the root of your
 * web application directory, in combination with filesystem-based resource loading.
 * This allows to load the context files as used in the web application, with
 * relative paths getting interpreted correctly. Such a setup will work with both
 * {@code FileSystemXmlApplicationContext} (which will load straight from the
 * filesystem) and {@code XmlWebApplicationContext} with an underlying
 * {@code MockServletContext} (as long as the {@code MockServletContext} has been
 * configured with a {@code FileSystemResourceLoader}).
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @see #MockServletContext(org.springframework.core.io.ResourceLoader)
 * @see org.springframework.web.context.support.AnnotationConfigWebApplicationContext
 * @see org.springframework.web.context.support.XmlWebApplicationContext
 * @see org.springframework.web.context.support.GenericWebApplicationContext
 * @see org.springframework.context.support.ClassPathXmlApplicationContext
 * @see org.springframework.context.support.FileSystemXmlApplicationContext
 * @since 1.0.2
 */
public class MockServletContext implements ServletContext {

    /**
     * Default Servlet name used by Tomcat, Jetty, JBoss, and GlassFish: {@value}.
     */
    private static final String COMMON_DEFAULT_SERVLET_NAME = "default";

    private static final String TEMP_DIR_SYSTEM_PROPERTY = "java.io.tmpdir";

    private final Log logger = LogFactory.getLog(getClass());

    private final Map<String, ServletContext> contexts = new HashMap<String, ServletContext>();

    private final Map<String, String> initParameters = new LinkedHashMap<String, String>();

    private final Map<String, Object> attributes = new LinkedHashMap<String, Object>();

    private final Set<String> declaredRoles = new HashSet<String>();

    private final Map<String, RequestDispatcher> namedRequestDispatchers = new HashMap<String, RequestDispatcher>();

    private final ResourceLoader resourceLoader;

    private final String resourceBasePath;

    private String contextPath = "";

    private int majorVersion = 2;

    private int minorVersion = 5;

    private int effectiveMajorVersion = 2;

    private int effectiveMinorVersion = 5;

    private String servletContextName = "MockServletContext";

    private String defaultServletName = COMMON_DEFAULT_SERVLET_NAME;


    /**
     * Create a new MockServletContext, using no base path and a
     * DefaultResourceLoader (i.e. the classpath root as WAR root).
     *
     * @see org.springframework.core.io.DefaultResourceLoader
     */
    public MockServletContext() {
        this("", null);
    }

    /**
     * Create a new MockServletContext, using a DefaultResourceLoader.
     *
     * @param resourceBasePath the root directory of the WAR (should not end with a slash)
     * @see org.springframework.core.io.DefaultResourceLoader
     */
    public MockServletContext(String resourceBasePath) {
        this(resourceBasePath, null);
    }

    /**
     * Create a new MockServletContext, using the specified ResourceLoader
     * and no base path.
     *
     * @param resourceLoader the ResourceLoader to use (or null for the default)
     */
    public MockServletContext(ResourceLoader resourceLoader) {
        this("", resourceLoader);
    }

    /**
     * Create a new MockServletContext using the supplied resource base path and
     * resource loader.
     * <p>Registers a {@link MockRequestDispatcher} for the Servlet named
     * {@linkplain #COMMON_DEFAULT_SERVLET_NAME "default"}.
     *
     * @param resourceBasePath the root directory of the WAR (should not end with a slash)
     * @param resourceLoader   the ResourceLoader to use (or null for the default)
     * @see #registerNamedDispatcher
     */
    @SuppressWarnings("javadoc")
    public MockServletContext(String resourceBasePath, ResourceLoader resourceLoader) {
        this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
        this.resourceBasePath = (resourceBasePath != null ? resourceBasePath : "");

        // Use JVM temp dir as ServletContext temp dir.
        String tempDir = System.getProperty(TEMP_DIR_SYSTEM_PROPERTY);
        if (tempDir != null) {
            this.attributes.put(WebUtils.TEMP_DIR_CONTEXT_ATTRIBUTE, new File(tempDir));
        }

        registerNamedDispatcher(this.defaultServletName, new MockRequestDispatcher(this.defaultServletName));
    }

    /**
     * Build a full resource location for the given path,
     * prepending the resource base path of this MockServletContext.
     *
     * @param path the path as specified
     * @return the full resource path
     */
    protected String getResourceLocation(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return this.resourceBasePath + path;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = (contextPath != null ? contextPath : "");
    }

    /* This is a Servlet API 2.5 method. */
    public String getContextPath() {
        return this.contextPath;
    }

    public void registerContext(String contextPath, ServletContext context) {
        this.contexts.put(contextPath, context);
    }

    public ServletContext getContext(String contextPath) {
        if (this.contextPath.equals(contextPath)) {
            return this;
        }
        return this.contexts.get(contextPath);
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    public int getMajorVersion() {
        return this.majorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    public int getMinorVersion() {
        return this.minorVersion;
    }

    public void setEffectiveMajorVersion(int effectiveMajorVersion) {
        this.effectiveMajorVersion = effectiveMajorVersion;
    }

    public int getEffectiveMajorVersion() {
        return this.effectiveMajorVersion;
    }

    public void setEffectiveMinorVersion(int effectiveMinorVersion) {
        this.effectiveMinorVersion = effectiveMinorVersion;
    }

    public int getEffectiveMinorVersion() {
        return this.effectiveMinorVersion;
    }

    /**
     * This method uses the Java Activation framework, which returns "application/octet-stream"
     * when the mime type is unknown (i.e. it never returns {@code null}). In order to maintain
     * the {@link ServletContext#getMimeType(String)} contract, this method returns {@code null}
     * if the mimeType is "application/octet-stream", as of Spring 3.2.2.
     */
    public String getMimeType(String filePath) {
        String mimeType = MimeTypeResolver.getMimeType(filePath);
        return ("application/octet-stream".equals(mimeType)) ? null : mimeType;
    }

    public Set<String> getResourcePaths(String path) {
        String actualPath = (path.endsWith("/") ? path : path + "/");
        Resource resource = this.resourceLoader.getResource(getResourceLocation(actualPath));
        try {
            File file = resource.getFile();
            String[] fileList = file.list();
            if (ObjectUtils.isEmpty(fileList)) {
                return null;
            }
            Set<String> resourcePaths = new LinkedHashSet<String>(fileList.length);
            for (String fileEntry : fileList) {
                String resultPath = actualPath + fileEntry;
                if (resource.createRelative(fileEntry).getFile().isDirectory()) {
                    resultPath += "/";
                }
                resourcePaths.add(resultPath);
            }
            return resourcePaths;
        } catch (IOException ex) {
            logger.warn("Couldn't get resource paths for " + resource, ex);
            return null;
        }
    }

    public URL getResource(String path) throws MalformedURLException {
        Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
        if (!resource.exists()) {
            return null;
        }
        try {
            return resource.getURL();
        } catch (MalformedURLException ex) {
            throw ex;
        } catch (IOException ex) {
            logger.warn("Couldn't get URL for " + resource, ex);
            return null;
        }
    }

    public InputStream getResourceAsStream(String path) {
        Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
        if (!resource.exists()) {
            return null;
        }
        try {
            return resource.getInputStream();
        } catch (IOException ex) {
            logger.warn("Couldn't open InputStream for " + resource, ex);
            return null;
        }
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("RequestDispatcher path at ServletContext level must start with '/'");
        }
        return new MockRequestDispatcher(path);
    }

    public RequestDispatcher getNamedDispatcher(String path) {
        return this.namedRequestDispatchers.get(path);
    }

    /**
     * Register a {@link RequestDispatcher} (typically a {@link MockRequestDispatcher})
     * that acts as a wrapper for the named Servlet.
     *
     * @param name              the name of the wrapped Servlet
     * @param requestDispatcher the dispatcher that wraps the named Servlet
     * @see #getNamedDispatcher
     * @see #unregisterNamedDispatcher
     */
    public void registerNamedDispatcher(String name, RequestDispatcher requestDispatcher) {
        Assert.notNull(name, "RequestDispatcher name must not be null");
        Assert.notNull(requestDispatcher, "RequestDispatcher must not be null");
        this.namedRequestDispatchers.put(name, requestDispatcher);
    }

    /**
     * Unregister the {@link RequestDispatcher} with the given name.
     *
     * @param name the name of the dispatcher to unregister
     * @see #getNamedDispatcher
     * @see #registerNamedDispatcher
     */
    public void unregisterNamedDispatcher(String name) {
        Assert.notNull(name, "RequestDispatcher name must not be null");
        this.namedRequestDispatchers.remove(name);
    }

    /**
     * Get the name of the <em>default</em> {@code Servlet}.
     * <p>Defaults to {@linkplain #COMMON_DEFAULT_SERVLET_NAME "default"}.
     *
     * @see #setDefaultServletName
     */
    @SuppressWarnings("javadoc")
    public String getDefaultServletName() {
        return this.defaultServletName;
    }

    /**
     * Set the name of the <em>default</em> {@code Servlet}.
     * <p>Also {@link #unregisterNamedDispatcher unregisters} the current default
     * {@link RequestDispatcher} and {@link #registerNamedDispatcher replaces}
     * it with a {@link MockRequestDispatcher} for the provided
     * {@code defaultServletName}.
     *
     * @param defaultServletName the name of the <em>default</em> {@code Servlet};
     *                           never {@code null} or empty
     * @see #getDefaultServletName
     */
    public void setDefaultServletName(String defaultServletName) {
        Assert.hasText(defaultServletName, "defaultServletName must not be null or empty");
        unregisterNamedDispatcher(this.defaultServletName);
        this.defaultServletName = defaultServletName;
        registerNamedDispatcher(this.defaultServletName, new MockRequestDispatcher(this.defaultServletName));
    }

    public Servlet getServlet(String name) {
        return null;
    }

    public Enumeration<Servlet> getServlets() {
        return Collections.enumeration(new HashSet<Servlet>());
    }

    public Enumeration<String> getServletNames() {
        return Collections.enumeration(new HashSet<String>());
    }

    public void log(String message) {
        logger.info(message);
    }

    public void log(Exception ex, String message) {
        logger.info(message, ex);
    }

    public void log(String message, Throwable ex) {
        logger.info(message, ex);
    }

    public String getRealPath(String path) {
        Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
        try {
            return resource.getFile().getAbsolutePath();
        } catch (IOException ex) {
            logger.warn("Couldn't determine real path of resource " + resource, ex);
            return null;
        }
    }

    public String getServerInfo() {
        return "MockServletContext";
    }

    public String getInitParameter(String name) {
        Assert.notNull(name, "Parameter name must not be null");
        return this.initParameters.get(name);
    }

    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(this.initParameters.keySet());
    }

    public boolean setInitParameter(String name, String value) {
        Assert.notNull(name, "Parameter name must not be null");
        if (this.initParameters.containsKey(name)) {
            return false;
        }
        this.initParameters.put(name, value);
        return true;
    }

    public void addInitParameter(String name, String value) {
        Assert.notNull(name, "Parameter name must not be null");
        this.initParameters.put(name, value);
    }

    public Object getAttribute(String name) {
        Assert.notNull(name, "Attribute name must not be null");
        return this.attributes.get(name);
    }

    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(new LinkedHashSet<String>(this.attributes.keySet()));
    }

    public void setAttribute(String name, Object value) {
        Assert.notNull(name, "Attribute name must not be null");
        if (value != null) {
            this.attributes.put(name, value);
        } else {
            this.attributes.remove(name);
        }
    }

    public void removeAttribute(String name) {
        Assert.notNull(name, "Attribute name must not be null");
        this.attributes.remove(name);
    }

    public void setServletContextName(String servletContextName) {
        this.servletContextName = servletContextName;
    }

    public String getServletContextName() {
        return this.servletContextName;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String s, String s1) {
        return null;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String s, Servlet servlet) {
        return null;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String s, Class<? extends Servlet> aClass) {
        return null;
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> aClass) throws ServletException {
        return null;
    }

    @Override
    public ServletRegistration getServletRegistration(String s) {
        return null;
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String s, String s1) {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String s, Filter filter) {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String s, Class<? extends Filter> aClass) {
        return null;
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> aClass) throws ServletException {
        return null;
    }

    @Override
    public FilterRegistration getFilterRegistration(String s) {
        return null;
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return null;
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> set) {

    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return null;
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return null;
    }

    @Override
    public void addListener(String s) {

    }

    @Override
    public <T extends EventListener> void addListener(T t) {

    }

    @Override
    public void addListener(Class<? extends EventListener> aClass) {

    }

    @Override
    public <T extends EventListener> T createListener(Class<T> aClass) throws ServletException {
        return null;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }

    public ClassLoader getClassLoader() {
        return ClassUtils.getDefaultClassLoader();
    }

    public void declareRoles(String... roleNames) {
        Assert.notNull(roleNames, "Role names array must not be null");
        for (String roleName : roleNames) {
            Assert.hasLength(roleName, "Role name must not be empty");
            this.declaredRoles.add(roleName);
        }
    }

    @Override
    public String getVirtualServerName() {
        return null;
    }

    public Set<String> getDeclaredRoles() {
        return Collections.unmodifiableSet(this.declaredRoles);
    }


    /**
     * Inner factory class used to introduce a Java Activation Framework
     * dependency when actually asked to resolve a MIME type.
     */
    private static class MimeTypeResolver {

        public static String getMimeType(String filePath) {
            return FileTypeMap.getDefaultFileTypeMap().getContentType(filePath);
        }
    }

}
