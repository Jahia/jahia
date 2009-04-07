package org.jahia.ajax.gwt.client.widget.form;

import com.extjs.gxt.ui.client.widget.form.FormPanel;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 6 avr. 2009
 * Time: 11:29:25
 * To change this template use File | Settings | File Templates.
 */
public abstract class FormQuickMashup extends FormPanel {
    private String folderPath;

    protected FormQuickMashup(String folderPath) {
        this.folderPath = folderPath;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public abstract void onMashupCreated();

}
