/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
