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
import org.jahia.settings.SettingsBean;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.junit.AfterClass;
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
                                SettingsBean.getInstance().getJahiaVarDiskPath() + "/prepackagedSites/webtemplates.zip",
                                "ACME.zip");
                    } catch (Exception e) {
                        logger.error("Cannot create or publish site", e);
                        fail("Cannot create or publish site");
                    }
                    session.save();
                    return null;
                }
            });
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
            fail();
        }
    }

    @Test
    public void testSimpleFulltextSearchOnSingleSite() throws Exception {
        SearchService searchService = ServicesRegistry.getInstance().getSearchService();
        try {
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
        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        }
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