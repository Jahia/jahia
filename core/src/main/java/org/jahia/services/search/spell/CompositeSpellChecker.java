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

package org.jahia.services.search.spell;

import org.apache.jackrabbit.core.query.lucene.SearchIndex;
import org.apache.jackrabbit.core.query.lucene.FieldNames;
import org.apache.jackrabbit.core.query.QueryHandler;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.query.LocationStepQueryNode;
import org.apache.jackrabbit.spi.commons.query.PathQueryNode;
import org.apache.jackrabbit.spi.commons.query.QueryRootNode;
import org.apache.jackrabbit.spi.commons.query.RelationQueryNode;
import org.apache.jackrabbit.spi.commons.query.TraversingQueryNodeVisitor;
import org.slf4j.Logger;
import org.apache.lucene.search.spell.JahiaExtendedSpellChecker;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NativeFSLockFactory;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Token;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSitesBaseService;

import java.io.IOException;
import java.io.StringReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

/**
 * <code>LuceneSpellChecker</code> implements a spell checker based on the terms
 * present in a lucene index.
 */
public class CompositeSpellChecker implements org.apache.jackrabbit.core.query.lucene.SpellChecker {
    
    /**
     * Logger instance for this class.
     */
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(CompositeSpellChecker.class);

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

    private static Map<String, InternalSpellChecker> spellCheckers = new HashMap<String, InternalSpellChecker>(2);
    
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
     * @param handler
     *            the query handler that created this spell checker.
     * @throws IOException
     *             if <code>handler</code> is not of type {@link SearchIndex}.
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
                        spellcheckInfo.put("statement", node.getStringValue());
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
                    for (int i : new int[] { 0, 1 }) {
                        if (node.getPathSteps().length > i + 1
                                && "sites".equals(node.getPathSteps()[i].getNameTest().getLocalName())) {
                            spellcheckInfo.put("site", node.getPathSteps()[++i].getNameTest().getLocalName());
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
        }

        return spellChecker.suggest(spellcheckInfo.get("statement"), spellcheckInfo
                .get("site"), spellcheckInfo.get("language"));
    }

    public void close() {
        try {
            spellChecker.close();
        } finally {
            spellCheckers.remove(spellChecker.handler.getPath());
        }
    }

    // ------------------------------< internal
    // >--------------------------------

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
         * @param handler
         *            the associated query handler.
         */
        InternalSpellChecker(SearchIndex handler) throws IOException {
            this.handler = handler;
            String path = handler.getPath() + File.separatorChar + "spellchecker";
            this.spellIndexDirectory = FSDirectory.getDirectory(path, new NativeFSLockFactory(path));
            if (IndexReader.indexExists(spellIndexDirectory)) {
                this.lastRefresh = System.currentTimeMillis();
            }
            this.spellChecker = new JahiaExtendedSpellChecker(spellIndexDirectory);
            refreshSpellChecker();
        }

