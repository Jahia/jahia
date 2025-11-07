package org.jahia.bundles.core.services;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.jahia.api.settings.SettingsBean;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.security.AuthenticationOptions;
import org.jahia.services.security.AuthenticationRequest;
import org.jahia.services.security.ConcurrentLoggedInUsersLimitExceededLoginException;
import org.jahia.services.security.InvalidSessionLoginException;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(JUnitParamsRunner.class)
public class AuthenticationServiceImplTest {

    // input parameters:
    private AuthenticationRequest authenticationRequest;
    private AuthenticationOptions authenticationOptions;
    private HttpServletRequest httpRequest;
    private HttpServletResponse httpResponse;
    private JCRUserNode userNodeRequest;

    private JCRUserNode userNodeRetrieved;
    private JahiaUserManagerService userManagerService;
    private SettingsBean settingsBean;

    private final AuthenticationServiceImpl authenticationService = new AuthenticationServiceImpl();

    @Before
    public void setUp() {
        // setup the mocks before each test
        authenticationRequest = mock(AuthenticationRequest.class);
        authenticationOptions = mock(AuthenticationOptions.class);
        httpRequest = mock(HttpServletRequest.class);
        httpResponse = mock(HttpServletResponse.class);
        userNodeRequest = mock(JCRUserNode.class);

        userNodeRetrieved = mock(JCRUserNode.class);
        userManagerService = mock(JahiaUserManagerService.class);
        authenticationService.setUserManagerService(userManagerService);
        settingsBean = mock(SettingsBean.class);
        authenticationService.setSettingsBean(settingsBean);
    }

