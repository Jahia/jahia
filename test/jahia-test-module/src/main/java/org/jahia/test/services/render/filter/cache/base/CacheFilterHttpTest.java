/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
import org.assertj.core.api.SoftAssertions;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.cache.CacheHelper;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.render.filter.AggregateFilter;
import org.jahia.services.render.filter.cache.*;
import org.jahia.services.render.monitoring.DefaultRenderTimeMonitor;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.jahia.test.services.render.filter.cache.CacheFilterCheckFilter;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.SimpleCredentials;
import javax.jcr.query.Query;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Base Cache filter test for both old and new implementation of CacheFilter
 * provide unit tests that are based on HTTP calls on current server to serve pages and tests different states
 */
public class CacheFilterHttpTest extends JahiaTestCase {

    public static final Logger logger = LoggerFactory.getLogger(CacheFilterHttpTest.class);
    public static final String TESTSITE_NAME = "cachetest";
    public static final String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    public static final String LONG_PAGE_TITLE = "<h1>long</h1>";
    public static final String LONG_CREATED_ELEMENT_TEXT = "Very long to appear";
    private static final String USER_1 = "user1";
    private static final String USER_2 = "user2";
    private static final String USER_3 = "user3";
    private static final String USER_AB = "userAB";
    private static final String USER_AC = "userAC";
    private static final String USER_BC = "userBC";
    private static final String SIMPLE_PASSWORD = "password";
    private static final String READER = "reader";
    private static final String GROUP_A = "g:groupA";
    private static final String GROUP_B = "g:groupB";
    private static final String CANNOT_SEE_CONTENT = " cannot see content, she should see";
    private static final String SEES_WRONG_CONTENT = " sees content, she should not see";
    private static final String CONTENT_FOR = "content for ";
    
    protected static final String ERROR_PAGE_PATH = SITECONTENT_ROOT_NODE + "/home/error";
    protected static final String LONG_PAGE_PATH = SITECONTENT_ROOT_NODE + "/home/long";
    protected static final String LONG_5_PAGE_PATH = SITECONTENT_ROOT_NODE + "/home/long5";
    
    protected static final String REQUEST_ID_PREFIX = "testModuleWait";
    protected static final String CONCURRENT_REQUEST_ID_PREFIX = "testMaxConcurrent";
    
    protected static final String CACHE_RENDER_FILTER_1 = "CacheHttpTestRenderFilter1";
    protected static final String CACHE_RENDER_FILTER_2 = "CacheHttpTestRenderFilter2";
    private static final Set<String> PAGE_NAMES_TO_SKIP = ImmutableSet.of("long", "error", "user-per-content-test", "simple-page-A", "simple-page-B",
            "simple-page-C", "simple-page-AC", "simple-page-BC", "simple-page-AB", "simple-page-root", "simple-page-users");
    
    private static boolean cacheFilterDisabled;
    private static boolean aggregateFilterDisabled;
    private static boolean aggregateCacheFilterDisabled;
    private static boolean areaResourceCacheKeyPartGeneratorDisabled;
    
    Random random = new SecureRandom();
    private String[] texts = {"visible for root", "visible for users only", "visible for userAB", "visible for userBC", "visible for userAC", "visible for groupA", "visible for groupB", "visible for groupC"};
    char[] simplePassword = SIMPLE_PASSWORD.toCharArray();

