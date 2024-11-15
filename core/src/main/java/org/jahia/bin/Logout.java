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
package org.jahia.bin;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.osgi.FrameworkService;
import org.jahia.params.valves.CookieAuthConfig;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.render.URLResolver;
import org.jahia.services.render.URLResolverFactory;
import org.jahia.services.seo.urlrewrite.UrlRewriteService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.tuckey.web.filters.urlrewrite.RewrittenUrl;

import javax.jcr.RepositoryException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

/**
 * Logout controller.
 * User: toto
 * Date: Nov 17, 2009
 * Time: 1:47:45 PM
 */
public class Logout implements Controller {
    private static final String DEFAULT_LOCALE = Locale.ENGLISH.toString();
    private static final transient Logger logger = LoggerFactory.getLogger(Logout.class);
    private JahiaUserManagerService userManagerService;

    public static String getLogoutServletPath() {
        // TODO move this into configuration
        return "/cms/logout";
    }

    protected CookieAuthConfig cookieAuthConfig;

    protected URLResolverFactory urlResolverFactory;

    protected UrlRewriteService urlRewriteService;

    private boolean fireLogoutEvent = false;

    private String preserveSessionAttributes = null;

    public void setFireLogoutEvent(boolean fireLogoutEvent) {
        this.fireLogoutEvent = fireLogoutEvent;
    }

    public class LogoutEvent extends ApplicationEvent {

        private static final long serialVersionUID = 7031797336948851970L;
        private HttpServletRequest request;
        private HttpServletResponse response;

        public LogoutEvent(Object source, HttpServletRequest request, HttpServletResponse response) {
            super(source);
            this.request = request;
            this.response = response;
        }

        public HttpServletRequest getRequest() {
            return request;
        }

        public HttpServletResponse getResponse() {
            return response;
        }
    }

    public void setPreserveSessionAttributes(String preserveSessionAttributes) {
        this.preserveSessionAttributes = preserveSessionAttributes;
    }

