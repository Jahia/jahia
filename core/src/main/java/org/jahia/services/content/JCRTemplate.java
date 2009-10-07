package org.jahia.services.content;

import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.RepositoryException;
import java.util.Locale;

public class JCRTemplate {
    private JCRSessionFactory sessionFactory;
    private static JCRTemplate instance;

    private JCRTemplate() {
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public JCRSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public <X> X doExecuteWithSystemSession(JCRCallback<X> callback) throws RepositoryException {
        return doExecuteWithSystemSession(callback,  null, null, null);
    }

    public <X> X doExecuteWithSystemSession(JCRCallback<X> callback, String username) throws RepositoryException {
        return doExecuteWithSystemSession(callback,  username, null, null);
    }

    public <X> X doExecuteWithSystemSession(JCRCallback<X> callback, String username, String workspace) throws RepositoryException {
        return doExecuteWithSystemSession(callback,  username, workspace, null);
    }

    public <X> X doExecuteWithSystemSession(JCRCallback<X> callback, String username, String workspace, Locale locale) throws RepositoryException {
        JCRSessionWrapper session = null;
        try {
            session = sessionFactory.getSystemSession(username, workspace, locale);
            return callback.doInJCR(session);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    private <X> X doExecute(JCRCallback<X> callback, boolean useSystemSession, JahiaUser user, String workspace, Locale locale) throws RepositoryException {
        JCRSessionWrapper session = null;
        try {
            session = (useSystemSession ? sessionFactory.getSystemSession(user != null ? user.getName() : null,
                    workspace) : sessionFactory.getCurrentUserSession(workspace, locale));
            return callback.doInJCR(session);
        } finally {
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