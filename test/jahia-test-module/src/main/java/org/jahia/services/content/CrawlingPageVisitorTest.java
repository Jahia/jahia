/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.crawl.Generator;
import org.apache.nutch.crawl.Injector;
import org.apache.nutch.fetcher.Fetcher;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.sites.JahiaSite;
import org.jahia.settings.SettingsBean;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Basic fetcher test 1. generate seedlist 2. inject 3. generate 4. fetch
 * 
 * @author nutch-dev <nutch-dev at lucene.apache.org> and Benjamin Papez
 */

public class CrawlingPageVisitorTest {
    private static Logger logger = Logger.getLogger(CrawlingPageVisitorTest.class);

    private final static Path testdir = new Path(System.getProperty("java.io.tmpdir")
            + (!(System.getProperty("java.io.tmpdir").endsWith("/") || System.getProperty("java.io.tmpdir").endsWith(
                    "\\")) ? System.getProperty("file.separator") : "") + "test/fetch-test");

    private final static String ACMESITE_NAME = "CrawlACMETest";

    private final static String ACME_SITECONTENT_ROOT_NODE = "sites/" + ACMESITE_NAME;

    private static String DEFAULT_LANGUAGE = "en";

    private static Configuration conf;

    private static FileSystem fs;

    private static Path crawldbPath;

    private static Path segmentsPath;

    private static Path urlPath;

    private LogToJUnitOutputAppender appender = null;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            conf = CrawlDBTestUtil.createConfiguration();
            fs = FileSystem.get(conf);
            fs.delete(testdir, true);
            urlPath = new Path(testdir, "urls");
            crawldbPath = new Path(testdir, "crawldb");
            segmentsPath = new Path(testdir, "segments");

            final JahiaSite defaultSite = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(ACMESITE_NAME);
            if (defaultSite == null) {
                final JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();
                JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        try {
                            TestHelper.createSite(ACMESITE_NAME, "localhost", TestHelper.WEB_BLUE_TEMPLATES,
                                    SettingsBean.getInstance().getJahiaVarDiskPath()
                                            + "/prepackagedSites/acme.zip", "ACME.zip");
                            jcrService.publishByMainId(session.getRootNode().getNode(ACME_SITECONTENT_ROOT_NODE + "/home")
                                    .getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
                            session.save();
                        } catch (Exception e) {
                            logger.error("Cannot create or publish site", e);
                        }
                        return null;
                    }
                });
            }
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }
    }

    @Before
    public void setUp() {
        appender = new LogToJUnitOutputAppender();
        Logger rootLogger = Logger.getRootLogger();
        // when we want to add it back...
        rootLogger.addAppender(appender);

    }

    @After
    public void tearDown() {
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.removeAppender(appender);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            TestHelper.deleteSite(ACMESITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }
    
    private String getBaseServerURL() {
        return "http://localhost:8080";
    }


    private String getPrecompileServletURL() {
        return getBaseServerURL()+ Jahia.getContextPath() + "/tools/precompileServlet";
    }    

    @Test
    public void testPrecompileJsps() throws IOException {
        try {
            HttpClient client = new HttpClient();

            client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("jahia", "password"));

            GetMethod get = new GetMethod(getPrecompileServletURL() + "?compile_type=all&jsp_precompile=true");
            try {
                get.setDoAuthentication(true);

                int statusCode = client.executeMethod(get);

                assertEquals("Precompile servlet failed", HttpStatus.SC_OK, statusCode);
                assertThat("Precompilation found buggy JSPs", get.getResponseBodyAsString(), containsString("No problems found!"));
                assertEquals("There were exceptions during the precompile process", "", appender.getErrorLogs());
            } finally {
                get.releaseConnection();
            }
        } catch (Exception e) {
            assertNotNull("Precompile servlet request threw exception: " + e.getLocalizedMessage() , null);
        }
    }

    @Test
    public void testFetchDefaultSiteLive() throws IOException {
        crawlUrls(getBaseUrls(Constants.LIVE_WORKSPACE, ACME_SITECONTENT_ROOT_NODE));
        assertEquals("There were errors during the crawling", "", appender.getErrorLogs());        
    }

