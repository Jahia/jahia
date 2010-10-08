/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.remotepublish;

import junit.framework.TestCase;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.modules.remotepublish.RemotePublicationService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Value;
import java.io.*;
import java.util.*;

/**
 * Unit test for remote publishing
 */
public class LogReplayTest extends TestCase {
    private static Logger logger = Logger.getLogger(LogReplayTest.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "jcrRPTest";

    @Override
    protected void setUp() throws Exception {
        try {
            site = TestHelper.createSite(TESTSITE_NAME);
            assertNotNull(site);
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
            JCRSessionFactory.getInstance().closeAllSessions();
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }

    /*public void testLogImport() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        final JCRNodeWrapper node = session.getNode("/sites/jcrRPTest/home");
        final JCRNodeWrapper target = node.addNode("target", "jnt:page");
        target.setProperty("jcr:title", "Target");
        session.save();

        InputStream is = getClass().getClassLoader().getResourceAsStream("remotepublish/remote1.log.gz");
        RemotePublicationService.getInstance().replayLog(target, is);

        assertTrue("Node not added", target.hasNode("page1"));
        assertTrue("Node not added", target.hasNode("page2"));
        assertTrue("Node not added", target.hasNode("page3"));

    }*/

    public void testLogAddAndRemoveNode() throws Exception {
        logger.info("testLogAddAndUpdateOfProperty : start");
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        final JCRNodeWrapper node = session.getNode("/sites/jcrRPTest/home");
        JCRNodeWrapper source = node.addNode("source", "jnt:page");
        source.setProperty("jcr:title", "Source");
        JCRNodeWrapper page1 = source.addNode("page1", "jnt:page");
        page1.setProperty("jcr:title", "Page1");
        JCRNodeWrapper page2 = source.addNode("page2", "jnt:page");
        page2.setProperty("jcr:title", "Page2");
        JCRNodeWrapper page3 = source.addNode("page3", "jnt:page");
        page3.setProperty("jcr:title", "Page3");
        JCRNodeWrapper target = node.addNode("target", "jnt:page");
        target.setProperty("jcr:title", "Target");
        session.save();

        Calendar now = new GregorianCalendar();

        JCRPublicationService.getInstance()
                .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true,null);

        JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));
        target = liveSession.getNode("/sites/jcrRPTest/home/target");

        JCRNodeWrapper liveSource = liveSession.getNodeByUUID(source.getIdentifier());

        File tmp = File.createTempFile("remoteAddNode", ".log.gz");
        RemotePublicationService.getInstance().generateLog(liveSource, now, new FileOutputStream(tmp));
        session.checkout(node);
        session.save();

        RemotePublicationService.getInstance().replayLog(target, new FileInputStream(tmp));

