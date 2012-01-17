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

package org.jahia.services.search;

import org.slf4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.search.SearchCriteria.CommaSeparatedMultipleValue;
import org.jahia.services.search.SearchCriteria.Term.MatchType;
import org.jahia.settings.SettingsBean;
import org.jahia.test.TestHelper;

import java.util.*;

import static org.junit.Assert.*;
import org.junit.*;

import javax.jcr.RepositoryException;

/**
 * Unit test for simple fulltext search
 * settings (all, none, one, two languages) - with using user not having rights - publication with automatically publishing parent
 * 
 * @author Benjamin Papez
 * 
 */
public class SimpleSearchTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(SimpleSearchTest.class);
    private final static String FIRST_TESTSITE_NAME = "jcrSearchTest";
    private final static String SECOND_TESTSITE_NAME = "jcrSearchTest2";
    private final static String FIRST_SITECONTENT_ROOT_NODE = "/sites/"
            + FIRST_TESTSITE_NAME;
    private final static String SECOND_SITECONTENT_ROOT_NODE = "/sites/"
            + SECOND_TESTSITE_NAME;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
//            final JCRPublicationService jcrService = ServicesRegistry
//                    .getInstance().getJCRPublicationService();
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        TestHelper.createSite(FIRST_TESTSITE_NAME, "localhost",
                                TestHelper.WEB_TEMPLATES, SettingsBean.getInstance()
                                        .getJahiaVarDiskPath()
                                        + "/prepackagedSites/webtemplates.zip", "ACME.zip");
//                        jcrService.publishByMainId(
//                                session.getNode(FIRST_SITECONTENT_ROOT_NODE + "/home")
//                                        .getIdentifier(), Constants.EDIT_WORKSPACE,
//                                Constants.LIVE_WORKSPACE, null, true, null);
                    } catch (Exception e) {
                        logger.error("Cannot create or publish site", e);
                    }
                    session.save();
                    return null;
                }
            });
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            session.getUuidMapping().clear();
            session.getPathMapping().clear();
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        TestHelper.createSite(SECOND_TESTSITE_NAME, "127.0.0.1",
                                TestHelper.WEB_TEMPLATES, SettingsBean.getInstance()
                                        .getJahiaVarDiskPath()
                                        + "/prepackagedSites/webtemplates.zip", "ACME.zip");
