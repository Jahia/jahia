package org.jahia.services.workflow.jbpm.custom.email;

/**
 * Created with IntelliJ IDEA.
 * User: loom
 * Date: 28.06.13
 * Time: 20:58
 * To change this template use File | Settings | File Templates.
 */
public class AttachmentTemplate {
    private String name;
    private String description;
    private String mimeType;
    private String url;
    private String expression;
    private String file;
    private String resource;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
