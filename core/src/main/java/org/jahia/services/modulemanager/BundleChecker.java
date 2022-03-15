package org.jahia.services.modulemanager;

import org.jahia.services.modulemanager.persistence.PersistentBundle;

public interface BundleChecker {

    void check(PersistentBundle bundle);
}
