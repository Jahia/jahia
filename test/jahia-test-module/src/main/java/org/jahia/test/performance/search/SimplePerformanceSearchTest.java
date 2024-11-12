/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.test.performance.search;

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
import org.jahia.services.search.SearchCriteria.CommaSeparatedMultipleValue;
import org.jahia.services.search.SearchService;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.util.StopWatch;

import javax.jcr.RepositoryException;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Locale;

/**
 * Unit test for simple fulltext search
 * settings (all, none, one, two languages) - with using user not having rights - publication with automatically publishing parent
 *
 * @author Benjamin Papez
 */
public class SimplePerformanceSearchTest extends JahiaTestCase {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(SimplePerformanceSearchTest.class);
    private final static String FIRST_TESTSITE_NAME = "jcrSearchTest";
    private final static String FIRST_SITECONTENT_ROOT_NODE = "/sites/" + FIRST_TESTSITE_NAME;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        TestHelper.createSite(FIRST_TESTSITE_NAME, "localhost", TestHelper.WEB_TEMPLATES,
                                "prepackagedSites/webtemplates.zip", "ACME.zip");
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
            Assert.fail();
        }
    }

    @Test
    public void testSimpleFulltextSearchOnSingleSite() throws Exception {
        SearchService searchService = ServicesRegistry.getInstance().getSearchService();
        RenderContext context = new RenderContext(getRequest(), getResponse(), getUser());
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(null, Locale.ENGLISH);
        JCRNodeWrapper homeNode = session.getNode(FIRST_SITECONTENT_ROOT_NODE + "/home");
        Resource resource = new Resource(homeNode, "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        context.setSite(homeNode.getResolveSite());
        new URLGenerator(context, resource);

        SearchCriteria criteria = createSearchCriteria();
        StopWatch stopWatch = new StopWatch("search");
        stopWatch.start("Starting 1000 searchs");
        for (int j = 0; j < 1000; j++) {
            List<Hit<?>> hits = searchService.search(criteria, context).getResults();
            int i = 0;
            for (Hit<?> hit : hits) {
                logger.info("[" + j + "][" + (++i) + "]: " + hit.getLink());
            }
        }
        stopWatch.stop();
        logger.info(stopWatch.prettyPrint());
    }

    private SearchCriteria createSearchCriteria() {
        SearchCriteria criteria = new SearchCriteria();

        CommaSeparatedMultipleValue oneSite = new CommaSeparatedMultipleValue();
        oneSite.setValue(FIRST_TESTSITE_NAME);

        CommaSeparatedMultipleValue englishLang = new CommaSeparatedMultipleValue();
        englishLang.setValue("en");

        criteria.setSites(oneSite);
        criteria.setLanguages(englishLang);
        criteria.getTerms().get(0).setTerm("ACME");
        criteria.getTerms().get(0).getFields().setSiteContent(true);
        return criteria;
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite(FIRST_TESTSITE_NAME);
    }

}
