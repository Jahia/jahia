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
package org.apache.lucene.search.spell;

import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.analysis.LimitTokenCountAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefIterator;
import org.apache.lucene.util.Version;
import org.jahia.utils.LuceneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 *   Jahia Spell Checker class  (Main class) <br/>
 *  (initially inspired by the David Spencer code).
 * </p>
 *
 * The Jahia specific part is mainly to be able to create/maintain own dictionaries by site and language.
 *
 * <p>Example Usage:
 *
 * <pre>
 *  JahiaSpellChecker spellchecker = new JahiaSpellChecker(spellIndexDirectory);
 *  // To index a field of a user index:
 *  spellchecker.indexDictionary(new LuceneDictionary(my_lucene_reader, a_field));
 *  // To index a file containing words:
 *  spellchecker.indexDictionary(new PlainTextDictionary(new File("myfile.txt")));
 *  String[] suggestions = spellchecker.suggestSimilar("misspelt", 5);
 * </pre>
 *
  */
public class JahiaExtendedSpellChecker implements java.io.Closeable {
    /**
     * Field name for each word in the ngram index.
     */
    public static final String F_WORD = "word";

    public static final String F_LANGUAGE = "language".intern();

    public static final String F_SITE = "site".intern();

    private static final String F_WORD_INTERNED = F_WORD.intern();

    private static final Logger logger = LoggerFactory.getLogger(JahiaExtendedSpellChecker.class);

    /**
     * the spell index
     */
    // don't modify the directory directly - see #swapSearcher()
    Directory spellIndex;

    /**
     * Boost value for start and end grams
     */
    private float bStart = 2.0f;
    private float bEnd = 1.0f;

    // don't use this searcher directly - see #swapSearcher()
    private IndexSearcher searcher;

    /*
     * this locks all modifications to the current searcher.
     */
    private final Object searcherLock = new Object();

    /*
     * this lock synchronizes all possible modifications to the
     * current index directory. It should not be possible to try modifying
     * the same index concurrently. Note: Do not acquire the searcher lock
     * before acquiring this lock!
     */
    private final Object modifyCurrentIndexLock = new Object();
    private volatile boolean closed = false;

    // minimum score for hits generated by the spell checker query
    private float minScore = 0.5f;

    private StringDistance sd;

    /**
     * Use the given directory as a spell checker index. The directory is
     * created if it doesn't exist yet.
     *
     * @param spellIndex
     * @throws IOException
     */
    public JahiaExtendedSpellChecker(Directory spellIndex, StringDistance sd) throws IOException {
        setSpellIndex(spellIndex);
        setStringDistance(sd);
    }

    public JahiaExtendedSpellChecker(Directory spellIndex) throws IOException {
        this(spellIndex, new LevensteinDistance());
    }

    /**
     * Use a different index as the spell checker index or re-open
     * the existing index if <code>spellIndex</code> is the same value
     * as given in the constructor.
     * @param spellIndexDir the spell directory to use
     * @throws AlreadyClosedException if the Spellchecker is already closed
     * @throws  IOException if spellchecker can not open the directory
     */
    public void setSpellIndex(Directory spellIndexDir) throws IOException {
        // this could be the same directory as the current spellIndex
        // modifications to the directory should be synchronized
        synchronized (modifyCurrentIndexLock) {
          ensureOpen();
          if (!IndexReader.indexExists(spellIndexDir)) {
              createIndexWriter(spellIndexDir);
          }
          swapSearcher(spellIndexDir);
        }
    }

    private void createIndexWriter(Directory spellIndexDir) throws IOException {
        IndexWriter writer = new IndexWriter(spellIndexDir,
                new IndexWriterConfig(Version.LUCENE_36,
                new LimitTokenCountAnalyzer(null, Integer.MAX_VALUE)).setOpenMode(IndexWriterConfig.OpenMode.CREATE));
        writer.close();
    }

