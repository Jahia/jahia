package org.jahia.ajax.gwt.client.data;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Jan 14, 2009
 * Time: 3:26:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaInlineEditingResultBean implements Serializable {
    private boolean contentModified = false;
    private boolean successful = true;

    public boolean isContentModified() {
        return contentModified;
    }

    public void setContentModified(boolean contentModified) {
        this.contentModified = contentModified;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
}
