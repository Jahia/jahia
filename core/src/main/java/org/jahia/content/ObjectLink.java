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
 package org.jahia.content;

import java.util.Map;

import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaLinkManager;
import org.jahia.hibernate.manager.SpringContextSingleton;

import java.util.List;
import java.io.Serializable;

/**
 * <p>Title: Represents a link between two objects.</p>
 * <p>Description: A link between two objects represents a semantic link that
 * could create any association of sense, ownership, categorization, reference,
 * and so on between two objects. This is a very general link definition that
 * allows us to create dynamic links that can then be used to do data integrity
 * checks, link data analysis, etc..</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Serge Huber
 * @version 1.0
 */

public class ObjectLink implements Serializable {

    private static final long serialVersionUID = -300945884777460149L;
    
    private int ID = -1;
    private ObjectKey leftObjectKey;
    private ObjectKey rightObjectKey;
    private String type;
    private Map<String, String> commonMetadata;

    public ObjectLink(int ID, ObjectKey leftObjectKey,
                      ObjectKey rightObjectKey,
                      String type,
                      Map<String, String> commonMetadata) {
        this.ID = ID;
        this.leftObjectKey = leftObjectKey;
        this.rightObjectKey = rightObjectKey;
        this.type = type;
        this.commonMetadata = commonMetadata;
    }

    static public ObjectLink createLink (ObjectKey leftObjectKey,
                                         ObjectKey rightObjectKey,
                                         String type, int status,
                                         java.util.Date creationDate,
                                         String creationUserKey,
                                         java.util.Date lastModificationDate,
                                         String lastModificationUserKey,
                                         Map<String, String> leftObjectMetadata,
                                         Map<String, String> rightObjectMetadata,
                                         Map<String, String> commonMetadata)
        throws JahiaException {
           return createLink(leftObjectKey, rightObjectKey, type, commonMetadata);
    }

    static public ObjectLink createLink(ObjectKey leftObjectKey,
                                        ObjectKey rightObjectKey,
                                        String type,
                                        Map<String, String> commonMetadata)
        throws JahiaException {
        ObjectLink link = new ObjectLink( -1, leftObjectKey, rightObjectKey,
                                         type,
                commonMetadata);
        JahiaLinkManager linkManager = (JahiaLinkManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaLinkManager.class.getName());
        linkManager.createObjectLink(link);
        return link;
    }

    static public ObjectLink getLink (int linkID)
        throws JahiaException {
        JahiaLinkManager linkManager = (JahiaLinkManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaLinkManager.class.getName());
        return linkManager.getObjectLink(linkID);
    }

    static public List<ObjectLink> findByLeftAndRightObjectKeys (ObjectKey leftObjectKey,
        ObjectKey rightObjectKey)
        throws JahiaException {
        JahiaLinkManager linkManager = (JahiaLinkManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaLinkManager.class.getName());
        return linkManager.findByLeftAndRightObjectKeys(leftObjectKey, rightObjectKey);
    }

    static public List<ObjectLink> findByLeftObjectKey (ObjectKey leftObjectKey)
        throws JahiaException {
        JahiaLinkManager linkManager = (JahiaLinkManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaLinkManager.class.getName());
        return linkManager.findByLeftObjectKey(leftObjectKey);
    }

    static public List<ObjectLink> findByRightObjectKey (ObjectKey rightObjectKey)
        throws JahiaException {
        JahiaLinkManager linkManager = (JahiaLinkManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaLinkManager.class.getName());
        return linkManager.findByRightObjectKey(rightObjectKey);
    }

    static public List<ObjectLink> findByTypeAndLeftAndRightObjectKeys (String type,
        ObjectKey leftObjectKey,
        ObjectKey rightObjectKey)
        throws JahiaException {
        JahiaLinkManager linkManager = (JahiaLinkManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaLinkManager.class.getName());
        return linkManager.findByTypeAndLeftAndRightObjectKeys(type, leftObjectKey, rightObjectKey);
    }

    static public List<ObjectLink> findByTypeAndLeftAndLikeRightObjectKeys (String type,
            ObjectKey leftObjectKey,
            String rightObjectKey)
            throws JahiaException {
    	JahiaLinkManager linkManager = (JahiaLinkManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaLinkManager.class.getName());    	
        return linkManager.findByTypeAndLeftAndLikeRightObjectKeys(type, leftObjectKey, rightObjectKey);
    }     
    
    static public List<ObjectLink> findByTypeAndLeftObjectKey (String type,
        ObjectKey leftObjectKey)
        throws JahiaException {
        JahiaLinkManager linkManager = (JahiaLinkManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaLinkManager.class.getName());
        return linkManager.findByTypeAndLeftObjectKey(type, leftObjectKey);
    }

    static public List<ObjectLink> findByTypeAndRightObjectKey (String type,
        ObjectKey rightObjectKey)
        throws JahiaException {
        JahiaLinkManager linkManager = (JahiaLinkManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaLinkManager.class.getName());
        return linkManager.findByTypeAndRightObjectKey(type, rightObjectKey);
    }

    static public List<ObjectLink> findByTypeAndRightAndLikeLeftObjectKey(String type,
			ObjectKey rightObjectKey, String leftObjectKey)
			throws JahiaException {
        JahiaLinkManager linkManager = (JahiaLinkManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaLinkManager.class.getName());    	
		return linkManager.findByTypeAndRightAndLikeLeftObjectKey(type,
				rightObjectKey, leftObjectKey);
	}    
    
    public void save()
        throws JahiaException {
        JahiaLinkManager linkManager = (JahiaLinkManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaLinkManager.class.getName());
        linkManager.updateObjectLink(this);
    }

    public void remove()
        throws JahiaException {
        JahiaLinkManager linkManager = (JahiaLinkManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaLinkManager.class.getName());
        linkManager.removeObjectLink(type, leftObjectKey, rightObjectKey, getID());
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getID () {
        return ID;
    }

    public ObjectKey getLeftObjectKey () {
        return leftObjectKey;
    }

    public void setLeftObjectKey (ObjectKey leftObjectKey) {
        this.leftObjectKey = leftObjectKey;
    }

    public ObjectKey getRightObjectKey () {
        return rightObjectKey;
    }

    public void setRightObjectKey (ObjectKey rightObjectKey) {
        this.rightObjectKey = rightObjectKey;
    }

    public String getType () {
        return type;
    }

    public void setType (String type) {
        this.type = type;
    }

    public Map<String, String> getCommonMetadata () {
        return commonMetadata;
    }

    public void setCommonObjectMetadata (Map<String, String> commonObjectMetadata) {
        if ( commonObjectMetadata != null ){
            this.commonMetadata = commonObjectMetadata;
        }
    }

}
