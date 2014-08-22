package org.jahia.services.usermanager;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.decorator.JCRUserNode;

import javax.jcr.RepositoryException;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

/**
 * Created by toto on 22/08/14.
 */
public class JahiaUserImpl implements JahiaUser, Serializable {

    private final String name;
    private final String path;
    private final Properties properties;
    private final boolean isRoot;
    private final String providerName;

    public JahiaUserImpl(String name, String path, Properties properties, boolean isRoot, String providerName) {
        this.name = name;
        this.path = path;
        this.properties = properties;
        this.isRoot = isRoot;
        this.providerName = providerName;
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public String getUserKey() {
        return path;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public UserProperties getUserProperties() {
        return new UserProperties(properties, true);
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public UserProperty getUserProperty(String key) {
        return getUserProperties().getUserProperty(key);
    }

    @Override
    public boolean removeProperty(String key) {
        throw new UnsupportedOperationException("Setter not supported here, use JCRUserNode instead");
    }

    @Override
    public boolean setProperty(String key, String value) {
        throw new UnsupportedOperationException("Setter not supported here, use JCRUserNode instead");
    }

    @Override
    public boolean setPassword(String password) {
        throw new UnsupportedOperationException("Setter not supported here, use JCRUserNode instead");
    }

    @Override
    public boolean isMemberOfGroup(int siteID, String name) {
        throw new UnsupportedOperationException("Site id not supported, use sitekey");
    }

    @Override
    public boolean isAdminMember(int siteID) {
        throw new UnsupportedOperationException("Site id not supported, use sitekey");
    }

    @Override
    public boolean isRoot() {
        return isRoot;
    }

    @Override
    public boolean verifyPassword(String password) {
        return StringUtils.isNotEmpty(password) && JahiaUserManagerService.encryptPassword(password).equals(getProperty(JCRUserNode.J_PASSWORD));
    }

    @Override
    public String getProviderName() {
        return providerName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLocalPath() {
        return path;
    }

    @Override
    public boolean isAccountLocked() {
        return !isRoot() && Boolean.valueOf(getProperty("j:accountLocked"));
    }
}