        assertTrue("Node not added", target.hasNode("page1"));
        assertTrue("Node not added", target.hasNode("page2"));
        assertTrue("Node not added", target.hasNode("page3"));
        JCRPublicationService.getInstance()
                .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
        now = new GregorianCalendar();
        session.checkout(source);
        session.checkout(page1);
        page1.remove();
        session.save();
        JCRPublicationService.getInstance()
                .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
        try {
            liveSession.getNode("/sites/jcrRPTest/home/source/page1");
            fail("/sites/jcrRPTest/home/source/page1 should not have been found in live");
        } catch (PathNotFoundException e) {
        }
        tmp = File.createTempFile("remoteRemoveNode", ".log.gz");
        RemotePublicationService.getInstance().generateLog(liveSource, now, new FileOutputStream(tmp));
        RemotePublicationService.getInstance().replayLog(target, new FileInputStream(tmp));
        assertFalse("Node has not been removed", target.hasNode("page1"));

    }

    public void testLogAddAndUpdateOfProperty() throws Exception {
        logger.info("testLogAddAndUpdateOfProperty : start");
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));
        JahiaUser user1 = JCRUserManagerProvider.getInstance().createUser("source-user", "password", new Properties());
        JahiaUser user2 = JCRUserManagerProvider.getInstance().createUser("target-user", "password", new Properties());
        
        try {
            final JCRNodeWrapper node = session.getNode("/sites/jcrRPTest/home");
            JCRNodeWrapper source = node.addNode("source-user", "jnt:user");
            JCRNodeWrapper target = node.addNode("target-user", "jnt:user");
            session.save();


            JCRPublicationService.getInstance()
                    .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);

            Calendar now = new GregorianCalendar();
            session.checkout(source);
            source.setProperty("j:firstName", "testLogAddAndUpdateOfProperty");
            session.save();

            JCRPublicationService.getInstance()
                    .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
            JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE,
                    LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));
            target = liveSession.getNode("/sites/jcrRPTest/home/target-user");

            JCRNodeWrapper liveSource = liveSession.getNodeByUUID(source.getIdentifier());

            File tmp = File.createTempFile("remoteAddNode", ".log.gz");
            RemotePublicationService.getInstance().generateLog(liveSource, now, new FileOutputStream(tmp));

            RemotePublicationService.getInstance().replayLog(target, new FileInputStream(tmp));

            assertEquals("Property j:firstName does not match testLogAddAndUpdateOfProperty",
                    "testLogAddAndUpdateOfProperty", target.getProperty("j:firstName").getString());

            JCRPublicationService.getInstance()
                    .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
            now = new GregorianCalendar();
            session.checkout(source);
            source.setProperty("j:firstName", "testLogAddAndUpdateOfProperty_updated");
            session.save();
            JCRPublicationService.getInstance()
                    .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
            tmp = File.createTempFile("remoteRemoveNode", ".log.gz");
            RemotePublicationService.getInstance().generateLog(liveSource, now, new FileOutputStream(tmp));
            RemotePublicationService.getInstance().replayLog(target, new FileInputStream(tmp));
            assertEquals("Property j:firstName does not match testLogAddAndUpdateOfProperty_updated",
                    "testLogAddAndUpdateOfProperty_updated", target.getProperty("j:firstName").getString());
        } finally {
            ServicesRegistry.getInstance().getJahiaUserManagerService().deleteUser(user1);
            ServicesRegistry.getInstance().getJahiaUserManagerService().deleteUser(user2);
        }

    }

    public void testLogAddAndRemoveOfProperty() throws Exception {
        logger.info("testLogAddAndRemoveOfProperty : start");
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));
        JahiaUser user1 = JCRUserManagerProvider.getInstance().createUser("source-user", "password", new Properties());
        JahiaUser user2 = JCRUserManagerProvider.getInstance().createUser("target-user", "password", new Properties());
        try {
            final JCRNodeWrapper node = session.getNode("/sites/jcrRPTest/home");
            JCRNodeWrapper source = node.addNode("source-user", "jnt:user");
            JCRNodeWrapper target = node.addNode("target-user", "jnt:user");
            session.save();

            JCRPublicationService.getInstance().publish(node.getIdentifier(),
                    Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);

            Calendar now = new GregorianCalendar();
            session.checkout(source);
            source.setProperty("j:firstName", "testLogAddAndUpdateOfProperty");
            session.save();

            JCRPublicationService.getInstance().publish(node.getIdentifier(),
                    Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
            JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(
                    Constants.LIVE_WORKSPACE,
                    LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

            target = liveSession.getNode("/sites/jcrRPTest/home/target-user");

            JCRNodeWrapper liveSource = liveSession.getNodeByUUID(source.getIdentifier());

            File tmp = File.createTempFile("remoteAddNode", ".log.gz");
            RemotePublicationService.getInstance().generateLog(liveSource, now,
                    new FileOutputStream(tmp));
            RemotePublicationService.getInstance().replayLog(target, new FileInputStream(tmp));

            assertEquals("Property j:firstName does not match testLogAddAndUpdateOfProperty",
                    "testLogAddAndUpdateOfProperty", target.getProperty("j:firstName").getString());

            JCRPublicationService.getInstance().publish(node.getIdentifier(),
                    Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
            now = new GregorianCalendar();
            session.checkout(source);
            source.getProperty("j:firstName").remove();
            session.save();
            JCRPublicationService.getInstance().publish(node.getIdentifier(),
                    Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
            tmp = File.createTempFile("remoteRemoveNode", ".log.gz");
            RemotePublicationService.getInstance().generateLog(liveSource, now,
                    new FileOutputStream(tmp));
            RemotePublicationService.getInstance().replayLog(target, new FileInputStream(tmp));
            try {
                target.getProperty("j:firstName");
                fail("Property j:firstName should not exist");
            } catch (PathNotFoundException e) {
            }
        } finally {
            ServicesRegistry.getInstance().getJahiaUserManagerService().deleteUser(user1);
            ServicesRegistry.getInstance().getJahiaUserManagerService().deleteUser(user2);
        }

    }

    public void testLogMoveOfNode() throws Exception {
        logger.info("testLogMoveOfNode : start");
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        final JCRNodeWrapper node = session.getNode("/sites/jcrRPTest/home");
        JCRNodeWrapper source = node.addNode("source", "jnt:page");
        source.setProperty("jcr:title", "Source");
        JCRNodeWrapper page1 = source.addNode("page1", "jnt:page");
        page1.setProperty("jcr:title", "Page1");
        JCRNodeWrapper page2 = page1.addNode("page2", "jnt:page");
        page2.setProperty("jcr:title", "Page2");
        JCRNodeWrapper page3 = page2.addNode("page3", "jnt:page");
        page3.setProperty("jcr:title", "testLogMoveOfNode");
        JCRNodeWrapper target = node.addNode("target", "jnt:page");
        target.setProperty("jcr:title", "Target");        
        session.save();

        Calendar now = new GregorianCalendar();

        JCRPublicationService.getInstance()
                .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
        JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        target = liveSession.getNode("/sites/jcrRPTest/home/target");

        JCRNodeWrapper liveSource = liveSession.getNodeByUUID(source.getIdentifier());

        File tmp = File.createTempFile("remoteAddNode", ".log.gz");

        RemotePublicationService.getInstance().generateLog(liveSource, now, new FileOutputStream(tmp));
        RemotePublicationService.getInstance().replayLog(target, new FileInputStream(tmp));

        try {
            liveSession.getNode("/sites/jcrRPTest/home/target/page1/page2/page3");
        } catch (PathNotFoundException e) {
            fail("We should have found path : /sites/jcrRPTest/home/target/page1/page2/page3");
        }

        JCRPublicationService.getInstance()
                .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);

        session.checkout(page1);
        session.checkout(page3);
        session.checkout(page2);
        session.move(page3.getPath(), page1.getPath() + "/page3_moved");
        session.save();
        now = new GregorianCalendar();
        JCRPublicationService.getInstance()
                .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);

        tmp = File.createTempFile("remoteMoveNode", ".log.gz");
        RemotePublicationService.getInstance().generateLog(liveSource, now, new FileOutputStream(tmp));
        RemotePublicationService.getInstance().replayLog(target, new FileInputStream(tmp));
        try {
            liveSession.getNode("/sites/jcrRPTest/home/target/page1/page2/page3");
            fail("We should not have found path : /sites/jcrRPTest/home/target/page1/page2/page3");
        } catch (PathNotFoundException e) {
        }

        try {
            JCRNodeWrapper movedNode = liveSession.getNode("/sites/jcrRPTest/home/target/page1/page3_moved");
            assertTrue("The moved page should have a title", movedNode.hasProperty("jcr:title"));
            assertEquals("The title page should be testLogMoveOfNode", "testLogMoveOfNode",
                    movedNode.getProperty("jcr:title").getString());
        } catch (PathNotFoundException e) {
            fail("We should have found path : /sites/jcrRPTest/home/target/page1/page3_moved");
        }

    }

    public void testLogBinaryNode() throws Exception {
        logger.info("testLogBinaryNode : start");
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        JCRNodeWrapper node = session.getNode("/sites/jcrRPTest/files");
        JCRNodeWrapper source = node.addNode("source", "jnt:folder");
        JCRNodeWrapper target = node.addNode("target", "jnt:folder");

        Calendar now = new GregorianCalendar();

        String value = "This is a test";
        String mimeType = "text/plain";

        InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

        String name = "test.txt";
        JCRNodeWrapper fileNode = source.uploadFile(name, is, mimeType);

        session.save();

        JCRPublicationService.getInstance()
                .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
        JCRPublicationService.getInstance()
                .publish(source.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
        JCRPublicationService.getInstance()
                .publish(fileNode.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE,
                        null, true, null);
        JCRPublicationService.getInstance()
                .publish(target.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);

        JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        target = liveSession.getNode("/sites/jcrRPTest/files/target");

        source = liveSession.getNodeByUUID(source.getIdentifier());

        File tmp = File.createTempFile("remote", ".log.gz");
        RemotePublicationService.getInstance().generateLog(source, now, new FileOutputStream(tmp));

        RemotePublicationService.getInstance().replayLog(target, new FileInputStream(tmp));

        assertTrue("File node not found", target.hasNode("test.txt"));
        JCRNodeWrapper targetFile = target.getNode("test.txt");
        assertTrue("Content node not found", targetFile.hasNode("jcr:content"));
        JCRNodeWrapper targetContent = targetFile.getNode("jcr:content");
        assertTrue("data property not found", targetContent.hasProperty("jcr:data"));

        Value v = targetContent.getProperty("jcr:data").getValue();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(v.getBinary().getStream(), baos);
        assertEquals("Binary content invalid", value, new String(baos.toByteArray(), "UTF-8"));

    }

    public void testLogSharedNode() throws Exception {
        logger.info("testLogSharedNode : start");
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        final JCRNodeWrapper node = session.getNode("/sites/jcrRPTest/home");
        JCRNodeWrapper source = node.addNode("source", "jnt:page");
        source.setProperty("jcr:title", "Source");
        JCRNodeWrapper page1 = source.addNode("page1", "jnt:page");
        page1.setProperty("jcr:title", "Page1");
        JCRNodeWrapper subpage = page1.addNode("subpage", "jnt:page");
        subpage.setProperty("jcr:title", "Subpage");
        JCRNodeWrapper subsubpage = subpage.addNode("subsubpage", "jnt:page");
        subsubpage.setProperty("jcr:title", "SubSubPage");
        JCRNodeWrapper page2 = source.addNode("page2", "jnt:page");
        page2.setProperty("jcr:title", "Page2");
        JCRNodeWrapper target = node.addNode("target", "jnt:page");
        target.setProperty("jcr:title", "Target");
        session.save();

        Calendar now = new GregorianCalendar();

        JCRPublicationService.getInstance()
                .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);

        JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        target = liveSession.getNode("/sites/jcrRPTest/home/target");

        JCRNodeWrapper liveSource = liveSession.getNodeByUUID(source.getIdentifier());

        File tmp = File.createTempFile("remoteAddNode", ".log.gz");
        RemotePublicationService.getInstance().generateLog(liveSource, now, new FileOutputStream(tmp));

            RemotePublicationService.getInstance().replayLog(target, new FileInputStream(tmp));


        assertTrue("Node not added", target.hasNode("page1"));
        assertTrue("Node not added", target.hasNode("page2"));
        JCRPublicationService.getInstance()
                .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);

        now = new GregorianCalendar();
        session.checkout(source);
        session.checkout(page2);
        page2.clone(subpage, "sharedsubpage");
        session.save();
        JCRPublicationService.getInstance()
                .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
        tmp = File.createTempFile("remoteShareNode", ".log.gz");
        RemotePublicationService.getInstance().generateLog(liveSource, now, new FileOutputStream(tmp));

        RemotePublicationService.getInstance().replayLog(target, new FileInputStream(tmp));

        JCRNodeWrapper sharedTarget = liveSession.getNode("/sites/jcrRPTest/home/target/page2/sharedsubpage");
        List<String> res = new ArrayList<String>();
        NodeIterator ni = sharedTarget.getSharedSet();
        while (ni.hasNext()) {
            JCRNodeWrapper s = (JCRNodeWrapper) ni.next();
            res.add(s.getPath());
        }
        assertTrue("Node has not been shared", res.contains("/sites/jcrRPTest/home/target/page2/sharedsubpage"));
        assertTrue("Node has not been shared", res.contains("/sites/jcrRPTest/home/target/page1/subpage"));

    }

    public void testLogOrderingOfNodes() throws Exception {
        logger.info("testLogOrderingOfNodes : start");
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        final JCRNodeWrapper node = session.getNode("/sites/jcrRPTest/home");
        JCRNodeWrapper source = node.addNode("source", "jnt:page");
        source.setProperty("jcr:title", "Source");
        JCRNodeWrapper page1 = source.addNode("page1", "jnt:page");
        page1.setProperty("jcr:title", "Page1");
        JCRNodeWrapper page2 = source.addNode("page2", "jnt:page");
        page2.setProperty("jcr:title", "Page2");
        JCRNodeWrapper page3 = source.addNode("page3", "jnt:page");
        page3.setProperty("jcr:title", "Page3");
        JCRNodeWrapper page4 = source.addNode("page4", "jnt:page");
        page4.setProperty("jcr:title", "Page4");
        JCRNodeWrapper page5 = source.addNode("page5", "jnt:page");
        page5.setProperty("jcr:title", "Page5");
        JCRNodeWrapper page6 = source.addNode("page6", "jnt:page");
        page6.setProperty("jcr:title", "Page6");
        JCRNodeWrapper target = node.addNode("target", "jnt:page");
        target.setProperty("jcr:title", "Target");
        session.save();

        Calendar now = new GregorianCalendar();

        JCRPublicationService.getInstance()
                .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
        JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        target = liveSession.getNode("/sites/jcrRPTest/home/target");

        JCRNodeWrapper liveSource = liveSession.getNodeByUUID(source.getIdentifier());

        File tmp = File.createTempFile("remoteAddNode", ".log.gz");

        RemotePublicationService.getInstance().generateLog(liveSource, now, new FileOutputStream(tmp));
        RemotePublicationService.getInstance().replayLog(target, new FileInputStream(tmp));

        List<String> pageNames = Arrays.asList("page1", "page2", "page3", "page4", "page5", "page6");
        List<String> pageFound = new ArrayList<String>();
        NodeIterator nodeIterator = target.getNodes();
        int i = 0;
        while (nodeIterator.hasNext()) {
            JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodeIterator.next();
            String s = pageNames.get(i);
            logger.info("testLogOrderingOfNodes : [" + i + "] " + nodeWrapper.getName());
            assertEquals("Node name should be : " + s, pageNames.get(i), nodeWrapper.getName());
            pageFound.add(s);
            i++;
        }
        assertTrue("Number of pages should be " + pageNames.size() + "but found " + pageFound.size(),
                pageNames.size() == pageFound.size());

        JCRPublicationService.getInstance()
                .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
        session.checkout(source);
        session.checkout(page1);
        session.checkout(page2);
        session.checkout(page3);
        session.checkout(page4);
        session.checkout(page5);
        session.checkout(page6);
        source.orderBefore("page4", "page2");
        source.orderBefore("page6", "page5");
        session.save();
        pageNames = Arrays.asList("page1", "page4", "page2", "page3", "page6", "page5");
        pageFound = new ArrayList<String>();
        i = 0;
        nodeIterator = source.getNodes();
        while (nodeIterator.hasNext()) {
            JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodeIterator.next();
            String s = pageNames.get(i);
            logger.info("testLogOrderingOfNodes source : [" + i + "] " + nodeWrapper.getName());
            assertEquals("Node name should be : " + s, pageNames.get(i), nodeWrapper.getName());
            pageFound.add(s);
            i++;
        }
        now = new GregorianCalendar();
        JCRPublicationService.getInstance()
                .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);

        tmp = File.createTempFile("remoteMoveNode", ".log.gz");

        RemotePublicationService.getInstance().generateLog(liveSource, now, new FileOutputStream(tmp));
        RemotePublicationService.getInstance().replayLog(target, new FileInputStream(tmp));

        pageFound = new ArrayList<String>();
        i = 0;
        nodeIterator = target.getNodes();
        while (nodeIterator.hasNext()) {
            JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodeIterator.next();
            String s = pageNames.get(i);
            logger.info("testLogOrderingOfNodes : [" + i + "] " + nodeWrapper.getName());
            assertEquals("Node name should be : " + s, pageNames.get(i), nodeWrapper.getName());
            pageFound.add(s);
            i++;
        }
        assertTrue("Number of pages should be " + pageNames.size() + "but found " + pageFound.size(),
                pageNames.size() == pageFound.size());

    }
}