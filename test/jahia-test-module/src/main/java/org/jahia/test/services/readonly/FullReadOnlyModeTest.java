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

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.settings.readonlymode.ReadOnlyModeController;
import org.jahia.settings.readonlymode.ReadOnlyModeException;
import org.jahia.test.JahiaTestCase;
import org.junit.*;
import org.quartz.Scheduler;

import static org.junit.Assert.*;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Kevan
 *
 * Test class to test Full read only feature
 */
public class FullReadOnlyModeTest extends JahiaTestCase {

    private static final String SYSTEM_SITE_PATH = "/sites/systemsite";
    private static final String TEST_FOLDER_PATH = SYSTEM_SITE_PATH + "/files/testFolder";
    private static final String TEST_FOLDER_PATH2 = SYSTEM_SITE_PATH + "/files/testFolder2";

    private static ReadOnlyModeController readOnlyModeController;
    private static SchedulerService schedulerService;


    private static FullReadOnlyModeTestService fullReadOnlyModeTestService;
    private static boolean originSystemSiteWCAcompliance;

    private static List<String> openedTestSessions = new ArrayList<>();

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        fullReadOnlyModeTestService = (FullReadOnlyModeTestService) SpringContextSingleton.getBean("fullReadOnlyModeTestService");
        readOnlyModeController = ReadOnlyModeController.getInstance();
        schedulerService = ServicesRegistry.getInstance().getSchedulerService();
        originSystemSiteWCAcompliance = JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            JCRNodeWrapper systemSiteNode = session.getNode(SYSTEM_SITE_PATH);
            systemSiteNode.getNode("files").addNode("testFolder", "jnt:folder");
            systemSiteNode.getNode("files").addNode("testFolder2", "jnt:folder");
            session.save();
            return systemSiteNode.getProperty("j:wcagCompliance").getBoolean();
        });
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession((JCRCallback<Boolean>) session -> {
            session.getNode(TEST_FOLDER_PATH).remove();
            session.getNode(TEST_FOLDER_PATH2).remove();
            session.getNode(SYSTEM_SITE_PATH).setProperty("j:wcagCompliance", originSystemSiteWCAcompliance);
            session.save();
            return null;
        });
    }

    @After
    public void tearDown() {
        // reset test service
        fullReadOnlyModeTestService.setTestCallback(null);

        // switch off read only
        try {
            readOnlyModeController.switchReadOnlyMode(false);
        } catch (IllegalArgumentException e) {
            // the read only mode was off already
        }

        // close test sessions potentially opened for testing purpose
        for (String openedTestSession : openedTestSessions) {
            JCRSessionWrapper openedSession = JCRSessionWrapper.getActiveSessionsObjects().get(UUID.fromString(openedTestSession));
            if (openedSession != null) {
                openedSession.logout();
            }
        }
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

    /**
     * Test the pending status of the read only mode
     * @throws Exception
     */
    @Test
    public void testPendingStatus() throws Exception {

        // set waiting test service to be able to test inner status between switch
        setWaitingTestService(30 * 1000);

        // session should still exist, because we did not log out
        // switch to ready only mode ON
        TestThread<?> switchThead = getReadOnlySwitchThread(true);
        switchThead.start();

        Thread.sleep(3000);

        // verify state is pending
        assertTrue("status: " + readOnlyModeController.getReadOnlyStatus(), readOnlyModeController.getReadOnlyStatus() == ReadOnlyModeController.ReadOnlyModeStatus.PENDING_ON);

        TestThread<?> switchThead2 = getReadOnlySwitchThread(false);
        switchThead2.start();

        // verify state is still pending
        assertTrue("status: " + readOnlyModeController.getReadOnlyStatus(),readOnlyModeController.getReadOnlyStatus() == ReadOnlyModeController.ReadOnlyModeStatus.PENDING_ON);

        // wait for switch to finish
        switchThead.join();

        Thread.sleep(3000);

        // switch ON is finished, but thread2 should be released and switch OFF should be starting
        assertTrue("status: " + readOnlyModeController.getReadOnlyStatus(),readOnlyModeController.getReadOnlyStatus() == ReadOnlyModeController.ReadOnlyModeStatus.PENDING_OFF);

        switchThead2.join();

        assertTrue("status: " + readOnlyModeController.getReadOnlyStatus(),readOnlyModeController.getReadOnlyStatus() == ReadOnlyModeController.ReadOnlyModeStatus.OFF);
    }

    /**
     * Test that switch is correctly done on living session
     */
    @Test
    public void testSwitchOnJCRSession() throws Exception {
        // get session uuid
        UUID existingSessionId = UUID.fromString(createUnclosedJCRSession());

        // assert the session is still active and not read only
        JCRSessionWrapper existingSesion = JCRSessionWrapper.getActiveSessionsObjects().get(existingSessionId);
        assertTrue(existingSesion != null);
        assertFalse(existingSesion.isReadOnly());

        readOnlyModeController.switchReadOnlyMode(true);

        // assert the session is still active but read only
        assertTrue(JCRSessionWrapper.getActiveSessionsObjects().containsKey(existingSessionId));
        assertTrue(existingSesion.isReadOnly());

        // create new session
        // get session uuid
        UUID newlyCreatedSessionId = UUID.fromString(createUnclosedJCRSession());
        JCRSessionWrapper newlyCreatedSession = JCRSessionWrapper.getActiveSessionsObjects().get(newlyCreatedSessionId);

        // insure newly created session is readonly
        assertTrue(newlyCreatedSession.isReadOnly());

        readOnlyModeController.switchReadOnlyMode(false);

        // assert the session is still active and not read only
        assertTrue(JCRSessionWrapper.getActiveSessionsObjects().containsKey(existingSessionId));
        assertFalse(existingSesion.isReadOnly());
        assertFalse(newlyCreatedSession.isReadOnly());
    }

    /**
     * Test that JCR save operations are correctly blocked
     */
    @Test
    public void testJCRSessionSaveBlocked() throws Exception {

        // create and opened session
        UUID sessionUUID = UUID.fromString(createUnclosedJCRSession());
        JCRSessionWrapper openedSession = JCRSessionWrapper.getActiveSessionsObjects().get(sessionUUID);
        assertTrue(openedSession != null);

        // test save before switch
        saveSomething(openedSession);
        saveSomething();

        readOnlyModeController.switchReadOnlyMode(true);

        // test that save is blocked for new session
        testReadOnlyModeViolation(this::saveSomething, true, false);

        // test that save is blocked for the opened session
        testReadOnlyModeViolation(() -> saveSomething(openedSession), true, false);

        readOnlyModeController.switchReadOnlyMode(false);

        // test save after switch
        saveSomething(openedSession);
        saveSomething();
    }

    /**
     * Test that JCR lock operations are correctly blocked
     */
    @Test
    public void testJCRLockBlocked() throws Exception {

        // create and opened session
        UUID sessionUUID = UUID.fromString(createUnclosedJCRSession());
        JCRSessionWrapper openedSession = JCRSessionWrapper.getActiveSessionsObjects().get(sessionUUID);
        assertTrue(openedSession != null);

        testLocks(false);
        testLocks(openedSession, false);

        readOnlyModeController.switchReadOnlyMode(true);

        testLocks(true);
        testLocks(openedSession, true);

        readOnlyModeController.switchReadOnlyMode(false);

        testLocks(false);
        testLocks(openedSession, false);
    }

    /**
     * Test that unlock operations are correctly blocked
     */
    @Test
    public void testJCRUnlockBlocked() throws Exception {
        testUnlock(true, true, false);
        testUnlock(false, true, false);
        testUnlock(true, false, false);
        testUnlock(false, false, false);
        testUnlockOnTokenAndUser("unit-test", null, false);
        testUnlockOnTokenAndUser("unit-test", "root", false);
    }

    /**
     * Test that clear all locks operation is correctly blocked
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
     * Test checking and checkout operations are correctly blocked
     */
    @Test
    public void testJCRCheckinCheckoutBlocked() throws Exception {
        // create and opened session
        UUID sessionUUID = UUID.fromString(createUnclosedJCRSession());
        JCRSessionWrapper openedSession = JCRSessionWrapper.getActiveSessionsObjects().get(sessionUUID);
        assertTrue(openedSession != null);

        // insure it's working
        openedSession.getNode(TEST_FOLDER_PATH).checkin();
        openedSession.getNode(TEST_FOLDER_PATH).checkout();

        // checkin a folder
        openedSession.getNode(TEST_FOLDER_PATH2).checkin();

        readOnlyModeController.switchReadOnlyMode(true);

        // test blocked for new session
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            try {
                 testReadOnlyModeViolation(() -> session.getNode(TEST_FOLDER_PATH).checkin(), true, false);

                 testReadOnlyModeViolation(() -> session.getNode(TEST_FOLDER_PATH2).checkout(), true, false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });

        // test blocked for the opened session
        testReadOnlyModeViolation(() -> openedSession.getNode(TEST_FOLDER_PATH).checkin(), true, false);
        testReadOnlyModeViolation(() -> openedSession.getNode(TEST_FOLDER_PATH2).checkout(), true, false);

        readOnlyModeController.switchReadOnlyMode(false);

        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            session.getNode(TEST_FOLDER_PATH).checkin();
            session.getNode(TEST_FOLDER_PATH).checkout();
            return null;
        });

        openedSession.getNode(TEST_FOLDER_PATH).checkin();
        openedSession.getNode(TEST_FOLDER_PATH).checkout();

        openedSession.getNode(TEST_FOLDER_PATH2).checkout();
    }

    /**
     * Test checkpoint opeartion is correctly blocked
     * @throws Exception
     */
    @Test
    public void testJCRCheckpointBlocked() throws Exception {
        // create and opened session
        UUID sessionUUID = UUID.fromString(createUnclosedJCRSession());
        JCRSessionWrapper openedSession = JCRSessionWrapper.getActiveSessionsObjects().get(sessionUUID);
        assertTrue(openedSession != null);

        openedSession.getNode(TEST_FOLDER_PATH).checkpoint();

        readOnlyModeController.switchReadOnlyMode(true);

        // test blocked for new session
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            try {
                testReadOnlyModeViolation(() -> session.getNode(TEST_FOLDER_PATH).checkpoint(), true, false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });

        // test blocked for opened session
        testReadOnlyModeViolation(() -> openedSession.getNode(TEST_FOLDER_PATH).checkpoint(), true, false);

        readOnlyModeController.switchReadOnlyMode(false);

        // test it's unblocked
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            session.getNode(TEST_FOLDER_PATH).checkpoint();
            return null;
        });
        openedSession.getNode(TEST_FOLDER_PATH).checkpoint();
    }

    /**
     * Test scheduler are correctly in stand by mode
     * @throws Exception
     */
    @Test
    public void testSchedulerIsStandbyMode() throws Exception {
        assertFalse(schedulerService.getRAMScheduler().isInStandbyMode());
        assertFalse(schedulerService.getScheduler().isInStandbyMode());

        readOnlyModeController.switchReadOnlyMode(true);

        assertTrue(schedulerService.getRAMScheduler().isInStandbyMode());
        assertTrue(schedulerService.getScheduler().isInStandbyMode());

        readOnlyModeController.switchReadOnlyMode(false);

        assertFalse(schedulerService.getRAMScheduler().isInStandbyMode());
        assertFalse(schedulerService.getScheduler().isInStandbyMode());
    }

    /**
     * Test persisted scheduler actions are blocked
     * @throws Exception
     */
    @Test
    public void testPersistedSchedulerBlocked() throws Exception {
        testScheduler(schedulerService.getScheduler(), false);

        readOnlyModeController.switchReadOnlyMode(true);

        testScheduler(schedulerService.getScheduler(), true);

        readOnlyModeController.switchReadOnlyMode(false);

        testScheduler(schedulerService.getScheduler(), false);
    }

    private void testScheduler(Scheduler scheduler, boolean shouldFail) throws Exception {
        testReadOnlyModeViolation(() -> scheduler.scheduleJob(null), shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.scheduleJob(null, null), shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.unscheduleJob(null, null), shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.rescheduleJob(null, null, null), shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.addJob(null, false), shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.addJob(null, false), shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.deleteJob(null, null), shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.triggerJob(null, null), shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.triggerJob(null, null, null), shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.triggerJobWithVolatileTrigger(null, null), shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.triggerJobWithVolatileTrigger(null, null, null), shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.pauseJob(null, null), shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.pauseJobGroup(null), shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.pauseTrigger(null, null), shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.pauseTriggerGroup(null), shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.resumeJob(null, null), shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.resumeJobGroup(null), shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.resumeTrigger(null, null), shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.resumeTriggerGroup(null), shouldFail, !shouldFail);
        testReadOnlyModeViolation(scheduler::pauseAll, shouldFail, !shouldFail);
        testReadOnlyModeViolation(scheduler::resumeAll, shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.addCalendar(null, null, false, false), shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.deleteCalendar(null), shouldFail, !shouldFail);
        testReadOnlyModeViolation(() -> scheduler.interrupt(null, null), shouldFail, !shouldFail);
    }

    private void testUnlock(boolean isDeep, boolean isSessionScope, boolean useClearAllLocks) throws Exception {
        // create and opened session
        UUID sessionUUID = UUID.fromString(createUnclosedJCRSession());
        JCRSessionWrapper openedSession = JCRSessionWrapper.getActiveSessionsObjects().get(sessionUUID);
        assertTrue(openedSession != null);

        // do lock
        openedSession.getNode(SYSTEM_SITE_PATH).lock(isDeep, isSessionScope);

        readOnlyModeController.switchReadOnlyMode(true);

        // test that unlock is blocked
       testUnlock(openedSession, useClearAllLocks, true);

        if (!isSessionScope) {
            // try unlock with a new session
            JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
                try {
                    testUnlock(session, useClearAllLocks, true);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
        }

        readOnlyModeController.switchReadOnlyMode(false);

        // test that unlock is working
        testUnlock(openedSession, useClearAllLocks, false);
    }

    private void testUnlockOnTokenAndUser(String type, String userId, boolean useClearAllLocks) throws Exception {
        // create and opened session
        UUID sessionUUID = UUID.fromString(createUnclosedJCRSession());
        JCRSessionWrapper openedSession = JCRSessionWrapper.getActiveSessionsObjects().get(sessionUUID);
        assertTrue(openedSession != null);

        // do lock
        JCRNodeWrapper systemSiteNode = openedSession.getNode(SYSTEM_SITE_PATH);
        if (userId != null) {
            systemSiteNode.lockAndStoreToken(type, userId);
        } else {
            systemSiteNode.lockAndStoreToken(type);
        }

        readOnlyModeController.switchReadOnlyMode(true);

        // test that unlock is blocked
        testUnlockOnTokenAndUser(openedSession, type, userId, useClearAllLocks, true);

        // test unlock from an other session
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            try {
                testUnlockOnTokenAndUser(session, type, userId, useClearAllLocks, true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });

        readOnlyModeController.switchReadOnlyMode(false);

        testUnlockOnTokenAndUser(openedSession, type, userId, useClearAllLocks, false);
    }

    private void testUnlock(JCRSessionWrapper sessionWrapper, boolean useClearAllLocks, boolean shouldFail) throws Exception {
        JCRNodeWrapper systemSiteNode = sessionWrapper.getNode(SYSTEM_SITE_PATH);
        testReadOnlyModeViolation(() -> {
            if (useClearAllLocks) {
                systemSiteNode.clearAllLocks();
            } else {
                systemSiteNode.unlock();
            }
        }, shouldFail, false);
    }

    private void testUnlockOnTokenAndUser(JCRSessionWrapper sessionWrapper, String type, String userId, boolean useClearAllLocks, boolean shouldFail) throws Exception {
        JCRNodeWrapper systemSiteNode = sessionWrapper.getNode(SYSTEM_SITE_PATH);
        testReadOnlyModeViolation(() -> {
            if (useClearAllLocks) {
                systemSiteNode.clearAllLocks();
            } else {
                if (userId != null) {
                    systemSiteNode.unlock(type, userId);
                } else {
                    systemSiteNode.unlock(type);
                }
            }
        }, shouldFail, false);
    }

    private void testLocks(boolean shouldFail) throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession((session) -> {
            try {
                testLocks(session, shouldFail);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    private void testLocks(JCRSessionWrapper sessionWrapper, boolean shouldFail) throws Exception {
        testLock(sessionWrapper, true, true, shouldFail);
        testLock(sessionWrapper, true, false, shouldFail);
        testLock(sessionWrapper, false, true, shouldFail);
        testLock(sessionWrapper, false, false, shouldFail);
        testLockAndStoreToken(sessionWrapper, "unit-test", null, shouldFail);
        testLockAndStoreToken(sessionWrapper, "unit-test", "root", shouldFail);
    }

    private void testLock(JCRSessionWrapper sessionWrapper, boolean isDeep, boolean sessionScoped, boolean shouldFail) throws Exception {
        JCRNodeWrapper systemSiteNode = sessionWrapper.getNode(SYSTEM_SITE_PATH);
        try {
            testReadOnlyModeViolation(() -> {
                systemSiteNode.lock(isDeep, sessionScoped);
            }, shouldFail, false);
        } finally {
            try {
                systemSiteNode.unlock();
            } catch (ReadOnlyModeException | LockException e) {
                // the node hasn't been locked or we are read only
            }
        }
    }

    private void testLockAndStoreToken(JCRSessionWrapper sessionWrapper, String type, String userId, boolean shouldFail) throws Exception {
        JCRNodeWrapper systemSiteNode = sessionWrapper.getNode(SYSTEM_SITE_PATH);
        try {
            testReadOnlyModeViolation(() -> {
                if (userId != null) {
                    systemSiteNode.lockAndStoreToken(type, userId);
                } else {
                    systemSiteNode.lockAndStoreToken(type);
                }
            }, shouldFail, false);
        } finally {
            try {
                if (userId != null) {
                    systemSiteNode.unlock(type, userId);
                } else {
                    systemSiteNode.unlock(type);
                }
            } catch (ReadOnlyModeException | LockException e) {
                // the node hasn't been locked or we are read only
            }
        }
    }

    private void saveSomething() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            saveSomething(session);
            return null;
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
        openedTestSessions.add(jcrSessionThread.getResult());
        return jcrSessionThread.getResult();
    }

    private void setWaitingTestService(long millis) {
        // using test service to wait 30 sec during the switch
        fullReadOnlyModeTestService.setTestCallback(() -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
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

    private interface ReadOnlyModeViolationAction {
        void doExecute() throws Exception;
    }

    private void testReadOnlyModeViolation(ReadOnlyModeViolationAction action, boolean shouldFail, boolean ignoreExceptions) throws Exception {
        try {
            action.doExecute();
            if (shouldFail) {
                fail();
            }
        } catch (ReadOnlyModeException e) {
            if (!shouldFail) {
                fail();
            }
        } catch (Exception e) {
            if (!ignoreExceptions) {
                throw e;
            }
        }
    }
}
