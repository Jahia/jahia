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
