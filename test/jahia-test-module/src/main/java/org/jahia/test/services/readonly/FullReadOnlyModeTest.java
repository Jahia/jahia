/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.readonly;

import org.jahia.services.content.*;
import org.jahia.settings.readonlymode.ReadOnlyModeController;
import org.jahia.settings.readonlymode.ReadOnlyModeException;
import org.jahia.test.JahiaTestCase;
import org.junit.*;

import static org.junit.Assert.*;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;

import java.util.UUID;

/**
 * Created by Kevan
 *
 * Test class to test Full read only feature
 */
public class FullReadOnlyModeTest extends JahiaTestCase {

    private static final String SYSTEM_SITE_PATH = "/sites/systemsite";

    private static ReadOnlyModeController readOnlyModeController;
    private static boolean originSystemSiteWCAcompliance;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        readOnlyModeController = ReadOnlyModeController.getInstance();
        originSystemSiteWCAcompliance = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            @Override
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                return session.getNode(SYSTEM_SITE_PATH).getProperty("j:wcagCompliance").getBoolean();
            }
        });
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            @Override
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                session.getNode(SYSTEM_SITE_PATH).setProperty("j:wcagCompliance", originSystemSiteWCAcompliance);
                return null;
            }
        });

        // in case a unit test failed, it's possible that read only mode is still "ON", switch it off to avoid disturbing other unit tests suites
        switchReadOnlyOffIfNecessary();
    }

    @Before
    public void setUp() {
        switchReadOnlyOffIfNecessary();
    }

    private static void switchReadOnlyOffIfNecessary() {
        try {
            readOnlyModeController.switchReadOnlyMode(false);
        } catch (IllegalArgumentException e) {
            // the read only mode was off already
        }
    }

    /**
     * Test that it's not possible to switch ROM:OFF when a pending switch is still not finished
     * - test PENDING status
     * - test try to switch back during the not finished switch
     */
    @Test
    public void testPendingStatus() throws Exception {

        createUnclosedJCRSession();

        // session should still exist, because we did not log out
        // switch to ready only mode ON
        TestThread<?> switchThead = getReadOnlySwitchThread(true);
        switchThead.start();

        // sleep a little bit
        Thread.sleep(3000);

        // verify state is pending
        assertTrue(readOnlyModeController.getReadOnlyStatus() == ReadOnlyModeController.ReadOnlyModeStatus.PENDING_ON);

        TestThread<?> switchThead2 = getReadOnlySwitchThread(false);
        switchThead2.start();

        // verify state is still pending
        assertTrue(readOnlyModeController.getReadOnlyStatus() == ReadOnlyModeController.ReadOnlyModeStatus.PENDING_ON);

        // wait for switch to finish
        switchThead.join();

        // switch ON is finished, but thread2 should be released and switch OFF should be starting
        assertTrue(readOnlyModeController.getReadOnlyStatus() == ReadOnlyModeController.ReadOnlyModeStatus.PENDING_OFF);

        switchThead2.join();

        assertTrue(readOnlyModeController.getReadOnlyStatus() == ReadOnlyModeController.ReadOnlyModeStatus.OFF);
    }

    /**
     * Test that a running JCR session is force closed after the configured timeout
     */
    @Test
    public void testJCRSessionForceLogout() throws Exception {
        // get session uuid
        UUID sessionUuid = UUID.fromString(createUnclosedJCRSession());

        // assert the session is still active
        assertTrue(JCRSessionWrapper.getActiveSessionsObjects().containsKey(sessionUuid));

        readOnlyModeController.switchReadOnlyMode(true);

        // assert the session is not active anymore
        assertFalse(JCRSessionWrapper.getActiveSessionsObjects().containsKey(sessionUuid));
    }

    /**
     * Test that JCR session save are blocked even when the switch is pending because of unclosed sessions
     */
    @Test
    public void testJCRSessionSaveBlocked() throws Exception {

        String sessionUUID = createUnclosedJCRSession();

        // session should still exist, because we did not log out
        // switch to read only mode ON
        TestThread<?> switchThead = getReadOnlySwitchThread(true);
        switchThead.start();

        // sleep a little bit
        Thread.sleep(3000);

        // test that save is blocked even if some sessions are not closed
        try {
            saveSomething();
            fail("It should have failed");
        } catch (ReadOnlyModeException exception) {
            // nothing to do this is the expected exception
        }

        // test that save is not blocked for the opened session
        try {
            saveSomething(JCRSessionWrapper.getActiveSessionsObjects().get(UUID.fromString(sessionUUID)));
            assertTrue("save should have worked for the session opened before the switch", true);
        } catch (ReadOnlyModeException exception) {
            fail("save should have worked for the session opened before the switch");
        }

        // wait for the switch to end
        switchThead.join();

        // switch is now ON

        // test that save is blocked
        try {
            saveSomething();
            fail("It should have failed");
        } catch (ReadOnlyModeException exception) {
            // nothing to do this is the expected exception
        }
    }

    /**
     * Test that JCR node locks are blocked even when the switch is pending because of unclosed sessions
     */
    @Test
    public void testJCRLockBlocked() throws Exception {
        String sessionUUID = createUnclosedJCRSession();

        // session should still exist, because we did not log out
        // switch to ready only mode ON
        TestThread<?> switchThead = getReadOnlySwitchThread(true);
        switchThead.start();

        // sleep a little bit
        Thread.sleep(3000);

        // test that lock is blocked even if some sessions are not closed
        JCRTemplate.getInstance().doExecuteWithSystemSession((session) -> {
            try {
                testLock(session, true, true, true);
                testLock(session, true, false, true);
                testLock(session, false, true, true);
                testLock(session, false, false, true);
                testLockAndStoreToken(session, "unit-test", null, true);
                testLockAndStoreToken(session, "unit-test", "root", true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return null;
        });

        // test that the opened session can still lock nodes
        JCRSessionWrapper previousOpenSession = JCRSessionWrapper.getActiveSessionsObjects().get(UUID.fromString(sessionUUID));
        testLock(previousOpenSession, true, true, false);
        testLock(previousOpenSession, true, false, false);
        testLock(previousOpenSession, false, true, false);
        testLock(previousOpenSession, false, false, false);
        testLockAndStoreToken(previousOpenSession, "unit-test", null, false);
        testLockAndStoreToken(previousOpenSession, "unit-test", "root", false);

        // wait for the switch to finish
        switchThead.join();

        // test after switch is complete
        JCRTemplate.getInstance().doExecuteWithSystemSession((sessionWrapper) -> {
            try {
                testLock(sessionWrapper, true, true, true);
                testLock(sessionWrapper, true, false, true);
                testLock(sessionWrapper, false, true, true);
                testLock(sessionWrapper, false, false, true);
                testLockAndStoreToken(sessionWrapper, "unit-test", null, true);
                testLockAndStoreToken(sessionWrapper, "unit-test", "root", true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return null;
        });
    }

    /**
     * Test that JCR node unlocks are blocked even when the switch is pending because of unclosed sessions
     */
    @Test
    public void testJCRUnlockBlocked() throws Exception {
        // session scope locks do not make sense to be tested, because session will be automatically killed at the end of the switch

        testUnlock(true, false, false);
        testUnlock(false, false, false);
        testUnlockOnTokenAndUser("unit-test", null, false);
        testUnlockOnTokenAndUser("unit-test", "root", false);
    }

    /**
     * Test that JCR node unlocks are blocked even when the switch is pending because of unclosed sessions
     */
    @Test
    public void testJCRClearAllLocksBlocked() throws Exception {
        testUnlock(true, true, true);
        testUnlock(false, true, true);
        testUnlock(true, false, true);
        testUnlock(false, false, true);
        testUnlockOnTokenAndUser("unit-test", null, true);
        testUnlockOnTokenAndUser("unit-test", "root", true);
    }

    /**
     * Test that switch ON then switch OFF is working
     */
    @Test
    public void testSwitchOnAndOff() throws Exception {
        readOnlyModeController.switchReadOnlyMode(true);
        assertTrue(readOnlyModeController.getReadOnlyStatus() == ReadOnlyModeController.ReadOnlyModeStatus.ON);
        readOnlyModeController.switchReadOnlyMode(false);
        assertTrue(readOnlyModeController.getReadOnlyStatus() == ReadOnlyModeController.ReadOnlyModeStatus.OFF);
    }

    private void testUnlock(boolean isDeep, boolean isSessionScope, boolean useClearAllLocks) throws Exception {
        // do lock
        JCRTemplate.getInstance().doExecuteWithSystemSession((sessionWrapper) -> {
            sessionWrapper.getNode(SYSTEM_SITE_PATH).lock(isDeep, isSessionScope);
            return null;
        });

        // switch ON
        TestThread<?> switchThead = getReadOnlySwitchThread(true);
        switchThead.start();
        switchThead.join();

        // test that unlock is blocked
        JCRTemplate.getInstance().doExecuteWithSystemSession((sessionWrapper) -> {
            try {
                if (useClearAllLocks) {
                    sessionWrapper.getNode(SYSTEM_SITE_PATH).clearAllLocks();
                } else {
                    sessionWrapper.getNode(SYSTEM_SITE_PATH).unlock();
                }
                fail("It should fail here");
            } catch (ReadOnlyModeException e) {
                // nothing to do we expect this exception
            }

            return null;
        });

        // switch OFF
        switchThead = getReadOnlySwitchThread(false);
        switchThead.start();
        switchThead.join();

        // test that unlock is working
        JCRTemplate.getInstance().doExecuteWithSystemSession((sessionWrapper) -> {
            try {
                if (useClearAllLocks) {
                    sessionWrapper.getNode(SYSTEM_SITE_PATH).clearAllLocks();
                } else {
                    sessionWrapper.getNode(SYSTEM_SITE_PATH).unlock();
                }

            } catch (ReadOnlyModeException e) {
                fail("It should work");
            }

            return null;
        });
    }

    private void testUnlockOnTokenAndUser(String type, String userId, boolean useClearAllLocks) throws Exception {
        // do lock
        JCRTemplate.getInstance().doExecuteWithSystemSession((sessionWrapper) -> {
            JCRNodeWrapper systemSiteNode = sessionWrapper.getNode(SYSTEM_SITE_PATH);
            if (userId != null) {
                systemSiteNode.lockAndStoreToken(type, userId);
            } else {
                systemSiteNode.lockAndStoreToken(type);
            }
            return null;
        });

        // switch ON
        TestThread<?> switchThead = getReadOnlySwitchThread(true);
        switchThead.start();
        switchThead.join();

        // test that unlock is blocked
        JCRTemplate.getInstance().doExecuteWithSystemSession((sessionWrapper) -> {
            JCRNodeWrapper systemSiteNode = sessionWrapper.getNode(SYSTEM_SITE_PATH);
            try {
                if (useClearAllLocks) {
                    systemSiteNode.clearAllLocks();
                } else {
                    if (userId != null) {
                        systemSiteNode.unlock(type, userId);
                    } else {
                        systemSiteNode.unlock(type);
                    }
                }

                fail("It should fail here");
            } catch (ReadOnlyModeException e) {
                // nothing to do we expect this exception
            }

            return null;
        });

        // switch OFF
        switchThead = getReadOnlySwitchThread(false);
        switchThead.start();
        switchThead.join();

        // test that unlock is working
        JCRTemplate.getInstance().doExecuteWithSystemSession((sessionWrapper) -> {
            JCRNodeWrapper systemSiteNode = sessionWrapper.getNode(SYSTEM_SITE_PATH);
            try {
                if (useClearAllLocks) {
                    systemSiteNode.clearAllLocks();
                } else {
                    if (userId != null) {
                        systemSiteNode.unlock(type, userId);
                    } else {
                        systemSiteNode.unlock(type);
                    }
                }
            } catch (ReadOnlyModeException e) {
                fail("It should work");
            }

            return null;
        });
    }

    private void testLock(JCRSessionWrapper sessionWrapper, boolean isDeep, boolean sessionScoped, boolean shouldFail) throws Exception {
        JCRNodeWrapper systemSiteNode = sessionWrapper.getNode(SYSTEM_SITE_PATH);
        try {
            systemSiteNode.lock(isDeep, sessionScoped);
            if (shouldFail) {
                fail("It should fail");
            }
        } catch (ReadOnlyModeException exception) {
            if (!shouldFail) {
                fail("It should work");
            }
        } finally {
            try {
                systemSiteNode.unlock();
            } catch (LockException e) {
                // the node hasn't been locked
            }
        }
    }

    private void testLockAndStoreToken(JCRSessionWrapper sessionWrapper, String type, String userId, boolean shouldFail) throws Exception {
        JCRNodeWrapper systemSiteNode = sessionWrapper.getNode(SYSTEM_SITE_PATH);
        try {
            if (userId != null) {
                systemSiteNode.lockAndStoreToken(type, userId);
            } else {
                systemSiteNode.lockAndStoreToken(type);
            }
            if (shouldFail) {
                fail("It should fail");
            }
        } catch (ReadOnlyModeException exception) {
            if (!shouldFail) {
                fail("It should work");
            }
        } finally {
            try {
                if (userId != null) {
                    systemSiteNode.unlock(type, userId);
                } else {
                    systemSiteNode.unlock(type);
                }
            } catch (LockException e) {
                // the node hasn't been locked
            }
        }
    }

    private void saveSomething() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                saveSomething(session);
                return null;
            }
        });
    }

    private void saveSomething(JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper systemSiteNode = session.getNode(SYSTEM_SITE_PATH);
        systemSiteNode.setProperty("j:wcagCompliance", !systemSiteNode.getProperty("j:wcagCompliance").getBoolean());
        session.save();
    }

    private TestThread<?> getReadOnlySwitchThread(boolean readOnlyModeOn) {
        return new TestThread<Void>(() -> {
            readOnlyModeController.switchReadOnlyMode(readOnlyModeOn);
            return null;
        });
    }

    private TestThread<String> getJCRSessionCreationThread() {
        return new TestThread<String>(() -> {
            try {
                return JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null).getIdentifier();
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String createUnclosedJCRSession() throws Exception {
        TestThread<String> jcrSessionThread = getJCRSessionCreationThread();
        jcrSessionThread.start();
        jcrSessionThread.join();
        return jcrSessionThread.getResult();
    }

    private class TestThread<T> extends Thread {

        private T result;

        private TestThreadCallback<T> testThreadCallback;

        TestThread(TestThreadCallback<T> testThreadCallback) {
            this.testThreadCallback = testThreadCallback;
        }

        @Override
        public void run() {
            result = testThreadCallback.doExecute();
        }

        public T getResult() {
            return result;
        }
    }

    private interface TestThreadCallback<T> {
        T doExecute();
    }
}
