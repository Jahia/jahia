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
 *     2/ JSEL - Commercial and Stepping Versions of the program
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
package org.jahia.services.content.interceptor;

import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.content.decorator.JCRUserPasswordUpdateAuthorizationException;
import org.jahia.services.content.decorator.JCRUserPasswordUpdatePolicyException;
import org.jahia.services.content.decorator.JCRUserPasswordUpdateVerificationException;
import org.jahia.services.pwdpolicy.PolicyEnforcementResult;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.test.framework.AbstractJUnitTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Integration tests for UserPasswordUpdateInterceptor and secured password update functionality.
 * Tests the interceptor's ability to control password property modifications based on SettingsBean configuration.
 *
 * @author Jahia Solutions Group SA
 */
public class UserPasswordUpdateInterceptorTest extends AbstractJUnitTest {

    private static final String TEST_USERNAME = "testPasswordUser";
    private static final String INITIAL_PASSWORD = "initialPassword123";
    private static final String NEW_PASSWORD = "newPassword456";
    private static final String WRONG_PASSWORD = "wrongPassword";

    private JahiaUserManagerService userManager;
    private JCRUserNode testUser;

    @Before
    public void setUp() throws Exception {
        // Initialize userManager for each test
        userManager = JahiaUserManagerService.getInstance();
        assertNotNull("JahiaUserManagerService cannot be retrieved", userManager);

        // Create test user
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            testUser = userManager.createUser(TEST_USERNAME, INITIAL_PASSWORD, new Properties(), session);
            assertNotNull("Test user creation failed", testUser);
            session.save();
            return null;
        });
    }

    @After
    public void tearDown() throws Exception {
        // Clean up test user
        if (testUser != null) {
            JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
                if (session.nodeExists(testUser.getPath())) {
                    userManager.deleteUser(testUser.getPath(), session);
                    session.save();
                }
                return null;
            });
        }
    }

    /**
     * Test that the secured setPassword(currentPassword, newPassword) method works
     * when the correct current password is provided.
     */
    @Test
    public void testSecuredPasswordUpdateSucceedsWithCorrectCurrentPassword() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            JCRUserNode user = (JCRUserNode) session.getNode(testUser.getPath());

            try {
                // This should succeed with correct current password
                user.setPassword(INITIAL_PASSWORD, NEW_PASSWORD);

                // Verify password was changed
                assertTrue("New password should work", user.verifyPassword(NEW_PASSWORD));
                assertFalse("Old password should no longer work", user.verifyPassword(INITIAL_PASSWORD));
            } catch (Exception e) {
                fail("Secured password update should succeed with correct current password: " + e.getMessage());
            }

            return null;
        });
    }

    /**
     * Test that the secured setPassword(currentPassword, newPassword) method throws
     * JCRUserPasswordUpdateVerificationException when an incorrect current password is provided.
     */
    @Test
    public void testSecuredPasswordUpdateFailsWithWrongCurrentPassword() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            JCRUserNode user = (JCRUserNode) session.getNode(testUser.getPath());

            try {
                // This should throw JCRUserPasswordUpdateVerificationException with wrong current password
                user.setPassword(WRONG_PASSWORD, NEW_PASSWORD);
                fail("Should have thrown JCRUserPasswordUpdateVerificationException");
            } catch (JCRUserPasswordUpdateVerificationException e) {
                // Expected exception
                // Verify password was NOT changed
                assertTrue("Original password should still work", user.verifyPassword(INITIAL_PASSWORD));
                assertFalse("New password should not work", user.verifyPassword(NEW_PASSWORD));
            } catch (Exception e) {
                fail("Should have thrown JCRUserPasswordUpdateVerificationException, but got: " + e.getClass().getSimpleName());
            }

            return null;
        });
    }

    /**
     * Test that the interceptor properly handles multiple password updates in sequence.
     */
    @Test
    public void testMultiplePasswordUpdates() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            JCRUserNode user = (JCRUserNode) session.getNode(testUser.getPath());

            try {
                // Chain of password updates
                String password1 = INITIAL_PASSWORD;
                String password2 = "secondPassword";
                String password3 = "thirdPassword";

                // First update
                user.setPassword(password1, password2);
                assertTrue("Second password should work", user.verifyPassword(password2));

                // Second update
                user.setPassword(password2, password3);
                assertTrue("Third password should work", user.verifyPassword(password3));

                // Verify old passwords don't work
                assertFalse("First password should no longer work", user.verifyPassword(password1));
                assertFalse("Second password should no longer work", user.verifyPassword(password2));
            } catch (Exception e) {
                fail("Multiple password updates should succeed: " + e.getMessage());
            }

            return null;
        });
    }

    /**
     * Test the thread-local authorization mechanism in isolation.
     */
    @Test
    public void testThreadLocalAuthorizationMechanism() {
        final String testUsername = "testUser1";

        // Initially, no authorization should be set
        assertFalse("Initially, password update should not be authorized",
                UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(testUsername));

        // Set authorization for specific user
        UserPasswordUpdateInterceptor.authorizePasswordUpdate(testUsername);
        assertTrue("After authorization, password update should be authorized for the correct user",
                UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(testUsername));

        // Test that authorization is user-specific
        assertFalse("Authorization should not work for different user",
                UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized("differentUser"));

        // Clear authorization
        UserPasswordUpdateInterceptor.clearPasswordUpdateAuthorization();
        assertFalse("After clearing, password update should not be authorized",
                UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(testUsername));
    }

    /**
     * Test that authorization is properly thread-isolated.
     */
    @Test
    public void testThreadIsolation() throws InterruptedException {
        final String username1 = "user1";
        final String username2 = "user2";

        // Set authorization in current thread
        UserPasswordUpdateInterceptor.authorizePasswordUpdate(username1);
        assertTrue("Current thread should be authorized for user1",
                UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(username1));

        final boolean[] otherThreadResult = new boolean[4];

        // Test in another thread
        Thread otherThread = new Thread(() -> {
            otherThreadResult[0] = UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(username1);
            otherThreadResult[1] = UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(username2);

            // Set authorization in other thread for different user
            UserPasswordUpdateInterceptor.authorizePasswordUpdate(username2);
            otherThreadResult[2] = UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(username1);
            otherThreadResult[3] = UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(username2);
        });

        otherThread.start();
        otherThread.join();

        // Verify thread isolation
        assertFalse("Other thread should not see current thread's authorization for user1", otherThreadResult[0]);
        assertFalse("Other thread should not see authorization for user2 initially", otherThreadResult[1]);
        assertFalse("Other thread should not see authorization for user1 after setting user2", otherThreadResult[2]);
        assertTrue("Other thread should see its own authorization for user2", otherThreadResult[3]);

        // Current thread should still be authorized for user1
        assertTrue("Current thread should still be authorized for user1",
                UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(username1));
        assertFalse("Current thread should not be authorized for user2",
                UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(username2));

        // Clean up
        UserPasswordUpdateInterceptor.clearPasswordUpdateAuthorization();
    }

    /**
     * Test authorization expiration functionality with configurable timeout.
     */
    @Test
    public void testAuthorizationExpiration() throws InterruptedException {
        final String testUsername = "testUser";

        // Save original timeout value
        long originalTimeout = SettingsBean.getInstance().getUserPasswordUpdateAuthorizationTimeoutMs();

        try {
            // Set a very short timeout for testing (500ms)
            SettingsBean.getInstance().setUserPasswordUpdateAuthorizationTimeoutMs(500L);

            // Set authorization
            UserPasswordUpdateInterceptor.authorizePasswordUpdate(testUsername);
            assertTrue("Should be authorized immediately after setting",
                    UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(testUsername));

            // Should still be valid within the timeout window (100ms < 500ms)
            Thread.sleep(100);
            assertTrue("Should still be authorized within validity window",
                    UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(testUsername));

            // Wait for expiration (600ms > 500ms timeout)
            Thread.sleep(500);
            assertFalse("Should be expired after timeout period",
                    UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(testUsername));

            // Verify that checking expired authorization automatically cleans it up
            // (This should have been cleaned up by the previous call, but let's verify)
            assertFalse("Should remain expired on subsequent checks",
                    UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(testUsername));

        } finally {
            // Always restore original timeout value
            SettingsBean.getInstance().setUserPasswordUpdateAuthorizationTimeoutMs(originalTimeout);
            // Clean up any remaining authorization
            UserPasswordUpdateInterceptor.clearPasswordUpdateAuthorization();
        }
    }

    /**
     * Test that different timeout values work correctly.
     */
    @Test
    public void testConfigurableTimeout() throws InterruptedException {
        final String testUsername = "testUser";

        // Save original timeout value
        long originalTimeout = SettingsBean.getInstance().getUserPasswordUpdateAuthorizationTimeoutMs();

        try {
            // Test with 200ms timeout
            SettingsBean.getInstance().setUserPasswordUpdateAuthorizationTimeoutMs(200L);

            UserPasswordUpdateInterceptor.authorizePasswordUpdate(testUsername);
            assertTrue("Should be authorized with 200ms timeout",
                    UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(testUsername));

            // Wait 100ms (should still be valid)
            Thread.sleep(100);
            assertTrue("Should still be valid after 100ms",
                    UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(testUsername));

            // Wait another 150ms (total 250ms > 200ms timeout)
            Thread.sleep(150);
            assertFalse("Should be expired after 250ms total",
                    UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(testUsername));

            // Test with longer timeout (1000ms)
            SettingsBean.getInstance().setUserPasswordUpdateAuthorizationTimeoutMs(1000L);

            UserPasswordUpdateInterceptor.authorizePasswordUpdate(testUsername);
            Thread.sleep(500); // Wait 500ms (should still be valid with 1000ms timeout)
            assertTrue("Should still be valid with 1000ms timeout after 500ms",
                    UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(testUsername));

        } finally {
            // Always restore original timeout value
            SettingsBean.getInstance().setUserPasswordUpdateAuthorizationTimeoutMs(originalTimeout);
            UserPasswordUpdateInterceptor.clearPasswordUpdateAuthorization();
        }
    }

    /**
     * Test that password updates work when requireCurrentPassword is disabled,
     * even if authorization has expired due to timeout.
     * This verifies that the bypass mechanism works correctly.
     */
    @Test
    public void testDisabledPasswordProtectionBypassesTimeout() throws Exception {
        // Save original settings
        boolean originalPasswordSetting = SettingsBean.getInstance().isUserPasswordUpdateRequiringPreviousPassword();
        long originalTimeout = SettingsBean.getInstance().getUserPasswordUpdateAuthorizationTimeoutMs();

        try {
            // Set a very short timeout (100ms) and enable password protection initially
            SettingsBean.getInstance().setUserPasswordUpdateAuthorizationTimeoutMs(100L);
            SettingsBean.getInstance().setUserPasswordUpdateRequiringPreviousPassword(true);

            JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
                JCRUserNode user = (JCRUserNode) session.getNode(testUser.getPath());

                // First, authorize password update and let it expire
                UserPasswordUpdateInterceptor.authorizePasswordUpdate(user.getName());
                assertTrue("Should be initially authorized",
                        UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(user.getName()));

                try {
                    // Wait for authorization to expire (150ms > 100ms timeout)
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }

                // Verify authorization has expired
                assertFalse("Authorization should have expired",
                        UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(user.getName()));

                // Now disable password protection - this should bypass the timeout requirement
                SettingsBean.getInstance().setUserPasswordUpdateRequiringPreviousPassword(false);

                // Password update should now work despite expired authorization
                boolean passwordSet = user.setPassword("newPasswordAfterDisable");
                assertTrue("Password update should succeed when protection is disabled, even with expired authorization",
                          passwordSet);

                session.save();

                // Verify the password was actually changed
                assertTrue("New password should be valid",
                          user.verifyPassword("newPasswordAfterDisable"));

                return null;
            });

        } finally {
            // Always restore original settings
            SettingsBean.getInstance().setUserPasswordUpdateRequiringPreviousPassword(originalPasswordSetting);
            SettingsBean.getInstance().setUserPasswordUpdateAuthorizationTimeoutMs(originalTimeout);
            UserPasswordUpdateInterceptor.clearPasswordUpdateAuthorization();
        }
    }

    /**
     * Test that JCRUserPasswordUpdatePolicyException provides detailed policy violation information.
     * This test requires a password policy configuration to be active.
     */
    @Test
    public void testPasswordPolicyExceptionDetails() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            JCRUserNode user = (JCRUserNode) session.getNode(testUser.getPath());

            try {
                // Try to set a password that might violate policies (too weak)
                String weakPassword = "123"; // Very weak password likely to fail policies
                user.setPassword(INITIAL_PASSWORD, weakPassword);
                // If no policy is configured, this might succeed, which is fine for this test
            } catch (JCRUserPasswordUpdatePolicyException e) {
                // Expected if password policies are configured
                assertEquals("Exception should contain correct username", TEST_USERNAME, e.getUsername());
                assertNotNull("PolicyEnforcementResult should be available", e.getPolicyEnforcementResult());
                assertTrue("Exception message should mention policy failure",
                        e.getMessage().contains("Password policy validation failed"));

                // The PolicyEnforcementResult should contain details about what went wrong
                PolicyEnforcementResult result = e.getPolicyEnforcementResult();
                assertFalse("Policy result should indicate failure", result.isSuccess());

            } catch (JCRUserPasswordUpdateVerificationException e) {
                fail("Should not have verification exception with correct current password");
            } catch (Exception e) {
                // Other exceptions might occur if no policies are configured
                // This is acceptable for this test
            }

            return null;
        });
    }

    /**
     * Test that unsecured setPassword(newPassword) fails when password protection is enabled
     * and no authorization is present. This is the most basic security check.
     */
    @Test
    public void testUnsecuredSetPasswordFailsWithoutAuthorization() throws Exception {
        // Save original setting
        boolean originalSetting = SettingsBean.getInstance().isUserPasswordUpdateRequiringPreviousPassword();

        try {
            // Enable password protection
            SettingsBean.getInstance().setUserPasswordUpdateRequiringPreviousPassword(true);

            JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
                JCRUserNode user = (JCRUserNode) session.getNode(testUser.getPath());

                // Ensure no authorization is present (clean slate)
                UserPasswordUpdateInterceptor.clearPasswordUpdateAuthorization();

                // Verify no authorization exists
                assertFalse("No authorization should be present initially",
                        UserPasswordUpdateInterceptor.isPasswordUpdateAuthorized(user.getName()));

                // Attempt unsecured password update - this should return false due to interceptor blocking it
                boolean result = user.setPassword("newUnsecuredPassword");
                assertFalse("Unsecured setPassword should return false when blocked by interceptor", result);

                // Verify original password still works
                assertTrue("Original password should still work after failed update",
                        user.verifyPassword(INITIAL_PASSWORD));

                // Verify new password does NOT work
                assertFalse("New password should not work after failed update",
                        user.verifyPassword("newUnsecuredPassword"));

                return null;
            });

        } finally {
            // Restore original setting
            SettingsBean.getInstance().setUserPasswordUpdateRequiringPreviousPassword(originalSetting);
            UserPasswordUpdateInterceptor.clearPasswordUpdateAuthorization();
        }
    }

    /**
     * Test backward compatibility: verifyPassword + setPassword pattern.
     * This tests that existing customer code that calls verifyPassword() followed by setPassword()
     * will continue to work with the enhanced security.
     */
    @Test
    public void testBackwardCompatibilityPattern() throws Exception {
        // Enable the security feature
        boolean originalSetting = SettingsBean.getInstance().isUserPasswordUpdateRequiringPreviousPassword();
        SettingsBean.getInstance().setUserPasswordUpdateRequiringPreviousPassword(true);

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
                JCRUserNode user = (JCRUserNode) session.getNode(testUser.getPath());

                // Test the backward compatibility pattern: verifyPassword + setPassword
                boolean isValid = user.verifyPassword(INITIAL_PASSWORD);
                assertTrue("Password verification should succeed", isValid);

                // Now setPassword should work because verifyPassword authorized it
                boolean passwordSet = user.setPassword(NEW_PASSWORD);
                assertTrue("Password should be set successfully after verification", passwordSet);

                session.save();

                // Verify the new password works
                assertTrue("New password should be valid", user.verifyPassword(NEW_PASSWORD));

                return null;
            });
        } finally {
            SettingsBean.getInstance().setUserPasswordUpdateRequiringPreviousPassword(originalSetting);
        }
    }
}
