/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.content;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.crawl.Generator;
import org.apache.nutch.crawl.Injector;
import org.apache.nutch.fetcher.Fetcher;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.osgi.BundleResource;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.sites.JahiaSite;
import org.jahia.settings.SettingsBean;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Log4jEventCollectorWrapper;
import org.jahia.utils.Log4jEventCollectorWrapper.LoggingEventWrapper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;

/**
 * Basic fetcher test 1. generate seedlist 2. inject 3. generate 4. fetch
 *
 * @author nutch-dev <nutch-dev at lucene.apache.org> and Benjamin Papez
 */

public class CrawlingPageVisitorTest extends JahiaTestCase {

    private static final Logger logger = Logger.getLogger(CrawlingPageVisitorTest.class);

    private static final Path TEST_DIR;
    static {
        String tmpDir = System.getProperty("java.io.tmpdir");
        if (!tmpDir.endsWith("/") && !tmpDir.endsWith("\\")) {
            tmpDir = tmpDir + System.getProperty("file.separator");
        }
        TEST_DIR = new Path(tmpDir + "test/fetch-test");
    }

    private static final String ACMESITE_NAME = "CrawlACMETest";
    private static final String ACME_SITECONTENT_ROOT_NODE = "sites/" + ACMESITE_NAME;
    private static final String DEFAULT_LANGUAGE = "en";

    private static Configuration conf;
    private static FileSystem fileSystem;
    private static Path crawldbPath;
    private static Path segmentsPath;
    private static Path urlPath;

    private Log4jEventCollectorWrapper logEventCollector;

