/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.search.facets;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.CharStream;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParserTokenManager;
import org.apache.lucene.util.Version;
import org.apache.solr.schema.*;

/**
 * Extension of the Lucene QueryParser used by Jahia's query facets, which for range queries tries
 * to parse the passed arguments as "math" like strings relating to Dates, a syntax used in Solr.
 * This enables to use relative dates based on now or another fixed date.
 *
 * @author Benjamin
 */
public class JahiaQueryParser extends QueryParser {
    public static final BinaryField BINARY_TYPE = new BinaryField();    
    public static final BoolField BOOLEAN_TYPE = new BoolField();    
    public static final DateField DATE_TYPE = new DateField();    
    public static final JackrabbitDateField JR_DATE_TYPE = new JackrabbitDateField();    
    public static final JackrabbitDoubleField JR_DOUBLE_TYPE = new JackrabbitDoubleField();
    public static final JackrabbitLongField JR_LONG_TYPE = new JackrabbitLongField();
    public static final DoubleField DOUBLE_TYPE = new DoubleField();
    public static final SortableDoubleField SORTABLE_DOUBLE_TYPE = new SortableDoubleField();
    public static final LongField LONG_TYPE = new LongField();
    public static final SortableLongField SORTABLE_LONG_TYPE = new SortableLongField();
    public static final StrField STRING_TYPE = new StrField();
    
    public JahiaQueryParser(CharStream stream) {
        super(stream);
    }

    public JahiaQueryParser(QueryParserTokenManager tm) {
        super(tm);
    }

    public JahiaQueryParser(String f, Analyzer a) {
        super(Version.LUCENE_30, f, a);
    }

    @Override
    protected org.apache.lucene.search.Query getRangeQuery(String field, String part1,
            String part2, boolean inclusive) throws ParseException {
        try {
            if ("*".equals(part1)) {
                part1 = null;
            } else {
                part1 = DATE_TYPE.toInternal(part1);                
            } 
        } catch (Exception e) { }
        try {
            if ("*".equals(part2)) {
                part2 = null;
            } else {
               part2 = DATE_TYPE.toInternal(part2);
            }
        } catch (Exception e) { }

        return super.getRangeQuery(field, part1, part2, inclusive);
    }
}
