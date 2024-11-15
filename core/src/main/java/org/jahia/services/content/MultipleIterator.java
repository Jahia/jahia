/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import javax.jcr.RangeIterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Aggregate multiple iterators
 */
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

    private int getIteratorIndex() {
        while (!iterators.get(iteratorIndex).hasNext() && iteratorIndex < iterators.size()-1) {
            iteratorIndex++;
        }
        return iteratorIndex;
    }
}
