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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.jahia.hibernate.model.JahiaLink;
import org.jahia.hibernate.model.JahiaXRef;
import org.jahia.hibernate.model.JahiaXRefPK;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 14 mars 2005
 * Time: 12:06:02
 * To change this template use File | Settings | File Templates.
 */
public class JahiaLinkDAO extends AbstractGeneratorDAO {
// ------------------------------ FIELDS ------------------------------

    private Log log = LogFactory.getLog(JahiaLinkDAO.class);

// -------------------------- OTHER METHODS --------------------------

    public void delete(JahiaLink jahiaLink) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.delete(jahiaLink);
    }

    public void delete(JahiaXRef jahiaLink) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.delete(jahiaLink);
    }

    public List<JahiaLink> findByLeftAndRightObjectKeys(String leftKey, String rightKey) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return hibernateTemplate.find("from JahiaLink link where link.rightOid=? and link.leftOid=?",
                                      new Object[]{rightKey, leftKey});
    }

    public List<JahiaLink> findByLeftObjectKey(String objectKey) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return hibernateTemplate.find("from JahiaLink link where link.leftOid=?", objectKey);
    }

    public List<JahiaLink> findByRightObjectKey(String objectKey) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return hibernateTemplate.find("from JahiaLink link where link.rightOid=?", objectKey);
    }
    
    public List<JahiaLink> findByRightAndLikeLeftObjectKey(String objectKey, String leftKey) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return hibernateTemplate.find("from JahiaLink link where link.rightOid=? and link.leftOid like ?", 
        		new Object[]{objectKey, leftKey});
    }    
    
    public List<JahiaLink> findByTypeAndLeftAndLikeRightObjectKeys(String leftKey, String rightKey, String type) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return hibernateTemplate.find(
                "from JahiaLink link where link.type=? and link.leftOid=? and link.rightOid like ?",
                new Object[]{type, leftKey, rightKey});
    }    

    public List<JahiaLink> findByTypeAndLeftAndRightObjectKeys(String leftKey, String rightKey, String type) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return hibernateTemplate.find(
                "from JahiaLink link where link.rightOid=? and link.leftOid=? and link.type=?",
                new Object[]{rightKey, leftKey, type});
    }

    public Integer countType(String type) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        List l = hibernateTemplate.find("select count(l) from JahiaLink l where l.type=?", new Object[]{type});
        return new Integer(((Long)l.get(0)).intValue());
    }

    public List<JahiaLink> findByTypeAndLeftObjectKey(String leftKey, String type) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return hibernateTemplate.find("from JahiaLink link where link.leftOid=? and link.type=?",
                                      new Object[]{leftKey, type});
    }

    public List<JahiaLink> findByTypeAndRightObjectKey(String rightKey, String type) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return hibernateTemplate.find("from JahiaLink link where link.rightOid=? and link.type=?",
                                      new Object[]{rightKey, type});
    }

    public List<JahiaLink> findByTypeAndRightAndLikeLeftObjectKey(String rightKey, String leftKey, String type) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return hibernateTemplate.find("from JahiaLink link where link.type=? and link.rightOid=? and link.leftOid like ?",
                                      new Object[]{type, rightKey, leftKey});
    }    
    
    public List<JahiaLink> findByTypeAndRightObjectKeys(String[] rightKeys, String type) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        StringBuffer buff = new StringBuffer("from JahiaLink link where link.type=? AND ( ");
        Object[] objects = new Object[rightKeys.length+1];
        objects[0]=type;
        for ( int i=0; i<rightKeys.length ; i++ ){
            buff.append("link.rightOid=?");
            objects[i+1]=rightKeys[i];
            if ( i<rightKeys.length-1 ){
                buff.append(" OR ");
            }
        }
        buff.append(" )");
        return hibernateTemplate.find("from JahiaLink link where link.type=? AND link.rightOid in (",
                                      objects);
    }

    public JahiaLink getJahiaLink(Integer linkId) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return (JahiaLink) hibernateTemplate.load(JahiaLink.class, linkId);
    }

    public void preloadObjectXRefsByRefObjectKey(String objectKey) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        try {
            if (getSession().connection().getMetaData().supportsSubqueriesInIns()) {
                template.find("from JahiaLink link " +
                              "where link.rightOid in (" +
                              "select link2.rightOid from JahiaLink link2 " +
                              "where link2.leftOid=?) " +
                              "order by link.rightOid", objectKey);
            }
        } catch (SQLException e) {
            log.error("SQLException during preload of ObjectXRefs", e);
        } catch (HibernateException e) {
            log.error("HibernateException during preload of ObjectXRefs", e);
        }
    }

    public void save(JahiaLink jahiaLink) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        if (jahiaLink.getId() == null) {
            jahiaLink.setId(getNextInteger(jahiaLink));
        }
        template.merge(jahiaLink);
    }

    public void update(JahiaLink jahiaLink) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.update(jahiaLink);
    }


    public JahiaXRef getJahiaXRef(Integer pageId, Integer refId, Integer refType) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return (JahiaXRef) hibernateTemplate.get(JahiaXRef.class, new JahiaXRefPK(pageId, refId, refType));
    }

    public List<JahiaXRef> findJahiaXRefForPage(Integer pageId) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return hibernateTemplate.find("from JahiaXRef r " +
                      "where r.comp_id.pageId=?", pageId);
    }

    public List<JahiaXRef> findJahiaXRefForPageAndType(Integer pageId, Integer type) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return hibernateTemplate.find("from JahiaXRef r " +
                      "where r.comp_id.pageId=? and r.comp_id.refType=?", new Object[] {pageId, type});
    }

    public List<JahiaXRef> findJahiaXRefForObject(Integer refId, Integer type) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return hibernateTemplate.find("from JahiaXRef r " +
                      "where r.comp_id.refId=? and r.comp_id.refType=?", new Object[] {refId, type});
    }

    public void save(JahiaXRef jahiaLink) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        synchronized(this) {
            if (template.get(JahiaXRef.class, jahiaLink.getComp_id()) == null) {
                template.save(jahiaLink);
                template.flush();
            }
        }
    }

}

