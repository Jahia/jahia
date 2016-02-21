/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
 package org.jahia.utils;

import java.io.Serializable;
import java.util.*;


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