package org.jahia.bundles.core.services;

import org.apache.commons.lang3.StringUtils;
import org.jahia.api.Constants;
import org.jahia.api.settings.SettingsBean;
import org.jahia.osgi.FrameworkService;
import org.jahia.params.valves.AuthValveContext;
import org.jahia.params.valves.CookieAuthConfig;
import org.jahia.params.valves.CookieAuthValveImpl;
import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.jahia.security.spi.LicenseCheckUtil;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.observation.JahiaEventService;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.security.*;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Patterns;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component(service = AuthenticationService.class, immediate = true)
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private JahiaUserManagerService userManagerService;
    private SettingsBean settingsBean;

    @Override
    public JCRUserNode authenticate(AuthenticationRequest authenticationRequest, AuthenticationOptions authenticationOptions,
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws AccountNotFoundException, AccountLockedException, FailedLoginException,
            ConcurrentLoggedInUsersLimitExceededLoginException, InvalidSessionLoginException, IllegalArgumentException {
        validateAuthenticationRequest(authenticationRequest);
        validateAuthenticationOptions(authenticationOptions, httpServletRequest, httpServletResponse);

        JCRUserNode jcrUserNode = validate(authenticationRequest);

        // at this point, the user is valid
        performAuthentication(jcrUserNode, authenticationOptions, httpServletRequest, httpServletResponse);
        return jcrUserNode;

    }

    @Override
    public JCRUserNode authenticate(AuthenticationRequest authenticationRequest)
            throws AccountNotFoundException, AccountLockedException, FailedLoginException,
            ConcurrentLoggedInUsersLimitExceededLoginException, IllegalArgumentException {
        AuthenticationOptions authenticationOptions = AuthenticationOptions.Builder.withDefaults().stateless().build();
        try {
            return authenticate(authenticationRequest, authenticationOptions, null, null);
        } catch (InvalidSessionLoginException e) {
            // should not happen as we are in stateless mode
            throw new IllegalStateException("Unexpected InvalidSessionLoginException", e);
        }
    }

    @Override
    public JCRUserNode authenticate(JCRUserNode jcrUserNode, AuthenticationOptions authenticationOptions,
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws InvalidSessionLoginException, IllegalArgumentException, AccountNotFoundException {
        if (jcrUserNode == null) {
            throw new IllegalArgumentException("JCR user node cannot be null");
        }
        validateAuthenticationOptions(authenticationOptions, httpServletRequest, httpServletResponse);

        JCRUserNode refreshedJcrUserNode = userManagerService.lookupUserByPath(jcrUserNode.getPath());
        if (refreshedJcrUserNode == null) {
            throw new AccountNotFoundException("Not user found at " + jcrUserNode.getPath());
        }

        // at this point, the user is valid
        performAuthentication(refreshedJcrUserNode, authenticationOptions, httpServletRequest, httpServletResponse);
        return refreshedJcrUserNode;
    }

    @Override
    public JCRUserNode getUserNodeFromCredentials(AuthenticationRequest authenticationRequest)
            throws AccountNotFoundException, AccountLockedException, FailedLoginException,
            ConcurrentLoggedInUsersLimitExceededLoginException, IllegalArgumentException {
        validateAuthenticationRequest(authenticationRequest);

        return validate(authenticationRequest);
    }

    /**
     * Validate the authentication request.
     *
     * @param authenticationRequest the authentication request to validate
     */
    static void validateAuthenticationRequest(AuthenticationRequest authenticationRequest) {
        if (authenticationRequest == null) {
            throw new IllegalArgumentException("Authentication request cannot be null");
        }
        if (StringUtils.isEmpty(authenticationRequest.getUsername())) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (StringUtils.isEmpty(authenticationRequest.getPassword())) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
    }

    /**
     * Validate the authentication options.
     *
     * @param authenticationOptions the authentication options to validate
     * @param httpServletRequest    the HTTP servlet request
     * @param httpServletResponse   the HTTP servlet response
     */
    static void validateAuthenticationOptions(AuthenticationOptions authenticationOptions, HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        if (authenticationOptions == null) {
            throw new IllegalArgumentException("Authentication options cannot be null");
        }
        // Ensure the authentication options are compatible with the provided HTTP request/response
        if (authenticationOptions.isStateful() && httpServletRequest == null) {
            throw new IllegalArgumentException("HTTP request is required for stateful authentication");
        }
        if (authenticationOptions.shouldRememberMe() && (httpServletRequest == null || httpServletResponse == null)) {
            throw new IllegalArgumentException("HTTP request and response are required for the remember me feature");
        }
    }

    private void performAuthentication(JCRUserNode jcrUserNode, AuthenticationOptions authenticationOptions,
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws InvalidSessionLoginException {
        JahiaUser jahiaUser = jcrUserNode.getJahiaUser();
        if (authenticationOptions.isStateful()) {
            HttpSession session = getOrCreateSession(authenticationOptions, httpServletRequest, jcrUserNode);
            // Store the authenticated user in the session, next requests will use it (SessionAuthValveImpl)
            session.setAttribute(Constants.SESSION_USER, jahiaUser);
            updateLocalesInSession(authenticationOptions, httpServletRequest, jcrUserNode, session);
            if (httpServletResponse != null) {
                // only available if an HTTP servlet response is passed as param
                updateRememberMeFlagInSession(authenticationOptions, httpServletRequest, httpServletResponse, jcrUserNode);
            }
        }

        JCRSessionFactory.getInstance().setCurrentUser(jahiaUser);

        // trigger login event if requested
        if (authenticationOptions.isTriggerLoginEventEnabled()) {
            triggerLoginEvent(httpServletRequest, httpServletResponse, jahiaUser);
        }

    }

    /**
     * Processes the given authentication request and performs necessary checks such as verifying the username,
     * password, account status, and login user limits. It returns the corresponding JCRUserNode object if authentication is successful.
     *
     * @param authenticationRequest the authentication request containing credentials and related details
     * @return the authenticated {@link JCRUserNode} if the authentication is successful
     * @throws AccountNotFoundException                           if the user account is not found
     * @throws FailedLoginException                               if the password verification fails
     * @throws AccountLockedException                             if the user account is locked
     * @throws ConcurrentLoggedInUsersLimitExceededLoginException if the maximum number of concurrent logged-in users is exceeded
     */
    private JCRUserNode validate(AuthenticationRequest authenticationRequest)
            throws AccountNotFoundException, FailedLoginException, AccountLockedException,
            ConcurrentLoggedInUsersLimitExceededLoginException {
        JCRUserNode jcrUserNode = userManagerService.lookupUser(authenticationRequest.getUsername(), authenticationRequest.getSite(),
                authenticationRequest.isGlobalSearchIncluded());
        if (jcrUserNode == null) {
            logger.debug("Login failed. Unknown username {}", authenticationRequest.getUsername());
            throw new AccountNotFoundException("User " + authenticationRequest.getUsername() + " not found");
        }

        if (!jcrUserNode.verifyPassword(authenticationRequest.getPassword())) {
            logger.warn("Login failed: password verification failed for user {}", jcrUserNode.getName());
            throw new FailedLoginException("Authentication failed for user " + authenticationRequest.getUsername());
        }

        if (jcrUserNode.isAccountLocked()) {
            logger.warn("Login failed: account for user {} is locked.", jcrUserNode.getName());
            throw new AccountLockedException("Account is locked for user " + authenticationRequest.getUsername());
        }

        if (!jcrUserNode.isRoot() && LicenseCheckUtil.isLoggedInUsersLimitReached()) {
            logger.warn("The number of logged in users has reached the authorized limit.");
            throw new ConcurrentLoggedInUsersLimitExceededLoginException();
        }
        return jcrUserNode;
    }

    private HttpSession getOrCreateSession(AuthenticationOptions authenticationOptions, HttpServletRequest request, JCRUserNode jcrUserNode)
            throws InvalidSessionLoginException {
        // if there are any attributes to conserve between session, let's copy them into a map first
        HttpSession existingSession = request.getSession(false);
        Map<String, Object> preservedSessionAttributes;
        if (existingSession == null) {
            preservedSessionAttributes = Collections.emptyMap();
        } else {
            // reusing an existing session

            // check if the session is still valid
            if (authenticationOptions.isSessionValidityCheckEnabled()) {
                long invalidateSessionTime = jcrUserNode.getInvalidatedSessionTime();
                if (invalidateSessionTime > 0 && existingSession.getCreationTime() < invalidateSessionTime) {
                    throw new InvalidSessionLoginException();
                }
            }

            existingSession.invalidate();
            if (authenticationOptions.areSessionAttributesPreserved()) {
                preservedSessionAttributes = preserveSessionAttributes(request);
            } else {
                preservedSessionAttributes = Collections.emptyMap();
            }
        }
        HttpSession newSession = request.getSession(true);
        // restore preserved attributes into the new session
        for (Map.Entry<String, Object> preservedSessionAttribute : preservedSessionAttributes.entrySet()) {
            newSession.setAttribute(preservedSessionAttribute.getKey(), preservedSessionAttribute.getValue());
        }
        return newSession;
    }

    /**
     * Updates locale-related session attributes based on the user's preferred locale settings.<br/>
     * If {@link AuthenticationOptions#isUpdateUILocaleEnabled()} is {@code true}, the UI locale ({@link Constants#SESSION_UI_LOCALE} is
     * set
     * If {@link AuthenticationOptions#isUpdateCurrentLocaleEnabled()} is {@code true} and {@link SettingsBean#isConsiderPreferredLanguageAfterLogin()}
     * is enabled, the current locale ({@link Constants#SESSION_LOCALE}) is set.
     *
     * @param authenticationOptions the authentication options that control which locale updates are performed
     * @param httpServletRequest    the HTTP servlet request used to resolve the guest locale
     * @param jcrUserNode           the authenticated user node whose preferences are used
     * @param session               the HTTP session where locale attributes will be set
     */
    void updateLocalesInSession(AuthenticationOptions authenticationOptions, HttpServletRequest httpServletRequest, JCRUserNode jcrUserNode,
            HttpSession session) {
        Locale preferredUserLocale = UserPreferencesHelper.getPreferredLocale(jcrUserNode,
                LanguageCodeConverters.resolveLocaleForGuest(httpServletRequest));

        // set UI locale to the user's preferred locale after login
        if (authenticationOptions.isUpdateUILocaleEnabled()) {
            session.setAttribute(Constants.SESSION_UI_LOCALE, preferredUserLocale);
        }

        // do a switch to the user's preferred locale
        if (authenticationOptions.isUpdateCurrentLocaleEnabled() && settingsBean.isConsiderPreferredLanguageAfterLogin()) {
            session.setAttribute(Constants.SESSION_LOCALE, preferredUserLocale);
        }
    }

    private void updateRememberMeFlagInSession(AuthenticationOptions authenticationOptions, HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse, JCRUserNode jcrUserNode) {
        // handle "remember me" feature
        if (authenticationOptions.shouldRememberMe() && !settingsBean.isFullReadOnlyMode()) {
            // the user has indicated he wants to use cookie authentication
            CookieAuthConfig cookieAuthConfig = settingsBean.getCookieAuthConfig();
            createAndSendCookie(httpServletRequest, httpServletResponse, jcrUserNode, cookieAuthConfig);
        }
    }

    private void triggerLoginEvent(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, JahiaUser jahiaUser) {
        // keep the existing event to be backward compatible
        AuthValveContext authContext = new AuthValveContext(httpServletRequest, httpServletResponse, JCRSessionFactory.getInstance());
        // TODO deprecated event to be refactored, see https://github.com/Jahia/jahia-private/issues/4453
        LoginEngineAuthValveImpl.LoginEvent event = new LoginEngineAuthValveImpl.LoginEvent(this, jahiaUser, authContext);
        SpringContextSingleton.getInstance().publishEvent(event);
        ((JahiaEventService) SpringContextSingleton.getBean("jahiaEventService")).publishEvent(event);

        Map<String, Object> m = new HashMap<>();
        m.put("user", jahiaUser);
        m.put("authContext", authContext);
        m.put("source", this);
        FrameworkService.sendEvent("org/jahia/usersgroups/login/LOGIN", m, false);
    }

    private static void createAndSendCookie(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            JCRUserNode jcrUserNode, CookieAuthConfig cookieAuthConfig) {
        // now let's look for a free random cookie value key.
        String cookieUserKey = CookieAuthValveImpl.getAvailableCookieKey(cookieAuthConfig);
        // let's save the identifier for the user in the database
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.LIVE_WORKSPACE, null, session -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("Saving cookie auth for user: {}...", jcrUserNode.getPath());
                }
                JCRUserNode innerUserNode = (JCRUserNode) session.getNode(jcrUserNode.getPath());
                innerUserNode.setProperty(cookieAuthConfig.getUserPropertyName(), cookieUserKey);
                session.save();
                return null;
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        sendCookie(cookieUserKey, httpServletRequest, httpServletResponse, jcrUserNode, cookieAuthConfig);
    }

    private static void sendCookie(String cookieUserKey, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            JCRUserNode theUser, CookieAuthConfig cookieAuthConfig) {
        // now let's save the same identifier in the cookie.
        String realm = theUser.getRealm();
        if (realm != null && logger.isDebugEnabled()) {
            logger.debug("Found realm: {}", realm);
        }
        Cookie authCookie = new Cookie(cookieAuthConfig.getCookieName(), cookieUserKey + (realm != null ? (":" + realm) : ""));
        authCookie.setPath(StringUtils.isNotEmpty(httpServletRequest.getContextPath()) ? httpServletRequest.getContextPath() : "/");
        authCookie.setMaxAge(cookieAuthConfig.getMaxAgeInSeconds());
        authCookie.setHttpOnly(cookieAuthConfig.isHttpOnly());
        authCookie.setSecure(cookieAuthConfig.isSecure());
        httpServletResponse.addCookie(authCookie);
    }

    // TODO review this duplicated method (see Logout). To be refactored.
    private Map<String, Object> preserveSessionAttributes(HttpServletRequest httpServletRequest) {
        Map<String, Object> savedSessionAttributes = new HashMap<>();
        HttpSession existingSession = httpServletRequest.getSession(false);
        if (existingSession != null) {
            String[] sessionAttributes = getPreservedSessionAttributes();
            for (String attributeName : sessionAttributes) {
                Object attributeValue = existingSession.getAttribute(attributeName);
                if (attributeValue != null) {
                    savedSessionAttributes.put(attributeName, attributeValue);
                }
            }
        }
        return savedSessionAttributes;
    }

    private String[] getPreservedSessionAttributes() {
        String preservedSessionAttributesOnLogin = settingsBean.getString("preserveSessionAttributesOnLogin", "wemSessionId");
        if (StringUtils.isNotEmpty(preservedSessionAttributesOnLogin)) {
            return Patterns.TRIPLE_HASH.split(preservedSessionAttributesOnLogin);
        }
        return new String[0];
    }

    @Reference
    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    @Reference
    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }
}

