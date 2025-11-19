package org.jahia.bundles.core.services;

import org.apache.commons.lang3.StringUtils;
import org.jahia.api.Constants;
import org.jahia.api.settings.SettingsBean;
import org.jahia.osgi.FrameworkService;
import org.jahia.params.valves.AuthValveContext;
import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.jahia.security.spi.LicenseCheckUtil;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.observation.JahiaEventService;
import org.jahia.services.security.*;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Patterns;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

@Component(service = AuthenticationService.class, immediate = true)
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private JahiaUserManagerService userManagerService;
    private SettingsBean settingsBean;

    @Override
    public void authenticate(AuthenticationRequest authenticationRequest, AuthenticationOptions authenticationOptions,
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws AccountNotFoundException, AccountLockedException, FailedLoginException,
            ConcurrentLoggedInUsersLimitExceededLoginException, InvalidSessionLoginException, IllegalArgumentException {
        validateAuthenticationRequest(authenticationRequest);
        validateAuthenticationOptions(authenticationOptions, httpServletRequest, httpServletResponse);

        UserDetails userDetails = validate(authenticationRequest);

        // at this point, the user is valid
        performAuthentication(userDetails, authenticationOptions, httpServletRequest, httpServletResponse);
    }

    @Override
    public void authenticate(AuthenticationRequest authenticationRequest)
            throws AccountNotFoundException, AccountLockedException, FailedLoginException,
            ConcurrentLoggedInUsersLimitExceededLoginException, IllegalArgumentException {
        AuthenticationOptions authenticationOptions = AuthenticationOptions.Builder.withDefaults().stateless().build();
        try {
            authenticate(authenticationRequest, authenticationOptions, null, null);
        } catch (InvalidSessionLoginException e) {
            // should not happen as we are in stateless mode
            throw new IllegalStateException("Unexpected InvalidSessionLoginException", e);
        }
    }

    @Override
    public void authenticate(String userNodePath, AuthenticationOptions authenticationOptions, HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse)
            throws InvalidSessionLoginException, IllegalArgumentException, AccountNotFoundException {
        if (userNodePath == null) {
            throw new IllegalArgumentException("User node path cannot be null");
        }
        validateAuthenticationOptions(authenticationOptions, httpServletRequest, httpServletResponse);

        JCRUserNode refreshedJcrUserNode = userManagerService.lookupUserByPath(userNodePath);
        if (refreshedJcrUserNode == null) {
            throw new AccountNotFoundException("Not user found at " + userNodePath);
        }

        // at this point, the user is valid
        performAuthentication(new UserDetails(refreshedJcrUserNode), authenticationOptions, httpServletRequest, httpServletResponse);
    }

    @Override
    public JahiaUser getUserFromCredentials(AuthenticationRequest authenticationRequest)
            throws AccountNotFoundException, AccountLockedException, FailedLoginException,
            ConcurrentLoggedInUsersLimitExceededLoginException, IllegalArgumentException {
        validateAuthenticationRequest(authenticationRequest);

        return validate(authenticationRequest).getJahiaUser();
    }

    @Override
    public void validateUserNode(String userNodePath) throws AccountLockedException, ConcurrentLoggedInUsersLimitExceededLoginException {
        JCRUserNode jcrUserNode = userManagerService.lookupUserByPath(userNodePath);
        validateUserNode(jcrUserNode);
    }

    /**
     * Validate the authentication request.
     *
     * @param authenticationRequest the authentication request to validate
     */
    private static void validateAuthenticationRequest(AuthenticationRequest authenticationRequest) {
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
    private static void validateAuthenticationOptions(AuthenticationOptions authenticationOptions, HttpServletRequest httpServletRequest,
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

    private static void validateUserNode(JCRUserNode jcrUserNode)
            throws AccountLockedException, ConcurrentLoggedInUsersLimitExceededLoginException {
        String username = jcrUserNode.getName();
        if (jcrUserNode.isAccountLocked()) {
            logger.warn("Login failed: account for user {} is locked.", username);
            throw new AccountLockedException("Account is locked for user " + username);
        }

        if (!jcrUserNode.isRoot() && LicenseCheckUtil.isLoggedInUsersLimitReached()) {
            logger.warn("The number of logged in users has reached the authorized limit while trying to login user {}.", username);
            throw new ConcurrentLoggedInUsersLimitExceededLoginException();
        }
    }

    private void performAuthentication(UserDetails userDetails, AuthenticationOptions authenticationOptions,
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws InvalidSessionLoginException {

        if (authenticationOptions.isStateful()) {
            HttpSession session = getOrCreateSession(authenticationOptions, httpServletRequest, httpServletResponse, userDetails);
            // Store the authenticated user in the session, next requests will use it (SessionAuthValveImpl)
            logger.debug("Attaching user to session");
            session.setAttribute(Constants.SESSION_USER, userDetails.getJahiaUser());
            updateLocalesInSession(authenticationOptions, httpServletRequest, userDetails, session);
            if (httpServletResponse != null) {
                // only available if an HTTP servlet response is passed as param
                updateRememberMeFlagInSession(authenticationOptions, httpServletRequest, httpServletResponse, userDetails);
            }
        }

        JCRSessionFactory.getInstance().setCurrentUser(userDetails.getJahiaUser());

        // trigger login event if requested
        if (authenticationOptions.isTriggerLoginEventEnabled()) {
            triggerLoginEvent(httpServletRequest, httpServletResponse, userDetails.getJahiaUser());
        }

    }

    /**
     * Processes the given authentication request and performs necessary checks such as verifying the username,
     * password, account status, and login user limits. It returns a {@link UserDetails} object if authentication is successful, that
     * contains all the information needed to complete the authentication.
     *
     * @param authenticationRequest the authentication request containing credentials and related details
     * @return a {@link UserDetails} if the authentication request is valid, {@code null} otherwise
     * @throws AccountNotFoundException                           if the user account is not found
     * @throws FailedLoginException                               if the password verification fails
     * @throws AccountLockedException                             if the user account is locked
     * @throws ConcurrentLoggedInUsersLimitExceededLoginException if the maximum number of concurrent logged-in users is exceeded
     */
    private UserDetails validate(AuthenticationRequest authenticationRequest)
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

        validateUserNode(jcrUserNode);

        // Extract user details into a separate object to avoid JCR session issues.
        // The HTTP session may be invalidated (in getOrCreateSession()), which closes associated JCR sessions.
        // If the authenticated user is already in the session, accessing the JCRUserNode after invalidation
        // would throw "javax.jcr.RepositoryException: This session has been closed".
        return new UserDetails(jcrUserNode);
    }

    private HttpSession getOrCreateSession(AuthenticationOptions authenticationOptions, HttpServletRequest request,
            HttpServletResponse response, UserDetails userDetails) throws InvalidSessionLoginException {
        // if there are any attributes to conserve between session, let's copy them into a map first
        HttpSession existingSession = request.getSession(false);
        Map<String, Object> preservedSessionAttributes;
        if (existingSession == null) {
            preservedSessionAttributes = Collections.emptyMap();
        } else {
            // reusing an existing session

            // check if the session is still valid
            if (authenticationOptions.isSessionValidityCheckEnabled()) {
                long invalidateSessionTime = userDetails.getInvalidatedSessionTime();
                if (invalidateSessionTime > 0 && existingSession.getCreationTime() < invalidateSessionTime) {
                    if (response != null) {
                        CookieUtils.clearRememberMeCookieForUser(userDetails.getJahiaUser(), request, response);
                    }
                    throw new InvalidSessionLoginException();
                }
            }

            if (authenticationOptions.areSessionAttributesPreserved()) {
                preservedSessionAttributes = preserveSessionAttributes(existingSession);
            } else {
                preservedSessionAttributes = Collections.emptyMap();
            }
            existingSession.invalidate();
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
     * @param userDetails           the user details extracted from the JCR user node
     * @param session               the HTTP session where locale attributes will be set
     */
    void updateLocalesInSession(AuthenticationOptions authenticationOptions, HttpServletRequest httpServletRequest, UserDetails userDetails,
            HttpSession session) {
        Locale preferredUserLocale = Optional.ofNullable(userDetails.getPreferredLanguageProperty())
                .map(LanguageCodeConverters::languageCodeToLocale).orElse(LanguageCodeConverters.resolveLocaleForGuest(httpServletRequest));

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
            HttpServletResponse httpServletResponse, UserDetails userDetails) {
        // handle "remember me" feature
        if (!authenticationOptions.shouldRememberMe()) {
            logger.debug("Remember me feature is not supported");
            return;
        }
        if (settingsBean.isFullReadOnlyMode()) {
            logger.debug("Jahia is in full read-only mode, the session will not be modified for the remember me feature");
            return;
        }
        CookieUtils.createRememberMeCookieForUser(userDetails.getJahiaUser(), httpServletRequest, httpServletResponse);
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

    // TODO review this duplicated method (see Logout). To be refactored.
    private Map<String, Object> preserveSessionAttributes(HttpSession httpSession) {
        Map<String, Object> savedSessionAttributes = new HashMap<>();
        String[] sessionAttributes = getPreservedSessionAttributes();
        for (String attributeName : sessionAttributes) {
            Object attributeValue = httpSession.getAttribute(attributeName);
            if (attributeValue != null) {
                savedSessionAttributes.put(attributeName, attributeValue);
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

    /**
     * Holds essential user details extracted from a {@link JCRUserNode} for authentication purposes.
     * This class is used to preserve user information before session invalidation, as the JCR
     * session linked to the {@link JCRUserNode} may be closed during the authentication process.
     */
    private static class UserDetails {
        private final JahiaUser jahiaUser;
        private final long invalidatedSessionTime;
        private final String preferredLanguageProperty;

        public UserDetails(JCRUserNode jcrUserNode) {
            this.jahiaUser = jcrUserNode.getJahiaUser();
            this.invalidatedSessionTime = jcrUserNode.getInvalidatedSessionTime();
            this.preferredLanguageProperty = jcrUserNode.getPropertyAsString("preferredLanguage");

        }

        public JahiaUser getJahiaUser() {
            return jahiaUser;
        }

        public long getInvalidatedSessionTime() {
            return invalidatedSessionTime;
        }

        public String getPreferredLanguageProperty() {
            return preferredLanguageProperty;
        }
    }
}

