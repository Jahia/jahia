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
 package org.jahia.utils;

import java.util.*;
import java.io.Serializable;


/**
 * <p>Title: Implementation of a set that is ordered by insertion order</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class InsertionSortedSet<E> extends AbstractSet<E> implements Serializable {

    private static final long serialVersionUID = 1289857734190978537L;
    private List<E> internalSet;

    public InsertionSortedSet() {
        internalSet = new ArrayList<E>();
    }

    public InsertionSortedSet(Collection<? extends E> c) {
        internalSet = new ArrayList<E>(c);
    }

    protected void setInternalList (List<E> internalList) {
        this.internalSet = internalList;
    }

    public boolean add(E o)
        throws UnsupportedOperationException,
        NullPointerException,
        ClassCastException,
        IllegalArgumentException {
        if (internalSet.contains(o)) {
            return false;
        } else {
            internalSet.add(o);
            return true;
        }
    }

    public Iterator<E> iterator() {
        return internalSet.iterator();
    }

    public int size() {
        return internalSet.size();
    }

}