    /**
     * Sets the {@link StringDistance} implementation for this
     * {@link SpellChecker} instance.
     *
     * @param sd the {@link StringDistance} implementation for this
     * {@link SpellChecker} instance
     */
    public void setStringDistance(StringDistance sd) {
      this.sd = sd;
    }
    /**
     * Returns the {@link StringDistance} instance used by this
     * {@link SpellChecker} instance.
     *
     * @return the {@link StringDistance} instance used by this
     *         {@link SpellChecker} instance.
     */
    public StringDistance getStringDistance() {
      return sd;
    }


    /**
     * Sets the accuracy 0 &lt; minScore &lt; 1; default 0.5
     */
    public void setAccuracy(float minScore) {
        this.minScore = minScore;
    }


    /**
     * Suggest similar words (optionally restricted to a field of an index).
     *
     * <p>
     * As the Lucene similarity that is used to fetch the most relevant
     * n-grammed terms is not the same as the edit distance strategy used to
     * calculate the best matching spell-checked word from the hits that Lucene
     * found, one usually has to retrieve a couple of numSug's in order to get
     * the true best match.
     *
     * <p>
     * I.e. if numSug == 1, don't count on that suggestion being the best one.
     * Thus, you should set this value to <b>at least</b> 5 for a good
     * suggestion.
     *
     * @param word
     *            the word you want a spell check done on
     * @param numSug
     *            the number of suggested words
     * @param ir
     *            the indexReader of the user index (can be null see field
     *            param)
     * @param morePopular
     *            return only the suggest words that are as frequent or more
     *            frequent than the searched word (only if restricted mode =
     *            (indexReader!=null and field!=null)
     * @param sites an array of site keys to search in
     * @param language the current languages, used for the search
     * @throws IOException in case of index read error
     * @return String[] the sorted list of the suggest words with these 2
     *         criteria: first criteria: the edit distance, second criteria
     *         (only if restricted mode): the popularity of the suggest words in
     *         the field of the user index
     */
    @SuppressWarnings("resource")
    public String[] suggestSimilar(String word, int numSug, IndexReader ir, boolean morePopular,
                                   String[] sites, String language) throws IOException {
        long startTime = System.currentTimeMillis();
        // obtainSearcher calls ensureOpen
        final IndexSearcher indexSearcher = obtainSearcher();
        try {
            float min = this.minScore;
            final int lengthWord = word.length();

            List<String> fields = getFields(sites, language);

            int freq = 0;
            for (String aField : fields) {
                freq += (ir != null) ? ir.docFreq(new Term(aField, word)) : 0;
            }

            final int goalFreq = (morePopular && ir != null) ? freq : 0;
            // if the word exists in the real index and we don't care for word
            // frequency, return the word itself
            if (!morePopular && freq > 0) {
                return new String[] { word };
            }

            BooleanQuery query = getClauses(word, sites, language, lengthWord);

            int maxHits = 10 * numSug;

            SuggestWordQueue sugQueue = new SuggestWordQueue(numSug);
            ScoreDoc[] hits = indexSearcher.search(query, maxHits).scoreDocs;

            // go through more than 'maxr' matches in case the distance filter triggers

            int stop = hits == null ? 0 : Math.min(hits.length, 10 * numSug);

            getSuggestionWords(word, numSug, ir, morePopular, language, indexSearcher, min, fields, goalFreq, sugQueue, hits, stop);

            // convert to array string
            return suggestedWordsArray(word, startTime, sugQueue);
        } finally {
            releaseSearcher(indexSearcher);
        }
    }

    private String[] suggestedWordsArray(String word, long startTime, SuggestWordQueue sugQueue) {
        String[] list = null;
        int queueSize = sugQueue.size();
        if (queueSize > 0) {
            list = new String[queueSize];
            for (int i = sugQueue.size() - 1; i >= 0; i--) {
                list[i] = sugQueue.pop().string;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Found suggestions for word '{}' (took {} ms): {}", word, System.currentTimeMillis() - startTime, list);
            }
        } else {
            list = ArrayUtils.EMPTY_STRING_ARRAY;
            if (logger.isDebugEnabled()) {
                logger.debug("No suggestions found for word {} (took {} ms)", word,
                        System.currentTimeMillis() - startTime);
            }
        }

        return list;
    }