    @Test
    @Parameters(method = "invalidCredentialRequests")
    public void GIVEN_an_invalid_credential_request_WHEN_authenticating_THEN_IllegalArgumentException(String username, String password,
            String expectedMessage) {
        // GIVEN:
        when(authenticationRequest.getUsername()).thenReturn(username);
        when(authenticationRequest.getPassword()).thenReturn(password);

        // WHEN:
        ThrowingRunnable runnable = () -> authenticationService.authenticate(authenticationRequest, authenticationOptions, httpRequest,
                httpResponse);

        // THEN:
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, runnable);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void GIVEN_a_null_authentication_request_WHEN_authenticating_THEN_IllegalArgumentException() {
        // GIVEN:
        AuthenticationRequest authReq = null;

        // WHEN:
        ThrowingRunnable runnable = () -> authenticationService.authenticate(authReq, authenticationOptions, httpRequest, httpResponse);

        // THEN:
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, runnable);
        assertEquals("Authentication request cannot be null", exception.getMessage());
    }

    @Test
    public void GIVEN_null_authentication_options_WHEN_authenticating_THEN_IllegalArgumentException() {
        // GIVEN:
        // valid authentication request:
        when(authenticationRequest.getUsername()).thenReturn("myUser");
        when(authenticationRequest.getPassword()).thenReturn("myPassword");
        // null authentication options:
        AuthenticationOptions authOptions = null;

        // WHEN:
        ThrowingRunnable runnable = () -> authenticationService.authenticate(authenticationRequest, authOptions, httpRequest, httpResponse);

        // THEN:
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, runnable);
        assertEquals("Authentication options cannot be null", exception.getMessage());
    }

    @Test
    public void GIVEN_authentication_options_stateful_without_a_request_WHEN_authenticating_THEN_IllegalArgumentException() {
        // GIVEN:
        // valid authentication request:
        when(authenticationRequest.getUsername()).thenReturn("myUser");
        when(authenticationRequest.getPassword()).thenReturn("myPassword");
        // stateful authentication options:
        when(authenticationOptions.isStateful()).thenReturn(true);

        // WHEN:
        ThrowingRunnable runnable = () -> authenticationService.authenticate(authenticationRequest, authenticationOptions, null,
                httpResponse);

        // THEN:
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, runnable);
        assertEquals("HTTP request is required for stateful authentication", exception.getMessage());
    }

    @Test
    public void GIVEN_authentication_options_with_remember_me_but_no_request_WHEN_authenticating_THEN_IllegalArgumentException() {
        // GIVEN:
        // valid authentication request:
        when(authenticationRequest.getUsername()).thenReturn("myUser");
        when(authenticationRequest.getPassword()).thenReturn("myPassword");
        // "remember me" enabled:
        when(authenticationOptions.shouldRememberMe()).thenReturn(true);

        // WHEN:
        ThrowingRunnable runnable = () -> authenticationService.authenticate(authenticationRequest, authenticationOptions, null,
                httpResponse);

        // THEN:
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, runnable);
        assertEquals("HTTP request and response are required for the remember me feature", exception.getMessage());
    }

    @Test
    public void GIVEN_authentication_options_with_remember_me_but_no_response_WHEN_authenticating_THEN_IllegalArgumentException() {
        // GIVEN:
        // valid authentication request:
        when(authenticationRequest.getUsername()).thenReturn("myUser");
        when(authenticationRequest.getPassword()).thenReturn("myPassword");
        // "remember me" enabled:
        when(authenticationOptions.shouldRememberMe()).thenReturn(true);

        // WHEN:
        ThrowingRunnable runnable = () -> authenticationService.authenticate(authenticationRequest, authenticationOptions, httpRequest,
                null);

        // THEN:
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, runnable);
        assertEquals("HTTP request and response are required for the remember me feature", exception.getMessage());
    }

    @Test
    public void GIVEN_an_unknown_username_WHEN_authenticating_THEN_AccountNotFoundException() {
        // GIVEN:
        // valid authentication request:
        when(authenticationRequest.getUsername()).thenReturn("unkownUsername");
        when(authenticationRequest.getPassword()).thenReturn("password");
        when(authenticationRequest.isGlobalSearchIncluded()).thenReturn(false);
        when(userManagerService.lookupUser("unkownUsername", null, false)).thenReturn(null);

        // WHEN:
        ThrowingRunnable runnable = () -> authenticationService.authenticate(authenticationRequest, authenticationOptions, httpRequest,
                httpResponse);

        // THEN:
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, runnable);
        assertEquals("User unkownUsername not found", exception.getMessage());
        verify(userManagerService).lookupUser("unkownUsername", null, false);
    }

    @Test
    public void GIVEN_invalid_credentials_WHEN_authenticating_THEN_AccountNotFoundException() {
        // GIVEN:
        // valid authentication request:
        when(authenticationRequest.getUsername()).thenReturn("myUser");
        when(authenticationRequest.getPassword()).thenReturn("wrongPassword");
        when(authenticationRequest.isGlobalSearchIncluded()).thenReturn(false);
        // existing account:
        when(userManagerService.lookupUser("myUser", null, false)).thenReturn(userNodeRetrieved);
        // wrong password:
        when(userNodeRetrieved.verifyPassword("wrongPassword")).thenReturn(false);

        // WHEN:
        ThrowingRunnable runnable = () -> authenticationService.authenticate(authenticationRequest, authenticationOptions, httpRequest,
                httpResponse);

        // THEN:
        FailedLoginException exception = assertThrows(FailedLoginException.class, runnable);
        verify(userManagerService).lookupUser("myUser", null, false);
        verify(userNodeRetrieved).verifyPassword("wrongPassword");
        assertEquals("Authentication failed for user myUser", exception.getMessage());
    }

    @Test
    public void GIVEN_a_locked_account_WHEN_authenticating_THEN_AccountLockedException() {
        // GIVEN:
        // valid authentication request:
        when(authenticationRequest.getUsername()).thenReturn("user");
        when(authenticationRequest.getPassword()).thenReturn("pwd");
        when(authenticationRequest.isGlobalSearchIncluded()).thenReturn(false);
        // existing account:
        when(userManagerService.lookupUser("user", null, false)).thenReturn(userNodeRetrieved);
        // valid password:
        when(userNodeRetrieved.verifyPassword("pwd")).thenReturn(true);
        // locked account:
        when(userNodeRetrieved.isAccountLocked()).thenReturn(true);

        // WHEN:
        ThrowingRunnable runnable = () -> authenticationService.authenticate(authenticationRequest, authenticationOptions, httpRequest,
                httpResponse);

        // THEN:
        AccountLockedException exception = assertThrows(AccountLockedException.class, runnable);
        verify(userManagerService).lookupUser("user", null, false);
        verify(userNodeRetrieved).verifyPassword("pwd");
        assertEquals("Account is locked for user user", exception.getMessage());
    }

    @Test
    @Parameters({
            // with a site and with global search included
            "user,pass,mySite,true",
            // with a site and without global search included
            "otherUser,otherPassword,otherSite,false",
            // without a site and with global search included
            "user,pass,,true",
            // without a site and without global search included
            "foo,bar,,false" })
    public void GIVEN_a_valid_credential_request_WHEN_authenticating_THEN_success(String username, String password, String site,
            boolean globalSearchIncluded)
            throws ConcurrentLoggedInUsersLimitExceededLoginException, AccountLockedException, FailedLoginException,
            InvalidSessionLoginException, AccountNotFoundException {
        // GIVEN:
        when(authenticationRequest.getUsername()).thenReturn(username);
        when(authenticationRequest.getPassword()).thenReturn(password);
        when(authenticationRequest.getSite()).thenReturn(site);
        when(authenticationRequest.isGlobalSearchIncluded()).thenReturn(globalSearchIncluded);
        when(userManagerService.lookupUser(username, site, globalSearchIncluded)).thenReturn(userNodeRetrieved);
        when(userNodeRetrieved.verifyPassword(password)).thenReturn(true);

        // WHEN:
        JCRUserNode result = authenticationService.authenticate(authenticationRequest, authenticationOptions, httpRequest, httpResponse);

        // THEN:
        assertSame(result, userNodeRetrieved);

        // verify the mocked methods have been called:
        verify(userManagerService).lookupUser(username, site, globalSearchIncluded);
        verify(userNodeRetrieved).verifyPassword(password);
    }

    @Test
    @Parameters(method = "invalidCredentialRequests")
    public void GIVEN_an_invalid_credential_request_WHEN_authenticating_with_minimal_params_THEN_IllegalArgumentException(String username,
            String password, String expectedMessage) {
        // GIVEN:
        when(authenticationRequest.getUsername()).thenReturn(username);
        when(authenticationRequest.getPassword()).thenReturn(password);

        // WHEN:
        ThrowingRunnable runnable = () -> authenticationService.authenticate(authenticationRequest);

        // THEN:
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, runnable);
        assertEquals(expectedMessage, exception.getMessage());
    }

    private Object[] invalidCredentialRequests() {
        return new Object[] { new Object[] { null, "pass", "Username cannot be empty" },
                new Object[] { "user", null, "Password cannot be empty" }, new Object[] { null, null, "Username cannot be empty" } };
    }

    @Test
    public void GIVEN_a_null_authentication_request_WHEN_authenticating_with_minimal_params_THEN_IllegalArgumentException() {
        // GIVEN:
        AuthenticationRequest authReq = null;

        // WHEN:
        ThrowingRunnable runnable = () -> authenticationService.authenticate(authReq);

        // THEN:
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, runnable);
        assertEquals("Authentication request cannot be null", exception.getMessage());
    }

    @Test
    public void GIVEN_a_null_JCR_user_node_WHEN_authenticating_THEN_IllegalArgumentException() {
        // GIVEN:
        JCRUserNode jcrUserNode = null;

        // WHEN:
        ThrowingRunnable runnable = () -> authenticationService.authenticate(jcrUserNode, authenticationOptions, httpRequest, httpResponse);

        // THEN:
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, runnable);
        assertEquals("JCR user node cannot be null", exception.getMessage());
    }

    @Test
    public void GIVEN_a_user_node_not_found_in_JCR_WHEN_authenticating_THEN_AccountNotFoundException() {
        // GIVEN:
        when(userNodeRequest.getPath()).thenReturn("/path/to/user/node");
        when(userManagerService.lookupUserByPath("/path/to/user/node")).thenReturn(null);

        // WHEN:
        ThrowingRunnable runnable = () -> authenticationService.authenticate(userNodeRequest, authenticationOptions, httpRequest,
                httpResponse);

        // THEN:
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, runnable);
        assertEquals("Not user found at /path/to/user/node", exception.getMessage());

        verify(userManagerService).lookupUserByPath("/path/to/user/node");
    }

    @Test
    public void GIVEN_a_valid_JCR_user_node_WHEN_authenticating_THEN_success()
            throws InvalidSessionLoginException, AccountNotFoundException {
        // GIVEN:
        when(userNodeRequest.getPath()).thenReturn("/path/to/user/node");
        when(userManagerService.lookupUserByPath("/path/to/user/node")).thenReturn(userNodeRetrieved);

        // WHEN:
        JCRUserNode result = authenticationService.authenticate(userNodeRequest, authenticationOptions, httpRequest, httpResponse);

        // THEN:
        assertSame(result, userNodeRetrieved);

        verify(userManagerService).lookupUserByPath("/path/to/user/node");
    }

}
