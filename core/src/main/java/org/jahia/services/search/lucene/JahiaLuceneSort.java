/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.search.lucene;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 11 janv. 2008
 * Time: 10:57:25
 * To change this template use File | Settings | File Templates.
 */
public class JahiaLuceneSort extends Sort {

    private static final long serialVersionUID = -1297174277754104244L;

    public JahiaLuceneSort() {
        super();
    }

    public JahiaLuceneSort(String field) {
        super(field);
    }

    public JahiaLuceneSort(String field, boolean reverse) {
        super(field, reverse);
    }

    public JahiaLuceneSort(String[] fields) {
        super(fields);
    }

    public JahiaLuceneSort(SortField field) {
        super(field);
    }

    public JahiaLuceneSort(SortField[] fields) {
        super(fields);
    }

}
