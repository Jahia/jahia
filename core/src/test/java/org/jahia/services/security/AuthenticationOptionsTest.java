package org.jahia.services.security;

import org.junit.Assert;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertThrows;

public class AuthenticationOptionsTest {

    @Test
    public void testDefaults() {
        AuthenticationOptions options = AuthenticationOptions.Builder.withDefaults().build();
        assertIsStateful(options);
    }

    @Test
    public void testStatelessThenStateful() {
        AuthenticationOptions options = AuthenticationOptions.Builder.withDefaults().stateless().stateful().build();
        // the session-related options have been disabled with stateless() and are not re-enabled with stateful()
        assertFalse(options.isStateless());
        assertTrue(options.isStateful());
        assertFalse(options.shouldRememberMe());
        assertFalse(options.isUpdateCurrentLocaleEnabled());
        assertFalse(options.isUpdateUILocaleEnabled());
        assertFalse(options.areSessionAttributesPreserved());
        assertFalse(options.isSessionValidityCheckEnabled());
    }

    @Test
    public void testChangeAllFlags() {
        AuthenticationOptions options = AuthenticationOptions.Builder.withDefaults().sessionUsed(false).shouldRememberMe(false)
                .updateCurrentLocaleEnabled(false).updateUILocaleEnabled(false).sessionAttributesPreserved(false)
                .sessionValidityCheckEnabled(false).triggerLoginEventEnabled(false).build();
        assertFalse(options.isStateful());
        assertTrue(options.isStateless());
        assertFalse(options.shouldRememberMe());
        assertFalse(options.isTriggerLoginEventEnabled());
        assertFalse(options.isUpdateCurrentLocaleEnabled());
        assertFalse(options.isUpdateUILocaleEnabled());
        assertFalse(options.areSessionAttributesPreserved());
        assertFalse(options.isSessionValidityCheckEnabled());
    }

    @Test
    public void testStateless() {
        AuthenticationOptions options = AuthenticationOptions.Builder.withDefaults().stateless().build();
        assertIsStateless(options);
    }

    @Test
    public void testSessionUsedFalse() {
        AuthenticationOptions options = AuthenticationOptions.Builder.withDefaults().sessionUsed(false).build();
        assertIsStateless(options);
    }

    @Test
    public void testStatelessAndRememberMeSupportedThrows() {
        AuthenticationOptions.Builder builder = AuthenticationOptions.Builder.withDefaults().stateless().shouldRememberMe(true);
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        Assert.assertEquals("Cannot use both stateless and rememberMeEnabled", exception.getMessage());
    }

    @Test
    public void testStatelessAndUpdateCurrentLocaleThrows() {
        AuthenticationOptions.Builder builder = AuthenticationOptions.Builder.withDefaults().stateless().updateCurrentLocaleEnabled(true);
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        Assert.assertEquals("Cannot use both stateless and updateCurrentLocaleEnabled", exception.getMessage());
    }

    @Test
    public void testStatelessAndUpdateUILocaleThrows() {
        AuthenticationOptions.Builder builder = AuthenticationOptions.Builder.withDefaults().stateless().updateUILocaleEnabled(true);
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        Assert.assertEquals("Cannot use both stateless and updateUILocaleEnabled", exception.getMessage());
    }

    @Test
    public void testStatelessAndSessionAttributesPreservedThrows() {
        AuthenticationOptions.Builder builder = AuthenticationOptions.Builder.withDefaults().stateless().sessionAttributesPreserved(true);
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        Assert.assertEquals("Cannot use both stateless and sessionAttributesPreserved", exception.getMessage());
    }

    @Test
    public void testStatelessAndSessionValidityCheckEnabledThrows() {
        AuthenticationOptions.Builder builder = AuthenticationOptions.Builder.withDefaults().stateless().sessionValidityCheckEnabled(true);
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        Assert.assertEquals("Cannot use both stateless and sessionValidityCheckEnabled", exception.getMessage());
    }

    @Test
    public void testStatelessWithSessionOptionThrows() {
        AuthenticationOptions.Builder builder = AuthenticationOptions.Builder.withDefaults().stateless().shouldRememberMe(true);
        assertThrows(IllegalStateException.class, builder::build);
    }

    private static void assertIsStateful(AuthenticationOptions options) {
        assertTrue(options.isStateful());
        assertFalse(options.isStateless());
        assertTrue(options.shouldRememberMe());
        assertTrue(options.isTriggerLoginEventEnabled());
        assertTrue(options.isUpdateCurrentLocaleEnabled());
        assertTrue(options.isUpdateUILocaleEnabled());
        assertTrue(options.areSessionAttributesPreserved());
        assertTrue(options.isSessionValidityCheckEnabled());
    }

    private void assertIsStateless(AuthenticationOptions options) {
        assertTrue(options.isStateless());
        assertFalse(options.isStateful());
        assertFalse(options.shouldRememberMe());
        assertFalse(options.isUpdateCurrentLocaleEnabled());
        assertFalse(options.isUpdateUILocaleEnabled());
        assertFalse(options.areSessionAttributesPreserved());
        assertFalse(options.isSessionValidityCheckEnabled());
    }
}
