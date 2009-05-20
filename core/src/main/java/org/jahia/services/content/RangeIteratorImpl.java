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
package org.jahia.services.content;

import javax.jcr.RangeIterator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 17 d�c. 2007
 * Time: 10:09:55
 * To change this template use File | Settings | File Templates.
 */
public class RangeIteratorImpl implements RangeIterator {

    private long size;
    private long pos=0;
    private Iterator iterator;

    public RangeIteratorImpl(Iterator iterator, long size) {
        this.iterator = iterator;
        this.size = size;
    }

    public void skip(long l) {
        if ((pos + l + 1) > size) {
            throw new NoSuchElementException("Tried to skip past " + l +
                    " elements, which with current pos (" + pos +
                    ") brings us past total size=" + size);
        }
        for (int i=0; i < l; i++) {
            next();
        }
    }

    public long getSize() {
        return size;
    }

    public long getPosition() {
        return pos;
    }


    public boolean hasNext() {
        return iterator.hasNext();
    }

    public Object next() {
        pos += 1;
        return iterator.next();
    }

    public void remove() {
        iterator.remove();
        size -= 1;
    }
}
