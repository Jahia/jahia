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
import java.util.UUID;

/**
 * Created by Kevan
 *
 * Test class to test Full read only feature
 */
public class FullReadOnlyModeTest extends JahiaTestCase {
    
    private static final String SYSTEM_SITE_PATH = "/sites/systemsite";

    private static ReadOnlyModeController readOnlyModeController;
    private static boolean originReadOnlyModeEnabled;
    private static boolean originSystemSiteWCAcompliance;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        readOnlyModeController = ReadOnlyModeController.getInstance();
        originReadOnlyModeEnabled = readOnlyModeController.isReadOnlyModeEnabled();
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
    }

    @Before
    public void setUp() {
        readOnlyModeController.switchReadOnlyMode(originReadOnlyModeEnabled);
    }

    @After
    public void tearDown() {
        readOnlyModeController.switchReadOnlyMode(originReadOnlyModeEnabled);
    }

    /**
     * Test that it's not possible to switch ROM:OFF when a pending switch is still not finished
     * - test PENDING status
     * - test try to switch back during the not finished switch
     *
     * @throws Exception
     */
    @Test
    public void testPendingStatus() throws Exception {

        createUnclosedJCRSession();

        // session should still exist, because we do not logged out
        // switch to ready only mode ON
        TestThread switchThead = getReadOnlySwitchThread(true);
        switchThead.start();

        // sleep a little bit
        Thread.sleep(3000);

        // insure state is pending
        assertTrue(readOnlyModeController.getReadOnlyStatus() == ReadOnlyModeController.ReadOnlyModeStatus.PENDING);

        // try to ask for a switch during PENDING state
        readOnlyModeController.switchReadOnlyMode(false);

        // insure state is still pending
        assertTrue(readOnlyModeController.getReadOnlyStatus() == ReadOnlyModeController.ReadOnlyModeStatus.PENDING);

        // wait for switch to finish
        switchThead.join();

        assertTrue(readOnlyModeController.isReadOnlyModeEnabled());
    }

    /**
     * Test that a running JCR session not closed is force close after the configured timeout
     *
     * @throws Exception
     */
    @Test
    public void testJCRSessionForceLogout() throws Exception {
        // get session uuid
        UUID sessionUuid = UUID.fromString(createUnclosedJCRSession());

        // assert the session is still active
        assertTrue(JCRSessionWrapper.getActiveSessionsObjects().containsKey(sessionUuid));

        readOnlyModeController.switchReadOnlyMode(true);

        // assert the session is not active anymore
        assertTrue(!JCRSessionWrapper.getActiveSessionsObjects().containsKey(sessionUuid));
        assertTrue(readOnlyModeController.isReadOnlyModeEnabled());
    }

    /**
     * Test that JCR session save are blocked even when the switch is pending because of unclosed sessions
     *
     * @throws Exception
     */
    @Test
    public void testJCRSessionSaveBlocked() throws Exception {

        String sessionUUID = createUnclosedJCRSession();

        // session should still exist, because we do not logged out
        // switch to ready only mode ON
        TestThread switchThead = getReadOnlySwitchThread(true);
        switchThead.start();

        // sleep a little bit
        Thread.sleep(3000);

        // test that save is blocked even if some sessions are not closed
        try {
            saveSomething();
            fail("It should have failed");
        } catch (ReadOnlyModeException exception) {
            // nothing to do this is the expected Exception
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
            // nothing to do this is the expected Exception
        }
    }

    /**
     * Test that JCR node locks are blocked even when the switch is pending because of unclosed sessions
     *
     * @throws Exception
     */
    @Test
    public void testJCRLockBlocked() throws Exception {
        String sessionUUID = createUnclosedJCRSession();

        // session should still exist, because we do not logged out
        // switch to ready only mode ON
        TestThread switchThead = getReadOnlySwitchThread(true);
        switchThead.start();

        // sleep a little bit
        Thread.sleep(3000);

        // test that lock is blocked even if some sessions are not closed
        JCRTemplate.getInstance().doExecuteWithSystemSession((session) -> {
            try {
                insureLock(session, true, true, true);
                insureLock(session, true, false, true);
                insureLock(session, false, true, true);
                insureLock(session, false, false, true);
                insureLockAndStoreToken(session, "unit-test", null, true);
                insureLockAndStoreToken(session, "unit-test", "root", true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return null;
        });

        // test that the opened session can still lock nodes
        JCRSessionWrapper previousOpenSession = JCRSessionWrapper.getActiveSessionsObjects().get(UUID.fromString(sessionUUID));
        insureLock(previousOpenSession, true, true, false);
        insureLock(previousOpenSession, true, false, false);
        insureLock(previousOpenSession, false, true, false);
        insureLock(previousOpenSession, false, false, false);
        insureLockAndStoreToken(previousOpenSession, "unit-test", null, false);
        insureLockAndStoreToken(previousOpenSession, "unit-test", "root", false);

        // wait for the switch to finish
        switchThead.join();

        // test after switch is complete
        // test that lock is blocked even if some sessions are not closed
        JCRTemplate.getInstance().doExecuteWithSystemSession((sessionWrapper) -> {
            try {
                insureLock(sessionWrapper, true, true, true);
                insureLock(sessionWrapper, true, false, true);
                insureLock(sessionWrapper, false, true, true);
                insureLock(sessionWrapper, false, false, true);
                insureLockAndStoreToken(sessionWrapper, "unit-test", null, true);
                insureLockAndStoreToken(sessionWrapper, "unit-test", "root", true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return null;
        });
    }

    /**
     * Test that JCR node unlocks are blocked even when the switch is pending because of unclosed sessions
     *
     * @throws Exception
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
     *
     * @throws Exception
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
     *
     * @throws Exception
     */
    @Test
    public void testSwitchOnAndSwitchBack() throws Exception {
        readOnlyModeController.switchReadOnlyMode(true);
        assertTrue(readOnlyModeController.isReadOnlyModeEnabled());
        readOnlyModeController.switchReadOnlyMode(false);
        assertTrue(!readOnlyModeController.isReadOnlyModeEnabled());
    }

    private void testUnlock(boolean isDeep, boolean isSessionScope, boolean useClearAllLocks) throws Exception {
        // do lock
        JCRTemplate.getInstance().doExecuteWithSystemSession((sessionWrapper) -> {
            sessionWrapper.getNode(SYSTEM_SITE_PATH).lock(isDeep, isSessionScope);
            return null;
        });

        // switch ON
        TestThread switchThead = getReadOnlySwitchThread(true);
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

        // test that unlock is blocked
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
        TestThread switchThead = getReadOnlySwitchThread(true);
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
            } catch (ReadOnlyModeException e) {
                fail("It should work");
            }

            return null;
        });
    }

    private void insureLock(JCRSessionWrapper sessionWrapper, boolean isDeep, boolean sessionScoped, boolean shouldFail) throws Exception {
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
            if(!shouldFail) {
                systemSiteNode.unlock();
            }
        }
    }

    private void insureLockAndStoreToken(JCRSessionWrapper sessionWrapper, String type, String userId, boolean shouldFail) throws Exception {
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
            if(!shouldFail) {
                if (userId != null) {
                    systemSiteNode.unlock(type, userId);
                } else {
                    systemSiteNode.unlock(type);
                }
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

    private TestThread getReadOnlySwitchThread(final boolean readOnlyModeOn){
        return new TestThread<Void>(() -> {
            readOnlyModeController.switchReadOnlyMode(readOnlyModeOn);
            return null;
        });
    }

    private TestThread<String> getJCRSessionCreationThread(){
        return new TestThread<String>(() -> {
            try {
                return JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null).getIdentifier();
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void saveSomething(JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper systemSiteNode = session.getNode(SYSTEM_SITE_PATH);
        systemSiteNode.setProperty("j:wcagCompliance", !systemSiteNode.getProperty("j:wcagCompliance").getBoolean());
        session.save();
    }

    private String createUnclosedJCRSession() throws Exception {
        // create a JCR session first not closed
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
