/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

 package org.jahia.services.search.analyzer;

import org.compass.core.lucene.engine.analyzer.LuceneAnalyzerTokenFilterProvider;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.CompassException;
import org.compass.core.engine.SearchEngineException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

public class SnowballFilterProvider implements LuceneAnalyzerTokenFilterProvider {

    private String snowballName;

    public void configure(CompassSettings settings) throws CompassException {
        snowballName = settings.getSetting(LuceneEnvironment.Analyzer.Snowball.NAME_TYPE);
        if (snowballName == null) {
            throw new SearchEngineException("When using a snowball analyzer, must set the + ["
                    + LuceneEnvironment.Analyzer.Snowball.NAME_TYPE + "] setting for it");
        }
    }

    public TokenFilter createTokenFilter(TokenStream tokenStream) {
        return new SnowballFilter(tokenStream, snowballName);
    }
}
