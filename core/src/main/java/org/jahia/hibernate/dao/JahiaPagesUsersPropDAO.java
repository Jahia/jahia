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

package org.jahia.hibernate.dao;

import org.jahia.hibernate.model.JahiaPagesUsersProp;
import org.jahia.hibernate.model.JahiaPagesUsersPropPK;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Jahia.
 * User: ktlili
 * Date: 22 nov. 2007
 * Time: 11:01:36
 */
public class JahiaPagesUsersPropDAO extends AbstractGeneratorDAO {
    /**
     * logging
     */
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JahiaPagesUsersPropDAO.class);

    public void savePageUserProperty(Integer pageID, String principalKey, String principalType, String propType, String name, String value) {
        logger.debug("save properties");
        logger.debug("pageID=[" + pageID + "]");
        logger.debug("principalKey=[" + principalKey + "]");
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);

        // get pk
        JahiaPagesUsersPropPK comp_id = new JahiaPagesUsersPropPK(pageID, principalKey, principalType, propType, name);

        // get entities
        List entities = template.find("from JahiaPagesUsersProp p where p.comp_id.pageId=? and p.comp_id.principalKey=? and p.comp_id.principalType=? and p.comp_id.propType=? and p.comp_id.name=?", new Object[]{comp_id.getPageId(), comp_id.getPrincipalKey(), comp_id.getPrincipalType(), comp_id.getPropType(), comp_id.getName()});
        if (entities == null || entities.size() == 0) {
            try {
                template.save(new JahiaPagesUsersProp(comp_id, value));
            } catch (Exception t) {
                logger.debug("enable to save jahiaPageUserProp due to following error:", t);
            }
        } else {
            try {
                JahiaPagesUsersProp entity = new JahiaPagesUsersProp(comp_id, value);
                if (entities.contains(entity)) {
                    template.merge(entity);
                } else {
                    template.save(entity);
                }
            } catch (Exception t) {
                logger.debug("enable to update jahiaPageUserProp due to following error:", t);
            }
        }
    }

    public void savePageUserProperties(Integer pageID, String principalKey, String principalType, String commonPropType, List names, Map values) {
        logger.debug("save properties");
        logger.debug("pageID=[" + pageID + "]");
        logger.debug("principalKey=[" + principalKey + "]");
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);

        // get pks
        List jahiaPagesUsersPropPKList = new ArrayList();
        for (int i = 0; i < names.size(); i++) {
            String name = (String) names.get(i);
            JahiaPagesUsersPropPK comp_id = new JahiaPagesUsersPropPK(pageID, principalKey, principalType, commonPropType, name);
            jahiaPagesUsersPropPKList.add(comp_id);
        }

        // get entities
        List entities = template.find("from JahiaPagesUsersProp p where p.comp_id in ?", new Object[]{jahiaPagesUsersPropPKList});
        for (int i = 0; i < jahiaPagesUsersPropPKList.size(); i++) {
            JahiaPagesUsersPropPK comp_id = (JahiaPagesUsersPropPK) jahiaPagesUsersPropPKList.get(i);
            String value = (String) values.get(comp_id.getName());
            JahiaPagesUsersProp entity = new JahiaPagesUsersProp(comp_id, value);
            if (entities != null && entities.contains(entity)) {
                // update
                if (!entity.getValue().equals(value)) {
                    template.merge(entity);
                }
            } else {
                // add new
                template.save(entity);
            }
        }
    }

    public void deleteProperties(Integer pageId, String principalKey, String principalType) {
        logger.debug("delete properties");
        HibernateTemplate template = getHibernateTemplate();
        template.deleteAll(template.find("from JahiaPagesUsersProp p where p.comp_id.pageId=? and p.comp_id.principalKey=? and p.comp_id.principalType=? ", new Object[]{pageId, principalKey, principalType}));
    }

    public void deleteProperties(String propType) {
        logger.debug("delete properties by propType");
        HibernateTemplate template = getHibernateTemplate();

        // get entities
        List entities = template.find("from JahiaPagesUsersProp p where p. propType=? ", new Object[]{propType});
        template.deleteAll(entities);
    }

    public void deleteProperty(Integer pageID, String principalKey, String principalType, String propType, String name) {
        logger.debug("delete property");
        HibernateTemplate template = getHibernateTemplate();
        // get pk
        JahiaPagesUsersPropPK comp_id = new JahiaPagesUsersPropPK(pageID, principalKey, principalType, propType, name);

        // get entities
        List entities = template.find("from JahiaPagesUsersProp p where p.comp_id=? ", new Object[]{comp_id});
        template.deleteAll(entities);
    }

    public JahiaPagesUsersProp getPageUserProperty(Integer pageID, String principalKey, String principalType, String propType, String propName) {
        logger.debug("pageID=[" + pageID + "]");
        logger.debug("principalKey=[" + principalKey + "]");
        logger.debug("principalType=[" + principalType + "]");
        logger.debug("propName=[" + propName + "]");
        HibernateTemplate template = getHibernateTemplate();
        List result = template.find("from JahiaPagesUsersProp p where p.comp_id.pageId=? and p.comp_id.principalKey=? and p.comp_id.principalType=? and p.comp_id.propType=?", new Object[]{pageID, principalKey, principalType, propType, propName});
        if (result != null && !result.isEmpty()) {
            if (result.size() > 0) {
                logger.error("There is more than one entry with the following key properties:\n" +
                        "pageID=[" + pageID + "]\n" +
                        "principalKey=[" + principalKey + "]\n" +
                        "principalType=[" + principalType + "]\n" +
                        "propType=[" + propType + "]\n" +
                        "propName=[" + propName + "]");
            }
            return (JahiaPagesUsersProp) result.iterator().next();
        }
        return null;
    }

    public List getPageUserProperties(Integer pageID, String principalKey, String principalType, String propType) {
        logger.debug("pageID=[" + pageID + "]");
        logger.debug("principalKey=[" + principalKey + "]");
        logger.debug("principalType=[" + principalType + "]");
        HibernateTemplate template = getHibernateTemplate();

        return template.find("from JahiaPagesUsersProp p where p.comp_id.pageId=? and p.comp_id.principalKey=? and p.comp_id.principalType=? and p.comp_id.propType=?", new Object[]{pageID, principalKey, principalType, propType});
    }

    public List getPropertiesByPrincipalKey(String principalKey, String principalType, String propType) {
        logger.debug("principalKey=[" + principalKey + "]");
        logger.debug("principalType=[" + principalType + "]");
        HibernateTemplate template = getHibernateTemplate();

        return template.find("from JahiaPagesUsersProp p where p.comp_id.principalKey=? and p.comp_id.principalType=? and p.comp_id.propType=? ", new Object[]{principalKey, principalType, propType});
    }

    public List getPropertiesByPageID(Integer pageID) {
        logger.debug("pageID=[" + pageID + "]");
        HibernateTemplate template = getHibernateTemplate();

        return template.find("from JahiaPagesUsersProp p where p.comp_id.pageId=? ", new Object[]{pageID});
    }
}
