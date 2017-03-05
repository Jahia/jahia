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
