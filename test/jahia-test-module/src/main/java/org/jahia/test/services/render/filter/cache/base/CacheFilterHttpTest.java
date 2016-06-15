/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.render.filter.cache.base;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.render.filter.AggregateFilter;
import org.jahia.services.render.filter.cache.AggregateCacheFilter;
import org.jahia.services.render.filter.cache.CacheFilter;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.jahia.services.render.filter.cache.ModuleGeneratorQueue;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.jahia.test.services.render.filter.cache.CacheFilterCheckFilter;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Base Cache filter test for both old and new implementation of CacheFilter
 * provide unit tests that are based on HTTP calls on current server to serve pages and tests different states
 */
public class CacheFilterHttpTest extends JahiaTestCase {

    public static final Logger logger = LoggerFactory.getLogger(CacheFilterTest.class);
    public static final String TESTSITE_NAME = "cachetest";
    public static final String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    private static boolean cacheFilterDisabled;
    private static boolean aggregateFilterDisabled;
    private static boolean aggregateCacheFilterDisabled;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {

        try {

            JahiaSite site = TestHelper.createSite(TESTSITE_NAME, "localhost", "templates-web-blue");
            assertNotNull(site);
            JCRStoreService jcrService = ServicesRegistry.getInstance().getJCRStoreService();
            JCRSessionWrapper session = jcrService.getSessionFactory().getCurrentUserSession();

            ServicesRegistry.getInstance().getJahiaTemplateManagerService().installModule("jahia-test-module", SITECONTENT_ROOT_NODE, session.getUser().getName());

            final JahiaUserManagerService userManagerProvider = JahiaUserManagerService.getInstance();
            final JCRUserNode userAB = userManagerProvider.createUser("userAB", "password", new Properties(), session);
            final JCRUserNode userAC = userManagerProvider.createUser("userAC", "password", new Properties(), session);
            final JCRUserNode userBC = userManagerProvider.createUser("userBC", "password", new Properties(), session);
            userManagerProvider.createUser("user1", "password", new Properties(), session);
            userManagerProvider.createUser("user2", "password", new Properties(), session);
            userManagerProvider.createUser("user3", "password", new Properties(), session);

            // Create three groups
            final JahiaGroupManagerService groupManagerProvider = JahiaGroupManagerService.getInstance();
            final JCRGroupNode groupA = groupManagerProvider.createGroup(site.getSiteKey(), "groupA", new Properties(), false, session);
            final JCRGroupNode groupB = groupManagerProvider.createGroup(site.getSiteKey(), "groupB", new Properties(), false, session);
            final JCRGroupNode groupC = groupManagerProvider.createGroup(site.getSiteKey(), "groupC", new Properties(), false, session);
            // Associate each user to two group
            groupA.addMember(userAB);
            groupA.addMember(userAC);
            groupB.addMember(userAB);
            groupB.addMember(userBC);
            groupC.addMember(userAC);
            groupC.addMember(userBC);

            InputStream importStream = CacheFilterHttpTest.class.getClassLoader().getResourceAsStream("imports/cachetest-site.xml");
            session.importXML(SITECONTENT_ROOT_NODE, importStream, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
            importStream.close();
            session.save();
            JCRNodeWrapper siteNode = session.getNode(SITECONTENT_ROOT_NODE);
            JCRPublicationService.getInstance().publishByMainId(siteNode.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);

            // store filters config
            cacheFilterDisabled = ((CacheFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.cache.CacheFilter")).isDisabled();
            aggregateFilterDisabled = ((AggregateFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.AggregateFilter")).isDisabled();
            aggregateCacheFilterDisabled = ((AggregateCacheFilter) SpringContextSingleton.getBean("cacheFilter")).isDisabled();

            //enable test filters
            getCheckFilter("CacheHttpTestRenderFilter1").setDisabled(false);
            getCheckFilter("CacheHttpTestRenderFilter2").setDisabled(false);
        } catch (Exception e) {
            logger.warn("Exception during test setUp", e);
        }
    }


    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
            final JahiaUserManagerService userManagerProvider = JahiaUserManagerService.getInstance();
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            userManagerProvider.deleteUser(userManagerProvider.lookupUser("userAB").getPath(), session);
            userManagerProvider.deleteUser(userManagerProvider.lookupUser("userAC").getPath(), session);
            userManagerProvider.deleteUser(userManagerProvider.lookupUser("userBC").getPath(), session);
            userManagerProvider.deleteUser(userManagerProvider.lookupUser("user1").getPath(), session);
            userManagerProvider.deleteUser(userManagerProvider.lookupUser("user2").getPath(), session);
            userManagerProvider.deleteUser(userManagerProvider.lookupUser("user3").getPath(), session);
            session.save();

            // restore filters config
            ((CacheFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.cache.CacheFilter")).setDisabled(cacheFilterDisabled);
            ((AggregateFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.AggregateFilter")).setDisabled(aggregateFilterDisabled);
            ((AggregateCacheFilter) SpringContextSingleton.getBean("cacheFilter")).setDisabled(aggregateCacheFilterDisabled);

            //enable test filters
            getCheckFilter("CacheHttpTestRenderFilter1").setDisabled(true);
            getCheckFilter("CacheHttpTestRenderFilter2").setDisabled(true);
            getCheckFilter("CacheHttpTestRenderFilter1").clear();
            getCheckFilter("CacheHttpTestRenderFilter2").clear();
        } catch (Exception e) {
            logger.warn("Exception during test tearDown", e);
        }
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Before
    public void setUp() {
        ModuleCacheProvider cacheProvider = ModuleCacheProvider.getInstance();
        Ehcache cache = cacheProvider.getCache();
        Ehcache depCache = cacheProvider.getDependenciesCache();
        cache.flush();
        cache.removeAll();
        depCache.flush();
        depCache.removeAll();
        cacheProvider.flushNonCacheableFragments();
        getCheckFilter("CacheHttpTestRenderFilter1").clear();
        getCheckFilter("CacheHttpTestRenderFilter2").clear();
        cache.getCacheConfiguration().setEternal(true);
        depCache.getCacheConfiguration().setEternal(true);
    }

    @After
    public void tearDown() {
        ModuleCacheProvider cacheProvider = ModuleCacheProvider.getInstance();
        Ehcache cache = cacheProvider.getCache();
        Ehcache depCache = cacheProvider.getDependenciesCache();
        cache.getCacheConfiguration().setEternal(false);
        depCache.getCacheConfiguration().setEternal(false);
    }

    @Test
    public void testACLs() throws Exception {
        testACLs(SITECONTENT_ROOT_NODE + "/home/acl1");
        testACLs(SITECONTENT_ROOT_NODE + "/home/acl2");
    }

    @Test
    public void testReferencesFlush() throws Exception {

        URL url = getUrl(SITECONTENT_ROOT_NODE + "/home/references");
        getContent(url, "root", "root1234", null);

        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession("live", new Locale("en"));
            JCRNodeWrapper n = session.getNode(SITECONTENT_ROOT_NODE + "/home/references/maincontent/simple-text");
            try {
                n.setProperty("text", "text content updated");
                session.save();
                String newvalue = getContent(url, "root", "root1234", "testReferencesFlush1");
                Matcher m = Pattern.compile("text content updated").matcher(newvalue);
                assertTrue("Value has not been updated", m.find());
                assertTrue("References have not been flushed", m.find());
                assertTrue("References have not been flushed", m.find());
            } finally {
                n.setProperty("text", "text content");
                session.save();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRandomFlush() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession("live", new Locale("en"));
        Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:page] as p where isdescendantnode(p,'" + SITECONTENT_ROOT_NODE + "/home')", Query.JCR_SQL2);
        List<String> paths = new ArrayList<String>();
        NodeIterator nodes = q.execute().getNodes();
        Set<String> skipped = new HashSet<>(Arrays.asList("long", "error", "user-per-content-test"));
        while (nodes.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) nodes.next();
            if (!skipped.contains(next.getName())) {
                paths.add(next.getPath());
            }
        }
        List<String> users = Arrays.asList("userAB", "userAC", "userBC");
        Map<String, String> m = new HashMap<String, String>();
        for (String user : users) {
            for (String path : paths) {
                m.put(user + path, getContent(getUrl(path), user, "password", null));
            }
        }

        final Cache cache = ModuleCacheProvider.getInstance().getCache();
        List<String> keysBefore = cache.getKeys();

        Map<String, Object> cacheCopy = new HashMap<String, Object>();
        for (String s : keysBefore) {
            final Element element = cache.get(s);
            if (element != null) {
                cacheCopy.put(s, element.getObjectValue());
            }
        }

        for (int j = 0; j < 10; j++) {
            System.out.println("flush " + j);
            List<String> toFlush = randomizeFlush(keysBefore, 10);
            for (String user : users) {
                for (String path : paths) {
                    System.out.println(user + " - " + path);
                    if (!m.get(user + path).equals(getContent(getUrl(path), user, "password", null))) {
                        fail("Different content for " + user + " , " + path + " when flushing : " + toFlush);
                    }
                    checkCacheContent(cache, cacheCopy, toFlush);
                }
            }
            List<String> keysAfter = cache.getKeys();
            Collections.sort(keysBefore);
            Collections.sort(keysAfter);
            if (!keysBefore.equals(keysAfter)) {
                List<String> onlyInBefore = new ArrayList<String>(keysBefore);
                onlyInBefore.removeAll(keysAfter);
                List<String> onlyInAfter = new ArrayList<String>(keysAfter);
                onlyInAfter.removeAll(keysBefore);
                fail("Key sets are not the same before and after flushing : " + toFlush + "\n Before flushs :" + onlyInBefore + " ,\n After flush : " + onlyInAfter);
            }
            checkCacheContent(cache, cacheCopy, toFlush);
        }
    }

    @Test
    public void testACLsUserPerContent() throws Exception {
        // test for https://jira.jahia.org/browse/QA-7383
        String path = SITECONTENT_ROOT_NODE + "/home/user-per-content-test";

        String contentForUser1 = getContent(getUrl(path), "user1", "password", "testACLs11");
        assertTrue("user1 cannot see content, she should see", contentForUser1.contains("content for user1"));
        assertFalse("user1 sees content, she should not see", contentForUser1.contains("content for user2"));
        assertFalse("user1 sees content, she should not see", contentForUser1.contains("content for user3"));

        String contentForUser2 = getContent(getUrl(path), "user2", "password", "testACLs12");
        assertTrue("user2 cannot see content, she should see", contentForUser2.contains("content for user2"));
        assertFalse("user2 sees content, she should not see", contentForUser2.contains("content for user1"));
        assertFalse("user2 sees content, she should not see", contentForUser2.contains("content for user3"));

        String contentForUser3 = getContent(getUrl(path), "user3", "password", "testACLs13");
        assertTrue("user3 cannot see content, she should see", contentForUser3.contains("content for user3"));
        assertFalse("user3 sees content, she should not see", contentForUser3.contains("content for user1"));
        assertFalse("user3 sees content, she should not see", contentForUser3.contains("content for user2"));
    }

    public void testMaxConcurrent(int generationTime) throws Exception{
        long previousModuleGenerationWaitTime = ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).getModuleGenerationWaitTime();
        int previousMaxModulesToGenerateInParallel = ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).getMaxModulesToGenerateInParallel();

        try {

            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setModuleGenerationWaitTime(generationTime);
            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setMaxModulesToGenerateInParallel(1);

            HttpThread t1 = new HttpThread(getUrl(SITECONTENT_ROOT_NODE + "/home/long"), "root", "root1234", "testMaxConcurrent1");
            t1.start();
            Thread.sleep(500);

            HttpThread t2 = new HttpThread(getUrl(SITECONTENT_ROOT_NODE + "/home"), "root", "root1234", "testMaxConcurrent2");
            t2.start();
            t2.join();
            t1.join();

            assertEquals("Incorrect response code for first thread", 200, t1.resultCode);
            assertEquals("Incorrect response code for second thread", 500, t2.resultCode);

            assertTrue(getContent(getUrl(SITECONTENT_ROOT_NODE + "/home"), "root", "root1234", "testMaxConcurrent3").contains("<title>Home</title>"));
        } finally {
            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setModuleGenerationWaitTime(previousModuleGenerationWaitTime);
            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setMaxModulesToGenerateInParallel(previousMaxModulesToGenerateInParallel);
        }
    }

    public void testACLs(String path) throws Exception {

        String guest = getContent(getUrl(path), null, null, null);
        String root = getContent(getUrl(path), "root", "root1234", null);
        String userAB = getContent(getUrl(path), "userAB", "password", null);
        String userBC = getContent(getUrl(path), "userBC", "password", null);
        String userAC = getContent(getUrl(path), "userAC", "password", null);

        checkAcl(guest, new boolean[]{false, false, false, false, false, false, false, false});
        checkAcl(root, new boolean[]{true, true, true, true, true, true, true, true});
        checkAcl(userAB, new boolean[]{false, true, true, false, false, true, true, false});
        checkAcl(userBC, new boolean[]{false, true, false, true, false, false, true, true});
        checkAcl(userAC, new boolean[]{false, true, false, false, true, true, false, true});

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession("live", new Locale("en"));
        JCRNodeWrapper n = session.getNode(path + "/maincontent/simple-text-A");

        try {

            n.revokeRolesForPrincipal("g:groupA");
            n.grantRoles("g:groupB", new HashSet<String>(Arrays.asList("reader")));
            session.save();

            String guest2 = getContent(getUrl(path), null, null, "testACLs1");
            String root2 = getContent(getUrl(path), "root", "root1234", "testACLs2");
            String userAB2 = getContent(getUrl(path), "userAB", "password", "testACLs3");
            String userBC2 = getContent(getUrl(path), "userBC", "password", "testACLs4");
            String userAC2 = getContent(getUrl(path), "userAC", "password", "testACLs5");

            checkAcl(guest2, new boolean[]{false, false, false, false, false, false, false, false});
            checkAcl(root2, new boolean[]{true, true, true, true, true, true, true, true});
            checkAcl(userAB2, new boolean[]{false, true, true, false, false, true, true, false});
            checkAcl(userBC2, new boolean[]{false, true, false, true, false, true, true, true});
            checkAcl(userAC2, new boolean[]{false, true, false, false, true, false, false, true});
        } finally {
            n.revokeRolesForPrincipal("g:groupB");
            n.grantRoles("g:groupA", new HashSet<String>(Arrays.asList("reader")));
            session.save();
        }

        assertEquals("Content served is not the same", guest, getContent(getUrl(path), null, null, "testACLs6"));
        assertEquals("Content served is not the same", root, getContent(getUrl(path), "root", "root1234", "testACLs7"));
        assertEquals("Content served is not the same", userAB, getContent(getUrl(path), "userAB", "password", "testACLs8"));
        assertEquals("Content served is not the same", userBC, getContent(getUrl(path), "userBC", "password", "testACLs9"));
        assertEquals("Content served is not the same", userAC, getContent(getUrl(path), "userAC", "password", "testACLs10"));
    }

    public void checkAcl(String content, boolean[] b) {
        assertEquals(b[0], content.contains("visible for root"));
        assertEquals(b[1], content.contains("visible for users only"));
        assertEquals(b[2], content.contains("visible for userAB"));
        assertEquals(b[3], content.contains("visible for userBC"));
        assertEquals(b[4], content.contains("visible for userAC"));
        assertEquals(b[5], content.contains("visible for groupA"));
        assertEquals(b[6], content.contains("visible for groupB"));
        assertEquals(b[7], content.contains("visible for groupC"));
    }

    public static CacheFilterCheckFilter getCheckFilter(String id) {
        return (CacheFilterCheckFilter) SpringContextSingleton.getBean(id);
    }


    @SuppressWarnings("unchecked")
    public void checkCacheContent(Cache cache, Map<String, Object> cacheCopy, List<String> toFlush) {
        List<String> keysNow = cache.getKeys();
        for (String s : keysNow) {
            CacheEntry<?> c1 = ((CacheEntry<?>) cacheCopy.get(s));
            final Element element = cache.get(s);
            if (element != null && c1 != null) {
                CacheEntry<?> c2 = ((CacheEntry<?>) element.getObjectValue());
                assertEquals("Cache fragment different for : " + s + " after flushing : " + toFlush, c1.getObject(), c2.getObject());
                assertEquals("Cache properties different for : " + s + " after flushing : " + toFlush, c1.getExtendedProperties(), c2.getExtendedProperties());
            }
        }
    }

    public List<String> randomizeFlush(List<String> l, int number) {
        Random r = new Random();
        List<String> toFlush = new ArrayList<String>();
        for (int i = 0; i < number; i++) {
            String s = l.get(r.nextInt(l.size()));
            toFlush.add(s);
            ModuleCacheProvider.getInstance().getCache().remove(s);
        }
        return toFlush;
    }

    public String getContent(URL url, String user, String password, String requestId) throws Exception {
        String content = null;
        GetMethod method = null;
        try {
            method = executeCall(url, user, password, requestId);
            assertEquals("Bad result code", 200, method.getStatusCode());
            content = method.getResponseBodyAsString();
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
        return content;
    }

    public GetMethod executeCall(URL url, String user, String password, String requestId) throws IOException {
        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);

        if (user != null && password != null) {
            Credentials defaultcreds = new UsernamePasswordCredentials(user, password);
            client.getState().setCredentials(new AuthScope(url.getHost(), url.getPort(), AuthScope.ANY_REALM), defaultcreds);
        }

        client.getHostConfiguration().setHost(url.getHost(), url.getPort(), url.getProtocol());

        GetMethod method = new GetMethod(url.toExternalForm());
        if (requestId != null) {
            method.setRequestHeader("request-id", requestId);
        }
        client.executeMethod(method);
        return method;
    }

    public URL getUrl(String path) throws MalformedURLException {
        String baseurl = getBaseServerURL() + Jahia.getContextPath() + "/cms";
        return new URL(baseurl + "/render/live/en" + path + ".html");
    }

    public class HttpThread extends Thread {

        public String result;
        public int resultCode;
        public URL url;
        public String user;
        public String password;
        public String requestId;

        public HttpThread(URL url, String user, String password, String requestId) {
            this.url = url;
            this.user = user;
            this.password = password;
            this.requestId = requestId;
        }

        public String getResult() {
            return result;
        }

        @Override
        public void run() {
            GetMethod method = null;
            try {
                method = executeCall(url, user, password, requestId);
                resultCode = method.getStatusCode();
                result = method.getResponseBodyAsString();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (method != null) {
                    method.releaseConnection();
                }
            }
        }
    }
}