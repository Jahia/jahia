/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.core.query.lucene;

import org.apache.lucene.analysis.Analyzer;
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
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.util.Version;
import org.jahia.services.search.analyzer.ASCIIFoldingAnalyzerWrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Christophe Laprun
 */
public class LanguageCustomizingAnalyzerRegistry implements AnalyzerRegistry<String> {
    /**
     * Language to Analyzer map.
     */
    private static final Map<String, Analyzer> languageToAnalyzer = new ConcurrentHashMap<String, Analyzer>();

    static {
        languageToAnalyzer.put("ar", new ASCIIFoldingAnalyzerWrapper(new ArabicAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("br", new ASCIIFoldingAnalyzerWrapper(new BrazilianAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("cjk", new ASCIIFoldingAnalyzerWrapper(new CJKAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("cn", new ASCIIFoldingAnalyzerWrapper(new ChineseAnalyzer()));
        languageToAnalyzer.put("cz", new ASCIIFoldingAnalyzerWrapper(new CzechAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("de", new ASCIIFoldingAnalyzerWrapper(new GermanAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("el", new ASCIIFoldingAnalyzerWrapper(new GreekAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("en", new ASCIIFoldingAnalyzerWrapper(new org.apache.lucene.analysis.standard.StandardAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("fa", new ASCIIFoldingAnalyzerWrapper(new PersianAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("fr", new ASCIIFoldingAnalyzerWrapper(new FrenchAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("nl", new ASCIIFoldingAnalyzerWrapper(new DutchAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("ru", new ASCIIFoldingAnalyzerWrapper(new RussianAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("th", new ASCIIFoldingAnalyzerWrapper(new ThaiAnalyzer(Version.LUCENE_30)));
    }

    @Override
    public Analyzer getAnalyzerFor(Document document) {
        final String key = getKeyFor(document);
        if (key != null) {
            return languageToAnalyzer.get(key);
        }

        return null;
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
        return languageToAnalyzer.get(key);
    }

    @Override
    public boolean acceptKey(Object key) {
        return key instanceof String;
    }
}