    @SuppressWarnings("java:S107")
    private void getSuggestionWords(String word, int numSug, IndexReader ir, boolean morePopular, String language, IndexSearcher indexSearcher, float min, List<String> fields, int goalFreq, SuggestWordQueue sugQueue, ScoreDoc[] hits, int stop) throws IOException {
        SuggestWord sugWord = new SuggestWord();
        Set<String> foundWords = new HashSet<>();
        for (int i = 0; i < stop; i++) {
            // get orig word
            sugWord.string = indexSearcher.doc(hits[i].doc).get(language != null ? (F_WORD + "-" + language) : F_WORD);

            // don't suggest a word for itself, that would be silly
            if (sugWord.string == null || word.equals(sugWord.string) || foundWords.contains(sugWord.string)) {
                continue;
            }

            foundWords.add(sugWord.string);

            if (!shouldSkipWord(word, ir, morePopular, min, fields, goalFreq, sugWord)) {
                sugQueue.insertWithOverflow(sugWord);
                if (sugQueue.size() == numSug) {
                    // if queue full, maintain the minScore score
                    min = sugQueue.top().score;
                }
                sugWord = new SuggestWord();
            }
        }
    }

    private boolean shouldSkipWord(String word, IndexReader ir, boolean morePopular, float min, List<String> fields, int goalFreq, SuggestWord sugWord) throws IOException {
        // edit distance
        sugWord.score = getStringDistance().getDistance(word, sugWord.string);
        if (sugWord.score < min) {
            return true;
        }

        if (ir != null) { // use the user index
            sugWord.freq = 0;
            for (String aField : fields) {
                sugWord.freq += ir.docFreq(new Term(aField, sugWord.string)); // freq
            }

            // in the index don't suggest a word that is not present in the field
            return (morePopular && goalFreq > sugWord.freq) || sugWord.freq < 1;
        }
        return false;
    }

    private BooleanQuery getClauses(String word, String[] sites, String language, int lengthWord) {
        BooleanQuery query = new BooleanQuery();

        // ensure language
        if (language != null) {
            add(query, F_LANGUAGE, language, Occur.MUST);
        }

        // ensure site
        BooleanQuery subQuery = new BooleanQuery();
        query.add(new BooleanClause(subQuery, Occur.MUST));
        for (String site : sites) {
            add(subQuery, F_SITE, site, Occur.SHOULD);
        }

        addNGRAMQueries(word, lengthWord, query);
        return query;
    }

    private List<String> getFields(String[] sites, String language) {
        List<String> fields = new ArrayList<>();
        for (String site : sites) {
            fields.add(LuceneUtils.getFullTextFieldName(site, language));
            if (language != null) {
                // we also consider non-language specific full text field to cover non-18n properties
                fields.add(LuceneUtils.getFullTextFieldName(site, null));
            }
        }
        return fields;
    }

    private void addNGRAMQueries(String word, int lengthWord, BooleanQuery query) {
        String key;
        String[] grams;
        for (int ng = getMin(lengthWord); ng <= getMax(lengthWord); ng++) {

            key = "gram" + ng; // form key

            grams = formGrams(word, ng); // form word into ngrams (allow dups
            // too)

            if (grams.length == 0) {
                continue; // hmm
            }

            if (bStart > 0) { // should we boost prefixes?
                add(query, "start" + ng, grams[0], bStart); // matches start of
                // word

            }
            if (bEnd > 0) { // should we boost suffixes
                add(query, "end" + ng, grams[grams.length - 1], bEnd); // matches
                // end of
                // word

            }
            for (int i = 0; i < grams.length; i++) {
                add(query, key, grams[i]);
            }
        }
    }

