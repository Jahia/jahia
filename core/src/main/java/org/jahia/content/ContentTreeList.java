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

 package org.jahia.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.jahia.exceptions.JahiaException;

/**
 * <p>Title: Class to traverse Jahia's Content Tree and maintaining an internal List of object.</p>
 * <p>Description: This class define a default implementation for traversing
 * Jahia's Content Tree.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */

public class ContentTreeList extends ContentTree {

    private List list;

    /**
     * The root content object of the three
     *
     * @param rootContentObject
     */
    public ContentTreeList(ContentObject rootContentObject) {
        super(rootContentObject);
        this.list = new ArrayList();
    }

    /**
     * Iterate through the entire Tree
     * Reset the internal list first before iterating the entire Tree
     *
     * @param visitor
     */
    public void iterate(ContentTreeVisitorInterface visitor)
    throws JahiaException {
        this.list = new ArrayList();
        super.iterate(visitor);
    }

    /**
     * Returns the internal list
     *
     * @return
     */
    public List getList(){
        return this.list;
    }

    public void sort(Comparator comparator){
        Object[] array = this.list.toArray();
        Arrays.sort(array,comparator);
        this.list = new ArrayList(Arrays.asList(array));
    }
}