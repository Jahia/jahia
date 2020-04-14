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

import static org.assertj.core.api.Assertions.assertThat;
import static org.jahia.services.sites.JahiaSitesService.SITES_JCR_PATH;
import static org.jahia.services.sites.JahiaSitesService.SYSTEM_SITE_KEY;

import java.io.ByteArrayInputStream;
import java.util.Locale;

import javax.jcr.RepositoryException;

import org.apache.commons.codec.Charsets;
import org.assertj.core.api.AbstractObjectAssert;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.search.SearchCriteria;
import org.jahia.services.search.SearchCriteria.Term;
import org.jahia.services.search.SearchCriteria.Term.MatchType;
import org.jahia.services.search.SearchService;
import org.jahia.services.search.Suggestion;
import org.jahia.services.search.spell.CompositeSpellChecker;
import org.jahia.test.TestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Integration test for the "did you mean" search feature.
 *
 * @author Sergiy Shyrkov
 */
public class SearchSuggestionsTest {

    private static final String CONTENT_FOLDER = "SearchSuggestionsTest-contents";
    private static final String CONTENT_FOLDER_PARENT_PATH = SITES_JCR_PATH + "/" + getSiteKey() + "/contents";
    private static final String CONTENT_FOLDER_PATH = CONTENT_FOLDER_PARENT_PATH + "/" + CONTENT_FOLDER;

    private static final String FILE_FOLDER = "SearchSuggestionsTest-files";
    private static final String FILE_FOLDER_PARENT_PATH = SITES_JCR_PATH + "/" + getSiteKey() + "/files";
    private static final String FILE_FOLDER_PATH = FILE_FOLDER_PARENT_PATH + "/" + FILE_FOLDER;

    private static SearchService searchService;

    private static final Locale TEST_LOCALE = Locale.ENGLISH;

    private static void deleteTestContent(JCRSessionWrapper session) throws RepositoryException {
        if (session.nodeExists(CONTENT_FOLDER_PATH)) {
            session.getNode(CONTENT_FOLDER_PATH).remove();
        }
        if (session.nodeExists(FILE_FOLDER_PATH)) {
            session.getNode(FILE_FOLDER_PATH).remove();
        }

        session.save();
    }

    private static String getSiteKey() {
        return SYSTEM_SITE_KEY;
    }

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        searchService = ServicesRegistry.getInstance().getSearchService();

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                TEST_LOCALE);
        deleteTestContent(session);

        JCRNodeWrapper contentFolder = session.getNode(CONTENT_FOLDER_PARENT_PATH).addNode(CONTENT_FOLDER,
                "jnt:contentFolder");
        JCRNodeWrapper fileFolder = session.getNode(FILE_FOLDER_PARENT_PATH).addNode(FILE_FOLDER,
                Constants.JAHIANT_FOLDER);

        // create one i18n content with 'digital' term
        JCRNodeWrapper text = contentFolder.addNode("text-1", "jnt:text");
        text.setProperty("text", "Make digital simpler with Jahia");

        // create one non-i18n content with 'velden' term
        JCRNodeWrapper location = contentFolder.addNode("location-1", "jnt:location");
        location.setProperty("j:street", "Excelsiorstraße 25");
        location.setProperty("j:zipCode", "9220");
        location.setProperty("j:town", "Velden am Wörther See");
        location.setProperty("j:country", "AT");

        // upload text file with 'jahia' term
        fileFolder.uploadFile("file.txt", new ByteArrayInputStream(
                "Created in Switzerland in 2002 by a web-addict team, Jahia was a pioneer of the 'Content Management System' concept which simplifies editing content for users."
                        .getBytes(Charsets.UTF_8)),
                "text/plain");

        // create two i18n contents with misspelled 'biutiful' term
        text = contentFolder.addNode("text-2", "jnt:text");
        text.setProperty("text", "biutiful");
        text = contentFolder.addNode("text-3", "jnt:text");
        text.setProperty("text", "biutiful");

        // create one i18n content with correctly spelled 'beautiful' term
        text = contentFolder.addNode("text-4", "jnt:text");
        text.setProperty("text", "beautiful");

        session.save();

        // wait for text extraction job to finish
        TestHelper.triggerScheduledJobsAndWait();

        // update the spell checker index (waiting to it to finish)
        CompositeSpellChecker.updateSpellCheckerIndex(false);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        deleteTestContent(JCRSessionFactory.getInstance().getCurrentUserSession());
        searchService = null;
    }

    private void doSuggestionTest(String searchTerm, String expectedSuggestion) throws RepositoryException {
        Suggestion suggestion = searchService.suggest(getSearchCriteriaForTerm(searchTerm), getRenderContext(), 1);
        AbstractObjectAssert<?, Suggestion> assertion = assertThat(suggestion)
                .describedAs("Expected search suggestion");
        if (expectedSuggestion != null) {
            assertion.isNotNull().hasFieldOrPropertyWithValue("suggestedQuery", expectedSuggestion);
        } else {
            assertion.isNull();
        }

    }

    private RenderContext getRenderContext() throws RepositoryException {
        return SimpleSearchTest.getContext(SITES_JCR_PATH + "/" + getSiteKey(), TEST_LOCALE);
    }

    private SearchCriteria getSearchCriteriaForTerm(String termText) {
        SearchCriteria criteria = new SearchCriteria();

        criteria.getSites().setValue(getSiteKey());
        criteria.getLanguages().setValue(TEST_LOCALE.toString());

        Term term = criteria.getTerms().get(0);
        term.setTerm(termText);
        term.setMatch(MatchType.ALL_WORDS);
        term.getFields().setAll(true);

        return criteria;
    }

    @Test
    public void testSuggestionInFiles() throws RepositoryException {
        doSuggestionTest("jahio", "jahia");
    }

    @Test
    public void testSuggestionInI18nContent() throws RepositoryException {
        doSuggestionTest("digitil", "digital");
    }

    @Test
    public void testSuggestionInNonI18nContent() throws RepositoryException {
        doSuggestionTest("veldin", "velden");
    }

    @Test
    public void testSuggestionMisspelled() throws RepositoryException {
        // here as the misspelled term 'biutiful' occurs 2 times, but its suggestion 'beautiful' only once,
        // it should not be suggested instead
        doSuggestionTest("biutiful", null);
    }
}
