package org.jahia.test.services.render.filter.cache;

import net.sf.ehcache.Ehcache;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.render.filter.cache.AggregateCacheFilter;
import org.jahia.services.render.filter.cache.DefaultCacheKeyGenerator;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.jahia.services.render.filter.cache.ModuleGeneratorQueue;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.jcr.JCRGroup;
import org.jahia.services.usermanager.jcr.JCRGroupManagerProvider;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.junit.*;
import org.slf4j.Logger;

import javax.jcr.ImportUUIDBehavior;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

public class CacheFilterHttpTest extends JahiaTestCase {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(CacheFilterTest.class);
    private final static String TESTSITE_NAME = "cachetest";
    private static final String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            JahiaSite site = TestHelper.createSite(TESTSITE_NAME, "localhost", "templates-web-blue");
            assertNotNull(site);
            JCRStoreService jcrService = ServicesRegistry.getInstance()
                    .getJCRStoreService();
            JCRSessionWrapper session = jcrService.getSessionFactory()
                    .getCurrentUserSession();

            ServicesRegistry.getInstance().getJahiaTemplateManagerService().installModule("jahia-test-module", SITECONTENT_ROOT_NODE, session.getUser().getUsername());;
            final JCRUserManagerProvider userManagerProvider = JCRUserManagerProvider.getInstance();
            final JCRUser userAB = userManagerProvider.createUser("userAB", "password", new Properties());
            final JCRUser userAC = userManagerProvider.createUser("userAC", "password", new Properties());
            final JCRUser userBC = userManagerProvider.createUser("userBC", "password", new Properties());

            // Create three groups
            final JCRGroupManagerProvider groupManagerProvider = JCRGroupManagerProvider.getInstance();
            final JCRGroup groupA = groupManagerProvider.createGroup(site.getID(), "groupA", new Properties(), false);
            final JCRGroup groupB = groupManagerProvider.createGroup(site.getID(), "groupB", new Properties(), false);
            final JCRGroup groupC = groupManagerProvider.createGroup(site.getID(), "groupC", new Properties(), false);
            // Associate each user to two group
            groupA.addMember(userAB);
            groupA.addMember(userAC);
            groupB.addMember(userAB);
            groupB.addMember(userBC);
            groupC.addMember(userAC);
            groupC.addMember(userBC);

