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