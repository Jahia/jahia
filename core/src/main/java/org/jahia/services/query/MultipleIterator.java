/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.query;

import javax.jcr.RangeIterator;
import java.util.List;
import java.util.NoSuchElementException;

public class MultipleIterator<T extends RangeIterator> implements RangeIterator {

    private List<T> iterators;
    private int iteratorIndex = 0;
    private long position = 0;
    private long size = -1;
    private long limit;

    public MultipleIterator(List<T> iterators, long limit) {
        this.iterators = iterators;
        this.limit = limit;
    }

    @Override
    public void skip(long skipNum) {
        for (long l = 0; l < skipNum; l++) {
            next();
        }
    }

    @Override
    public long getSize() {
        if (size < 0) {
            size = 0;
            for (T it : iterators) {
                size += it.getSize();
            }
            if (limit >= 0 && size > limit) {
                size = limit;
            }
        }
        return size;
    }

    @Override
    public long getPosition() {
        return position;
    }

    @Override
    public boolean hasNext() {
        if (limit >= 0 && position == limit) {
            return false;
        }
        if (!iterators.isEmpty()) {
            return iterators.get(getIteratorIndex()).hasNext();
        }
        return false;
    }

    @Override
    public Object next() {
        if (limit >= 0 && position == limit) {
            throw new NoSuchElementException();
        }
        if (!iterators.isEmpty()) {
            Object next = iterators.get(getIteratorIndex()).next();
            position++;
            return next;
        }
        throw new NoSuchElementException();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public int getIteratorIndex() {
        while (!iterators.get(iteratorIndex).hasNext() && iteratorIndex < iterators.size()-1) {
            iteratorIndex++;
        }
        return iteratorIndex;
    }
}
