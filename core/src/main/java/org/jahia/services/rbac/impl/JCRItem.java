package org.jahia.services.rbac.impl;

import java.io.Serializable;

/**
 * JCR based role/permission item.
 * 
 * @author Sergiy Shyrkov
 */
class JCRItem implements Serializable {

    protected String description;

    private String path;

    private String title;

    /**
     * Returns the description property value of the corresponding JCR node.
     * 
     * @return the description property value of the corresponding JCR node
     */
    public String getDescription() {
        return description;
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
     * Returns the title of the corresponding JCR node.
     * 
     * @return the title of the corresponding JCR node
     */
    public String getTitle() {
        return title;
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
     * Sets the JCR path of the corresponding node
     * 
     * @param path the JCR path of the corresponding node
     */
    void setPath(String path) {
        this.path = path;
    }

    /**
     * Sets the title of the corresponding JCR node
     * 
     * @param path the title of the corresponding JCR node
     */
    void setTitle(String title) {
        this.title = title;
    }

}