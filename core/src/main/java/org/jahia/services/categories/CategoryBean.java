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
 package org.jahia.services.categories;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 25 avr. 2005
 * Time: 16:47:40
 * To change this template use File | Settings | File Templates.
 */
public interface CategoryBean extends Serializable {
    /**
     * @return the database identifier for the category
     */
    int getId ();

    /**
     * @return the category key (unique) name
     */
    String getKey ();

    /**
     * Sets the category key (unique) name
     *
     * @param key the unique identifier for the category
     */
    void setKey (String key);

    /**
     * @return acl identifier for the category
     */
    int getAclID ();

}
