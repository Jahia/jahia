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
package org.jahia.hibernate.dao;

import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 17 janv. 2005
 * Time: 14:24:54
 * To change this template use File | Settings | File Templates.
 */
public class JahiaContainerStructureDAO extends AbstractGeneratorDAO {
    public boolean hasContainerDefinitionParents(Integer containerDefinitionId){
        boolean retVal = false;
        String hql = "select count(*) from JahiaCtnStruct s " +
                     "where s.comp_id.objDefId=? and s.comp_id.objType=2";
        if(containerDefinitionId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            retVal = ((Long)template.find(hql, new Object[]{containerDefinitionId}).iterator().next()).intValue() > 0;
        }
        return retVal;
    }
}
