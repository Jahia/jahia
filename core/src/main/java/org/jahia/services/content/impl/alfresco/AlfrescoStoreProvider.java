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
//import org.jahia.services.content.JCRStoreProvider;
//import org.jahia.services.content.JCRNodeWrapper;
//import org.jahia.services.usermanager.JahiaUser;
//import org.jahia.services.usermanager.JahiaUserManagerService;
//import org.jahia.jaas.JahiaLoginModule;
//import org.jahia.exceptions.JahiaInitializationException;
//
//import javax.jcr.*;
//
///**
// * Created by IntelliJ IDEA.
// * User: toto
// * Date: 5 déc. 2007
// * Time: 13:57:39
// * To change this template use File | Settings | File Templates.
// */
//public class AlfrescoStoreProvider extends JCRStoreProvider {
//    private static org.apache.log4j.Logger logger =
////        org.apache.log4j.Logger.getLogger(AlfrescoStoreProvider.class);
//
//
//    public void start() throws JahiaInitializationException {
//        repo = getRepository();
//    }
//
//    public JCRNodeWrapper getNodeWrapper(String path, JahiaUser user, Session session) {
//        return getService().decorate(new AlfrescoFileNodeWrapper(path, user, session, this));
//    }
//
//    public JCRNodeWrapper getNodeWrapper(Node node, JahiaUser user, Session session) {
//        return getService().decorate(new AlfrescoFileNodeWrapper(node, user, session, this));
//    }
//
//    // Alfresco does not support NS registration at workspace level, NS must be registered
//    // on session level (non standard)
//    protected void registerNamespaces(Workspace workspace) throws RepositoryException {
////        NamespaceRegistry ns = workspace.getNamespaceRegistry();
////        try {
////            Object s = ns.getClass().getMethod("getNamespaceService", new Class[] {}).invoke(ns, new Object[]{});
////            Method m = s.getClass().getMethod("registerNamespace", new Class[] {String.class, String.class});
////            m.invoke(s, new Object[] {Constants.JAHIA_PREF, Constants.JAHIA_NS});
////            m.invoke(s, new Object[] {Constants.JAHIANT_PREF, Constants.JAHIANT_NS});
////            m.invoke( s, new Object[] {Constants.JAHIAMIX_PREF, Constants.JAHIAMIX_NS});
////        } catch (Exception e) {
////          logger.error(e.getMessage(), e);
////        }
////        super.registerNamespaces(workspace);
//    }
//
//    protected void initObservers() throws RepositoryException {
//        return;
//    }
//
//    protected Session getThreadSession(JahiaUser user) throws RepositoryException {
//        // thread user session might be inited/closed in an http filter, instead of keeping it
//        logger.debug("Acquiring thread session for " + user.getUsername() + " / " +Thread.currentThread().getName());
//
//        Session s = (Session) userSession.get();
//        String username;
//
//        if (JahiaUserManagerService.isGuest(user)) {
//            username = JahiaLoginModule.GUEST;
//        } else {
//            username = user.getUsername();
//        }
//
//        try {
//            if (s != null && loginModuleActivated && !s.getUserID().equals(username)) {
//                s.logout();
//            }
//        } catch (IllegalStateException e) {
//            logger.error("Exception on session : "+e);
//            s = null;
//        }
//
//        logger.debug("From threadlocal = "+s);
//
//        try {
//            if (s == null || !s.isLive()) {
//                logger.debug("is not live, reactivate");
//                if (loginModuleActivated) {
//                    if (!JahiaLoginModule.GUEST.equals(username)) {
//                        s = repo.login(JahiaLoginModule.getCredentials(username));
//                    } else {
//                        s = repo.login(JahiaLoginModule.getGuestCredentials());
//                    }
//                } else {
//                    s = repo.login(new SimpleCredentials(this.user, password.toCharArray()));
//                }
//                registerNamespaces(s.getWorkspace());
//                userSession.set(s);
//            } else {
//                // not supported >
//                //s.refresh(true);
//            }
//        } catch (Exception e) {
//            logger.debug("exception , reacquire session",e);
//            if (loginModuleActivated) {
//                if (!JahiaLoginModule.GUEST.equals(username)) {
//                    s = repo.login(JahiaLoginModule.getCredentials(username));
//                } else {
//                    s = repo.login(JahiaLoginModule.getGuestCredentials());
//                }
//            } else {
//                s = repo.login(new SimpleCredentials(this.user, password.toCharArray()));
//            }
//            registerNamespaces(s.getWorkspace());
//            userSession.set(s);
//        }
//        logger.debug("Acquired");
//        return s;
//    }
//
//    public Session getSystemSession() throws RepositoryException {
//        logger.debug("Acquiring system session "+Thread.currentThread().getName());
//        Session s = super.getSystemSession();    //To change body of overridden methods use File | Settings | File Templates.
//        logger.debug("Acquired");
//        return s;
//    }
//
//    public Session getSystemSession(String username) throws RepositoryException {
//        logger.debug("Acquiring system session for "+username+" / " +Thread.currentThread().getName());
//        Session s =super.getSystemSession(username);    //To change body of overridden methods use File | Settings | File Templates.
//        logger.debug("Acquired.");
//        return s;
//    }
//}
