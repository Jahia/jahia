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
