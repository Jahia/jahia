package org.jahia.utils;

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.render.RenderContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SessionIdHashingUtilsTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpSession mockSession;

    @Mock
    private HttpSessionEvent mockEvent;

    @Mock
    private JahiaContextLoaderListener.HttpSessionDestroyedEvent mockDestroyedEvent;

    @Mock
    private RenderContext mockRenderContext;

    private static final String TEST_SESSION_ID = "JSESSIONID123456789";
    private static final String EXPECTED_HASH = "a1c2696f1e0e51163b3710ad00be0d64c6b07d78441a8182dc4c6d5833e2c2de"; // DigestUtils.sha256Hex(TEST_SESSION_ID);

    @Test
    public void testHashSessionIdString() {
        // normal case
        String hash = SessionIdHashingUtils.hashSessionId(TEST_SESSION_ID);
        assertEquals(EXPECTED_HASH, hash);

        // null value
        assertNull(SessionIdHashingUtils.hashSessionId((String) null));
    }

    @Test
    public void testGetHashedSessionIdFromRequest() {
        // Test with existing session
        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockSession.getId()).thenReturn(TEST_SESSION_ID);
        String hash = SessionIdHashingUtils.getHashedSessionId(mockRequest);
        assertEquals(EXPECTED_HASH, hash);

        // Test with no session
        when(mockRequest.getSession(false)).thenReturn(null);
        assertNull(SessionIdHashingUtils.getHashedSessionId(mockRequest));

        // Test with null request
        assertNull(SessionIdHashingUtils.getHashedSessionId((HttpServletRequest) null));
    }

    @Test
    public void testGetHashedSessionIdFromRequestWithFallback() {
        // Test with existing session, fallback not used
        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockSession.getId()).thenReturn(TEST_SESSION_ID);
        String hash = SessionIdHashingUtils.getHashedSessionId(mockRequest, true);
        assertEquals(EXPECTED_HASH, hash);
        verify(mockRequest, never()).getRequestedSessionId();

        // Test with no session, fallback enabled
        when(mockRequest.getSession(false)).thenReturn(null);
        // to ensure no session is created - use lenient() since it won't be called
        lenient().when(mockRequest.getSession(true)).thenThrow(new RuntimeException("Should not be called"));
        when(mockRequest.getRequestedSessionId()).thenReturn(TEST_SESSION_ID);
        String hashWithFallback = SessionIdHashingUtils.getHashedSessionId(mockRequest, true);
        assertEquals(EXPECTED_HASH, hashWithFallback);

        // Test with no session, fallback disabled
        String hashNoFallback = SessionIdHashingUtils.getHashedSessionId(mockRequest, false);
        assertNull(hashNoFallback);

        // Test with null request
        assertNull(SessionIdHashingUtils.getHashedSessionId(null, true));
        assertNull(SessionIdHashingUtils.getHashedSessionId(null, false));
    }

    @Test
    public void testGetHashedSessionIdFromSession() {
        // Test with valid session
        when(mockSession.getId()).thenReturn(TEST_SESSION_ID);
        String hash = SessionIdHashingUtils.getHashedSessionId(mockSession);
        assertEquals(EXPECTED_HASH, hash);

        // Test with null session
        assertNull(SessionIdHashingUtils.getHashedSessionId((HttpSession) null));
    }

    @Test
    public void testGetHashedSessionIdFromEvent() {
        // Test with valid event and session
        when(mockEvent.getSession()).thenReturn(mockSession);
        when(mockSession.getId()).thenReturn(TEST_SESSION_ID);
        String hash = SessionIdHashingUtils.getHashedSessionId(mockEvent);
        assertEquals(EXPECTED_HASH, hash);

        // Test with null event
        assertNull(SessionIdHashingUtils.getHashedSessionId((HttpSessionEvent) null));

        // Test with event having null session
        when(mockEvent.getSession()).thenReturn(null);
        assertNull(SessionIdHashingUtils.getHashedSessionId(mockEvent));
    }

    @Test
    public void testGetHashedSessionIdFromDestroyedEvent() {
        // Test with valid destroyed event and session
        when(mockDestroyedEvent.getSession()).thenReturn(mockSession);
        when(mockSession.getId()).thenReturn(TEST_SESSION_ID);
        String hash = SessionIdHashingUtils.getHashedSessionId(mockDestroyedEvent);
        assertEquals(EXPECTED_HASH, hash);

        // Test with null destroyed event
        assertNull(SessionIdHashingUtils.getHashedSessionId((JahiaContextLoaderListener.HttpSessionDestroyedEvent) null));

        // Test with destroyed event having null session
        when(mockDestroyedEvent.getSession()).thenReturn(null);
        assertNull(SessionIdHashingUtils.getHashedSessionId(mockDestroyedEvent));
    }

    @Test
    public void testGetHashedSessionIdFromRenderContext() {
        // Test with valid render context and request
        when(mockRenderContext.getRequest()).thenReturn(mockRequest);
        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockSession.getId()).thenReturn(TEST_SESSION_ID);
        String hash = SessionIdHashingUtils.getHashedSessionId(mockRenderContext);
        assertEquals(EXPECTED_HASH, hash);

        // Test with null render context
        assertNull(SessionIdHashingUtils.getHashedSessionId((RenderContext) null));

        // Test with render context having null request
        when(mockRenderContext.getRequest()).thenReturn(null);
        assertNull(SessionIdHashingUtils.getHashedSessionId(mockRenderContext));
    }

    @Test
    public void testDeterministicHashing() {
        // Test that the same session ID always produces the same hash
        String hash1 = SessionIdHashingUtils.hashSessionId(TEST_SESSION_ID);
        String hash2 = SessionIdHashingUtils.hashSessionId(TEST_SESSION_ID);
        String hash3 = SessionIdHashingUtils.hashSessionId(TEST_SESSION_ID);
        assertEquals(hash1, hash2);
        assertEquals(hash2, hash3);
        assertEquals(hash1, hash3);
    }

    @Test
    public void testDifferentSessionIdsDifferentHashes() {
        // Test that different session IDs produce different hashes
        String hash1 = SessionIdHashingUtils.hashSessionId("SESSION1");
        String hash2 = SessionIdHashingUtils.hashSessionId("SESSION2");
        String hash3 = SessionIdHashingUtils.hashSessionId("SESSION3");
        assertNotEquals(hash1, hash2);
        assertNotEquals(hash2, hash3);
        assertNotEquals(hash1, hash3);
    }

    @Test
    public void testCaseSensitiveHashing() {
        // Test that session IDs are case sensitive
        String hashLower = SessionIdHashingUtils.hashSessionId("sessionid");
        String hashUpper = SessionIdHashingUtils.hashSessionId("SESSIONID");
        String hashMixed = SessionIdHashingUtils.hashSessionId("SessionId");
        assertNotEquals(hashLower, hashUpper);
        assertNotEquals(hashLower, hashMixed);
        assertNotEquals(hashUpper, hashMixed);
    }

    @Test
    public void testHashLength() {
        // SHA-256 should always produce 64-character hex strings
        String hash = SessionIdHashingUtils.hashSessionId(TEST_SESSION_ID);
        assertEquals(64, hash.length());

        String emptyHash = SessionIdHashingUtils.hashSessionId("");
        assertEquals(64, emptyHash.length());
    }

    @Test
    public void testHashFormat() {
        // SHA-256 hex should only contain lowercase hex characters
        String hash = SessionIdHashingUtils.hashSessionId(TEST_SESSION_ID);
        assertTrue(hash.matches("^[0-9a-f]{64}$"));
    }

    @Test
    public void testFallbackSupplierNotCalledWhenSessionExists() {
        // Setup session to exist
        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockSession.getId()).thenReturn(TEST_SESSION_ID);

        // Create a fallback supplier that would fail if called - use lenient() since it won't be called
        lenient().when(mockRequest.getRequestedSessionId()).thenThrow(new RuntimeException("Should not be called"));

        // This should not throw because fallback is not used when session exists
        String hash = SessionIdHashingUtils.getHashedSessionId(mockRequest, true);
        assertEquals(EXPECTED_HASH, hash);
    }
}
