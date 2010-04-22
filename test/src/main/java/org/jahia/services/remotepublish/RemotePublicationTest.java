/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.json.JSONObject;

import javax.jcr.observation.Event;
import javax.jcr.observation.EventJournal;
import java.net.URL;

/**
 * Unit test for publish / unpublish using JCR
 * - tests publish / unpublish of pages, container lists, containers
 * - with different language settings (all, none, one, two languages)
 * - with using user not having rights
 * - publication with automatically publishing parent
 *
 * @author Benjamin Papez
 *
 *
 * TestA - standard nodes :
 *
 * 1/ create new node "nodeA" - publish it - check in live if nodeA is here
 * 2/ modify nodeA - publish it - check in live if nodeA is modified
 * 3/ unpublish nodeA - check in live if nodeA has disappeared
 * 4/ publish nodeA - check in live if nodeA is here
 * 5/ rename nodeA, in the same location - publish it - check that nodeA was properly renamed.
 * 6/ move nodeA in same list, before a node - publish it - check if nodeA is correctly moved (and removed from original place)
 * 7/ move nodeA in another list, before a node - publish it - check if nodeA is correctly moved (and removed from original place)
 * 8/ move and modify nodeA in another list, before a node - publish it - check if nodeA is correctly modified and moved (and removed from original place)
 * 9/ move nodeA in another list, before a node twice - publish it - check if nodeA is correctly moved (and removed from original place)
 * 10/ delete nodeA - publish parent - check in live the node is deleted
 *
 * TestB - shareable nodes - same scenarios
 * TestC - pages node with sub pages - same scenarios. sub pages should not be published.
 * TestD - modify content in live AND edit and merge with edit workspace.
 * TestE - test with shareable nodes published in different locations in different languages.
 * TestF - concurrent modifications (especially moves) in both workspaces.
 *
 */
public class RemotePublicationTest extends TestCase {
    private static Logger logger = Logger.getLogger(RemotePublicationTest.class);
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

    public void testRemotePublishAction() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(Constants.EDIT_WORKSPACE,
                        LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        final JCRNodeWrapper node = session.getNode("/sites/jcrRPTest/home");
        final JCRNodeWrapper source = node.addNode("source", "jnt:page");
        final JCRNodeWrapper target = node.addNode("target", "jnt:page");

        final JCRNodeWrapper rp = node.addNode("rp", "jnt:remotePublication");
        final String baseurl = "http://localhost:8080" + Jahia.getContextPath() + "/cms/render/default/en";

        rp.setProperty("remoteUrl", baseurl);
        rp.setProperty("node", source);
        rp.setProperty("remotePath", target.getPath());
        rp.setProperty("remoteUser", "root");
        rp.setProperty("remotePassword", "root1234");

        session.save();

        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);

        final URL url = new URL(baseurl + rp.getPath() + ".remotepublish.do");

        Credentials defaultcreds = new UsernamePasswordCredentials("root", "root1234");
        client.getState().setCredentials(new AuthScope(url.getHost(), url.getPort(), AuthScope.ANY_REALM), defaultcreds);

        client.getHostConfiguration().setHost(url.getHost(), url.getPort(), url.getProtocol());

        PostMethod remotePublish = new PostMethod(url.toExternalForm());
        remotePublish.addRequestHeader(new Header("accept", "application/json"));

        client.executeMethod(remotePublish);
        assertEquals("Bad result code", 200, remotePublish.getStatusCode());

        JSONObject response = new JSONObject(remotePublish.getResponseBodyAsString());
        remotePublish.releaseConnection();

    }


    public void testLogGeneration() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(Constants.EDIT_WORKSPACE,
                        LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        final JCRNodeWrapper node = session.getNode("/sites/jcrRPTest/home");
        final JCRNodeWrapper page1 = node.addNode("page1", "jnt:page");
        final JCRNodeWrapper page2 = node.addNode("page2", "jnt:page");
        final JCRNodeWrapper page3 = node.addNode("page3", "jnt:page");
        session.save();

        long now = System.currentTimeMillis();

        JCRPublicationService.getInstance().publish("/sites/jcrRPTest/home", Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, true);

        JCRSessionWrapper liveSession = JCRSessionFactory.getInstance()
                .getCurrentUserSession(Constants.LIVE_WORKSPACE,
                        LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        EventJournal journal = liveSession.getProviderSession(node.getProvider()).getWorkspace().getObservationManager().getEventJournal();
        journal.skipTo(now);
        while (journal.hasNext()) {
            Event event = journal.nextEvent();
            System.out.println("------"+event.getPath());
            System.out.println(event.getType());
            System.out.println(event.getUserData());
        }

    }

}