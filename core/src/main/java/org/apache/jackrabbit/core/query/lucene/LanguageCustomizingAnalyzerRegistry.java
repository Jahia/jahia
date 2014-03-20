/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.apache.jackrabbit.core.query.lucene;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cn.ChineseAnalyzer;
import org.apache.lucene.analysis.cz.CzechAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.fa.PersianAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of {@link org.apache.jackrabbit.core.query.lucene.AnalyzerRegistry} that associates Analyzers to
 * languages so that a language-specific Analyzer can be used if available.
 * <p/>
 * Note that this AnalyzerRegistry performs partial match on languages so that variants (e.g. <code>en_US</code>)
 * of the same language can use the Analyzer configured for the main variant (e.g. <code>en</code>) automatically.
 * Also note that, when an Analyzer is registered for a specific language with this AnalyzerRegistry,
 * it is automatically wrapped to first check whether a property-specific Analyzer has been configured (in which case
 * it's used) and then filter the token streams using {@link org.apache.lucene.analysis.ASCIIFoldingFilter}.
 *
 * @author Christophe Laprun
 */
public class LanguageCustomizingAnalyzerRegistry implements AnalyzerRegistry<String> {
    /**
     * Language to Analyzer map.
     */
    private final Map<String, Analyzer> languageToAnalyzer = new ConcurrentHashMap<String, Analyzer>();

    private IndexingConfiguration configuration;
    private Analyzer defaultAnalyzer;

    private static final LanguageCustomizingAnalyzerRegistry instance = new LanguageCustomizingAnalyzerRegistry();

    public static final LanguageCustomizingAnalyzerRegistry getInstance() {
        return instance;
    }

    private LanguageCustomizingAnalyzerRegistry() {
        languageToAnalyzer.put("ar", new AnalyzerWrapper(new ArabicAnalyzer(Version.LUCENE_30), true));
        languageToAnalyzer.put("br", new AnalyzerWrapper(new BrazilianAnalyzer(Version.LUCENE_30), true));
        languageToAnalyzer.put("cjk", new AnalyzerWrapper(new CJKAnalyzer(Version.LUCENE_30), true));
        languageToAnalyzer.put("cn", new AnalyzerWrapper(new ChineseAnalyzer(), true));
        languageToAnalyzer.put("cz", new AnalyzerWrapper(new CzechAnalyzer(Version.LUCENE_30), true));
        languageToAnalyzer.put("de", new AnalyzerWrapper(new GermanAnalyzer(Version.LUCENE_30), true));
        languageToAnalyzer.put("el", new AnalyzerWrapper(new GreekAnalyzer(Version.LUCENE_30), true));
        languageToAnalyzer.put("en", new AnalyzerWrapper(new SnowballAnalyzer(Version.LUCENE_30, "English", StopAnalyzer.ENGLISH_STOP_WORDS_SET), true));
        languageToAnalyzer.put("fa", new AnalyzerWrapper(new PersianAnalyzer(Version.LUCENE_30), true));
        languageToAnalyzer.put("fr", new AnalyzerWrapper(new FrenchAnalyzer(Version.LUCENE_30), true));
        languageToAnalyzer.put("nl", new AnalyzerWrapper(new DutchAnalyzer(Version.LUCENE_30), true));
        languageToAnalyzer.put("ru", new AnalyzerWrapper(new RussianAnalyzer(Version.LUCENE_30), true));
        languageToAnalyzer.put("th", new AnalyzerWrapper(new ThaiAnalyzer(Version.LUCENE_30), true));
    }

    @Override
    public Analyzer getAnalyzerFor(Document document) {
        final String key = getKeyFor(document);

        return getAnalyzer(key);
    }

    @Override
    public String getKeyFor(Document document) {
        if (document != null) {
            final Field field = document.getField(JahiaNodeIndexer.TRANSLATION_LANGUAGE);
            if (field != null) {
                return field.stringValue();
            }
        }
        return null;
    }

    @Override
    public Analyzer getAnalyzer(String key) {
        if (key != null) {
            // first attempt to get the exact match
            Analyzer analyzer = languageToAnalyzer.get(key);
            if (analyzer == null) {
                // if we didn't get an exact match, attempt to see if we're dealing with a language variant
                final int underscore = key.indexOf('_');
                if (underscore >= 0) {
                    // we have a variant, extract main language and use this as key
                    analyzer = languageToAnalyzer.get(key.substring(0, underscore));

                    if (analyzer != null) {
                        // we had a match on main language so add a new entry to avoid going through language parsing
                        // again next time around!
                        languageToAnalyzer.put(key, analyzer);
                        return analyzer;
                    }
                }
            } else {
                return analyzer;
            }
        }

        return null;
    }

    @Override
    public boolean acceptKey(Object key) {
        return key instanceof String;
    }

    void addAnalyzer(String key, Analyzer analyzer, Boolean useASCIIFoldingFilter) {
        languageToAnalyzer.put(key, new AnalyzerWrapper(analyzer, useASCIIFoldingFilter));
    }

    public void setDefaultAnalyzer(Analyzer defaultAnalyzer) {
        this.defaultAnalyzer = defaultAnalyzer;
    }

    public void setConfiguration(IndexingConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Wraps a configured {@link org.apache.lucene.analysis.Analyzer} instance to make sure property-specific as
     * configured are properly used and filter token streams using ASCIIFoldingFilter.
     *
     * @author Christophe Laprun
     */
    private class AnalyzerWrapper extends Analyzer {
        private final Analyzer wrappee;
        private final boolean useFilter;

        public AnalyzerWrapper(Analyzer wrappee, Boolean useASCIIFoldingFilter) {
            this.wrappee = wrappee;
            useFilter = useASCIIFoldingFilter != null && useASCIIFoldingFilter;
        }

        @Override
        public TokenStream tokenStream(String fieldName, Reader reader) {
            final Analyzer analyzer = getAnalyzer(fieldName);

            TokenStream result = analyzer.tokenStream(fieldName, reader);
            return useFilter ? new ASCIIFoldingFilter(result) : result;
        }

        @Override
        public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
            final Analyzer analyzer = getAnalyzer(fieldName);

            TokenStream result = analyzer.reusableTokenStream(fieldName, reader);
            return useFilter ? new ASCIIFoldingFilter(result) : result;
        }

        private Analyzer getAnalyzer(String fieldName) {
            // first look at indexing configuration to see if a property analyzer has been set for this field
            Analyzer analyzer = configuration.getPropertyAnalyzer(fieldName);
            if (analyzer == null) {
                // we didn't configure a property analyzer for this field
                if (FieldNames.isFulltextField(fieldName)) {
                    // we have a full text field, so we can use our wrapped Analyzer
                    analyzer = wrappee;
                } else {
                    analyzer = defaultAnalyzer;
                }
            }
            return analyzer;
        }
    }
}
