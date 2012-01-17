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

package org.apache.jackrabbit.core.query;

import org.apache.jackrabbit.core.query.lucene.join.SimpleQueryResult;

import javax.jcr.RepositoryException;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Simple query result implementation.
 */
public class JahiaSimpleQueryResult extends SimpleQueryResult {

    private RowIterator rowIterator;
    private List<Row> rows = new ArrayList<Row>();

    public JahiaSimpleQueryResult(String[] columnNames, String[] selectorNames, RowIterator rowIterator) {
        super(columnNames, selectorNames, rowIterator);
        this.rowIterator = rowIterator;
    }

    public RowIterator getRows() throws RepositoryException {
        return new Iterator();
    }

    private class Iterator implements RowIterator {
        private int position = 0;

        public Row nextRow() {
            if (position < rows.size()) {
                return rows.get(position++);
            }
            if (rowIterator.hasNext()) {
                rows.add(rowIterator.nextRow());
                return rows.get(position++);
            }
            throw new NoSuchElementException();
        }

        public void skip(long skipNum) {
            for (int i = 0; i < skipNum; i++) {
                nextRow();
            }
        }

        public long getSize() {
            return rowIterator.getSize();
        }

        public long getPosition() {
            return position;
        }

        public boolean hasNext() {
            return position < rows.size() || rowIterator.hasNext();
        }

        public Object next() {
            return nextRow();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