    private static void extract(JahiaTemplatesPackage p, org.springframework.core.io.Resource r, File f) throws Exception {
        if ((r instanceof BundleResource && r.contentLength() == 0) || (!(r instanceof BundleResource) && r.getFile().isDirectory())) {
            f.mkdirs();
            String path = r.getURI().getPath();
            for (org.springframework.core.io.Resource resource : p.getResources(path.substring(path.indexOf("/plugins")))) {
                extract(p, resource, new File(f, resource.getFilename()));
            }
        } else {
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(f);
                IOUtils.copy(r.getInputStream(), output);
            } finally {
                IOUtils.closeQuietly(output);
            }
        }
    }

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {

        // This is added to allow this parallel crawling for different jahia versions (see crawl-tests.xml)
        System.setProperty("crawl.jahia.version", Jahia.VERSION);

        conf = CrawlDBTestUtil.createConfiguration();
        conf.setClassLoader(CrawlingPageVisitorTest.class.getClassLoader());

        File f = File.createTempFile("plugins","");
        f.delete();
        final JahiaTemplatesPackage templatePackageById = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById("jahia-test-module");
        extract(templatePackageById, templatePackageById.getResource("/plugins"), f);

        conf.setStrings("plugin.folders", f.getPath());
        Thread.currentThread().setContextClassLoader(CrawlingPageVisitorTest.class.getClassLoader());
        fileSystem = FileSystem.get(conf);
        fileSystem.delete(TEST_DIR, true);
        urlPath = new Path(TEST_DIR, "urls");
        crawldbPath = new Path(TEST_DIR, "crawldb");
        segmentsPath = new Path(TEST_DIR, "segments");

        final JahiaSite defaultSite = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(ACMESITE_NAME);
        if (defaultSite == null) {

            final JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();

            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {

                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        TestHelper.createSite(ACMESITE_NAME, "localhost", TestHelper.BOOTSTRAP_ACME_SPACE_TEMPLATES,
                                "prepackagedSites/acmespaceelektra.zip",
                                "ACME-SPACE.zip");
                        jcrService.publishByMainId(session.getRootNode().getNode(ACME_SITECONTENT_ROOT_NODE + "/home").getIdentifier(),
                                Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
                        session.save();
                    } catch (Exception ex) {
                        throw new JahiaRuntimeException(ex);
                    }
                    return null;
                }
            });
        }
    }

    @Before
    public void setUp() {
        logEventCollector = new Log4jEventCollectorWrapper(Priority.ERROR_INT);
    }

    @After
    public void tearDown() {
        logEventCollector.close();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite(ACMESITE_NAME);
    }

    private String getPrecompileServletURL() {
        return getBaseServerURL()+ Jahia.getContextPath() + "/modules/tools/precompileServlet";
    }

    @Test
    public void testPrecompileJsps() throws IOException {
        HttpClient client = new HttpClient();
        client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("jahia", "password"));
        String url = getPrecompileServletURL() + "?compile_type=all&jsp_precompile=true";
        logger.info("Starting the precompileServlet with the following url: " + url);
        GetMethod get = new GetMethod(url);
        try {
            get.setDoAuthentication(true);
            int statusCode = client.executeMethod(get);
            Assert.assertEquals("Precompile servlet failed", HttpStatus.SC_OK, statusCode);
            Assert.assertThat("Precompilation found buggy JSPs", get.getResponseBodyAsString(), containsString("No problems found!"));
            Assert.assertEquals("There were errors during the precompile process", "", toText(logEventCollector.getCollectedEvents()));
        } finally {
            get.releaseConnection();
        }
    }

    @Test
    public void testFetchDefaultSiteLive() throws RepositoryException, IOException {
        crawlUrls(getBaseUrls(Constants.LIVE_WORKSPACE, ACME_SITECONTENT_ROOT_NODE));
        Assert.assertEquals("There were errors during the crawling", "", toText(logEventCollector.getCollectedEvents()));
    }

    private List<String> getBaseUrls(String workspace, String sitePath) throws RepositoryException {

        List<String> urls = new ArrayList<String>();
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE));

        // generate seedlist
        RenderContext renderCtx = new RenderContext(getRequest(), getResponse(), getUser());

        JCRNodeWrapper homeNode = null;
        try {
            homeNode = session.getRootNode().getNode(sitePath + "/home");
        } catch (PathNotFoundException e) {
            if (ACME_SITECONTENT_ROOT_NODE.equals(sitePath)) {
                JahiaSite defaultSite = ServicesRegistry.getInstance().getJahiaSitesService().getDefaultSite();
                sitePath = defaultSite != null ? "sites/" + defaultSite.getSiteKey() : null;
                homeNode = session.getRootNode().getNode(sitePath + "/home");
            }
        }

        Resource resource = new Resource(homeNode, "html", null, Resource.CONFIGURATION_PAGE);
        renderCtx.setMainResource(resource);
        renderCtx.setSite(homeNode.getResolveSite());
        URLGenerator urlgenerator = new URLGenerator(renderCtx, resource);
        urls.add(urlgenerator.getServer() + urlgenerator.getContext()
                + (Constants.LIVE_WORKSPACE.equals(workspace) ? urlgenerator.getLive() : urlgenerator.getEdit()));

        return urls;
    }

    private void crawlUrls(List<String> urls) throws IOException {

        CrawlDBTestUtil.generateSeedList(fileSystem, urlPath, urls);

        // inject
        Injector injector = new Injector(conf);
        injector.inject(crawldbPath, urlPath);

        // generate
        Generator g = new Generator(conf);
        // fetch
        conf.setBoolean("fetcher.parse", true);
        Fetcher fetcher = new Fetcher(conf);
        CrawlDb crawlDbTool = new CrawlDb(conf);

        int depth = 5;
        int threads = 4;
        for (int i = 0; i < depth; i++) { // generate new segment
            Path[] generatedSegments = g.generate(crawldbPath, segmentsPath, 1, Long.MAX_VALUE, Long.MAX_VALUE, false, false);
            if (generatedSegments == null) {
                logger.info("Stopping at depth=" + i + " - no more URLs to fetch.");
                break;
            }
            for (Path generatedSegment : generatedSegments) {
                fetcher.fetch(generatedSegment, threads);
                crawlDbTool.update(crawldbPath,
                        new Path[] { generatedSegment }, true, true);
            }
        }
    }

    private static String toText(List<LoggingEventWrapper> logEvents) {
        DateFormat timestampFormat = SimpleDateFormat.getDateTimeInstance();
        StringBuilder errors = new StringBuilder();
        for (LoggingEventWrapper logEvent : logEvents) {
            errors.append(timestampFormat.format(new Date(logEvent.getTimestamp()))).append(" ").append(logEvent.getMessage()).append("\n");
            String[] throwableInfo = logEvent.getThrowableInfo();
            if (throwableInfo != null) {
                for (String throwableInfoItem : throwableInfo) {
                    errors.append(throwableInfoItem).append("\n");
                }
                errors.append("\n");
            }
        }
        return errors.toString();
    }
}