    @BeforeClass
    public static void oneTimeSetUp() {

        try {

            JahiaSite site = TestHelper.createSite(TESTSITE_NAME, "localhost", TestHelper.BOOTSTRAP_ACME_SPACE_TEMPLATES);
            assertNotNull(site);
            JCRStoreService jcrService = ServicesRegistry.getInstance().getJCRStoreService();
            JCRSessionWrapper session = jcrService.getSessionFactory().getCurrentUserSession();

            ServicesRegistry.getInstance().getJahiaTemplateManagerService().installModule("jahia-test-module", SITECONTENT_ROOT_NODE, session.getUser().getName());

            final JahiaUserManagerService userManagerProvider = JahiaUserManagerService.getInstance();
            final JCRUserNode userAB = userManagerProvider.createUser(USER_AB, SIMPLE_PASSWORD, new Properties(), session);
            final JCRUserNode userAC = userManagerProvider.createUser(USER_AC, SIMPLE_PASSWORD, new Properties(), session);
            final JCRUserNode userBC = userManagerProvider.createUser(USER_BC, SIMPLE_PASSWORD, new Properties(), session);
            userManagerProvider.createUser(USER_1, SIMPLE_PASSWORD, new Properties(), session);
            userManagerProvider.createUser(USER_2, SIMPLE_PASSWORD, new Properties(), session);
            userManagerProvider.createUser(USER_3, SIMPLE_PASSWORD, new Properties(), session);

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
            areaResourceCacheKeyPartGeneratorDisabled = ((AreaResourceCacheKeyPartGenerator) SpringContextSingleton.getBean("areaResourceCacheKeyPartGenerator")).isDisabled();

            //enable test filters
            getCheckFilter(CACHE_RENDER_FILTER_1).setDisabled(false);
            getCheckFilter(CACHE_RENDER_FILTER_2).setDisabled(false);
        } catch (Exception e) {
            logger.warn("Exception during test setUp", e);
            fail();
        }
    }