            InputStream importStream = CacheFilterHttpTest.class.getClassLoader().getResourceAsStream("imports/cachetest-site.xml");
            session.importXML(SITECONTENT_ROOT_NODE, importStream,
                    ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
            importStream.close();
            session.save();
            JCRNodeWrapper siteNode = session.getNode(SITECONTENT_ROOT_NODE);
            JCRPublicationService.getInstance().publishByMainId(siteNode.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null,
                    true, null);

        } catch (Exception e) {
            logger.warn("Exception during test setUp", e);
        }
    }


    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
            final JCRUserManagerProvider userManagerProvider = JCRUserManagerProvider.getInstance();
            userManagerProvider.deleteUser(userManagerProvider.lookupUser("userAB"));
            userManagerProvider.deleteUser(userManagerProvider.lookupUser("userAC"));
            userManagerProvider.deleteUser(userManagerProvider.lookupUser("userBC"));
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
        AggregateCacheFilter.flushNotCacheableFragment();
        CacheFilterCheckFilter.clear();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testStartPage() throws Exception {
        final URL url = new URL(getBaseServerURL() + Jahia.getContextPath() + "/start");

        HttpThread t1 = new HttpThread(url, "root", "root1234", null);
        HttpThread t2 = new HttpThread(url, "userAB", "password", null);
        HttpThread t3 = new HttpThread(url, "userBC", "password", null);
        HttpThread t4 = new HttpThread(url, "userAC", "password", null);

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        t1.join();
        t2.join();
        t3.join();
        t4.join();

        String root = t1.getResult();
        String userAB = t2.getResult();
        String userBC = t3.getResult();
        String userAC = t4.getResult();

        assertEquals("Content served is not the same", root, getContent(url, "root", "root1234", null));
        assertEquals("Content served is not the same", userAB, getContent(url, "userAB", "password", null));
        assertEquals("Content served is not the same", userBC, getContent(url, "userBC", "password", null));
        assertEquals("Content served is not the same", userAC, getContent(url, "userAC", "password", null));
        assertEquals("Content served is not the same", userBC, getContent(url, "userBC", "password", null));
        assertEquals("Content served is not the same", userAC, getContent(url, "userAC", "password", null));
        assertEquals("Content served is not the same", root, getContent(url, "root", "root1234", null));
        assertEquals("Content served is not the same", userAB, getContent(url, "userAB", "password", null));

        ServicesRegistry.getInstance().getCacheService().flushAllCaches();
        assertEquals("Content served is not the same", root, getContent(url, "root", "root1234", null));
        assertEquals("Content served is not the same", userAB, getContent(url, "userAB", "password", null));
        assertEquals("Content served is not the same", userBC, getContent(url, "userBC", "password", null));
        assertEquals("Content served is not the same", userAC, getContent(url, "userAC", "password", null));
    }

    @Test
    public void testACLs() throws Exception {
        testACLs(SITECONTENT_ROOT_NODE + "/home/acl1");
        testACLs(SITECONTENT_ROOT_NODE + "/home/acl2");
    }

    private void testACLs(String path) throws Exception {
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

        assertEquals("Content served is not the same", guest, getContent(getUrl(path), null, null, null));
        assertEquals("Content served is not the same", root, getContent(getUrl(path), "root", "root1234", null));
        assertEquals("Content served is not the same", userAB, getContent(getUrl(path), "userAB", "password", null));
        assertEquals("Content served is not the same", userBC, getContent(getUrl(path), "userBC", "password", null));
        assertEquals("Content served is not the same", userAC, getContent(getUrl(path), "userAC", "password", null));
    }

    private void checkAcl(String content, boolean[] b) {
        assertEquals(b[0], content.contains("visible for root"));
        assertEquals(b[1], content.contains("visible for users only"));
        assertEquals(b[2], content.contains("visible for userAB"));
        assertEquals(b[3], content.contains("visible for userBC"));
        assertEquals(b[4], content.contains("visible for userAC"));
        assertEquals(b[5], content.contains("visible for groupA"));
        assertEquals(b[6], content.contains("visible for groupB"));
        assertEquals(b[7], content.contains("visible for groupC"));
    }

    @Test
    public void testModuleError() throws Exception {
        String s = getContent(getUrl(SITECONTENT_ROOT_NODE + "/home/error"), "root", "root1234", "error1");
        assertTrue(s.contains("<!-- Module error :"));
        getContent(getUrl(SITECONTENT_ROOT_NODE + "/home/error"), "root", "root1234", "error2");
        // All served from cache
        assertEquals(1, CacheFilterCheckFilter.getData("error2").getCount());
        Thread.sleep(5000);
        // Error should be flushed
        getContent(getUrl(SITECONTENT_ROOT_NODE + "/home/error"), "root", "root1234", "error3");
        assertEquals(2, CacheFilterCheckFilter.getData("error3").getCount());
    }

    @Test
    public void testModuleWait() throws Exception {
        long previousModuleGenerationWaitTime = ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).getModuleGenerationWaitTime();
        try {
            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setModuleGenerationWaitTime(1000);
            URL url = getUrl(SITECONTENT_ROOT_NODE + "/home/long");
            HttpThread t1 = new HttpThread(url, "root", "root1234", "testModuleWait1");
            t1.start();
            Thread.sleep(200);

            HttpThread t2 = new HttpThread(url, "root", "root1234", "testModuleWait2");
            t2.start();
            t2.join();

            String content = getContent(url, "root", "root1234", "testModuleWait3");

            t1.join();

            String content1 = getContent(url, "root", "root1234", "testModuleWait4");

            // Long module is left blank
            assertFalse(t2.getResult().contains("Very long to appear"));
            assertTrue(t2.getResult().contains("<h2 class=\"pageTitle\">long</h2>"));
            assertTrue("Second thread did not spend correct time", CacheFilterCheckFilter.getData("testModuleWait2").getTime() > 1000 && CacheFilterCheckFilter.getData("testModuleWait2").getTime() < 1900);

            // Entry is cached without the long module
            assertFalse(content.contains("Very long to appear"));
            assertTrue(content.contains("<h2 class=\"pageTitle\">long</h2>"));
            assertEquals(1, CacheFilterCheckFilter.getData("testModuleWait3").getCount());

            System.out.println("thread t1");
            assertTrue(t1.getResult().contains("Very long to appear"));
            assertTrue(t1.getResult().contains("<h2 class=\"pageTitle\">long</h2>"));
            assertTrue("First thread did not spend correct time", CacheFilterCheckFilter.getData("testModuleWait1").getTime() > 2000 && CacheFilterCheckFilter.getData("testModuleWait2").getTime() < 3000);

            // Entry is now cached with the long module
            assertTrue(content1.contains("Very long to appear"));
            assertTrue(content1.contains("<h2 class=\"pageTitle\">long</h2>"));
            assertEquals(1, CacheFilterCheckFilter.getData("testModuleWait4").getCount());
        } finally {
            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setModuleGenerationWaitTime(previousModuleGenerationWaitTime);
        }
    }

    @Test
    public void testMaxConcurrent() throws Exception {
        Thread.sleep(5000);

        long previousModuleGenerationWaitTime = ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).getModuleGenerationWaitTime();
        int previousMaxModulesToGenerateInParallel = ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).getMaxModulesToGenerateInParallel();
        try {
            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setModuleGenerationWaitTime(1000);
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

    private String getContent(URL url, String user, String password, String requestId) throws Exception {
        GetMethod method = executeCall(url, user, password, requestId);
        assertEquals("Bad result code", 200, method.getStatusCode());
        return method.getResponseBodyAsString();
    }

    private GetMethod executeCall(URL url, String user, String password, String requestId) throws IOException {
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

    private URL getUrl(String path) throws MalformedURLException {
        String baseurl = getBaseServerURL() + Jahia.getContextPath() + "/cms";
        return new URL(baseurl + "/render/live/en" + path + ".html");
    }

    class HttpThread extends Thread {
        private String result;
        private int resultCode;
        private URL url;
        private String user;
        private String password;
        private String requestId;

        HttpThread(URL url, String user, String password, String requestId) {
            this.url = url;
            this.user = user;
            this.password = password;
            this.requestId = requestId;
        }


        String getResult() {
            return result;
        }

        @Override
        public void run() {
            try {
                GetMethod method = executeCall(url, user, password, requestId);
                resultCode = method.getStatusCode();
                result = method.getResponseBodyAsString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}