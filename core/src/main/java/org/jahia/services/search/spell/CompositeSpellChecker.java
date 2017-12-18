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
package org.jahia.services.search.spell;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.query.QueryHandler;
import org.apache.jackrabbit.core.query.lucene.FieldNames;
import org.apache.jackrabbit.core.query.lucene.JahiaIndexingConfigurationImpl;
import org.apache.jackrabbit.core.query.lucene.JahiaSecondaryIndex;
import org.apache.jackrabbit.core.query.lucene.SearchIndex;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.query.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.JahiaExtendedSpellChecker;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.StringDistance;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NativeFSLockFactory;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.DateUtils;
import org.jahia.utils.LuceneUtils;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <code>LuceneSpellChecker</code> implements a spell checker based on the terms
 * present in a lucene index.
 */
public class CompositeSpellChecker implements org.apache.jackrabbit.core.query.lucene.SpellChecker {

    /**
     * Logger instance for this class.
     */
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(CompositeSpellChecker.class);

    public static final String SEPARATOR_IN_SUGGESTION = "#!#";
    public static final String MAX_TERMS_PARAM = "maxTerms";
    public static final String SITES_PARAM = "sites";

    public static final class FiveSecondsRefreshInterval extends CompositeSpellChecker {
        public FiveSecondsRefreshInterval() {
            super(5 * 1000);
        }
    }

    public static final class OneMinuteRefreshInterval extends CompositeSpellChecker {
        public OneMinuteRefreshInterval() {
            super(60 * 1000);
        }
    }

    public static final class FiveMinutesRefreshInterval extends CompositeSpellChecker {
        public FiveMinutesRefreshInterval() {
            super(5 * 60 * 1000);
        }
    }

    public static final class ThirtyMinutesRefreshInterval extends CompositeSpellChecker {
        public ThirtyMinutesRefreshInterval() {
            super(30 * 60 * 1000);
        }
    }

    public static final class OneHourRefreshInterval extends CompositeSpellChecker {
        public OneHourRefreshInterval() {
            super(60 * 60 * 1000);
        }
    }

    public static final class SixHoursRefreshInterval extends CompositeSpellChecker {
        public SixHoursRefreshInterval() {
            super(6 * 60 * 60 * 1000);
        }
    }

    public static final class TwelveHoursRefreshInterval extends CompositeSpellChecker {
        public TwelveHoursRefreshInterval() {
            super(12 * 60 * 60 * 1000);
        }
    }

    public static final class OneDayRefreshInterval extends CompositeSpellChecker {
        public OneDayRefreshInterval() {
            super(24 * 60 * 60 * 1000);
        }
    }

    private static Map<String, InternalSpellChecker> spellCheckers = new ConcurrentHashMap<String, InternalSpellChecker>(2);

    /**
     * Triggers update of the spell checker dictionary index.
     */
    public static void updateSpellCheckerIndex() {
        for (InternalSpellChecker checker : spellCheckers.values()) {
            checker.lastRefresh = 0;
            checker.refreshSpellChecker();
        }
    }

    /**
     * The internal spell checker.
     */
    private InternalSpellChecker spellChecker;

    /**
     * The refresh interval.
     */
    private final long refreshInterval;

    /**
     * Spell checker with a default refresh interval of one hour.
     */
    public CompositeSpellChecker() {
        this(60 * 60 * 1000); // default refresh interval: one hour
    }

