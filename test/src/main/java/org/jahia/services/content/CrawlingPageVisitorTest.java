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
package org.jahia.services.content;

import junit.framework.TestCase;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.apache.nutch.crawl.Generator;
import org.apache.nutch.crawl.Injector;
import org.apache.nutch.fetcher.Fetcher;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.settings.SettingsBean;
import org.jahia.test.TestHelper;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Basic fetcher test 1. generate seedlist 2. inject 3. generate 4. fetch
 *
 * @author nutch-dev <nutch-dev at lucene.apache.org> and Benjamin Papez
 */

public class CrawlingPageVisitorTest extends TestCase {
    private static Logger logger = Logger
            .getLogger(CrawlingPageVisitorTest.class);
    private final static Path testdir = new Path(System
            .getProperty("java.io.tmpdir")
            + (!(System.getProperty("java.io.tmpdir").endsWith("/") || System
            .getProperty("java.io.tmpdir").endsWith("\\")) ? System
            .getProperty("file.separator") : "") + "test/fetch-test");

    private final static String ACMESITE_NAME = "CrawlACMETest";

    private final static String ACME_SITECONTENT_ROOT_NODE = "/" + Constants.CONTENT + "/sites/" + ACMESITE_NAME;

    private final static String TESTSITE_NAME = "CrawlTCKTest";

    private final static String TEST_SITECONTENT_ROOT_NODE = "/" + Constants.CONTENT + "/sites/" + TESTSITE_NAME;

    private Configuration conf;
    private FileSystem fs;
    private Path crawldbPath;
    private Path segmentsPath;
    private Path urlPath;
    private ProcessingContext ctx;
    private JahiaSite defaultSite;
    private JahiaSite testSite;
    private boolean defaultSiteCreated = false;
    private boolean testSiteCreated = false;

