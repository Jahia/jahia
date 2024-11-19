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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.readonly;

import org.jahia.api.Constants;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.readonlymode.ReadOnlyModeController;
import org.jahia.settings.readonlymode.ReadOnlyModeException;
import org.jahia.test.JahiaTestCase;
import org.junit.*;
import org.quartz.Scheduler;

import com.google.common.collect.ImmutableMap;

import static org.junit.Assert.*;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Kevan
 *
 * Test class to test Full read only feature
 */
public class FullReadOnlyModeTest extends JahiaTestCase {

    private static final String SYSTEM_SITE_PATH = "/sites/systemsite";
    private static final String TEST_CONTENT_PATH = SYSTEM_SITE_PATH + "/contents";
    private static final String TEST_FOLDER_PATH = SYSTEM_SITE_PATH + "/files/testFolder";
    private static final String TEST_FOLDER_PATH2 = SYSTEM_SITE_PATH + "/files/testFolder2";

    private static ReadOnlyModeController readOnlyModeController;
    private static SchedulerService schedulerService;
    private static ReadOnlyModeCapablePlaceholder readOnlyModeCapablePlaceholder;
    private static boolean originalSystemSiteWCAcompliance;

    private static void assertLock(Node node, boolean shouldBeLocked)
            throws PathNotFoundException, RepositoryException {
        assertEquals("Node " + node.getPath() + " should " + (shouldBeLocked ? "" : " NOT ") + "be locked",
                shouldBeLocked, node.isLocked());
        assertEquals("Node " + node.getPath() + " should " + (shouldBeLocked ? "" : " NOT ") + "be locked",
                shouldBeLocked, node.hasProperty("j:lockTypes"));
        if (node instanceof JCRNodeWrapper) {
            // iterate over translation nodes
            for (NodeIterator ni = ((JCRNodeWrapper) node).getI18Ns(); ni.hasNext();) {
                assertLock(ni.nextNode(), shouldBeLocked);
            }
        }
    }

