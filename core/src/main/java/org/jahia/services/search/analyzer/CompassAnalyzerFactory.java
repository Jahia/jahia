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

import org.compass.core.lucene.engine.analyzer.LuceneAnalyzerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 21 sept. 2005
 * Time: 15:46:50
 * To change this template use File | Settings | File Templates.
 */
public class CompassAnalyzerFactory implements LuceneAnalyzerFactory {

    public org.apache.lucene.analysis.Analyzer
            createAnalyzer(String string,
                           org.compass.core.config.CompassSettings compassSettings)
            throws org.compass.core.engine.SearchEngineException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        return analyzer;
    }
}
