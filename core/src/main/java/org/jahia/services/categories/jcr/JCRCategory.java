/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.categories.jcr;

import org.apache.log4j.Logger;
import org.jahia.services.categories.CategoryBean;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.Serializable;


/**
 * This is the CategoryBean implementation for categories stored in a JCR
 *
 * @author Benjamin Papez
 *
 */
/**
 * TODO Comment me
 *
 * @author Benjamin
 *
 */
public class JCRCategory implements CategoryBean, Serializable {

    private static final long serialVersionUID = -7647957842076042700L;

    private transient static Logger logger = Logger.getLogger(JCRCategory.class);    

    private String id;    

    private String key;

    private String path;
    
    private Node categoryNode;
    
    /**
     * Initializes an instance of a JCR category.
     * @param categoryNode the node in the JCR
     */
    public JCRCategory(Node categoryNode) {
        super();
        this.categoryNode = categoryNode;
        try {
            this.id = categoryNode.getUUID();
            this.key =  categoryNode.getName();
            this.path = categoryNode.getPath();
        } catch (RepositoryException e) {
            logger.warn(e, e);
        }        
    }

    /* (non-Javadoc)
     * @see org.jahia.services.categories.CategoryBean#getId()
     */
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.jahia.services.categories.CategoryBean#getKey()
     */
    public String getKey() {
        return key;
    }

    /* (non-Javadoc)
     * @see org.jahia.services.categories.CategoryBean#setKey(java.lang.String)
     */
    public void setKey(String key) {
        this.key = key;
    }

    public String getPath(){
       return path;
    }


    /**
     * Get the category node in the JCR 
     * @return the JCR node
     */
    public Node getCategoryNode() {
        return categoryNode;
    }
}
