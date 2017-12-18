/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.categories.jcr;

import org.slf4j.Logger;
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

    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRCategory.class);    

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
            logger.warn(e.getMessage(), e);
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