    @Override
    protected void setUp() throws Exception {
        ctx = Jahia.getThreadParamBean();
        conf = CrawlDBTestUtil.createConfiguration();
        fs = FileSystem.get(conf);
        fs.delete(testdir);
        urlPath = new Path(testdir, "urls");
        crawldbPath = new Path(testdir, "crawldb");
        segmentsPath = new Path(testdir, "segments");
        defaultSite = ProcessingContext.getDefaultSite();
        final JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                if (defaultSite == null) {
                    String prepackedZIP = SettingsBean.getInstance().getJahiaVarDiskPath()
                            + "/prepackagedSites/webtemplates.zip";
                    try {
                        defaultSite = TestHelper.createSite(ACMESITE_NAME, "localhost", TestHelper.ACME_TEMPLATES, new File(prepackedZIP + "/ACME.zip"));
                    } catch (Exception e) {
                        logger.error("Cannot create site", e);
                        fail("Cannot create site : " + e);
                    }
                    defaultSiteCreated = true;
                    QueryResult queryResult = session.getWorkspace().getQueryManager().createQuery(
                            "select * from [jnt:page] as p where ischildnode(p,[" + ACME_SITECONTENT_ROOT_NODE + "])",
                            Query.JCR_SQL2).execute();
                    for (NodeIterator ni = queryResult.getNodes(); ni.hasNext();) {
                        jcrService.publish(((Node) ni.next()).getPath(), Constants.EDIT_WORKSPACE,
                                Constants.LIVE_WORKSPACE, null, true, true);
                    }
                }
                try {
                    JahiaSitesService sitesService = ServicesRegistry.getInstance().getJahiaSitesService();
                    for (Iterator<JahiaSite> it = sitesService.getSites(); it.hasNext();) {
                        JahiaSite site = it.next();
                        if (site.getTemplatePackageName().equals(TestHelper.TCK_TEMPLATES)) {
                            testSite = site;
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Error while trying to find test site", e);
                }
                return null;
            }
        });

//            if (testSite == null) {
//                String prepackedZIP = SettingsBean.getInstance().getJahiaVarDiskPath()
//                        + "/prepackagedSites/testSite.zip";
//                testSite = TestHelper.createSite(TESTSITE_NAME, "127.0.0.1", TestHelper.TCK_TEMPLATES, new File(prepackedZIP + "/testSite.zip"));
//                QueryResult queryResult = session.getWorkspace().getQueryManager().createQuery(
//                        "select * from [jnt:page] as p where ischildnode(p,[" + TEST_SITECONTENT_ROOT_NODE + "])",
//                        Query.JCR_SQL2).execute();
//                for (NodeIterator ni = queryResult.getNodes(); ni.hasNext();) {
//                    jcrService.publish(((Node) ni.next()).getPath(), Constants.EDIT_WORKSPACE,
//                            Constants.LIVE_WORKSPACE, null, ctx.getUser(), true, true);
//                }
//                siteCreated = true;
//            }
    }

    @Override
    protected void tearDown() throws InterruptedException, IOException {
        try {
            fs.delete(testdir);
            if (defaultSiteCreated) {
                TestHelper.deleteSite(ACMESITE_NAME);
            }
            if (testSiteCreated) {
                TestHelper.deleteSite(TESTSITE_NAME);
            }
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }

    public void testFetch() throws IOException {

        try {
            // generate seedlist
            ArrayList<String> urls = new ArrayList<String>();
            try {
                RenderContext renderCtx = new RenderContext(((ParamBean) ctx).getRequest(), ((ParamBean) ctx).getResponse(), ctx.getUser());
                if (defaultSite != null) {
                    Resource resource = new Resource(defaultSite.getHomeContentPage().getJCRNode(ctx), "html", null,
                            null);
                    URLGenerator urlgenerator = new URLGenerator(renderCtx, resource, ServicesRegistry.getInstance()
                            .getJCRStoreService());
                    addUrl(urls, urlgenerator.getEdit(), ctx);
                    addUrl(urls, urlgenerator.getLive(), ctx);
                }
                if (testSite != null) {
                    Resource resource = new Resource(testSite.getHomeContentPage().getJCRNode(ctx), "html", null,
                            null);
                    URLGenerator urlgenerator = new URLGenerator(renderCtx, resource, ServicesRegistry.getInstance()
                            .getJCRStoreService());
                    addUrl(urls, urlgenerator.getEdit(), ctx);
                    addUrl(urls, urlgenerator.getLive(), ctx);
                }
            } catch (JahiaException e) {
                logger.warn("Cannot get all homepage urls", e);
            }
            final StringBuffer adminUrl = new StringBuffer();
            adminUrl.append(ctx.getContextPath()).append(
                    Jahia.getInitAdminServletPath()).append("?do=passthru");
            addUrl(urls, adminUrl.toString(), ctx);

            CrawlDBTestUtil.generateSeedList(fs, urlPath, urls);

            // inject
            Injector injector = new Injector(conf);
            injector.inject(crawldbPath, urlPath);

            // generate
            Generator g = new Generator(conf);
            Path generatedSegment = g.generate(crawldbPath, segmentsPath, 1,
                    Long.MAX_VALUE, Long.MAX_VALUE, false, false);

            // fetch
            conf.setBoolean("fetcher.parse", true);
            Fetcher fetcher = new Fetcher(conf);
            fetcher.fetch(generatedSegment, 1, true);
        } catch (Exception e) {
            logger.error("Exception during test", e);
        }

    }

    private void addUrl(ArrayList<String> urls, String pageUrl,
                        ProcessingContext ctx) {
        final StringBuilder newSiteURL = new StringBuilder(64);
        newSiteURL.append(ctx.getScheme()).append("://");
        newSiteURL.append(ctx.getServerName());
        if (ctx.getServerPort() != 80) {
            newSiteURL.append(":");
            newSiteURL.append(ctx.getServerPort());
        }
        newSiteURL.append(pageUrl);
        urls.add(newSiteURL.toString());
    }
}
