package org.jahia.services.content;

import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.RepositoryException;
import java.util.Locale;

public class JCRTemplate {
    private JCRSessionFactory sessionFactory;
    private static JCRTemplate instance;

    public JCRTemplate() {
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public JCRSessionFactory getSessionFactory() {
        return sessionFactory;
    }
     public<X> X doExecuteWithSystemSession(JCRCallback<X> callback) throws RepositoryException {
         return doExecute(callback,true,null,null,null);
     }
    public<X> X doExecute(JCRCallback<X> callback, boolean useSystemSession, JahiaUser user, String workspace, Locale locale) throws RepositoryException {
        JCRSessionWrapper session = null;
        try {
            session = (useSystemSession ? sessionFactory.getSystemSession(user != null ? user.getName() : null,
                                                                                               workspace) : sessionFactory.getThreadSession(user, workspace, locale));
            return callback.doInJCR(session);
        }  finally {
            if (session != null && useSystemSession) {
                session.logout();
            }
        }
    }

    public static JCRTemplate getInstance() {
        if (instance == null) {
            instance = new JCRTemplate();
        }
        return instance;
    }
}