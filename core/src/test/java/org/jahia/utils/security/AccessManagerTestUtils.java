package org.jahia.utils.security;

import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;

public class AccessManagerTestUtils {
    public static JCRCallback<?> setCurrentUserCallback(JahiaUser jahiaUser, JCRCallback<?> callback) {
        return session -> {
            JahiaUser originalUser = JCRSessionFactory.getInstance().getCurrentUser();
            try {
                JCRSessionFactory.getInstance().setCurrentUser(jahiaUser);
                return callback.doInJCR(session);
            } finally {
                JCRSessionFactory.getInstance().setCurrentUser(originalUser);
            }
        };
    }
}
