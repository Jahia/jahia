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
package org.jahia.test.services.content.files;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.JahiaTestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * HTTP-based test for the /files servlet, including caching.
 * 
 * @author Sergiy Shyrkov
 */
public class FileServletTest extends JahiaTestCase {

    private final static String FOLDER_NAME = "fileServletTest";

    private final static String FOLDER_PATH;

    private static final String LARGE_TXT = "large.txt";

    private static final String LARGE_TXT_URL;

    private static Logger logger = LoggerFactory.getLogger(FileServletTest.class);

    private final static String PASSWORD = "password";

    private static final String PROTECTED_LARGE_TXT = "protected-large.txt";

    private static final String PROTECTED_LARGE_TXT_URL;

    private static final String PROTECTED_SMALL_TXT = "protected-small.txt";

    private static final String PROTECTED_SMALL_TXT_URL;

    private static JCRPublicationService publicationService;

    private static final String SITE_PATH = JahiaSitesService.SITES_JCR_PATH + "/" + JahiaSitesService.SYSTEM_SITE_KEY;

    private static final String SMALL_TXT = "small.txt";

    private static final String SMALL_TXT_URL;

    private static final String SWITCH_LARGE_TXT = "switch-large.txt";

    private static final String SWITCH_LARGE_TXT_URL;

    private static final String SWITCH_SMALL_TXT = "switch-small.txt";

    private static final String SWITCH_SMALL_TXT_URL;

    private final static String USERNAME = "fileServletTestUser";
    static {
        FOLDER_PATH = SITE_PATH + "/files/" + FOLDER_NAME;
        SMALL_TXT_URL = "/files/live" + FOLDER_PATH + "/" + SMALL_TXT;
        LARGE_TXT_URL = "/files/live" + FOLDER_PATH + "/" + LARGE_TXT;
        PROTECTED_SMALL_TXT_URL = "/files/live" + FOLDER_PATH + "/" + PROTECTED_SMALL_TXT;
        PROTECTED_LARGE_TXT_URL = "/files/live" + FOLDER_PATH + "/" + PROTECTED_LARGE_TXT;
        SWITCH_SMALL_TXT_URL = "/files/live" + FOLDER_PATH + "/" + SWITCH_SMALL_TXT;
        SWITCH_LARGE_TXT_URL = "/files/live" + FOLDER_PATH + "/" + SWITCH_LARGE_TXT;
    }

    private static void deleteTestFolder(JCRSessionWrapper session) throws PathNotFoundException, RepositoryException {
        if (session.nodeExists(FOLDER_PATH)) {
            JCRNodeWrapper folder = session.getNode(FOLDER_PATH);
            String uuid = folder.getParent().getIdentifier();
            folder.remove();
            session.save();

            publicationService.publishByMainId(uuid, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true,
                    null);
        }
    }

    private static InputStream generateText(char ch, int length) {
        return IOUtils.toInputStream(StringUtils.leftPad("", length, ch));
    }

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        // create site
        publicationService = ServicesRegistry.getInstance().getJCRPublicationService();
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        // create user
        Properties properties = new Properties();
        properties.setProperty("j:firstName", "John");
        properties.setProperty("j:lastName", "Doe");
        ServicesRegistry.getInstance().getJahiaUserManagerService().createUser(USERNAME, PASSWORD, properties, session);
        session.save();

        deleteTestFolder(session);

        // upload files
        JCRNodeWrapper filesNode = session.getNode(StringUtils.substringBeforeLast(FOLDER_PATH, "/"));
        JCRNodeWrapper testFolder = filesNode.addNode(FOLDER_NAME, "jnt:folder");
        testFolder.uploadFile(SMALL_TXT, generateText('a', 1000), "text/plain");
        testFolder.uploadFile(LARGE_TXT, generateText('b', 64 * 1024 + 10), "text/plain");
        JCRNodeWrapper protectedFile = testFolder.uploadFile(PROTECTED_SMALL_TXT, generateText('a', 1000),
                "text/plain");
        protectedFile.denyRoles("u:guest", ImmutableSet.of("reader"));
        protectedFile = testFolder.uploadFile(PROTECTED_LARGE_TXT, generateText('b', 64 * 1024 + 10), "text/plain");
        protectedFile.denyRoles("u:guest", ImmutableSet.of("reader"));
        JCRNodeWrapper aclTestFile = testFolder.uploadFile(SWITCH_SMALL_TXT, generateText('a', 1000), "text/plain");
        aclTestFile.grantRoles("u:" + USERNAME, ImmutableSet.of("reader"));
        aclTestFile = testFolder.uploadFile(SWITCH_LARGE_TXT, generateText('b', 64 * 1024 + 10), "text/plain");
        aclTestFile.grantRoles("u:" + USERNAME, ImmutableSet.of("reader"));
        session.save();