        /**
         * Checks a fulltext query statement and suggests a spell checked
         * version of the statement. If the spell checker thinks the spelling is
         * correct <code>null</code> is returned.
         * 
         * @param statement
         *            the fulltext query statement.
         * @return a suggestion or <code>null</code>.
         */
        String suggest(String statement, String site, String language) throws IOException {
            // tokenize the statement (field name doesn't matter actually...)
            List<String> words = new ArrayList<String>();
            List<Token> tokens = new ArrayList<Token>();
            tokenize(statement, words, tokens);

            String[] suggestions = check((String[]) words.toArray(new String[words.size()]), site, language);
            if (suggestions != null) {
                // replace words in statement in reverse order because length
                // of statement will change
                StringBuffer sb = new StringBuffer(statement);
                for (int i = suggestions.length - 1; i >= 0; i--) {
                    Token t = (Token) tokens.get(i);
                    // only replace if word actually changed
                    if (!t.termText().equalsIgnoreCase(suggestions[i])) {
                        sb.replace(t.startOffset(), t.endOffset(), suggestions[i]);
                    }
                }
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
         * @param statement
         *            the fulltext query statement.
         * @param words
         *            this list will be filled with the original words extracted
         *            from the statement.
         * @param tokens
         *            this list will be filled with the tokens parsed from the
         *            statement.
         * @throws IOException
         *             if an error occurs while parsing the statement.
         */
        private void tokenize(String statement, List<String> words, List<Token> tokens) throws IOException {
            TokenStream ts = handler.getTextAnalyzer().tokenStream(FieldNames.FULLTEXT, new StringReader(statement));
            try {
                Token t;
                while ((t = ts.next()) != null) {
                    String origWord = statement.substring(t.startOffset(), t.endOffset());
                    if (t.getPositionIncrement() > 0) {
                        words.add(t.termText());
                        tokens.add(t);
                    } else {
                        // very simple implementation: use termText with length
                        // closer to original word
                        Token current = (Token) tokens.get(tokens.size() - 1);
                        if (Math.abs(origWord.length() - current.termText().length()) > Math.abs(origWord.length()
                                - t.termText().length())) {
                            // replace current token and word
                            words.set(words.size() - 1, t.termText());
                            tokens.set(tokens.size() - 1, t);
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
         * @param words
         *            the words to check.
         * @return a suggestion of correctly spelled <code>words</code> or
         *         <code>null</code> if this spell checker thinks
         *         <code>words</code> are spelled correctly.
         * @throws IOException
         *             if an error occurs while spell checking.
         */
        private String[] check(String words[], String site, String language) throws IOException {
            refreshSpellChecker();
            boolean hasSuggestion = false;
            IndexReader reader = handler.getIndexReader();
            try {
                for (int retries = 0; retries < 100; retries++) {
                    try {
                        String[] suggestion = new String[words.length];
                        for (int i = 0; i < words.length; i++) {
                            String[] similar = spellChecker.suggestSimilar(words[i], 5, reader, FieldNames.FULLTEXT,
                                    true, site, language);
                            if (similar.length > 0) {
                                suggestion[i] = similar[0];
                                hasSuggestion = true;
                            } else {
                                suggestion[i] = words[i];
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
         * Refreshes the underlying spell checker in a background thread.
         * Synchronization is done on this <code>CompositeSpellChecker</code>
         * instance. While the refresh takes place {@link #refreshing} is set to
         * <code>true</code>.
         */
        private void refreshSpellChecker() {
            if (lastRefresh + refreshInterval < System.currentTimeMillis()) {
                synchronized (this) {
                    if (refreshing) {
                        return;
                    } else {
                        refreshing = true;
                        Runnable refresh = new Runnable() {
                            public void run() {
                                while (!SpringContextSingleton.getInstance().isInitialized()) {
                                    // wait until services are started
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException ex) {
                                        // do nothing
                                    }
                                }
                                try {
                                    Set<String> sites = JCRTemplate.getInstance().doExecuteWithSystemSession(
                                            new JCRCallback<Set<String>>() {
                                                public Set<String> doInJCR(JCRSessionWrapper session)
                                                        throws RepositoryException {
                                                    Set<String> sites = new HashSet<String>();
                                                    if (session.nodeExists("/sites")) {
                                                        Node sitesRoot = session.getNode("/sites");
                                                        for (NodeIterator it = sitesRoot.getNodes(); it.hasNext();) {
                                                            Node child = (Node) it.next();
                                                            if (child.isNodeType(Constants.JAHIANT_VIRTUALSITE)) {
                                                                sites.add(child.getName());
                                                            }
                                                        }
                                                    }
                                                    return sites;
                                                }
                                            });

                                    if (!sites.isEmpty()) {
                                        IndexReader reader = handler.getIndexReader();
                                        try {

                                            long time = System.currentTimeMillis();
                                            logger.debug("Starting spell checker index refresh");
                                            for (String site : sites) {
                                                for (String language : JahiaSitesBaseService.getInstance()
                                                        .getSiteByKey(site).getLanguages()) {
                                                    StringBuilder fullTextName = new StringBuilder(FieldNames.FULLTEXT);
                                                    if (site != null) {
                                                        fullTextName.append("-").append(site);
                                                    }
                                                    // add language independend
                                                    // fulltext values first
                                                    spellChecker.indexDictionary(new LuceneDictionary(reader,
                                                            fullTextName.toString()), 300, 10, site, language);

                                                    // add language dependend
                                                    // fulltext values
                                                    if (language != null) {
                                                        fullTextName.append("-").append(language);
                                                    }
                                                    spellChecker.indexDictionary(new LuceneDictionary(reader,
                                                            fullTextName.toString()), 300, 10, site, language);
                                                }
                                            }
                                            time = System.currentTimeMillis() - time;
                                            time = time / 1000;
                                            logger.info("Spell checker index refreshed in: {} s." + time);
                                        } finally {
                                            reader.close();
                                        }
                                    }
                                } catch (RepositoryException e) {
                                    logger.warn("Error creating spellcheck index", e);
                                } catch (JahiaException e) {
                                    logger.warn("Error creating spellcheck index", e);
                                } catch (IOException e) {
                                    // ignore
                                } finally {
                                    synchronized (InternalSpellChecker.this) {
                                        refreshing = false;
                                    }
                                }
                            }
                        };
                        new Thread(refresh, "SpellChecker Refresh").start();
                        lastRefresh = System.currentTimeMillis();
                    }
                }
            }
        }
    }
}
