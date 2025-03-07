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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import javax.el.ELContext;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

import org.springframework.util.Assert;

/**
 * Mock implementation of the {@link javax.servlet.jsp.PageContext} interface.
 *
 * <p>Used for testing the web framework; only necessary for testing
 * applications when testing custom JSP tags.
 *
 * <p>Note: Expects initialization via the constructor rather than via the
 * {@code PageContext.initialize} method. Does not support writing to
 * a JspWriter, request dispatching, and {@code handlePageException} calls.
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 */
@SuppressWarnings("deprecation")
public class MockPageContext extends PageContext {

	private final ServletContext servletContext;

	private final HttpServletRequest request;

	private final HttpServletResponse response;

	private final ServletConfig servletConfig;

	private final Map<String, Object> attributes = new LinkedHashMap<String, Object>();

	private JspWriter out;


	/**
	 * Create new MockPageContext with a default {@link MockServletContext},
	 * {@link MockHttpServletRequest}, {@link MockHttpServletResponse},
	 * {@link MockServletConfig}.
	 */
	public MockPageContext() {
		this(null, null, null, null);
	}

	/**
	 * Create new MockPageContext with a default {@link MockHttpServletRequest},
	 * {@link MockHttpServletResponse}, {@link MockServletConfig}.
	 * @param servletContext the ServletContext that the JSP page runs in
	 * (only necessary when actually accessing the ServletContext)
	 */
	public MockPageContext(ServletContext servletContext) {
		this(servletContext, null, null, null);
	}

	/**
	 * Create new MockPageContext with a MockHttpServletResponse,
	 * MockServletConfig.
	 * @param servletContext the ServletContext that the JSP page runs in
	 * @param request the current HttpServletRequest
	 * (only necessary when actually accessing the request)
	 */
	public MockPageContext(ServletContext servletContext, HttpServletRequest request) {
		this(servletContext, request, null, null);
	}

	/**
	 * Create new MockPageContext with a MockServletConfig.
	 * @param servletContext the ServletContext that the JSP page runs in
	 * @param request the current HttpServletRequest
	 * @param response the current HttpServletResponse
	 * (only necessary when actually writing to the response)
	 */
	public MockPageContext(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response) {
		this(servletContext, request, response, null);
	}

	/**
	 * Create new MockServletConfig.
	 * @param servletContext the ServletContext that the JSP page runs in
	 * @param request the current HttpServletRequest
	 * @param response the current HttpServletResponse
	 * @param servletConfig the ServletConfig (hardly ever accessed from within a tag)
	 */
	public MockPageContext(ServletContext servletContext, HttpServletRequest request,
			HttpServletResponse response, ServletConfig servletConfig) {

		this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
		this.request = (request != null ? request : new MockHttpServletRequest(servletContext));
		this.response = (response != null ? response : new MockHttpServletResponse());
		this.servletConfig = (servletConfig != null ? servletConfig : new MockServletConfig(servletContext));
	}


	public void initialize(
			Servlet servlet, ServletRequest request, ServletResponse response,
			String errorPageURL, boolean needsSession, int bufferSize, boolean autoFlush) {

		throw new UnsupportedOperationException("Use appropriate constructor");
	}

	public void release() {
	}

	public void setAttribute(String name, Object value) {
		Assert.notNull(name, "Attribute name must not be null");
		if (value != null) {
			this.attributes.put(name, value);
		}
		else {
			this.attributes.remove(name);
		}
	}

	public void setAttribute(String name, Object value, int scope) {
		Assert.notNull(name, "Attribute name must not be null");
		switch (scope) {
			case PAGE_SCOPE:
				setAttribute(name, value);
				break;
			case REQUEST_SCOPE:
				this.request.setAttribute(name, value);
				break;
			case SESSION_SCOPE:
				this.request.getSession().setAttribute(name, value);
				break;
			case APPLICATION_SCOPE:
				this.servletContext.setAttribute(name, value);
				break;
			default:
				throw new IllegalArgumentException("Invalid scope: " + scope);
		}
	}

	public Object getAttribute(String name) {
		Assert.notNull(name, "Attribute name must not be null");
		return this.attributes.get(name);
	}

	public Object getAttribute(String name, int scope) {
		Assert.notNull(name, "Attribute name must not be null");
		switch (scope) {
			case PAGE_SCOPE:
				return getAttribute(name);
			case REQUEST_SCOPE:
				return this.request.getAttribute(name);
			case SESSION_SCOPE:
				HttpSession session = this.request.getSession(false);
				return (session != null ? session.getAttribute(name) : null);
			case APPLICATION_SCOPE:
				return this.servletContext.getAttribute(name);
			default:
				throw new IllegalArgumentException("Invalid scope: " + scope);
		}
	}

