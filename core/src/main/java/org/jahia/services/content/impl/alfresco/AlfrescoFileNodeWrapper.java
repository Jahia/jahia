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
//
//package org.jahia.services.content.impl.alfresco;
//
//import org.jahia.services.content.*;
//import org.jahia.services.usermanager.JahiaUser;
//import org.jahia.api.Constants;
//
//import javax.jcr.Node;
//import javax.jcr.RepositoryException;
//import javax.jcr.Session;
//import java.util.Date;
//
///**
// * Created by IntelliJ IDEA.
// * User: toto
// * Date: 6 déc. 2007
// * Time: 17:07:16
// * To change this template use File | Settings | File Templates.
// */
//public class AlfrescoFileNodeWrapper extends JCRNodeWrapperImpl {
//    public AlfrescoFileNodeWrapper(String localPath, JahiaUser user, Session session, JCRStoreProvider provider) {
//        super(localPath, user, session, provider);
//    }
//
//    public AlfrescoFileNodeWrapper(Node objectNode, JahiaUser user, Session session, JCRStoreProvider provider) {
//        super(objectNode, user, session, provider);
//    }
//
//    public boolean isFile() {
//        if (exception != null) {
//            return false;
//        }
//        try {
//            return objectNode.isNodeType("cm:content");
//        } catch (RepositoryException e) {
//            logger.error(e.getMessage(), e);
//            return false;
//        }
//
//    }
//
//    public boolean isPortlet() {
//       return false;
//    }
//
//    public boolean isCollection() {
//        if (exception != null) {
//            return false;
//        }
//        try {
//            return objectNode.isNodeType("cm:folder") || objectNode.getPath().equals("/") || objectNode.isNodeType("sys:store_root");
//        } catch (RepositoryException e) {
//            logger.error(e.getMessage(), e);
//            return false;
//        }
//
//    }
//
//    public String getName() {
//        if (exception != null) {
//            return null;
//        }
//        try {
//            if (objectNode.getPath().equals("/") && provider.getMountPoint().length() > 1) {
//                return provider.getMountPoint().substring(1);
//            } else {
//                return objectNode.getProperty("cm:name").getString();
//            }
//        } catch (RepositoryException e) {
//            logger.error("Repository error", e);
//        }
//        return null;
//    }
//
//    public long getContentLength() {
//        try {
//            return objectNode.getProperty("cm:content").getLength();
//        } catch (RepositoryException e) {
//            logger.error(e.getMessage(), e);
//        }
//        return 0L;
//    }
//
//    public String getCreationUser() {
//        try {
//            return objectNode.getProperty("cm:author").getString();
//        } catch (RepositoryException e) {
//            logger.error(e.getMessage(), e);
//        }
//        return null;
//    }
//
//    public Date getCreationDateAsDate() {
//        try {
//            return objectNode.getProperty("cm:created").getDate().getTime();
//        } catch (RepositoryException e) {
//            logger.error(e.getMessage(), e);
//        }
//        return null;
//    }
//
//    public String getModificationUser() {
//        try {
//            return objectNode.getProperty("cm:modifier").getString();
//        } catch (RepositoryException e) {
//            logger.error(e.getMessage(), e);
//        }
//        return null;
//
//    }
//
//    public Date getLastModifiedAsDate() {
//        try {
//            return objectNode.getProperty("cm:modified").getDate().getTime();
//        } catch (RepositoryException e) {
//            logger.error(e.getMessage(), e);
//        }
//        return null;
//    }
//
//    public String getUrl() {
//        if (objectNode != null) {
//            try {
//                return provider.getWebdavPath() + "/d/d/workspace/" + objectNode.getSession().getWorkspace().getName() + "/" + objectNode.getUUID() + "/" + getName();
//            } catch (RepositoryException e) {
//                logger.error("Cannot get file path", e);
//            }
//        }
//        return "";
//    }
//
//    public JCRNodeWrapper addNode(String name, String type) {
//        try {
//            Node n;
//            if (type.equals(Constants.NT_FILE) || type.equals(Constants.JAHIANT_FILE)) {
//                n = objectNode.addNode(name, "cm:content");
//            } else {
//                n = objectNode.addNode(name, "cm:folder");
//            }
//            n.setProperty("cm:name", name);
//            return provider.getNodeWrapper(n, user, session);
//        } catch (RepositoryException e) {
//            logger.error("Repository error", e);
//        }
//        return null;
//    }
//
//    public String getPrimaryNodeTypeName() {
//        String alfType = null;
//        try {
//            alfType = objectNode.getPrimaryNodeType().getName();
//            if (alfType.equals("cm:folder")) {
//                return Constants.JAHIANT_FOLDER;
//            } else if (alfType.equals("cm:content")) {
//                return Constants.JAHIANT_FILE;
//            }
//        } catch (RepositoryException e) {
//            logger.error(e.getMessage(), e);
//        }
//        return alfType;
//    }
//
//    public JCRFileContent getFileContent() {
//        return new AlfrescoFileContent(this, objectNode);
//    }
//
//    public boolean isVisible() {
//        return true;
//    }
//
//}
