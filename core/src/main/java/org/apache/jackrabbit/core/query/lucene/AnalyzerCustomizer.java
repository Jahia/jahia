/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.core.query.lucene;

import org.apache.lucene.analysis.Analyzer;

import java.util.List;
import java.util.Map;

/**
 * Allows for customization of Analyzers. Specified in <code>indexing-configuration.xml</code>.
 *
 * @author Christophe Laprun
 */
public interface AnalyzerCustomizer {
    AnalyzerCustomizer NO_OP = new NoOpAnalyzerCustomizer();

    /**
     * Customize the specified Analyzer. Since the Analyzer interface doesn't provide many opportunities for customization after instantiation so this method has the opportunity
     * to return a different instance than the one provided. Note that it's even possible to return a completely different Analyzer instance (using a different class for
     * example) but we wouldn't advise doing this.
     *
     * @param analyzer the Analyzer instance to customize
     * @return a customized version of the provided Analyzer
     */
    Analyzer customize(Analyzer analyzer);

    /**
     * Initialize this AnalyzerCustomizer with the specified key/values pairs.
     *
     * @param properties a Map associating a key name to a List of associated values
     */
    void initFrom(Map<String, List<String>> properties);

    class NoOpAnalyzerCustomizer implements AnalyzerCustomizer {

        @Override
        public Analyzer customize(Analyzer analyzer) {
            // just return what we were given
            return analyzer;
        }

        @Override
        public void initFrom(Map<String, List<String>> properties) {
            for (Map.Entry<String, List<String>> entry : properties.entrySet()) {
                System.out.println("entry = " + entry);
            }
        }
    }
}