//    @Test
//    public void testFetchDefaultSiteEdit() throws IOException {
//        JahiaSite defaultSite = ServicesRegistry.getInstance().getJahiaSitesService().getDefaultSite();
//        String siteRootNode = defaultSite != null ? "sites/" + defaultSite.getSiteKey() : ACME_SITECONTENT_ROOT_NODE;
//        crawlUrls(getBaseUrls(Constants.EDIT_WORKSPACE, ACME_SITECONTENT_ROOT_NODE));
//        assertEquals("There were errors during the crawling", "", appender.getErrorLogs());
//    }
//
//    @Test
//    public void testFetchAdmin() throws IOException {
//        try {
//            ParamBean ctx = (ParamBean) Jahia.getThreadParamBean();
//            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
//                    LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE));
//            // generate seedlist
//            List<String> urls = new ArrayList<String>();
//            JahiaSite defaultSite = ServicesRegistry.getInstance().getJahiaSitesService().getDefaultSite();
//            if (defaultSite != null) {
//                RenderContext renderCtx = new RenderContext(ctx.getRequest(), ctx.getResponse(), ctx.getUser());
//                JCRNodeWrapper homeNode = session.getRootNode().getNode("sites/" + defaultSite.getSiteKey() + "/home");
//                Resource resource = new Resource(homeNode, "html", null, Resource.CONFIGURATION_PAGE);
//                renderCtx.setMainResource(resource);
//                renderCtx.setSite(homeNode.getResolveSite());
//                URLGenerator urlgenerator = new URLGenerator(renderCtx, resource);
//
//                final StringBuffer adminUrl = new StringBuffer();
//                adminUrl.append(ctx.getContextPath()).append(Jahia.getInitAdminServletPath()).append("?do=passthru");
//                urls.add(urlgenerator.getServer() + adminUrl.toString());
//
//                crawlUrls(urls);
//            }
//
//        } catch (Exception e) {
//            logger.error("Exception during test", e);
//        }
//        assertEquals("There were errors during the crawling", "", appender.getErrorLogs());
//    }

    private List<String> getBaseUrls(String workspace, String sitePath) {
        List<String> urls = new ArrayList<String>();
        try {
            ParamBean ctx = (ParamBean) Jahia.getThreadParamBean();
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace,
                    LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE));

            // generate seedlist
            RenderContext renderCtx = new RenderContext(ctx.getRequest(), ctx.getResponse(), ctx.getUser());
                        
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
        } catch (Exception e) {
            logger.error("Exception during test", e);
        }
        return urls;
    }

    private void crawlUrls(List<String> urls) {
        try {
            CrawlDBTestUtil.generateSeedList(fs, urlPath, urls);

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
                Path generatedSegment = g.generate(crawldbPath, segmentsPath, 1, Long.MAX_VALUE, Long.MAX_VALUE, false,
                        false);

                if (generatedSegment == null) {
                    logger.info("Stopping at depth=" + i + " - no more URLs to fetch.");
                    break;
                }
                fetcher.fetch(generatedSegment, threads, true);
                crawlDbTool.update(crawldbPath, new Path[] { generatedSegment }, true, true);
            }
        } catch (IOException e) {
            logger.error("Exception while crawling", e);
        }
    }

    class LogToJUnitOutputAppender extends AppenderSkeleton {
        StringBuffer errorLogs = new StringBuffer();
        DateFormat timestampFormatter = SimpleDateFormat.getDateTimeInstance();
        private String newLine = System.getProperty("line.separator") != null ? System.getProperty("line.separator") : "\n";
        long lastTimeStamp = 0L;

        public LogToJUnitOutputAppender() {
        }

        @Override
        protected void append(LoggingEvent event) {
            if (event.getLevel().toInt() >= Priority.ERROR_INT) {
                StringBuilder errorLog = new StringBuilder();
                if (event.getTimeStamp() - lastTimeStamp > 2000) {
                    errorLog.append(newLine);                    
                }
                errorLog.append(timestampFormatter.format(new Date(event.getTimeStamp()))).append(" ")
                        .append(event.getRenderedMessage()).append(newLine);
                String[] throwableStringRep = event.getThrowableStrRep();
                if (throwableStringRep != null) {
                    for (String stacktraceLine : throwableStringRep) {
                        errorLog.append(stacktraceLine).append(newLine);
                    }
                    errorLog.append(newLine);                    
                } 
                errorLogs.append(errorLog.toString());
                lastTimeStamp = event.getTimeStamp();
            }
        }

        public void close() {
        }

        public boolean requiresLayout() {
            return false;
        }
        
        public String getErrorLogs() {
            return errorLogs.toString();
        }
    }
}
