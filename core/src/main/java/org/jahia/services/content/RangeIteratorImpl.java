/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