    private static void assertLocks(final Map<String, Boolean> locks) throws RepositoryException {
        // non-localized session
        JCRTemplate.getInstance().doExecuteWithSystemSession((JCRCallback<Boolean>) session -> {
            for (Map.Entry<String, Boolean> lock : locks.entrySet()) {
                assertLock(session.getNode(lock.getKey()), lock.getValue());
            }
            return null;
        });
    }

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        readOnlyModeCapablePlaceholder = (ReadOnlyModeCapablePlaceholder) SpringContextSingleton.getBean("ReadOnlyModeCapablePlaceholder");
        readOnlyModeController = (ReadOnlyModeController) SpringContextSingleton.getBean("ReadOnlyModeController");
        schedulerService = (SchedulerService) SpringContextSingleton.getBean("SchedulerService");
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            JCRNodeWrapper systemSiteNode = session.getNode(SYSTEM_SITE_PATH);
            systemSiteNode.getNode("files").addNode("testFolder", "jnt:folder");
            systemSiteNode.getNode("files").addNode("testFolder2", "jnt:folder");
            session.save();
            originalSystemSiteWCAcompliance = systemSiteNode.getProperty("j:wcagCompliance").getBoolean();
            return null;
        });
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession((JCRCallback<Boolean>) session -> {
            session.getNode(TEST_FOLDER_PATH).remove();
            session.getNode(TEST_FOLDER_PATH2).remove();
            session.getNode(SYSTEM_SITE_PATH).setProperty("j:wcagCompliance", originalSystemSiteWCAcompliance);
            if (session.nodeExists(TEST_CONTENT_PATH + "/text-1")) {
                session.getNode(TEST_CONTENT_PATH + "/text-1").remove();
            }
            if (session.nodeExists(TEST_CONTENT_PATH + "/text-2")) {
                session.getNode(TEST_CONTENT_PATH + "/text-2").remove();
            }
            session.save();
            return null;
        });
    }

    private HashSet<String> openedTestSessions = new HashSet<>();

    @After
    public void tearDown() {

        // reset the placeholder
        readOnlyModeCapablePlaceholder.setReadOnlyModeSwitchImplementation(null);

        // switch off read only
        try {
            readOnlyModeController.switchReadOnlyMode(false);
        } catch (IllegalStateException e) {
            // the read only mode was off already
        }

        // close test sessions potentially opened for testing purpose
        for (String openedTestSession : openedTestSessions) {
            JCRSessionWrapper openedSession = JCRSessionWrapper.getActiveSessionsObjects().get(UUID.fromString(openedTestSession));
            if (openedSession != null) {
                openedSession.logout();
            }
        }
        openedTestSessions.clear();
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

        // ensure newly created session is readonly
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

        // create an open session
        UUID sessionUUID = UUID.fromString(createUnclosedJCRSession());
        JCRSessionWrapper openedSession = JCRSessionWrapper.getActiveSessionsObjects().get(sessionUUID);
        assertTrue(openedSession != null);

        // test save before switch
        saveSomething(openedSession);
        saveSomething();

        readOnlyModeController.switchReadOnlyMode(true);

        // test that save is blocked for new session
        testReadOnlyModeViolation(this::saveSomething, true);

        // test that save is blocked for the opened session
        testReadOnlyModeViolation(() -> saveSomething(openedSession), true);

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

        // create an open session
        UUID sessionUUID = UUID.fromString(createUnclosedJCRSession());
        JCRSessionWrapper openedSession = JCRSessionWrapper.getActiveSessionsObjects().get(sessionUUID);

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
     * Test that engine locks are removed when switching read only mode: ON
     */
    @Test
    public void testEngineLocksAreCleared() throws Exception {
        Locale locale = Locale.ENGLISH;
        JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();
        JCRTemplate jcrTemplate = JCRTemplate.getInstance();
        jcrTemplate.doExecuteWithSystemSessionAsUser(user, Constants.EDIT_WORKSPACE, locale,
                (JCRCallback<Boolean>) session -> {
                    // non-multi-language content: create engine lock
                    session.getNode(TEST_FOLDER_PATH).lockAndStoreToken("engine", "root");
                    // non-multi-language content: create user lock
                    session.getNode(TEST_FOLDER_PATH2).lockAndStoreToken("user", "root");

                    // multi-language content
                    JCRNodeWrapper text = session.getNode(TEST_CONTENT_PATH).addNode("text-1", "jnt:text");
                    text.setProperty("text", "My text 1");
                    text = session.getNode(TEST_CONTENT_PATH).addNode("text-2", "jnt:text");
                    text.setProperty("text", "My text 2");
                    session.save();

                    // create engine lock
                    session.getNode(TEST_CONTENT_PATH + "/text-1").lockAndStoreToken("engine", "root");

                    // create user lock
                    session.getNode(TEST_CONTENT_PATH + "/text-2").lockAndStoreToken("user", "root");

                    return null;
                });

        // all locks should be there
        assertLocks(ImmutableMap.of(TEST_FOLDER_PATH, true, TEST_FOLDER_PATH2, true, TEST_CONTENT_PATH + "/text-1",
                true, TEST_CONTENT_PATH + "/text-2", true));

        readOnlyModeController.switchReadOnlyMode(true);

        // engine locks should be gone, user locks should be still present
        Map<String, Boolean> locks = ImmutableMap.of(TEST_FOLDER_PATH, false, TEST_FOLDER_PATH2, true,
                TEST_CONTENT_PATH + "/text-1", false, TEST_CONTENT_PATH + "/text-2", true);

        assertLocks(locks);

        readOnlyModeController.switchReadOnlyMode(false);

        assertLocks(locks);

        jcrTemplate.doExecuteWithSystemSessionAsUser(user, Constants.EDIT_WORKSPACE, locale,
                (JCRCallback<Boolean>) session -> {
                    session.getNode(TEST_FOLDER_PATH2).unlock("user", "root");
                    session.getNode(TEST_CONTENT_PATH + "/text-2").unlock("user", "root");

                    session.getNode(TEST_CONTENT_PATH + "/text-1").remove();
                    session.getNode(TEST_CONTENT_PATH + "/text-2").remove();
                    return null;
                });
    }

    /**
     * Test checking and checkout operations are correctly blocked
     */
    @Test
    public void testJCRCheckinCheckoutBlocked() throws Exception {

        // create an open session
        UUID sessionUUID = UUID.fromString(createUnclosedJCRSession());
        JCRSessionWrapper openedSession = JCRSessionWrapper.getActiveSessionsObjects().get(sessionUUID);

        // ensure it's working
        openedSession.getWorkspace().getVersionManager().checkin(TEST_FOLDER_PATH);
        openedSession.getWorkspace().getVersionManager().checkout(TEST_FOLDER_PATH);

        // checkin a folder
        openedSession.getWorkspace().getVersionManager().checkin(TEST_FOLDER_PATH2);

        readOnlyModeController.switchReadOnlyMode(true);

        // test blocked for a new session
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            try {
                 testReadOnlyModeViolation(() -> session.getWorkspace().getVersionManager().checkin(TEST_FOLDER_PATH), true);
                 testReadOnlyModeViolation(() -> session.getWorkspace().getVersionManager().checkout(TEST_FOLDER_PATH2), true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });

        // test blocked for the open session
        testReadOnlyModeViolation(() -> openedSession.getWorkspace().getVersionManager().checkin(TEST_FOLDER_PATH), true);
        testReadOnlyModeViolation(() -> openedSession.getWorkspace().getVersionManager().checkout(TEST_FOLDER_PATH2), true);

        readOnlyModeController.switchReadOnlyMode(false);

        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            session.getWorkspace().getVersionManager().checkin(TEST_FOLDER_PATH);
            session.getWorkspace().getVersionManager().checkout(TEST_FOLDER_PATH);
            return null;
        });

        openedSession.getWorkspace().getVersionManager().checkin(TEST_FOLDER_PATH);
        openedSession.getWorkspace().getVersionManager().checkout(TEST_FOLDER_PATH);

        openedSession.getWorkspace().getVersionManager().checkout(TEST_FOLDER_PATH2);
    }

    /**
     * Test checkpoint operation is correctly blocked
     */
    @Test
    public void testJCRCheckpointBlocked() throws Exception {

        // create an open session
        UUID sessionUUID = UUID.fromString(createUnclosedJCRSession());
        JCRSessionWrapper openedSession = JCRSessionWrapper.getActiveSessionsObjects().get(sessionUUID);

        openedSession.getNode(TEST_FOLDER_PATH).checkpoint();

        readOnlyModeController.switchReadOnlyMode(true);

        // test blocked for a new session
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            try {
                testReadOnlyModeViolation(() -> session.getNode(TEST_FOLDER_PATH).checkpoint(), true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });

        // test blocked for the open session
        testReadOnlyModeViolation(() -> openedSession.getNode(TEST_FOLDER_PATH).checkpoint(), true);

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

        // Whenever we don't expect the scheduler to fail because of ReadOnlyModeException (shouldFail == false),
        // we instead expect it to fail because of NPE or another exception related to all null parameters we pass,
        // which actually indicates an expected invocation of the scheduler's underlying scheduler, so we will just
        // suppress these exceptions.
        ExceptionHandler exceptionHandler = null;
        if (!shouldFail) {
            exceptionHandler = new ExceptionHandler() {

                @Override
                public void handle(Exception e) throws Exception {
                    // The underlying scheduler was likely expectedly invoked with null parameters.
                }
            };
        }

        testReadOnlyModeViolation(() -> scheduler.scheduleJob(null), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.scheduleJob(null, null), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.unscheduleJob(null, null), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.rescheduleJob(null, null, null), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.addJob(null, false), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.addJob(null, false), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.deleteJob(null, null), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.triggerJob(null, null), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.triggerJob(null, null, null), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.triggerJobWithVolatileTrigger(null, null), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.triggerJobWithVolatileTrigger(null, null, null), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.pauseJob(null, null), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.pauseJobGroup(null), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.pauseTrigger(null, null), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.pauseTriggerGroup(null), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.resumeJob(null, null), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.resumeJobGroup(null), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.resumeTrigger(null, null), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.resumeTriggerGroup(null), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(scheduler::pauseAll, shouldFail, exceptionHandler);
        testReadOnlyModeViolation(scheduler::resumeAll, shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.addCalendar(null, null, false, false), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.deleteCalendar(null), shouldFail, exceptionHandler);
        testReadOnlyModeViolation(() -> scheduler.interrupt(null, null), shouldFail, exceptionHandler);
    }

    private void testUnlock(boolean isDeep, boolean isSessionScope, boolean useClearAllLocks) throws Exception {

        // create an open session
        UUID sessionUUID = UUID.fromString(createUnclosedJCRSession());
        JCRSessionWrapper openedSession = JCRSessionWrapper.getActiveSessionsObjects().get(sessionUUID);

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

        // create an open session
        UUID sessionUUID = UUID.fromString(createUnclosedJCRSession());
        JCRSessionWrapper openedSession = JCRSessionWrapper.getActiveSessionsObjects().get(sessionUUID);

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

        // test unlock from another session
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
        }, shouldFail);
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
        }, shouldFail);
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
            }, shouldFail);
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
            }, shouldFail);
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
        readOnlyModeCapablePlaceholder.setReadOnlyModeSwitchImplementation((boolean enable) -> {
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

    private interface ExceptionHandler {

        void handle(Exception e) throws Exception;
    }


    private void testReadOnlyModeViolation(ReadOnlyModeViolationAction action, boolean shouldFail) throws Exception {
        testReadOnlyModeViolation(action, shouldFail, new ExceptionHandler() {

            @Override
            public void handle(Exception e) throws Exception {
                throw e;
            }
        });
    }

    private void testReadOnlyModeViolation(ReadOnlyModeViolationAction action, boolean shouldFail, ExceptionHandler exceptionHandler) throws Exception {
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
            exceptionHandler.handle(e);
        }
    }
}