    /**
     * Add a clause to a boolean query.
     */
    private static void add(BooleanQuery q, String name, String value, float boost) {
        Query tq = new TermQuery(new Term(name, value));
        tq.setBoost(boost);
        q.add(new BooleanClause(tq, BooleanClause.Occur.SHOULD));
    }

    private static void add(BooleanQuery q, String name, String value, Occur occur) {
        q.add(new BooleanClause(new TermQuery(new Term(name, value)), occur));
    }

    /**
     * Add a clause to a boolean query.
     */
    private static void add(BooleanQuery q, String name, String value) {
        q.add(new BooleanClause(new TermQuery(new Term(name, value)), BooleanClause.Occur.SHOULD));
    }

    /**
     * Form all ngrams for a given word.
     *
     * @param text
     *            the word to parse
     * @param ng
     *            the ngram length e.g. 3
     * @return an array of all ngrams in the word and note that duplicates are
     *         not removed
     */
    private static String[] formGrams(String text, int ng) {
        int len = text.length();
        String[] res = new String[len - ng + 1];
        for (int i = 0; i < len - ng + 1; i++) {
            res[i] = text.substring(i, i + ng);
        }
        return res;
    }

    /**
     * Removes all terms from the spell check index.
     * @throws IOException
     * @throws AlreadyClosedException if the Spellchecker is already closed
     */
    public void clearIndex() throws IOException {
        synchronized (modifyCurrentIndexLock) {
            ensureOpen();
            final Directory dir = this.spellIndex;
            createIndexWriter(dir);
            swapSearcher(dir);
        }
    }

    /**
     * Check whether the word exists in the index.
     *
     * @param word
     * @throws IOException
     * @return true iff the word exists in the index
     */
    public boolean exist(String word, String langCode, String site) throws IOException {
        // obtainSearcher calls ensureOpen
        final IndexSearcher indexSearcher = obtainSearcher();
        try {
            BooleanQuery query = new BooleanQuery();
            add(query, langCode != null ? (F_WORD + "-" + langCode) : F_WORD, word, BooleanClause.Occur.MUST);
            add(query, F_SITE, site, BooleanClause.Occur.MUST);
            return indexSearcher.search(query, 1).scoreDocs.length > 0;
        } finally {
            releaseSearcher(indexSearcher);
        }
    }

