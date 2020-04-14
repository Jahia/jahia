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
package org.jahia.test.services.search;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.search.Hit;
import org.jahia.services.search.SearchCriteria;
import org.jahia.services.search.SearchService;
import org.jahia.services.search.SearchCriteria.CommaSeparatedMultipleValue;
import org.jahia.services.search.SearchCriteria.Term.MatchType;
import org.jahia.test.JahiaTestCase;
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
public class SimpleSearchTest extends JahiaTestCase {
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
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        TestHelper.createSite(FIRST_TESTSITE_NAME, "localhost", TestHelper.WEB_TEMPLATES,
                                "prepackagedSites/acme.zip", "ACME.zip");
                        session.save();
                    } catch (Exception ex) {
                        logger.warn("Exception during site creation", ex);
                        fail("Exception during site creation");
                    }
                    return null;
                }
            });
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            session.getUuidMapping().clear();
            session.getPathMapping().clear();
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        TestHelper.createSite(SECOND_TESTSITE_NAME, "127.0.0.1", TestHelper.WEB_TEMPLATES,
                                "prepackagedSites/acme.zip", "ACME.zip");
                        session.save();
                    } catch (Exception ex) {
                        logger.warn("Exception during site creation", ex);
                        fail("Exception during site creation");
                    }
                    return null;
                }
            });
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
            fail();
        }
    }

    private RenderContext getContext() throws RepositoryException {
    	return getContext(FIRST_SITECONTENT_ROOT_NODE, Locale.ENGLISH);
    }
    
    protected static RenderContext getContext(String siteRootNode, Locale locale) throws RepositoryException {
        RenderContext context = new RenderContext(getRequest(), getResponse(), getUser());
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(null, locale);
        JCRNodeWrapper homeNode = session
                .getNode(siteRootNode + "/home");
        Resource resource = new Resource(homeNode, "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        context.setSite(homeNode.getResolveSite());
        context.setServletPath("/cms/render");
        new URLGenerator(context, resource);
        
        return context;
    }
    
    @Test
    public void testSimpleFulltextSearchOnSingleSite() throws Exception {
        SearchService searchService = ServicesRegistry.getInstance()
                .getSearchService();
        RenderContext context = getContext();

        SearchCriteria criteria = new SearchCriteria();

        CommaSeparatedMultipleValue oneSite = new CommaSeparatedMultipleValue();
        oneSite.setValue(FIRST_TESTSITE_NAME);

        CommaSeparatedMultipleValue englishLang = new CommaSeparatedMultipleValue();
        englishLang.setValue("en");

        criteria.setSites(oneSite);
        criteria.setLanguages(englishLang);
        criteria.getTerms().get(0).setTerm("ACME");
        criteria.getTerms().get(0).getFields().setSiteContent(true);

        List<Hit<?>> hits = searchService.search(criteria, context).getResults();
        int i = 0;
        for (Hit<?> hit : hits) {
            logger.info("[" + (++i) + "]: " + hit.getLink());
        }
        assertEquals("Unexpected number of search results for: " + criteria.toString(), 19, hits.size());
    }

    @Test
    public void testSimpleFulltextSearchOnSingleSiteInFrench() throws Exception {
        SearchService searchService = ServicesRegistry.getInstance()
                .getSearchService();
        RenderContext context = getContext(FIRST_SITECONTENT_ROOT_NODE, Locale.FRENCH);

        SearchCriteria criteria = new SearchCriteria();

        CommaSeparatedMultipleValue oneSite = new CommaSeparatedMultipleValue();
        oneSite.setValue(FIRST_TESTSITE_NAME);

        CommaSeparatedMultipleValue frenchLang = new CommaSeparatedMultipleValue();
        frenchLang.setValue("fr");

        criteria.setSites(oneSite);
        criteria.setLanguages(frenchLang);
        criteria.getTerms().get(0).setTerm("ACME");
        criteria.getTerms().get(0).getFields().setSiteContent(true);

        List<Hit<?>> hits = searchService.search(criteria, context).getResults();
        assertEquals("Unexpected number of search results for: " + criteria.toString(), 15, hits.size());
    }

    @Test
    public void testSimpleFulltextSearchOnSingleSiteInDocsOnly()
            throws Exception {
        SearchService searchService = ServicesRegistry.getInstance()
                .getSearchService();
        RenderContext context = getContext();

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

        List<Hit<?>> hits = searchService.search(criteria, context).getResults();
        assertEquals("Unexpected number of search results for: " + criteria.toString(), 15, hits.size());

        criteria.setFileType("pdf");
        hits = searchService.search(criteria, context).getResults();
        assertEquals("Unexpected number of search results for: " + criteria.toString(), 10, hits.size());
    }

    @Test
    public void testFulltextMatchTypeSearchOnSingleSite() throws Exception {
        SearchService searchService = ServicesRegistry.getInstance()
                .getSearchService();
        RenderContext context = getContext();

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

        List<Hit<?>> hits = searchService.search(criteria, context).getResults();
        assertEquals("Unexpected number of search results for: " + criteria.toString(), 1, hits.size());

        criteria.getTerms().get(0).setTerm("civil Polytech");
        criteria.getTerms().get(0).setMatch(MatchType.ANY_WORD);
        hits = searchService.search(criteria, context).getResults();
        assertEquals("Unexpected number of search results for: " + criteria.toString(), 3, hits.size());

        criteria.getTerms().get(0).setTerm("civil engineering");
        criteria.getTerms().get(0).setMatch(MatchType.EXACT_PHRASE);
        hits = searchService.search(criteria, context).getResults();
        assertEquals("Unexpected number of search results for: " + criteria.toString(), 3, hits.size());

        criteria.getTerms().get(0).setTerm("civil -engineering");
        criteria.getTerms().get(0).setMatch(MatchType.AS_IS);
        hits = searchService.search(criteria, context).getResults();
        assertEquals("Unexpected number of search results for: " + criteria.toString(), 0, hits.size());

        criteria.getTerms().get(0).setTerm("civil");
        criteria.getTerms().get(0).setMatch(MatchType.ANY_WORD);
        criteria.getTerms().get(1).setTerm("engineering");
        criteria.getTerms().get(1).setMatch(MatchType.WITHOUT_WORDS);
        hits = searchService.search(criteria, context).getResults();
        assertEquals("Unexpected number of search results for: " + criteria.toString(), 0, hits.size());
    }

    @Test
    public void testSimpleFulltextSearchOnTwoSites() throws Exception {
        SearchService searchService = ServicesRegistry.getInstance()
                .getSearchService();
        RenderContext context = getContext(SECOND_SITECONTENT_ROOT_NODE, Locale.ENGLISH);

        SearchCriteria criteria = new SearchCriteria();

        CommaSeparatedMultipleValue twoSites = new CommaSeparatedMultipleValue();
        twoSites.setValue(FIRST_TESTSITE_NAME + "," + SECOND_TESTSITE_NAME);

        CommaSeparatedMultipleValue englishLang = new CommaSeparatedMultipleValue();
        englishLang.setValue("en");

        criteria.setSites(twoSites);
        criteria.setLanguages(englishLang);
        criteria.getTerms().get(0).setTerm("ACME");
        criteria.getTerms().get(0).getFields().setSiteContent(true);

        List<Hit<?>> hits = searchService.search(criteria, context).getResults();
        assertEquals("Unexpected number of search results for: " + criteria.toString(), 19 * 2, hits.size());
    }

    @Test
    public void testExcludeFileReferences() throws Exception {
        // Test cases for QA-9456
        SearchService searchService = ServicesRegistry.getInstance()
                .getSearchService();
       RenderContext context = getContext();

        SearchCriteria criteria = new SearchCriteria();

        CommaSeparatedMultipleValue oneSite = new CommaSeparatedMultipleValue();
        oneSite.setValue(FIRST_TESTSITE_NAME);

        CommaSeparatedMultipleValue englishLang = new CommaSeparatedMultipleValue();
        englishLang.setValue("en");

        criteria.setSites(oneSite);
        criteria.setLanguages(englishLang);
        criteria.getTerms().get(0).setTerm("Building Bridges");
        criteria.getTerms().get(0).setMatch(MatchType.ALL_WORDS);
        criteria.getTerms().get(0).getFields().setSiteContent(true);
        criteria.getTerms().get(0).getFields().setFiles(true);

        // search in site content and files
        verifyResults(searchService.search(criteria, context).getResults(), ImmutableMap.of("jnt:file", 1, "jnt:publication", 1));

        // search in files only
        criteria.getTerms().get(0).getFields().setSiteContent(false);
        verifyResults(searchService.search(criteria, context).getResults(), ImmutableMap.of("jnt:file", 1));

        // search in site content only (but considering file references)
        criteria.getTerms().get(0).getFields().setSiteContent(true);
        criteria.getTerms().get(0).getFields().setFiles(false);
        verifyResults(searchService.search(criteria, context).getResults(), ImmutableMap.of("jnt:file", 1, "jnt:publication", 1));

        // search in site content only (excluding file references)
        criteria.getTerms().get(0).getFields().setSiteContent(true);
        criteria.getTerms().get(0).getFields().setFiles(false);
        criteria.setExcludeFileReferences(true);
        verifyResults(searchService.search(criteria, context).getResults(), ImmutableMap.of("jnt:publication", 1));

        // search in site content and file (excluding file references should have no influence here)
        criteria.getTerms().get(0).getFields().setSiteContent(true);
        criteria.getTerms().get(0).getFields().setFiles(true);
        criteria.setExcludeFileReferences(true);
        verifyResults(searchService.search(criteria, context).getResults(), ImmutableMap.of("jnt:file", 1, "jnt:publication", 1));
    }

    private static final void verifyResults(List<Hit<?>> hits, Map<String, Integer> expectedCountsByType) {
        Map<String, Integer> countsByType = new HashMap<>();
        for (Hit<?> hit : hits) {
            if (countsByType.containsKey(hit.getType())) {
                countsByType.put(hit.getType(), countsByType.get(hit.getType()).intValue() + 1);
            } else {
                countsByType.put(hit.getType(), 1);
            }
        }
        
        assertEquals("Expected result counts/types are not matched", expectedCountsByType, countsByType);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite(FIRST_TESTSITE_NAME);
        TestHelper.deleteSite(SECOND_TESTSITE_NAME);
    }

}