    protected CompositeSpellChecker(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    /**
     * Initializes this spell checker.
     *
     * @param handler the query handler that created this spell checker.
     * @throws IOException if <code>handler</code> is not of type {@link SearchIndex}.
     */
    public void init(QueryHandler handler) throws IOException {
        if (handler instanceof SearchIndex) {
            this.spellChecker = new InternalSpellChecker((SearchIndex) handler);
            spellCheckers.put(((SearchIndex) handler).getPath(), spellChecker);
        } else {
            throw new IOException("CompositeSpellChecker only works with " + SearchIndex.class.getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    public String check(QueryRootNode aqt) throws IOException {
        final Map<String, String> spellcheckInfo = new HashMap<String, String>();
        try {
            aqt.accept(new TraversingQueryNodeVisitor() {
                public Object visit(RelationQueryNode node, Object data) throws RepositoryException {
                    if (!spellcheckInfo.containsKey("statement")
                            && node.getOperation() == RelationQueryNode.OPERATION_SPELLCHECK) {
                        String spellCheckParams = node.getStringValue();
                        String[] s = spellCheckParams.split(SEPARATOR_IN_SUGGESTION);
                        spellcheckInfo.put("statement", s[0]);
                        spellcheckInfo.put("maxTermCount", StringUtils.substringAfter(s[1], MAX_TERMS_PARAM + "="));
                        if (s.length > 2) {
                            spellcheckInfo.put("sites", StringUtils.substringAfter(s[2], SITES_PARAM + "="));
                        }
                    } else if (!spellcheckInfo.containsKey("language") && node.getRelativePath() != null
                            && node.getRelativePath().getNumOperands() > 0) {
                        Name propertyName = ((LocationStepQueryNode) node.getRelativePath().getOperands()[0])
                                .getNameTest();
                        if ("language".equals(propertyName.getLocalName())) {
                            spellcheckInfo.put("language", node.getStringValue());
                        }
                    }
                    return super.visit(node, data);
                }

                public Object visit(PathQueryNode node, Object data) throws RepositoryException {
                    for (int i : new int[]{0, 1}) {
                        if (node.getPathSteps().length > i + 1
                                && "sites".equals(node.getPathSteps()[i].getNameTest().getLocalName())) {
                            spellcheckInfo.put("sites", node.getPathSteps()[++i].getNameTest().getLocalName());
                        }
                    }
                    return super.visit(node, data);
                }
            }, null);
            if (!spellcheckInfo.containsKey("statement")) {
                // no spellcheck operation in query
                return null;
            }
            if (!spellcheckInfo.containsKey("language")) {
                Locale locale = JCRSessionFactory.getInstance().getCurrentLocale();
                if (locale != null) {
                    spellcheckInfo.put("language", locale.toString());
                }
            }
        } catch (RepositoryException e) {
            logger.debug("issue while checking " + aqt, e.getMessage());
        }

        int maxTermCount = 1;
        String maxTermCountStr = spellcheckInfo.get("maxTermCount");
        if (!StringUtils.isEmpty(maxTermCountStr) && StringUtils.isNumeric(maxTermCountStr)) {
            int parsedMaxTermCount = Integer.parseInt(maxTermCountStr);
            if (parsedMaxTermCount > 1) {
                maxTermCount = parsedMaxTermCount;
            }
        }
        return spellChecker.suggest(spellcheckInfo.get("statement"), StringUtils.split(spellcheckInfo.get("sites"), "*"), spellcheckInfo.get("language"), maxTermCount);
    }

    public void close() {
        try {
            spellChecker.close();
        } finally {
            spellCheckers.remove(spellChecker.handler.getPath());
        }
    }
    
    /**
     * Triggers update of the spell checker dictionary index.
     */
    public void updateIndex() {
        updateIndex(true);
    }

    /**
     * Triggers update of the spell checker dictionary index.
     * 
     * @param inBackground
     *            specifies if the update should be done in a separate thread
     */
    public void updateIndex(boolean inBackground) {
        spellChecker.lastRefresh = 0;
        spellChecker.refreshSpellChecker(inBackground);
    }

    private final class InternalSpellChecker {

        /**
         * Timestamp when the last refresh was done.
         */
        private long lastRefresh;

        /**
         * Set to true while a refresh is done in a separate thread.
         */
        private boolean refreshing = false;

        /**
         * The query handler associated with this spell checker.
         */
        private final SearchIndex handler;

        /**
         * The directory where the spell index is stored.
         */
        private final Directory spellIndexDirectory;

        /**
         * The underlying spell checker.
         */
        private JahiaExtendedSpellChecker spellChecker;

        /**
         * Creates a new internal spell checker.
         *
         * @param handler the associated query handler.
         */
        InternalSpellChecker(SearchIndex handler) throws IOException {
            this.handler = handler;
            String path = handler.getPath() + File.separatorChar + "spellchecker";
            this.spellIndexDirectory = FSDirectory.open(new File(path), new NativeFSLockFactory(path));
            if (IndexReader.indexExists(spellIndexDirectory)) {
                this.lastRefresh = System.currentTimeMillis();
            }
            this.spellChecker = new JahiaExtendedSpellChecker(spellIndexDirectory);
            spellChecker.setAccuracy(Float.parseFloat(SettingsBean.getInstance().getPropertiesFile().getProperty("jahia.jackrabbit.searchIndex.spellChecker.minimumScore")));
            try {
                spellChecker.setStringDistance((StringDistance) Class.forName(SettingsBean.getInstance().getPropertiesFile().getProperty("jahia.jackrabbit.searchIndex.spellChecker.distanceImplementation")).newInstance());
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
            if (!(handler instanceof JahiaSecondaryIndex)) {
                refreshSpellChecker();
            }
        }

        /**
         * Checks a fulltext query statement and suggests a spell checked
         * version of the statement. If the spell checker thinks the spelling is
         * correct <code>null</code> is returned.
         *
         * @param statement      the fulltext query statement.
         * @param sites          the sites being searched
         * @param language       the language being searched
         * @param maxSuggestions maximum number of suggestions to return
         * @return a suggestion or <code>null</code>.
         */
        String suggest(String statement, String[] sites, String language, int maxSuggestions) throws IOException {
            // tokenize the statement (field name doesn't matter actually...)
            List<String> words = new ArrayList<String>();
            List<Token> tokens = new ArrayList<Token>();
            tokenize(statement, words, tokens, null, language);

            String[][] suggestions = check((String[]) words.toArray(new String[words.size()]), sites, language, maxSuggestions);
            if (suggestions != null) {
                int possibleSuggestionsCount = 1;
                for (String[] suggestionsPerWord : suggestions) {
                    if (suggestionsPerWord.length > 1) {
                        if (possibleSuggestionsCount > 1) {
                            possibleSuggestionsCount = 1;
                            break;
                        } else {
                            possibleSuggestionsCount = suggestionsPerWord.length;
                        }
                    }
                }

                // replace words in statement in reverse order because length
                // of statement will change
                StringBuilder sb = new StringBuilder();
                int loopCount = 0;
                do {
                    if (loopCount > 0) {
                        sb.append(SEPARATOR_IN_SUGGESTION);
                    }
                    StringBuilder stmt = new StringBuilder(statement);
                    for (int i = suggestions.length - 1; i >= 0; i--) {
                        Token t = (Token) tokens.get(i);
                        int pos = suggestions[i].length > 1 ? loopCount : 0;
                        // only replace if word actually changed
                        if (!t.term().equalsIgnoreCase(suggestions[i][pos])) {
                            stmt.replace(t.startOffset(), t.endOffset(),
                                    suggestions[i][pos]);
                        }
                    }
                    sb.append(stmt);
                } while (++loopCount < possibleSuggestionsCount);
                return sb.toString();
            } else {
                return null;
            }
        }

        void close() {
            try {
                spellIndexDirectory.close();
            } catch (IOException e) {
                // ignore
            }

            try {
                spellChecker.close();
            } catch (IOException e) {
                // ignore
            }

            spellChecker = null;
        }

        /**
         * Tokenizes the statement into words and tokens.
         *
         * @param statement the fulltext query statement.
         * @param words     this list will be filled with the original words extracted
         *                  from the statement.
         * @param tokens    this list will be filled with the tokens parsed from the
         *                  statement.
         * @throws IOException if an error occurs while parsing the statement.
         */
        private void tokenize(String statement, List<String> words, List<Token> tokens, String site, String language) throws IOException {
            Analyzer analyzer = handler.getIndexingConfig().getPropertyAnalyzer(JahiaIndexingConfigurationImpl.FULL_SPELLCHECK_FIELD_NAME);
            if (analyzer == null) {
                analyzer = handler.getTextAnalyzer();
            }
            TokenStream ts = analyzer.tokenStream(LuceneUtils.getFullTextFieldName(site, language), new StringReader(statement));
            try {
                OffsetAttribute offsetAttribute = ts.getAttribute(OffsetAttribute.class);
                TermAttribute termAttribute = ts.getAttribute(TermAttribute.class);
                PositionIncrementAttribute position = ts.getAttribute(PositionIncrementAttribute.class);
                while (ts.incrementToken()) {
                    String origWord = statement.substring(offsetAttribute.startOffset(), offsetAttribute.endOffset());
                    if (position.getPositionIncrement() > 0) {
                        words.add(termAttribute.term());
                        tokens.add(new Token(termAttribute.term(), offsetAttribute.startOffset(), offsetAttribute.endOffset()));
                    } else {
                        // very simple implementation: use termText with length
                        // closer to original word
                        Token current = tokens.get(tokens.size() - 1);
                        if (Math.abs(origWord.length() - current.term().length()) > Math.abs(origWord.length()
                                - termAttribute.term().length())) {
                            // replace current token and word
                            words.set(words.size() - 1, termAttribute.term());
                            tokens.set(tokens.size() - 1, new Token(termAttribute.term(), offsetAttribute.startOffset(), offsetAttribute.endOffset()));
                        }
                    }
                }
            } finally {
                ts.close();
            }
        }

        /**
         * Checks the spelling of the passed <code>words</code> and returns a
         * suggestion.
         *
         * @param words the words to check.
         * @return a suggestion of correctly spelled <code>words</code> or
         * <code>null</code> if this spell checker thinks
         * <code>words</code> are spelled correctly.
         * @throws IOException if an error occurs while spell checking.
         */
        private String[][] check(String words[], String[] sites, String language, int maxSuggestionCount) throws IOException {
            refreshSpellChecker();
            boolean hasSuggestion = false;
            IndexReader reader = handler.getIndexReader();
            try {
                for (int retries = 0; retries < 100; retries++) {
                    try {
                        String[][] suggestion = new String[words.length][];
                        for (int i = 0; i < words.length; i++) {
                            String[] similar = spellChecker.suggestSimilar(words[i], maxSuggestionCount, reader,
                                    true, sites, language);
                            if (similar.length > 0) {
                                suggestion[i] = similar;
                                hasSuggestion = true;
                            } else {
                                suggestion[i] = new String[]{words[i]};
                            }
                        }
                        if (hasSuggestion) {
                            logger.debug("Successful after {} retries " + retries);
                            return suggestion;
                        } else {
                            return null;
                        }
                    } catch (AlreadyClosedException e) {
                        // it may happen that the index reader inside the
                        // spell checker is closed while searching for
                        // suggestions. this is actually a design flaw in the
                        // lucene spell checker, but for now we simply retry
                    }
                }
                // unsuccessful after retries
                return null;
            } finally {
                reader.close();
            }
        }

        /**
         * Refreshes the underlying spell checker in a background thread. Synchronization is done on this <code>CompositeSpellChecker</code>
         * instance. While the refresh takes place {@link #refreshing} is set to <code>true</code>.
         */
        private void refreshSpellChecker() {
            refreshSpellChecker(true);
        }
        
        /**
         * Refreshes the underlying spell checker. Synchronization is done on this <code>CompositeSpellChecker</code>
         * instance. While the refresh takes place {@link #refreshing} is set to <code>true</code>.
         * 
         * @param inBackground
         *            specifies if the update should be done in a separate thread; if <code>false</code> the update will be done in the main
         *            thread, blocking the return until it is finished
         */
        private void refreshSpellChecker(boolean inBackground) {
            if (lastRefresh + refreshInterval < System.currentTimeMillis()) {
                synchronized (this) {
                    if (!refreshing) {
                        refreshing = true;
                        Runnable refresh = new Runnable() {
                            public void run() {
                                while (!SpringContextSingleton.getInstance().isInitialized()
                                        || JCRSessionFactory.getInstance().getMountPoints().keySet().isEmpty()) {
                                    // wait until services are started
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException ex) {
                                        // do nothing
                                    }
                                }
                                try {
                                    JCRTemplate.getInstance().doExecuteWithSystemSession(
                                            new JCRCallback<Set<String>>() {
                                                public Set<String> doInJCR(JCRSessionWrapper session)
                                                        throws RepositoryException {
                                                    if (session.nodeExists("/sites")) {
                                                        IndexReader reader = null;
                                                        try {
                                                            reader = handler.getIndexReader();
                                                            long time = System.currentTimeMillis();
                                                            logger.debug("Starting spell checker index refresh");
                                                            List<JCRSiteNode> siteNodes = JahiaSitesService.getInstance().getSitesNodeList(
                                                                    session);
                                                            for (JCRSiteNode siteNode : siteNodes) {
                                                                for (String language : siteNode.getLanguages()) {
                                                                    StringBuilder fullTextName = new StringBuilder(
                                                                            FieldNames.FULLTEXT);

                                                                    String name = siteNode.getName();
                                                                    fullTextName.append("-").append(name);

                                                                    // add language independend
                                                                    // fulltext values first
                                                                    spellChecker.indexDictionary(new LuceneDictionary(
                                                                            reader, fullTextName.toString()), 300, 10,
                                                                            name, language);

                                                                    // add language dependend
                                                                    // fulltext values
                                                                    if (language != null) {
                                                                        fullTextName.append("-").append(language);
                                                                    }
                                                                    spellChecker.indexDictionary(new LuceneDictionary(
                                                                            reader, fullTextName.toString()), 300, 10,
                                                                            name, language);
                                                                }
                                                            }
                                                            logger.info(
                                                                    "Spell checker index refreshed in {}",
                                                                    DateUtils.formatDurationWords(System
                                                                            .currentTimeMillis() - time));
                                                        } catch (IOException e) {
                                                            logger.error(e.getMessage(), e);
                                                        } finally {
                                                            if (reader != null) {
                                                                try {
                                                                    reader.close();
                                                                } catch (IOException e) {
                                                                    logger.error(e.getMessage(), e);
                                                                }
                                                            }
                                                        }
                                                    }
                                                    return null;
                                                }
                                            });
                                } catch (RepositoryException e) {
                                    logger.warn("Error creating spellcheck index", e);
                                } finally {
                                    synchronized (InternalSpellChecker.this) {
                                        refreshing = false;
                                    }
                                }
                            }
                        };
                        if (inBackground) {
                            new Thread(refresh, "SpellChecker Refresh").start();
                        } else {
                            refresh.run();
                        }
                        lastRefresh = System.currentTimeMillis();
                    }
                }
            }
        }
    }
}
