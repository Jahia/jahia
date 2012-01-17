/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.search.facets;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.CharStream;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParserTokenManager;
import org.apache.solr.schema.DateField;

/**
 * Extension of the Lucene QueryParser used by Jahia's query facets, which for range queries tries
 * to parse the passed arguments as "math" like strings relating to Dates, a syntax used in Solr.
 * This enables to use relative dates based on now or another fixed date.
 *
 * @author Benjamin
 */
public class JahiaQueryParser extends QueryParser {

    public static final DateField DATE_TYPE = new DateField();    
    
    public JahiaQueryParser(CharStream stream) {
        super(stream);
    }

    public JahiaQueryParser(QueryParserTokenManager tm) {
        super(tm);
    }

    public JahiaQueryParser(String f, Analyzer a) {
        super(f, a);
    }

    @Override
    protected org.apache.lucene.search.Query getRangeQuery(String field, String part1,
            String part2, boolean inclusive) throws ParseException {
        try {
          part1 = DATE_TYPE.toInternal(part1);  
          part2 = DATE_TYPE.toInternal(part2);          
        } catch (Exception e) { }

        return super.getRangeQuery(field, part1, part2, inclusive);
    }
}