	public Object findAttribute(String name) {
		Object value = getAttribute(name);
		if (value == null) {
			value = getAttribute(name, REQUEST_SCOPE);
			if (value == null) {
				value = getAttribute(name, SESSION_SCOPE);
				if (value == null) {
					value = getAttribute(name, APPLICATION_SCOPE);
				}
			}
		}
		return value;
	}

	public void removeAttribute(String name) {
		Assert.notNull(name, "Attribute name must not be null");
		this.removeAttribute(name, PageContext.PAGE_SCOPE);
		this.removeAttribute(name, PageContext.REQUEST_SCOPE);
		this.removeAttribute(name, PageContext.SESSION_SCOPE);
		this.removeAttribute(name, PageContext.APPLICATION_SCOPE);
	}

	public void removeAttribute(String name, int scope) {
		Assert.notNull(name, "Attribute name must not be null");
		switch (scope) {
			case PAGE_SCOPE:
				this.attributes.remove(name);
				break;
			case REQUEST_SCOPE:
				this.request.removeAttribute(name);
				break;
			case SESSION_SCOPE:
				this.request.getSession().removeAttribute(name);
				break;
			case APPLICATION_SCOPE:
				this.servletContext.removeAttribute(name);
				break;
			default:
				throw new IllegalArgumentException("Invalid scope: " + scope);
		}
	}

	public int getAttributesScope(String name) {
		if (getAttribute(name) != null) {
			return PAGE_SCOPE;
		}
		else if (getAttribute(name, REQUEST_SCOPE) != null) {
			return REQUEST_SCOPE;
		}
		else if (getAttribute(name, SESSION_SCOPE) != null) {
			return SESSION_SCOPE;
		}
		else if (getAttribute(name, APPLICATION_SCOPE) != null) {
			return APPLICATION_SCOPE;
		}
		else {
			return 0;
		}
	}

	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(new LinkedHashSet<String>(this.attributes.keySet()));
	}

	@SuppressWarnings("unchecked")
	public Enumeration<String> getAttributeNamesInScope(int scope) {
		switch (scope) {
			case PAGE_SCOPE:
				return getAttributeNames();
			case REQUEST_SCOPE:
				return this.request.getAttributeNames();
			case SESSION_SCOPE:
				HttpSession session = this.request.getSession(false);
				return (session != null ? session.getAttributeNames() : null);
			case APPLICATION_SCOPE:
				return this.servletContext.getAttributeNames();
			default:
				throw new IllegalArgumentException("Invalid scope: " + scope);
		}
	}

	public JspWriter getOut() {
		if (this.out == null) {
			this.out = new MockJspWriter(this.response);
		}
		return this.out;
	}

	public ExpressionEvaluator getExpressionEvaluator() {
		return new MockExpressionEvaluator(this);
	}

	public ELContext getELContext() {
		return null;
	}

	public VariableResolver getVariableResolver() {
		return null;
	}

	public HttpSession getSession() {
		return this.request.getSession();
	}

	public Object getPage() {
		return this;
	}

	public ServletRequest getRequest() {
		return this.request;
	}

	public ServletResponse getResponse() {
		return this.response;
	}

	public Exception getException() {
		return null;
	}

	public ServletConfig getServletConfig() {
		return this.servletConfig;
	}

	public ServletContext getServletContext() {
		return this.servletContext;
	}

	public void forward(String path) throws ServletException, IOException {
		this.request.getRequestDispatcher(path).forward(this.request, this.response);
	}

	public void include(String path) throws ServletException, IOException {
		this.request.getRequestDispatcher(path).include(this.request, this.response);
	}

	public void include(String path, boolean flush) throws ServletException, IOException {
		this.request.getRequestDispatcher(path).include(this.request, this.response);
		if (flush) {
			this.response.flushBuffer();
		}
	}

	public byte[] getContentAsByteArray() {
		Assert.isTrue(this.response instanceof MockHttpServletResponse);
		return ((MockHttpServletResponse) this.response).getContentAsByteArray();
	}

	public String getContentAsString() throws UnsupportedEncodingException {
		Assert.isTrue(this.response instanceof MockHttpServletResponse);
		return ((MockHttpServletResponse) this.response).getContentAsString();
	}

	public void handlePageException(Exception ex) throws ServletException, IOException {
		throw new ServletException("Page exception", ex);
	}

	public void handlePageException(Throwable ex) throws ServletException, IOException {
		throw new ServletException("Page exception", ex);
	}

}