    private void addLocale(final JCRSiteNode site, final List<Locale> newLocaleList, final Locale curLocale) {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null,
                    Constants.LIVE_WORKSPACE, curLocale, new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            try {
                                if (site != null) {
                                    JCRSiteNode nodeByIdentifier = (JCRSiteNode) session.getNodeByIdentifier(site.getIdentifier());
                                    JCRNodeWrapper home = nodeByIdentifier.getHome();
                                    if (home != null && !newLocaleList.contains(curLocale)) {
                                        newLocaleList.add(curLocale);
                                    }
                                }
                            } catch (RepositoryException e) {
                                logger.debug("This site does not have a published home in language " + curLocale, e);
                            }
                            return null;
                        }
                    });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected void doRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String redirect = request.getParameter("redirect");
        if (redirect == null) {
            redirect = request.getHeader("referer");
            if (StringUtils.isNotEmpty(redirect) && Login.isAuthorizedRedirect(request, redirect, false)) {
                redirect = redirect.startsWith("http://") ? StringUtils.substringAfter(redirect, "http://") : StringUtils.substringAfter(redirect, "https://");
                redirect = redirect.contains("/") ? "/" + StringUtils.substringAfter(redirect, "/") : null;
            } else {
                redirect = null;
            }
        } else if (!Login.isAuthorizedRedirect(request, redirect, false)) {
            redirect = null;
        }
        if (StringUtils.isNotBlank(redirect)) {
            try {
                final String r = redirect;
                HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request) {
                    @Override
                    public String getRequestURI() {
                        return r;
                    }

                    @Override
                    public String getPathInfo() {
                        if (r.startsWith(getContextPath() + "/cms/")) {
                            return StringUtils.substringAfter(r,getContextPath() + "/cms");
                        }
                        return null;
                    }
                };

                if (urlRewriteService.prepareInbound(wrapper, response)) {
                    RewrittenUrl restored = urlRewriteService.rewriteInbound(wrapper, response);
                    if (restored != null) {
                        redirect = request.getContextPath() + restored.getTarget();
                    }
                }
            } catch (Exception e) {
                logger.error("Cannot rewrite redirection url",e);
            }

            String prefix = request.getContextPath() + "/cms/";
            if (redirect.startsWith(prefix)) {
                String url = "/" + StringUtils.substringAfter(redirect, prefix);
                String hash = StringUtils.substringAfterLast(url, "#");
                url = StringUtils.substringBefore(url, ";" + SettingsBean.getInstance().getJsessionIdParameterName());
                url = StringUtils.substringBefore(url, "?");
                url = StringUtils.substringBefore(url, "#");
                if (hash != null && hash.startsWith("/sites/") && url.contains("/sites/")) {
                    url = StringUtils.substringBefore(url, "/sites/") + StringUtils.substringBefore(hash, ":") + ".html";
                }

                List<String> urls = new ArrayList<String>();
                urls.add(url);
                if (url.startsWith("/edit/")) {
                    url = "/render/" + StringUtils.substringAfter(url,"/edit/");
                    urls.add(url);
                } else if (url.startsWith("/editframe/default/")) {
                    url = "/render/live/" + StringUtils.substringAfter(url,"/editframe/default/");
                    urls.add(url);
                } else if (url.startsWith("/contribute/")) {
                    url = "/render/" + StringUtils.substringAfter(url,"/contribute/");
                    urls.add(url);
                } else if (url.startsWith("/contributeframe/default/")) {
                    url = "/render/live/" + StringUtils.substringAfter(url,"/contributeframe/default/");
                    urls.add(url);
                }
                if (url.startsWith("/render/default/")) {
                    url = "/render/live/" + StringUtils.substringAfter(url,"/render/default/");
                    urls.add(url);
                }
                for (String currentUrl : urls) {
                    try {
                        URLResolver r = urlResolverFactory.createURLResolver(currentUrl, request.getServerName(), request);
                        if (r.getPath().startsWith("/sites/")) {
                            JCRNodeWrapper n = r.getNode();
                            // test that we do not get the site node, in that case, redirect to homepage
                            if (n.isNodeType("jnt:virtualsite")) {
                                n = ((JCRSiteNode) n).getHome();
                            }
                            if (n == null) {
                                // this can occur if the homepage of the site is not set
                                redirect = request.getContextPath() + "/";
                            } else {
                            redirect = prefix + r.getServletPart() + "/" + r.getWorkspace() + "/"
                                    + resolveLanguage(request, n.getResolveSite()) + n.getPath() + ".html";
                            }
                        } else {
                            redirect = request.getContextPath() + "/";
                        }
                        redirect = urlRewriteService.rewriteOutbound(redirect, request, response);
                        response.sendRedirect(response.encodeRedirectURL(redirect));
                        return;
                    } catch (Exception e) {
                        logger.debug("Cannot redirect to "+currentUrl, e);
                    }
                }
                response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/"));
                return;
            }
        }

        response.sendRedirect(response.encodeRedirectURL((StringUtils.isNotEmpty(redirect) && !StringUtils.equals(redirect, "/")) ? redirect : request.getContextPath() + "/"));
    }

    /**
     * Process the request and return a ModelAndView object which the DispatcherServlet
     * will render. A <code>null</code> return value is not an error: It indicates that
     * this object completed request processing itself, thus there is no ModelAndView
     * to render.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @return a ModelAndView to render, or <code>null</code> if handled directly
     * @throws Exception in case of errors
     */
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (cookieAuthConfig.isActivated()) {
            removeAuthCookie(request, response);
        }
        Locale uiLocale = (Locale) request.getSession().getAttribute(Constants.SESSION_UI_LOCALE);
        Locale locale = (Locale) request.getSession().getAttribute(Constants.SESSION_LOCALE);

        if (fireLogoutEvent) {
            SpringContextSingleton.getInstance().publishEvent(new LogoutEvent(this, request, response));
            Map<String, Object> m = new HashMap<>();
            m.put("request", request);
            m.put("response", response);
            FrameworkService.sendEvent("org/jahia/usersgroups/login/LOGOUT", m, false);
        }

        Map<String,Object> savedSessionAttributes = preserveSessionAttributes(request);

        request.getSession().invalidate();

        restoreSessionAttributes(request, savedSessionAttributes);

        JCRSessionFactory.getInstance().closeAllSessions();
        JCRSessionFactory.getInstance()
                .setCurrentUser(JahiaUserManagerService.getInstance().lookupUserByPath(JahiaUserManagerService.GUEST_USERPATH).getJahiaUser());

        request.getSession().setAttribute(Constants.SESSION_UI_LOCALE, uiLocale);
        request.getSession().setAttribute(Constants.SESSION_LOCALE, locale);

        String redirectActiveStr = request.getParameter("redirectActive");
        if (redirectActiveStr == null || Boolean.parseBoolean(redirectActiveStr)) {
            doRedirect(request, response);
        }

        return null;
    }

    protected void removeAuthCookie(HttpServletRequest request, HttpServletResponse response) {
        // now let's destroy the cookie authentication if there was one
        // set for this user.
        JahiaUser curUser = JCRSessionFactory.getInstance().getCurrentUser();
        JCRPropertyWrapper cookieAuthKey = null;
        try {
            if (!JahiaUserManagerService.isGuest(curUser)) {
                JCRUserNode userNode = userManagerService.lookupUserByPath(curUser.getLocalPath());
                String userPropertyName = cookieAuthConfig.getUserPropertyName();
                if (userNode != null && userNode.hasProperty(userPropertyName)) {
                    cookieAuthKey = userNode.getProperty(userPropertyName);
                }
            }
            if (cookieAuthKey != null) {
                Cookie authCookie = new Cookie(cookieAuthConfig.getCookieName(), cookieAuthKey.getString());
                authCookie.setPath(StringUtils.isNotEmpty(request.getContextPath()) ? request.getContextPath() : "/");
                authCookie.setMaxAge(0); // means we want it deleted now !
                authCookie.setHttpOnly(cookieAuthConfig.isHttpOnly());
                authCookie.setSecure(cookieAuthConfig.isSecure());
                response.addCookie(authCookie);
                if (!SettingsBean.getInstance().isFullReadOnlyMode()) {
                    cookieAuthKey.remove();
                    cookieAuthKey.getSession().save();
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected String resolveLanguage(HttpServletRequest request, final JCRSiteNode site)
            throws JahiaException {
        final List<Locale> newLocaleList = new ArrayList<Locale>();
        List<Locale> siteLanguages = Collections.emptyList();
        try {
            if (site != null) {
                siteLanguages = site.getLanguagesAsLocales();
            }
        } catch (Exception t) {
            logger.debug("Exception while getting language settings as locales", t);
        }

        // retrieve the browser locales
        for (@SuppressWarnings("unchecked") Iterator<Locale> browserLocales = new EnumerationIterator(request.getLocales()); browserLocales
                .hasNext();) {
            final Locale curLocale = browserLocales.next();
            if (siteLanguages.contains(curLocale)) {
                addLocale(site, newLocaleList, curLocale);
            } else if (!StringUtils.isEmpty(curLocale.getCountry())) {
                final Locale langOnlyLocale = new Locale(curLocale.getLanguage());
                if (siteLanguages.contains(langOnlyLocale)) {
                    addLocale(site, newLocaleList, langOnlyLocale);
                }
            }
        }

        String language = DEFAULT_LOCALE;
        if (!newLocaleList.isEmpty()) {
            language = newLocaleList.get(0).toString();
        } else if (site!=null){
            language = site.getDefaultLanguage();
        } else if (!StringUtils.isEmpty(SettingsBean.getInstance().getDefaultLanguageCode())) {
            language = SettingsBean.getInstance().getDefaultLanguageCode();
        }
        return language;
    }

    public void setCookieAuthConfig(CookieAuthConfig cookieAuthConfig) {
        this.cookieAuthConfig = cookieAuthConfig;
    }

    public void setUrlResolverFactory(URLResolverFactory urlResolverFactory) {
        this.urlResolverFactory = urlResolverFactory;
    }

    public void setUrlRewriteService(UrlRewriteService urlRewriteService) {
        this.urlRewriteService = urlRewriteService;
    }

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    private Map<String, Object> preserveSessionAttributes(HttpServletRequest httpServletRequest) {
        Map<String,Object> savedSessionAttributes = new HashMap<String,Object>();
        if ((preserveSessionAttributes != null) &&
            (httpServletRequest.getSession(false) != null) &&
                (preserveSessionAttributes.length() > 0)) {
            String[] sessionAttributeNames = Patterns.TRIPLE_HASH.split(preserveSessionAttributes);
            HttpSession session = httpServletRequest.getSession(false);
            for (String sessionAttributeName : sessionAttributeNames) {
                Object attributeValue = session.getAttribute(sessionAttributeName);
                if (attributeValue != null) {
                    savedSessionAttributes.put(sessionAttributeName, attributeValue);
                }
            }
        }
        return savedSessionAttributes;
    }

    private void restoreSessionAttributes(HttpServletRequest httpServletRequest, Map<String, Object> savedSessionAttributes) {
        if (savedSessionAttributes.size() > 0) {
            HttpSession session = httpServletRequest.getSession();
            for (Map.Entry<String,Object> savedSessionAttribute : savedSessionAttributes.entrySet()) {
                session.setAttribute(savedSessionAttribute.getKey(), savedSessionAttribute.getValue());
            }
        }
    }

}