        // publish test folder
        publicationService.publishByMainId(filesNode.getIdentifier(), Constants.EDIT_WORKSPACE,
                Constants.LIVE_WORKSPACE, null, true, null);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        try {
            deleteTestFolder(session);
        } catch (Exception ex) {
            logger.warn("Exception during test oneTimeTearDown", ex);
        }

        JahiaUserManagerService userManagerService = ServicesRegistry.getInstance().getJahiaUserManagerService();
        userManagerService.deleteUser(userManagerService.lookupUser(USERNAME).getPath(), session);
        session.save();
    }

    private void changeRole(String nodePath, boolean doGrantRole) throws RepositoryException {
        // revoke reader role from guest
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRNodeWrapper file = session.getNode(nodePath);
        if (doGrantRole) {
            file.grantRoles("u:guest", ImmutableSet.of("reader"));
        } else {
            file.denyRoles("u:guest", ImmutableSet.of("reader"));
        }
        session.save();
        publicationService.publishByMainId(file.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE,
                null, true, null);
    }

    @After
    public void tearDown() throws Exception {
        logout();
    }

    @Test
    public void testAclSwitch() throws Exception {
        testCached(SWITCH_SMALL_TXT_URL, "aaaaa");
        testCached(SWITCH_LARGE_TXT_URL, "bbbbb");

        // revoke reader role from guest
        changeRole(FOLDER_PATH + '/' + SWITCH_SMALL_TXT, false);
        changeRole(FOLDER_PATH + '/' + SWITCH_LARGE_TXT, false);

        // should not be accessible as guest
        getAsText(SWITCH_SMALL_TXT_URL, HttpServletResponse.SC_NOT_FOUND);
        getAsText(SWITCH_LARGE_TXT_URL, HttpServletResponse.SC_NOT_FOUND);

        login();

        // should be accessible with a logged in user
        testCached(SWITCH_SMALL_TXT_URL, "aaaaa");
        testCached(SWITCH_LARGE_TXT_URL, "bbbbb");
        
        logout();

        // should not be accessible as guest
        getAsText(SWITCH_SMALL_TXT_URL, HttpServletResponse.SC_NOT_FOUND);
        getAsText(SWITCH_LARGE_TXT_URL, HttpServletResponse.SC_NOT_FOUND);

        // grant reader role to guest
        changeRole(FOLDER_PATH + '/' + SWITCH_SMALL_TXT, true);
        changeRole(FOLDER_PATH + '/' + SWITCH_LARGE_TXT, true);

        // should have access
        testCached(SWITCH_SMALL_TXT_URL, "aaaaa");
        testCached(SWITCH_LARGE_TXT_URL, "bbbbb");
    }

    protected void testCached(String url, String testContent) throws Exception {
        Map<String, String> responseHeaders = new HashMap<>();
        String content = getAsText(url, null, HttpServletResponse.SC_OK, responseHeaders);
        assertTrue("Body does not contain the file content", content.contains(testContent));
        String eTag = responseHeaders.get("ETag");
        String lastModified = responseHeaders.get("Last-Modified");

        assertNotNull("ETag response header is not found", eTag);
        assertNotNull("Last-Modified response header is not found", lastModified);

        // test request for cached file by Last-Modified header
        getAsText(url, Collections.singletonMap("If-Modified-Since", lastModified), HttpServletResponse.SC_NOT_MODIFIED,
                null);

        // test request for cached file by ETag header
        getAsText(url, Collections.singletonMap("If-None-Match", eTag), HttpServletResponse.SC_NOT_MODIFIED, null);
    }

    @Test
    public void testGuestAccess() throws Exception {
        String content = getAsText(SMALL_TXT_URL);
        assertTrue(content.contains("aaaaa"));
        content = getAsText(LARGE_TXT_URL);
        assertTrue(content.contains("bbbbb"));
    }

    @Test
    public void testGuestAccessProtected() throws Exception {
        // should not be accessible as guest
        getAsText(PROTECTED_SMALL_TXT_URL, HttpServletResponse.SC_NOT_FOUND);
        getAsText(PROTECTED_LARGE_TXT_URL, HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testGuestCached() throws Exception {
        testCached(SMALL_TXT_URL, "aaaaa");
        testCached(LARGE_TXT_URL, "bbbbb");
    }

    @Test
    public void testUserProtectedAccess() throws Exception {
        // login with the user
        login();

        assertTrue(getAsText(PROTECTED_SMALL_TXT_URL).contains("aaaaa"));
        assertTrue(getAsText(PROTECTED_LARGE_TXT_URL).contains("bbbbb"));
    }

    private void login() {
        login(USERNAME, PASSWORD);
    }

    @Test
    public void testUserProtectedCached() throws Exception {
        login();

        testCached(PROTECTED_SMALL_TXT_URL, "aaaaa");
        testCached(PROTECTED_LARGE_TXT_URL, "bbbbb");
    }
}
