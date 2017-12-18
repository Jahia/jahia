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
package  org.jahia.services.search.analyzer;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.fr.ElisionFilter;
import org.apache.lucene.analysis.fr.FrenchStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;  // for javadoc
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.apache.lucene.analysis.fr.FrenchAnalyzer.FRENCH_STOP_WORDS;

/**
 * {@link Analyzer} for French language.
 * <p>
 * Supports an external list of stopwords (words that
 * will not be indexed at all) and an external list of exclusions (word that will
 * not be stemmed, but indexed).
 * A default set of stopwords is used unless an alternative list is specified, but the
 * exclusion list is empty by default.
 * </p>
 *
 * <a name="version"/>
 * <p>You must specify the required {@link Version}
 * compatibility when creating FrenchAnalyzer:
 * <ul>
 *   <li> As of 2.9, StopFilter preserves position
 *        increments
 * </ul>
 *
 * <p><b>NOTE</b>: This class uses the same {@link Version}
 * dependent settings as {@link StandardAnalyzer}.</p>
 */
public final class JahiaFrenchAnalyzer extends Analyzer {

    /**
     * Contains the stopwords used with the {@link StopFilter}.
     */
    private final Set<?> stoptable;
    /**
     * Contains words that should be indexed but not stemmed.
     */
    //TODO make this final in 3.0
    private Set<?> excltable = new HashSet();

    private final Version matchVersion;

    /**
     * Returns an unmodifiable instance of the default stop-words set.
     * @return an unmodifiable instance of the default stop-words set.
     */
    public static Set<?> getDefaultStopSet(){
        return DefaultSetHolder.DEFAULT_STOP_SET;
    }

    private static class DefaultSetHolder {
        static final Set<?> DEFAULT_STOP_SET = CharArraySet
                .unmodifiableSet(new CharArraySet(Arrays.asList(FRENCH_STOP_WORDS),
                        false));
    }

    /**
     * Builds an analyzer with the default stop words ({@link #FRENCH_STOP_WORDS}).
     */
    public JahiaFrenchAnalyzer(Version matchVersion) {
        this(matchVersion, DefaultSetHolder.DEFAULT_STOP_SET);
    }

    /**
     * Builds an analyzer with the given stop words
     *
     * @param matchVersion
     *          lucene compatibility version
     * @param stopwords
     *          a stopword set
     */
    public JahiaFrenchAnalyzer(Version matchVersion, Set<?> stopwords){
        this(matchVersion, stopwords, CharArraySet.EMPTY_SET);
    }

    /**
     * Builds an analyzer with the given stop words
     *
     * @param matchVersion
     *          lucene compatibility version
     * @param stopwords
     *          a stopword set
     * @param stemExclutionSet
     *          a stemming exclusion set
     */
    public JahiaFrenchAnalyzer(Version matchVersion, Set<?> stopwords,
                          Set<?> stemExclutionSet) {
        this.matchVersion = matchVersion;
        this.stoptable = CharArraySet.unmodifiableSet(CharArraySet.copy(stopwords));
        this.excltable = CharArraySet.unmodifiableSet(CharArraySet
                .copy(stemExclutionSet));
    }


    /**
     * Builds an analyzer with the given stop words.
     * @deprecated use {@link #JahiaFrenchAnalyzer(Version, Set)} instead
     */
    public JahiaFrenchAnalyzer(Version matchVersion, String... stopwords) {
        this(matchVersion, StopFilter.makeStopSet(stopwords));
    }

    /**
     * Builds an analyzer with the given stop words.
     * @throws IOException
     * @deprecated use {@link #JahiaFrenchAnalyzer(Version, Set)} instead
     */
    public JahiaFrenchAnalyzer(Version matchVersion, File stopwords) throws IOException {
        this(matchVersion, WordlistLoader.getWordSet(stopwords));
    }

    /**
     * Builds an exclusionlist from an array of Strings.
     * @deprecated use {@link #JahiaFrenchAnalyzer(Version, Set, Set)} instead
     */
    public void setStemExclusionTable(String... exclusionlist) {
        excltable = StopFilter.makeStopSet(exclusionlist);
        setPreviousTokenStream(null); // force a new stemmer to be created
    }

    /**
     * Builds an exclusionlist from a Map.
     * @deprecated use {@link #JahiaFrenchAnalyzer(Version, Set, Set)} instead
     */
    public void setStemExclusionTable(Map exclusionlist) {
        excltable = new HashSet(exclusionlist.keySet());
        setPreviousTokenStream(null); // force a new stemmer to be created
    }

    /**
     * Builds an exclusionlist from the words contained in the given file.
     * @throws IOException
     * @deprecated use {@link #JahiaFrenchAnalyzer(Version, Set, Set)} instead
     */
    public void setStemExclusionTable(File exclusionlist) throws IOException {
        excltable = new HashSet(WordlistLoader.getWordSet(exclusionlist));
        setPreviousTokenStream(null); // force a new stemmer to be created
    }

    /**
     * Creates a {@link TokenStream} which tokenizes all the text in the provided
     * {@link Reader}.
     *
     * @return A {@link TokenStream} built from a {@link StandardTokenizer}
     *         filtered with {@link StandardFilter}, {@link StopFilter},
     *         {@link FrenchStemFilter} and {@link LowerCaseFilter}
     */
    @Override
    public final TokenStream tokenStream(String fieldName, Reader reader) {
        TokenStream result = new StandardTokenizer(matchVersion, reader);
        result = new StandardFilter(result);
        result = new ElisionFilter(result, FrenchSnowballAnalyzer.DEFAULT_ARTICLES);
        result = new LowerCaseFilter(result);
        result = new StopFilter(StopFilter.getEnablePositionIncrementsVersionDefault(matchVersion),
                result, stoptable);
        result = new FrenchStemFilter(result, excltable);
        return result;
    }

    private class SavedStreams {
        Tokenizer source;
        TokenStream result;
    };

    /**
     * Returns a (possibly reused) {@link TokenStream} which tokenizes all the
     * text in the provided {@link Reader}.
     *
     * @return A {@link TokenStream} built from a {@link StandardTokenizer}
     *         filtered with {@link StandardFilter}, {@link StopFilter},
     *         {@link FrenchStemFilter} and {@link LowerCaseFilter}
     */
    @Override
    public TokenStream reusableTokenStream(String fieldName, Reader reader)
            throws IOException {
        SavedStreams streams = (SavedStreams) getPreviousTokenStream();
        if (streams == null) {
            streams = new SavedStreams();
            streams.source = new StandardTokenizer(matchVersion, reader);
            streams.result = new StandardFilter(streams.source);
            streams.result = new ElisionFilter(streams.result, FrenchSnowballAnalyzer.DEFAULT_ARTICLES);
            streams.result = new LowerCaseFilter(streams.result);
            streams.result = new StopFilter(StopFilter.getEnablePositionIncrementsVersionDefault(matchVersion),
                    streams.result, stoptable);
            streams.result = new FrenchStemFilter(streams.result, excltable);
            setPreviousTokenStream(streams);
        } else {
            streams.source.reset(reader);
        }
        return streams.result;
    }
}

