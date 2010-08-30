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
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.modules.remotepublish.*;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.observation.Event;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Unit test for remote publishing
 */
public class LogGenerationTest extends TestCase {
    private static Logger logger = Logger.getLogger(LogGenerationTest.class);
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
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }


    public void testAddNodeAndSubnodeLogGeneration() throws Exception {
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
        session.save();

        Calendar now = new GregorianCalendar();

        JCRPublicationService.getInstance()
                .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true);

        JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        source = liveSession.getNodeByUUID(source.getIdentifier());

        File tmp = File.createTempFile("remote", ".log.gz");
        RemotePublicationService.getInstance().generateLog(source, now, new FileOutputStream(tmp));

        Set<String> added = new HashSet<String>();
        Set<String> removed = new HashSet<String>();
        Map<String, Object> addedProperties = new HashMap<String, Object>();
        Map<String, Object> updatedProperties = new HashMap<String, Object>();
        Set<String> removedProperties = new HashSet<String>();
        LogBundleEnd log =
                parseResults(tmp, source, added, removed, addedProperties, updatedProperties, removedProperties);

        assertTrue("New page not in log", added.contains("/sites/jcrRPTest/home/source/page1"));
        assertTrue("New page not in log", added.contains("/sites/jcrRPTest/home/source/page2"));
        assertTrue("New page not in log", added.contains("/sites/jcrRPTest/home/source/page3"));

        now = log.getDate();

        session.checkout(page3);
        page3.setProperty("jcr:title", "title_changed");
        session.save();

        JCRPublicationService.getInstance()
                .publish(source.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true);

        RemotePublicationService.getInstance().generateLog(source, now, new FileOutputStream(tmp));

        added.clear();
        parseResults(tmp, source, added, removed, addedProperties, updatedProperties, removedProperties);
        assertEquals("Bad number of add node events", 1, added.size());
        assertTrue(added.contains("/sites/jcrRPTest/home/source/page3/j:translation"));
    }

    public void testRemoveNodeLogGeneration() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        final JCRNodeWrapper node = session.getNode("/sites/jcrRPTest/home");
        JCRNodeWrapper source = node.addNode("source", "jnt:page");
        source.setProperty("jcr:title", "Source");
        JCRNodeWrapper page1 = source.addNode("page1", "jnt:page");
        page1.setProperty("jcr:title", "Page1");
        session.save();


        JCRPublicationService.getInstance()
                .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true);

        Calendar now = new GregorianCalendar();
        session.checkout(source);
        session.checkout(page1);
        page1.remove();
        session.save();

        JCRPublicationService.getInstance()
                .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true);

        JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        source = liveSession.getNodeByUUID(source.getIdentifier());

        File tmp = File.createTempFile("remote", ".log.gz");
        RemotePublicationService.getInstance().generateLog(source, now, new FileOutputStream(tmp));

        Set<String> added = new HashSet<String>();
        Set<String> removed = new HashSet<String>();
        Map<String, Object> addedProperties = new HashMap<String, Object>();
        Map<String, Object> updatedProperties = new HashMap<String, Object>();
        Set<String> removedProperties = new HashSet<String>();
        parseResults(tmp, source, added, removed, addedProperties, updatedProperties, removedProperties);

        assertTrue("Removed page not in log", removed.contains("/sites/jcrRPTest/home/source/page1"));
    }


    public void testAddNewPropertyAndUpdatePropertyLogGeneration() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));
        JahiaUser user1 = JCRUserManagerProvider.getInstance().createUser("source-user", "password", new Properties());

        try {
            final JCRNodeWrapper node = session.getNode("/sites/jcrRPTest/home");
            JCRNodeWrapper source = node.addNode("source-user", "jnt:user");
            session.save();

            JCRPublicationService.getInstance()
                    .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true);

            Calendar now = new GregorianCalendar();
            session.checkout(source);
            source.setProperty("j:firstName", "testAddNewPropertyAndUpdatePropertyLogGeneration");
            session.save();

            JCRPublicationService.getInstance()
                    .publish(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true);

            JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE,
                    LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

            JCRNodeWrapper liveSource = liveSession.getNodeByUUID(source.getIdentifier());

            File tmp = File.createTempFile("remote", ".log.gz");
            RemotePublicationService.getInstance().generateLog(liveSource, now, new FileOutputStream(tmp));

            Set<String> addedNodes = new HashSet<String>();
            Set<String> removedNodes = new HashSet<String>();
            Map<String, Object> addedProperties = new HashMap<String, Object>();
            Map<String, Object> updatedProperties = new HashMap<String, Object>();
            Set<String> removedProperties = new HashSet<String>();
            parseResults(tmp, liveSource, addedNodes, removedNodes, addedProperties, updatedProperties,
                    removedProperties);

            assertTrue("New property j:firstname not in log",
                    addedProperties.containsKey("/sites/jcrRPTest/home/source-user/j:firstName"));
            assertEquals("New property j:firstname value must be testAddNewPropertyAndUpdatePropertyLogGeneration",
                    "testAddNewPropertyAndUpdatePropertyLogGeneration",
                    addedProperties.get("/sites/jcrRPTest/home/source-user/j:firstName"));
            assertTrue("Existing property should have been updated",
                    updatedProperties.containsKey("/sites/jcrRPTest/home/source-user/j:lastPublished"));
        } finally {
            ServicesRegistry.getInstance().getJahiaUserManagerService().deleteUser(user1);
        }
    }


    public void testRemovePropertyLogGeneration() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));
        JahiaUser user1 = JCRUserManagerProvider.getInstance().createUser("source-user", "password", new Properties());

        try {
            final JCRNodeWrapper node = session.getNode("/sites/jcrRPTest/home");
            JCRNodeWrapper source = node.addNode("source-user", "jnt:user");
            session.save();

            JCRPublicationService.getInstance().publish(node.getIdentifier(),
                    Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true);

            session.checkout(source);
            source.setProperty("j:firstName", "testAddNewPropertyAndUpdatePropertyLogGeneration");
            session.save();

            JCRPublicationService.getInstance().publish(node.getIdentifier(),
                    Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true);

            Calendar now = new GregorianCalendar();

            session.checkout(source);
            source.getProperty("j:firstName").remove();
            session.save();

            JCRPublicationService.getInstance().publish(node.getIdentifier(),
                    Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true);

            JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(
                    Constants.LIVE_WORKSPACE,
                    LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

            JCRNodeWrapper liveSource = liveSession.getNodeByUUID(source.getIdentifier());

            File tmp = File.createTempFile("remote", ".log.gz");
            RemotePublicationService.getInstance().generateLog(liveSource, now,
                    new FileOutputStream(tmp));

            Set<String> addedNodes = new HashSet<String>();
            Set<String> removedNodes = new HashSet<String>();
            Map<String, Object> addedProperties = new HashMap<String, Object>();
            Map<String, Object> updatedProperties = new HashMap<String, Object>();
            Set<String> removedProperties = new HashSet<String>();
            parseResults(tmp, liveSource, addedNodes, removedNodes, addedProperties,
                    updatedProperties, removedProperties);

            assertTrue("Removed property j:firstname not in log",
                    removedProperties.contains("/sites/jcrRPTest/home/source-user/j:firstName"));
        } finally {
            ServicesRegistry.getInstance().getJahiaUserManagerService().deleteUser(user1);
        }        
    }

    public void testBinaryNode() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        JCRNodeWrapper source = session.getNode("/sites/jcrRPTest/files");

        JCRPublicationService.getInstance()
                .publish(source.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true);

        Calendar now = new GregorianCalendar();

        String value = "This is a test";
        String mimeType = "text/plain";

        InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

        String name = "test.txt";
        JCRNodeWrapper testFileNode = source.uploadFile(name, is, mimeType);

        session.save();

        JCRPublicationService.getInstance()
                .publish(testFileNode.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null,
                        true);

        JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        source = liveSession.getNodeByUUID(source.getIdentifier());

        File tmp = File.createTempFile("remote", ".log.gz");
        RemotePublicationService.getInstance().generateLog(source, now, new FileOutputStream(tmp));

        Set<String> added = new HashSet<String>();
        Set<String> removed = new HashSet<String>();
        Map<String, Object> addedProperties = new HashMap<String, Object>();
        Map<String, Object> updatedProperties = new HashMap<String, Object>();
        Set<String> removedProperties = new HashSet<String>();
        parseResults(tmp, source, added, removed, addedProperties, updatedProperties, removedProperties);
        assertTrue("Invalid type for binary property",
                addedProperties.get("/sites/jcrRPTest/files/test.txt/jcr:content/jcr:data") instanceof byte[]);
        byte[] bytes = (byte[]) addedProperties.get("/sites/jcrRPTest/files/test.txt/jcr:content/jcr:data");
        assertEquals("Binary content invalid", value, new String(bytes, "UTF-8"));
    }

    private LogBundleEnd parseResults(File tmp, JCRNodeWrapper source, Set<String> addedNodes, Set<String> removedNodes,
                                      Map<String, Object> addedProperties, Map<String, Object> updatedProperties,
                                      Set<String> removedProperties) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(tmp)));
        LogBundle log = (LogBundle) ois.readObject();
        assertEquals("Source path not the same", source.getPath(), log.getSourcePath());
        assertEquals("Source path not the same", source.getIdentifier(), log.getSourceUuid());
        Object o = ois.readObject();
        while (o instanceof LogEntries) {
            while ((o = ois.readObject()) instanceof LogEntry) {
                LogEntry entry = (LogEntry) o;
                String path = entry.getPath();
                switch (entry.getEventType()) {
                    case Event.NODE_ADDED: {
                        System.out.println("node added " + path);
                        String data = (String) ois.readObject();
                        List<String> list = (List<String>) ois.readObject();
                        addedNodes.add(path);
                        break;
                    }
                    case Event.NODE_REMOVED: {
                        System.out.println("node removed " + path);
                        removedNodes.add(path);
                        break;
                    }

                    case Event.PROPERTY_ADDED: {
                        Object propertyValue = ois.readObject();
                        System.out.println("property added " + path + " with value " + propertyValue);
                        addedProperties.put(path, propertyValue);
                        break;
                    }

                    case Event.PROPERTY_CHANGED: {
                        Object propertyValue = ois.readObject();
                        System.out.println("property changed " + path + " with value " + propertyValue);
                        updatedProperties.put(path, propertyValue);
                        break;
                    }

                    case Event.PROPERTY_REMOVED: {
                        removedProperties.add(path);
                        break;
                    }
                }
            }
        }

        return (LogBundleEnd) o;
    }


}