//                        jcrService.publishByMainId(
//                                session.getNode(SECOND_SITECONTENT_ROOT_NODE + "/home")
//                                        .getIdentifier(), Constants.EDIT_WORKSPACE,
//                                Constants.LIVE_WORKSPACE, null, true, null);
                    } catch (Exception e) {
                        logger.error("Cannot create or publish site", e);
                    }
                    session.save();
                    return null;
                }
            });
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }
    }

    @Test
    public void testSimpleFulltextSearchOnSingleSite() throws Exception {
        SearchService searchService = ServicesRegistry.getInstance()
                .getSearchService();
        try {
            ProcessingContext ctx = Jahia.getThreadParamBean();
            RenderContext context = new RenderContext(((ParamBean) ctx)
                    .getRequest(), ((ParamBean) ctx).getResponse(), ctx
                    .getUser());
            JCRSessionWrapper session = JCRSessionFactory.getInstance()
                    .getCurrentUserSession(null, Locale.ENGLISH);
            JCRNodeWrapper homeNode = session
                    .getNode(FIRST_SITECONTENT_ROOT_NODE + "/home");
            Resource resource = new Resource(homeNode, "html", null, Resource.CONFIGURATION_PAGE);
            context.setMainResource(resource);
            context.setSite(homeNode.getResolveSite());
            new URLGenerator(context, resource);

            SearchCriteria criteria = new SearchCriteria();

            CommaSeparatedMultipleValue oneSite = new CommaSeparatedMultipleValue();
            oneSite.setValue(FIRST_TESTSITE_NAME);

            CommaSeparatedMultipleValue englishLang = new CommaSeparatedMultipleValue();
            englishLang.setValue("en");

            criteria.setSites(oneSite);
            criteria.setLanguages(englishLang);
            criteria.getTerms().get(0).setTerm("ACME");
            criteria.getTerms().get(0).getFields().setSiteContent(true);

            List<Hit<?>> hits = searchService.search(criteria, context)
                    .getResults();
            int i = 0;
            for (Hit<?> hit : hits) {
                logger.info("[" + (++i) + "]: " + hit.getLink());
            }
            assertEquals("Unexpected number of search results for: " + criteria.toString(),
                    21, hits.size());
        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        }
    }

    @Test
    public void testSimpleFulltextSearchOnSingleSiteInFrench() throws Exception {
        SearchService searchService = ServicesRegistry.getInstance()
                .getSearchService();
        try {
            ProcessingContext ctx = Jahia.getThreadParamBean();
            RenderContext context = new RenderContext(((ParamBean) ctx)
                    .getRequest(), ((ParamBean) ctx).getResponse(), ctx
                    .getUser());
            JCRSessionWrapper session = JCRSessionFactory.getInstance()
                    .getCurrentUserSession(null, Locale.FRENCH);
            JCRNodeWrapper homeNode = session
                    .getNode(FIRST_SITECONTENT_ROOT_NODE + "/home");
            Resource resource = new Resource(homeNode, "html", null, Resource.CONFIGURATION_PAGE);
            context.setMainResource(resource);
            context.setSite(homeNode.getResolveSite());
            new URLGenerator(context, resource);

            SearchCriteria criteria = new SearchCriteria();

            CommaSeparatedMultipleValue oneSite = new CommaSeparatedMultipleValue();
            oneSite.setValue(FIRST_TESTSITE_NAME);

            CommaSeparatedMultipleValue frenchLang = new CommaSeparatedMultipleValue();
            frenchLang.setValue("fr");

            criteria.setSites(oneSite);
            criteria.setLanguages(frenchLang);
            criteria.getTerms().get(0).setTerm("ACME");
            criteria.getTerms().get(0).getFields().setSiteContent(true);

            List<Hit<?>> hits = searchService.search(criteria, context)
                    .getResults();
            assertEquals("Unexpected number of search results for: " + criteria.toString(),
                    21, hits.size());
        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        }
    }

    @Test
    public void testSimpleFulltextSearchOnSingleSiteInDocsOnly()
            throws Exception {
        SearchService searchService = ServicesRegistry.getInstance()
                .getSearchService();
        try {
            ProcessingContext ctx = Jahia.getThreadParamBean();
            RenderContext context = new RenderContext(((ParamBean) ctx)
                    .getRequest(), ((ParamBean) ctx).getResponse(), ctx
                    .getUser());
            JCRSessionWrapper session = JCRSessionFactory.getInstance()
                    .getCurrentUserSession(null, Locale.ENGLISH);
            JCRNodeWrapper homeNode = session
                    .getNode(FIRST_SITECONTENT_ROOT_NODE + "/home");
            Resource resource = new Resource(homeNode, "html", null, Resource.CONFIGURATION_PAGE);
            context.setMainResource(resource);
            context.setSite(homeNode.getResolveSite());
            new URLGenerator(context, resource);

            SearchCriteria criteria = new SearchCriteria();

            CommaSeparatedMultipleValue oneSite = new CommaSeparatedMultipleValue();
            oneSite.setValue(FIRST_TESTSITE_NAME);

            CommaSeparatedMultipleValue englishLang = new CommaSeparatedMultipleValue();
            englishLang.setValue("en");

            criteria.setSites(oneSite);
            criteria.setLanguages(englishLang);
            criteria.getTerms().get(0).setTerm("ACME");
            criteria.getTerms().get(0).getFields().setFileContent(true);
            criteria.getTerms().get(0).getFields().setTitle(true);
            criteria.getTerms().get(0).getFields().setDescription(true);
            criteria.getTerms().get(0).getFields().setFilename(true);
            criteria.getTerms().get(0).getFields().setKeywords(true);

            List<Hit<?>> hits = searchService.search(criteria, context)
                    .getResults();
            assertEquals("Unexpected number of search results for: " + criteria.toString(),
                    16, hits.size());

            criteria.setFileType("pdf");
            hits = searchService.search(criteria, context).getResults();
            assertEquals("Unexpected number of search results for: " + criteria.toString(),
                    10, hits.size());
        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        }
    }

    @Test
    public void testFulltextMatchTypeSearchOnSingleSite() throws Exception {
        SearchService searchService = ServicesRegistry.getInstance()
                .getSearchService();
        try {
            ProcessingContext ctx = Jahia.getThreadParamBean();
            RenderContext context = new RenderContext(((ParamBean) ctx)
                    .getRequest(), ((ParamBean) ctx).getResponse(), ctx
                    .getUser());
            JCRSessionWrapper session = JCRSessionFactory.getInstance()
                    .getCurrentUserSession(null, Locale.ENGLISH);
            JCRNodeWrapper homeNode = session
                    .getNode(FIRST_SITECONTENT_ROOT_NODE + "/home");
            Resource resource = new Resource(homeNode, "html", null, Resource.CONFIGURATION_PAGE);
            context.setMainResource(resource);
            context.setSite(homeNode.getResolveSite());
            new URLGenerator(context, resource);

            SearchCriteria criteria = new SearchCriteria();

            CommaSeparatedMultipleValue oneSite = new CommaSeparatedMultipleValue();
            oneSite.setValue(FIRST_TESTSITE_NAME);

            CommaSeparatedMultipleValue englishLang = new CommaSeparatedMultipleValue();
            englishLang.setValue("en");

            criteria.setSites(oneSite);
            criteria.setLanguages(englishLang);
            criteria.getTerms().get(0).setTerm("civil Polytech");
            criteria.getTerms().get(0).setMatch(MatchType.ALL_WORDS);
            criteria.getTerms().get(0).getFields().setSiteContent(true);

            List<Hit<?>> hits = searchService.search(criteria, context)
                    .getResults();
            assertEquals("Unexpected number of search results for: " + criteria.toString(),
                    1, hits.size());

            criteria.getTerms().get(0).setTerm("civil Polytech");
            criteria.getTerms().get(0).setMatch(MatchType.ANY_WORD);
            hits = searchService.search(criteria, context).getResults();
            assertEquals("Unexpected number of search results for: " + criteria.toString(),
                    6, hits.size());

            criteria.getTerms().get(0).setTerm("civil engineering");
            criteria.getTerms().get(0).setMatch(MatchType.EXACT_PHRASE);
            hits = searchService.search(criteria, context).getResults();
            assertEquals("Unexpected number of search results for: " + criteria.toString(),
                    5, hits.size());

            criteria.getTerms().get(0).setTerm("civil -engineering");
            criteria.getTerms().get(0).setMatch(MatchType.AS_IS);
            hits = searchService.search(criteria, context).getResults();
            assertEquals("Unexpected number of search results for: " + criteria.toString(),
                    2, hits.size());

            criteria.getTerms().get(0).setTerm("civil");
            criteria.getTerms().get(0).setMatch(MatchType.ANY_WORD);
            criteria.getTerms().get(1).setTerm("engineering");
            criteria.getTerms().get(1).setMatch(MatchType.WITHOUT_WORDS);
            hits = searchService.search(criteria, context).getResults();
            assertEquals("Unexpected number of search results for: " + criteria.toString(),
                    2, hits.size());
        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        }
    }

    @Test
    public void testSimpleFulltextSearchOnTwoSites() throws Exception {
        SearchService searchService = ServicesRegistry.getInstance()
                .getSearchService();
        try {
            ProcessingContext ctx = Jahia.getThreadParamBean();
            RenderContext context = new RenderContext(((ParamBean) ctx)
                    .getRequest(), ((ParamBean) ctx).getResponse(), ctx
                    .getUser());
            JCRSessionWrapper session = JCRSessionFactory.getInstance()
                    .getCurrentUserSession();
            JCRNodeWrapper homeNode = session
                    .getNode(SECOND_SITECONTENT_ROOT_NODE + "/home");
            Resource resource = new Resource(homeNode, "html", null, Resource.CONFIGURATION_PAGE);
            context.setMainResource(resource);
            context.setSite(homeNode.getResolveSite());
            new URLGenerator(context, resource);

            SearchCriteria criteria = new SearchCriteria();

            CommaSeparatedMultipleValue twoSites = new CommaSeparatedMultipleValue();
            twoSites.setValue(FIRST_TESTSITE_NAME + "," + SECOND_TESTSITE_NAME);

            CommaSeparatedMultipleValue englishLang = new CommaSeparatedMultipleValue();
            englishLang.setValue("en");

            criteria.setSites(twoSites);
            criteria.setLanguages(englishLang);
            criteria.getTerms().get(0).setTerm("ACME");
            criteria.getTerms().get(0).getFields().setSiteContent(true);

            List<Hit<?>> hits = searchService.search(criteria, context)
                    .getResults();
            assertEquals("Unexpected number of search results for: "
                            + criteria.toString(), 21 * 2, hits.size());
        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        }
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite(FIRST_TESTSITE_NAME);
        TestHelper.deleteSite(SECOND_TESTSITE_NAME);
    }

}