    @AfterClass
    public static void oneTimeTearDown() {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
            final JahiaUserManagerService userManagerProvider = JahiaUserManagerService.getInstance();
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            userManagerProvider.deleteUser(userManagerProvider.lookupUser(USER_AB).getPath(), session);
            userManagerProvider.deleteUser(userManagerProvider.lookupUser(USER_AC).getPath(), session);
            userManagerProvider.deleteUser(userManagerProvider.lookupUser(USER_BC).getPath(), session);
            userManagerProvider.deleteUser(userManagerProvider.lookupUser(USER_1).getPath(), session);
            userManagerProvider.deleteUser(userManagerProvider.lookupUser(USER_2).getPath(), session);
            userManagerProvider.deleteUser(userManagerProvider.lookupUser(USER_3).getPath(), session);
            session.save();

            // restore filters config
            ((CacheFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.cache.CacheFilter")).setDisabled(cacheFilterDisabled);
            ((AggregateFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.AggregateFilter")).setDisabled(aggregateFilterDisabled);
            ((AggregateCacheFilter) SpringContextSingleton.getBean("cacheFilter")).setDisabled(aggregateCacheFilterDisabled);
            ((AreaResourceCacheKeyPartGenerator) SpringContextSingleton.getBean("areaResourceCacheKeyPartGenerator")).setDisabled(areaResourceCacheKeyPartGeneratorDisabled);

            //enable test filters
            getCheckFilter(CACHE_RENDER_FILTER_1).setDisabled(true);
            getCheckFilter(CACHE_RENDER_FILTER_2).setDisabled(true);
            getCheckFilter(CACHE_RENDER_FILTER_1).clear();
            getCheckFilter(CACHE_RENDER_FILTER_2).clear();
        } catch (Exception e) {
            logger.warn("Exception during test tearDown", e);
        }
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    static void permute(List<SimpleCredentials> arr, int k, List<List<SimpleCredentials>> res) {
        for (int i = k; i < arr.size(); i++) {
            Collections.swap(arr, i, k);
            permute(arr, k + 1, res);
            Collections.swap(arr, k, i);
        }
        if (k == arr.size() - 1) {
            res.add(new ArrayList<>(arr));
        }
    }

    public static CacheFilterCheckFilter getCheckFilter(String id) {
        return (CacheFilterCheckFilter) SpringContextSingleton.getBean(id);
    }

    @Before
    public void setUp() {
        clearAll();
    }

    private void clearAll() {
        ModuleCacheProvider cacheProvider = ModuleCacheProvider.getInstance();
        Ehcache cache = cacheProvider.getCache();
        Ehcache depCache = cacheProvider.getDependenciesCache();
        cache.flush();
        cache.removeAll();
        depCache.flush();
        depCache.removeAll();
        cacheProvider.flushNonCacheableFragments();
        getCheckFilter(CACHE_RENDER_FILTER_1).clear();
        getCheckFilter(CACHE_RENDER_FILTER_2).clear();
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
    public void testACLs() throws RepositoryException, IOException {
        testACLs(SITECONTENT_ROOT_NODE + "/home/acl1");
    }

    @Test
    public void testReferencesFlush() throws RepositoryException, IOException {

        URL url = getUrl(SITECONTENT_ROOT_NODE + "/home/references");
        getContent(url, JahiaTestCase.getRootUserCredentials(), null);

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession("live", new Locale("en"));
        JCRNodeWrapper n = session.getNode(SITECONTENT_ROOT_NODE + "/home/references/main/simple-text");
        try {
            n.setProperty("text", "text content updated");
            session.save();
            String newvalue = getContent(url, JahiaTestCase.getRootUserCredentials(), "testReferencesFlush1");
            Matcher m = Pattern.compile("text content updated").matcher(newvalue);
            assertTrue("Value has not been updated", m.find());
            assertTrue("References have not been flushed", m.find());
            assertTrue("References have not been flushed", m.find());
        } finally {
            n.setProperty("text", "text content");
            session.save();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRandomFlush() throws RepositoryException, IOException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession("live", new Locale("en"));
        Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:page] as p where isdescendantnode(p,'" + SITECONTENT_ROOT_NODE + "/home')", Query.JCR_SQL2);
        List<String> paths = new ArrayList<>();
        NodeIterator nodes = q.execute().getNodes();
        while (nodes.hasNext()) {
            JCRNodeWrapper node = (JCRNodeWrapper) nodes.next();
            if (!PAGE_NAMES_TO_SKIP.contains(node.getName())) {
                paths.add(node.getPath());
            }
        }
        List<SimpleCredentials> users = Arrays.asList(new SimpleCredentials(USER_AB, simplePassword),
                new SimpleCredentials(USER_AC, simplePassword), new SimpleCredentials(USER_BC, simplePassword));
        Map<String, String> m = new HashMap<>();
        for (SimpleCredentials userCredentials : users) {
            for (String path : paths) {
                m.put(userCredentials.getUserID() + path, getContent(getUrl(path), userCredentials, null));
            }
        }

        final Cache cache = ModuleCacheProvider.getInstance().getCache();
        List<String> keysBefore = cache.getKeys();

        Map<Object, Element> cacheCopy = cache.getAll(keysBefore);

        SoftAssertions softly = new SoftAssertions();

        for (int j = 0; j < 10; j++) {
            logger.info("flush {}", j);
            List<String> toFlush = randomizeFlush(keysBefore, 10);
            for (SimpleCredentials userCredentials : users) {
                for (String path : paths) {
                    logger.info("{} - {}", userCredentials.getUserID(), path);
                    softly.assertThat(getContent(getUrl(path), userCredentials, null))
                            .as("Different content for " + userCredentials.getUserID() + " , " + path + " when flushing : " + toFlush)
                            .isEqualTo(m.get(userCredentials.getUserID() + path));
                    checkCacheContent(cache, cacheCopy, toFlush, softly);
                }
            }
            List<String> keysAfter = cache.getKeys();
            Collections.sort(keysBefore);
            Collections.sort(keysAfter);
            if (!keysBefore.equals(keysAfter)) {
                List<String> onlyInBefore = new ArrayList<>(keysBefore);
                onlyInBefore.removeAll(keysAfter);
                List<String> onlyInAfter = new ArrayList<>(keysAfter);
                softly.fail("Key sets are not the same before and after flushing : " + toFlush + "\n Before flushs :" + onlyInBefore + " ,\n After flush : " + onlyInAfter);
            }
            checkCacheContent(cache, cacheCopy, toFlush, softly);
        }

        softly.assertAll();
    }

    @Test
    public void testACLsUserPerContent() throws IOException {
        // test for https://jira.jahia.org/browse/QA-7383
        String path = SITECONTENT_ROOT_NODE + "/home/user-per-content-test";

        String contentForUser1 = getContent(getUrl(path), new SimpleCredentials(USER_1, simplePassword), "testACLs11");
        assertTrue(USER_1 + CANNOT_SEE_CONTENT, contentForUser1.contains(CONTENT_FOR + USER_1));
        assertFalse(USER_1 + SEES_WRONG_CONTENT, contentForUser1.contains(CONTENT_FOR + USER_2));
        assertFalse(USER_1 + SEES_WRONG_CONTENT, contentForUser1.contains(CONTENT_FOR + USER_3));

        String contentForUser2 = getContent(getUrl(path), new SimpleCredentials(USER_2, simplePassword), "testACLs12");
        assertTrue(USER_2 + CANNOT_SEE_CONTENT, contentForUser2.contains(CONTENT_FOR + USER_2));
        assertFalse(USER_2 + SEES_WRONG_CONTENT, contentForUser2.contains(CONTENT_FOR + USER_1));
        assertFalse(USER_2 + SEES_WRONG_CONTENT, contentForUser2.contains(CONTENT_FOR + USER_3));

        String contentForUser3 = getContent(getUrl(path), new SimpleCredentials(USER_3, simplePassword), "testACLs13");
        assertTrue(USER_3 + CANNOT_SEE_CONTENT, contentForUser3.contains(CONTENT_FOR + USER_3));
        assertFalse(USER_3 + SEES_WRONG_CONTENT, contentForUser3.contains(CONTENT_FOR + USER_1));
        assertFalse(USER_3 + SEES_WRONG_CONTENT, contentForUser3.contains(CONTENT_FOR + USER_2));
    }

    public void testMaxConcurrent(int generationTime) throws IOException {
        ModuleGeneratorQueue moduleGeneratorQueue = ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue"));
        long previousModuleGenerationWaitTime = moduleGeneratorQueue.getModuleGenerationWaitTime();
        int previousMaxModulesToGenerateInParallel = moduleGeneratorQueue.getMaxModulesToGenerateInParallel();

        try {
            moduleGeneratorQueue.setModuleGenerationWaitTime(generationTime);
            moduleGeneratorQueue.setMaxModulesToGenerateInParallel(1);
            int counter = 1;
            HttpThread t1 = new HttpThread(getUrl(LONG_PAGE_PATH), JahiaTestCase.getRootUserCredentials(),
                    CONCURRENT_REQUEST_ID_PREFIX + counter++);
            t1.start();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            HttpThread t2 = new HttpThread(getUrl(SITECONTENT_ROOT_NODE + "/home"), JahiaTestCase.getRootUserCredentials(),
                    CONCURRENT_REQUEST_ID_PREFIX + counter++);
            t2.start();
            try {
                t2.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            try {
                t1.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            assertEquals("Incorrect response code for first thread", 200, t1.resultCode);
            assertEquals("Incorrect response code for second thread", 503, t2.resultCode);

            assertTrue(getContent(getUrl(SITECONTENT_ROOT_NODE + "/home"), JahiaTestCase.getRootUserCredentials(),
                    CONCURRENT_REQUEST_ID_PREFIX + counter++).contains("<title>Home</title>"));
        } finally {
            moduleGeneratorQueue.setModuleGenerationWaitTime(previousModuleGenerationWaitTime);
            moduleGeneratorQueue.setMaxModulesToGenerateInParallel(previousMaxModulesToGenerateInParallel);
        }
    }

    @Test
    public void testMaxRequestRenderTime() throws IOException {
        DefaultRenderTimeMonitor renderTimeMonitor = (DefaultRenderTimeMonitor) SpringContextSingleton.getBean("RenderTimeMonitor");
        long previousMaxRequestRenderTime = renderTimeMonitor.getMaxRequestRenderTime();
        try {
            // disable render time monitoring
            renderTimeMonitor.setMaxRequestRenderTime(-1);
            long startTime = System.currentTimeMillis();
            assertTrue(getContent(getUrl(LONG_5_PAGE_PATH), JahiaTestCase.getRootUserCredentials(), CONCURRENT_REQUEST_ID_PREFIX + 3)
                    .contains(LONG_CREATED_ELEMENT_TEXT));
            assertTrue("Execution time was too short", (System.currentTimeMillis() - startTime) >= 5000);

            // flush caches
            CacheHelper.flushOutputCaches();

            // set max render time to 3 seconds
            renderTimeMonitor.setMaxRequestRenderTime(3000);
            HttpThread t1 = new HttpThread(getUrl(LONG_5_PAGE_PATH), JahiaTestCase.getRootUserCredentials(),
                    CONCURRENT_REQUEST_ID_PREFIX + 1);
            t1.start();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            HttpThread t2 = new HttpThread(getUrl(LONG_5_PAGE_PATH), JahiaTestCase.getRootUserCredentials(),
                    CONCURRENT_REQUEST_ID_PREFIX + 2);
            t2.start();
            try {
                t2.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            try {
                t1.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            assertEquals("Incorrect response code for first thread", 503, t1.resultCode);
            assertEquals("Incorrect response code for second thread", 503, t2.resultCode);

            assertTrue(getContent(getUrl(LONG_5_PAGE_PATH), JahiaTestCase.getRootUserCredentials(), CONCURRENT_REQUEST_ID_PREFIX + 3)
                    .contains(LONG_CREATED_ELEMENT_TEXT));
        } finally {
            renderTimeMonitor.setMaxRequestRenderTime(previousMaxRequestRenderTime);
        }
    }

    private void testACLs(String path) throws RepositoryException, IOException {
        List<SimpleCredentials> users = Arrays.asList(new SimpleCredentials(null, simplePassword), JahiaTestCase.getRootUserCredentials(),
                new SimpleCredentials(USER_AB, simplePassword), new SimpleCredentials(USER_AC, simplePassword));
        List<String> userIds = users.stream().map(SimpleCredentials::getUserID).collect(Collectors.toList());

        List<List<SimpleCredentials>> allPerms = new ArrayList<>();
        permute(users, 0, allPerms);

        Map<String, String> results = new HashMap<>();

        for (List<SimpleCredentials> allPerm : allPerms) {
            results.clear();
            clearAll();

            for (SimpleCredentials userCredentials : allPerm) {
                results.put(userCredentials.getUserID(), getContent(getUrl(path), userCredentials, null));
            }
            
            List<String> allPermUserIds = allPerm.stream().map(SimpleCredentials::getUserID).collect(Collectors.toList());
            checkAcl(allPermUserIds + ", guest : ", results.get(null), new boolean[] { false, false, false, false, false, false, false, false });
            checkAcl(allPermUserIds + ", " + JahiaTestCase.getRootUserCredentials().getUserID() + " : ",
                    results.get(JahiaTestCase.getRootUserCredentials().getUserID()),
                    new boolean[] { true, true, true, true, true, true, true, true });
            checkAcl(allPermUserIds + ", " + USER_AB + " : ", results.get(USER_AB),
                    new boolean[] { false, true, true, false, false, true, true, false });
            checkAcl(allPermUserIds + ", " + USER_AC + " : ", results.get(USER_AC),
                    new boolean[] { false, true, false, false, true, true, false, true });
        }


        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession("live", new Locale("en"));
        JCRNodeWrapper n = session.getNode(path + "/main/simple-text-A");
        JCRNodeWrapper n2 = session.getNode(path + "/simple-page-A");
        try {
            n.revokeRolesForPrincipal(GROUP_A);
            n.grantRoles(GROUP_B, new HashSet<>(Arrays.asList(READER)));
            n2.revokeRolesForPrincipal(GROUP_A);
            n2.grantRoles(GROUP_B, new HashSet<>(Arrays.asList(READER)));
            session.save();

            Map<String, String> results2 = new HashMap<>();

            for (SimpleCredentials userCredentials : users) {
                results2.put(userCredentials.getUserID(), getContent(getUrl(path), userCredentials, null));
            }
            checkAcl(userIds + ", guest : ", results2.get(null), new boolean[] { false, false, false, false, false, false, false, false });
            checkAcl(userIds + ", " + JahiaTestCase.getRootUserCredentials().getUserID() + " : ",
                    results2.get(JahiaTestCase.getRootUserCredentials().getUserID()),
                    new boolean[] { true, true, true, true, true, true, true, true });
            checkAcl(userIds + ", " + USER_AB + " : ", results2.get(USER_AB),
                    new boolean[] { false, true, true, false, false, true, true, false });
            checkAcl(userIds + ", " + USER_AC + " : ", results2.get(USER_AC),
                    new boolean[] { false, true, false, false, true, false, false, true });
        } finally {
            n.revokeRolesForPrincipal(GROUP_B);
            n.grantRoles(GROUP_A, new HashSet<>(Arrays.asList(READER)));
            n2.revokeRolesForPrincipal(GROUP_B);
            n2.grantRoles(GROUP_A, new HashSet<>(Arrays.asList(READER)));
            session.save();
        }

        for (SimpleCredentials userCredentials : users) {
            assertEquals("Content served is not the same for " + userCredentials.getUserID(),
                    results.get(userCredentials.getUserID()), getContent(getUrl(path), userCredentials, null));
        }
    }

    private void checkAcl(String message, String content, boolean[] b) {
        for (int i = 0; i < texts.length; i++) {

            assertEquals(message + " , " + texts[i], b[i], content.contains(texts[i]));
        }
    }

    @SuppressWarnings("unchecked")
    public void checkCacheContent(Cache cache, Map<Object, Element> cacheCopy, List<String> toFlush, SoftAssertions softly) {
        List<String> keysNow = cache.getKeys();
        for (String s : keysNow) {
            CacheEntry<?> c1 = ((CacheEntry<?>) cacheCopy.get(s).getObjectValue());
            final Element element = cache.get(s);
            if (element != null && c1 != null) {
                CacheEntry<?> c2 = ((CacheEntry<?>) element.getObjectValue());
                softly.assertThat(c2.getObject()).as("Cache fragment different for : " + s + " after flushing : " + toFlush)
                        .isEqualTo(c1.getObject());
                softly.assertThat(c2.getExtendedProperties()).as("Cache properties different for : " + s + " after flushing : " + toFlush)
                        .isEqualTo(c1.getExtendedProperties());
            }
        }
    }

    public List<String> randomizeFlush(List<String> l, int number) {
        List<String> toFlush = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            String s = l.get(random.nextInt(l.size()));
            toFlush.add(s);
            ModuleCacheProvider.getInstance().getCache().remove(s);
        }
        return toFlush;
    }

    public String getContent(URL url, SimpleCredentials credentials, String requestId) throws IOException {
        String content = null;
        GetMethod method = null;
        try {
            method = executeCall(url, credentials, requestId);
            assertEquals("Bad result code", 200, method.getStatusCode());
            content = method.getResponseBodyAsString();
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
        return content;
    }
    
    public GetMethod executeCall(URL url, SimpleCredentials credentials, String requestId) throws IOException {
        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);

        if (credentials != null && credentials.getUserID() != null) {
            Credentials defaultcreds = new UsernamePasswordCredentials(credentials.getUserID(), new String(credentials.getPassword()));
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

    public URL getUrl(String path, String id) throws MalformedURLException {
        String baseurl = getBaseServerURL() + Jahia.getContextPath() + "/cms";
        return new URL(baseurl + "/render/live/en" + path + ".html?requestId=" + id);
    }

    public class HttpThread extends Thread {

        private String result;
        private int resultCode;
        private URL url;
        private SimpleCredentials credentials;
        private String requestId;

        public HttpThread(URL url, SimpleCredentials credentials, String requestId) {
            this.url = url;
            this.credentials = credentials;
            this.requestId = requestId;
        }

        public String getResult() {
            return result;
        }

        @Override
        public void run() {
            GetMethod method = null;
            try {
                method = executeCall(url, credentials, requestId);
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