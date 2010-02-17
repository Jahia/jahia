package org.jahia.services.rbac.jcr;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * JCR based role/permission item.
 * 
 * @author Sergiy Shyrkov
 */
class BaseImpl implements Serializable {

    private String description;

    private String identifier;

    private String name;

    private String path;

    private String site;

    private String title;

    /**
     * Initializes an instance of this class.
     * 
     * @param name the name of this identity
     */
    public BaseImpl(String name) {
        this(name, null);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param name the name of this identity
     * @param site the site key of this identity
     */
    public BaseImpl(String name, String site) {
        super();
        this.name = name;
        this.site = site;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && (obj instanceof BaseImpl) ? new EqualsBuilder().append(getName(),
                ((BaseImpl) obj).getName()).append(getSite(), ((BaseImpl) obj).getSite()).isEquals() : false;
    }

    /**
     * Returns the description property value of the corresponding JCR node.
     * 
     * @return the description property value of the corresponding JCR node
     */
    public String getDescription() {
        return description;
    }

    /**
     * The underlying node identifier.
     * 
     * @return the identifier of the underlying JCR node
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the name of this identity.
     * 
     * @return the name of this identity
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the JCR path of the corresponding node.
     * 
     * @return the JCR path of the corresponding node
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the site key of this identity or <code>null</code> if it is a
     * server-level identity.
     * 
     * @return the site key of this identity or <code>null</code> if it is a
     *         server-level identity
     */
    public String getSite() {
        return site;
    }

    /**
     * Returns the title of the corresponding JCR node.
     * 
     * @return the title of the corresponding JCR node
     */
    public String getTitle() {
        return title;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getName()).append(getSite()).toHashCode();
    }

    /**
     * Sets a description of the corresponding JCR node
     * 
     * @param description a description of the corresponding JCR node
     */
    void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the identifier of the underlying JCR node
     * 
     * @param identifier the identifier to set
     */
    void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Sets the name of this role.
     * 
     * @param name the name of this role
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the JCR path of the corresponding node
     * 
     * @param path the JCR path of the corresponding node
     */
    void setPath(String path) {
        this.path = path;
    }

    /**
     * Sets the key of the target virtual site.
     * 
     * @param site the key of the target virtual site
     */
    public void setSite(String site) {
        this.site = site;
    }

    /**
     * Sets the title of the corresponding JCR node
     * 
     * @param path the title of the corresponding JCR node
     */
    void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}