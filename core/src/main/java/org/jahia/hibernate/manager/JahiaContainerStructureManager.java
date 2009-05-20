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
/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.manager;

import org.jahia.hibernate.dao.JahiaContainerStructureDAO;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 17 janv. 2005
 * Time: 14:54:47
 * To change this template use File | Settings | File Templates.
 */
public class JahiaContainerStructureManager {
    private JahiaContainerStructureDAO dao = null;
    private Log log = LogFactory.getLog(getClass());

    public void setJahiaContainerStructureDAO(JahiaContainerStructureDAO dao) {
        this.dao = dao;
    }

    public boolean hasContainerDefinitionParents(int containerDefinitionID) {
        boolean retVal = false;
        try {
            retVal = dao.hasContainerDefinitionParents(new Integer(containerDefinitionID));
        } catch(ObjectRetrievalFailureException e) {
            log.warn("Container parent not found for containerDefinitionId "+containerDefinitionID,e);
        }
        return retVal;
    }
}