    /**
     * Indexes the data from the given {@link Dictionary}.
     *
     * @param dict
     *            Dictionary to index
     * @param mergeFactor
     *            mergeFactor to use when indexing
     * @param ramMB
     *            the max amount or memory in MB to use
     * @throws IOException
     */
    public void indexDictionary(Dictionary dict, int mergeFactor, int ramMB, String site, String langCode) throws IOException {
        synchronized (modifyCurrentIndexLock) {
            ensureOpen();
            final Directory dir = this.spellIndex;
            try (WhitespaceAnalyzer whitespaceAnalyzer = new WhitespaceAnalyzer(Version.LUCENE_36)) {
                IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_36,new LimitTokenCountAnalyzer(whitespaceAnalyzer, Integer.MAX_VALUE)).setRAMBufferSizeMB(ramMB);
                IndexWriter writer = new IndexWriter(dir, writerConfig);

                BytesRefIterator iter = dict.getWordsIterator();
                BytesRef spare;
                while ((spare = iter.next()) != null) {
                    String word = spare.utf8ToString();

                    int len = word.length();
                    if (len < 3 || this.exist(word, langCode, site)) {
                        // if len is too short or the word already exist in the ram index skip it
                        continue;
                    }

                    // ok index the word
                    Document doc = createDocument(word, getMin(len), getMax(len), site, langCode);
                    writer.addDocument(doc);
                }
                // No need to optimize anymore as it has been deprecated
                // Lucene's multi-segment search performance has improved over time, and the default TieredMergePolicy now targets segments with deletions.
                writer.close();
                // also re-open the spell index to see our own changes when the next suggestion is fetched:
                swapSearcher(dir);
            }
        }
    }

    private static int getMin(int l) {
        if (l > 5) {
            return 3;
        }
        if (l == 5) {
            return 2;
        }
        return 1;
    }

    private static int getMax(int l) {
        if (l > 5) {
            return 4;
        }
        if (l == 5) {
            return 3;
        }
        return 2;
    }


    private static Document createDocument(String text, int ng1, int ng2, String site, String langCode) {
        Document doc = new Document();
        doc.add(new Field(langCode != null ? (F_WORD + "-" + langCode) : F_WORD_INTERNED, langCode != null, text, Field.Store.YES,
                Field.Index.NOT_ANALYZED, Field.TermVector.NO)); // orig term
        doc.add(new Field(F_LANGUAGE, false, langCode, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO)); // language
        doc.add(new Field(F_SITE, false, site, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO)); // site
        addGram(text, doc, ng1, ng2);
        return doc;
    }

    private static void addGram(String text, Document doc, int ng1, int ng2) {
        int len = text.length();
        for (int ng = ng1; ng <= ng2; ng++) {
            String key = "gram" + ng;
            String end = null;
            for (int i = 0; i < len - ng + 1; i++) {
                String gram = text.substring(i, i + ng);
                doc.add(new Field(key, gram, Field.Store.NO, Field.Index.NOT_ANALYZED));
                if (i == 0) {
                    doc.add(new Field("start" + ng, gram, Field.Store.NO, Field.Index.NOT_ANALYZED));
                }
                end = gram;
            }
            if (end != null) { // may not be present if len==ng1
                doc.add(new Field("end" + ng, end, Field.Store.NO, Field.Index.NOT_ANALYZED));
            }
        }
    }

    private IndexSearcher obtainSearcher() {
        synchronized (searcherLock) {
          ensureOpen();
          searcher.getIndexReader().incRef();
          return searcher;
        }
      }

      private void releaseSearcher(final IndexSearcher aSearcher) throws IOException{
          // don't check if open - always decRef
          // don't decrement the private searcher - could have been swapped
          aSearcher.getIndexReader().decRef();
      }

      private void ensureOpen() {
        if (closed) {
          throw new AlreadyClosedException("Spellchecker has been closed");
        }
      }

      @Override
      public void close() throws IOException {
        synchronized (searcherLock) {
          ensureOpen();
          closed = true;
          if (searcher != null) {
            searcher.close();
          }
          searcher = null;
        }
      }

      private void swapSearcher(final Directory dir) throws IOException {
        /*
         * opening a searcher is possibly very expensive.
         * We rather close it again if the Spellchecker was closed during
         * this operation than block access to the current searcher while opening.
         */
        final IndexSearcher indexSearcher = createSearcher(dir);
        synchronized (searcherLock) {
          if(closed){
            indexSearcher.close();
            throw new AlreadyClosedException("Spellchecker has been closed");
          }
          if (searcher != null) {
            searcher.close();
          }
          // set the spellindex in the sync block - ensure consistency.
          searcher = indexSearcher;
          this.spellIndex = dir;
        }
      }

      /**
       * Creates a new read-only IndexSearcher
       * @param dir the directory used to open the searcher
       * @return a new read-only IndexSearcher
       * @throws IOException f there is a low-level IO error
       */
      // for testing purposes
      IndexSearcher createSearcher(final Directory dir) throws IOException{
          return new IndexSearcher(dir, true);
      }

      /**
       * Returns <code>true</code> if and only if the {@link SpellChecker} is
       * closed, otherwise <code>false</code>.
       *
       * @return <code>true</code> if and only if the {@link SpellChecker} is
       *         closed, otherwise <code>false</code>.
       */
      boolean isClosed(){
        return closed;
      }

}
