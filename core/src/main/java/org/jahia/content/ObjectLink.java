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
