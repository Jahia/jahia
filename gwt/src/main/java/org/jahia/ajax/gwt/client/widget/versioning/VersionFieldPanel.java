/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.versioning;

import java.util.Date;

import org.jahia.ajax.gwt.client.data.GWTJahiaRevision;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 19 nov. 2008
 * Time: 15:54:34
 * To change this template use File | Settings | File Templates.
 */
public class VersionFieldPanel extends VerticalPanel {

    private VersionField versionField;
    private Label bottomMsg;

    public VersionFieldPanel(VersionField versionField) {
        this.versionField = versionField;
        this.versionField.addListener(Events.Change, new Listener<BaseEvent>() {
            public void handleEvent(BaseEvent event) {
                updateVersionLabel();
            }
        });
        this.initWidget();
    }

    public VersionField getVersionField() {
        return versionField;
    }

    public void setVersionField(VersionField versionField) {
        this.versionField = versionField;
    }

    public void updateVersionLabel(){
        /*
        if (this.versionField != null &&
                this.versionField.isUseVersion() && this.versionField.getSelectedVersion()!=null){
            this.bottomMsg.setText(ResourceBundle.getNotEmptyResource("versioning_"+ GWTJahiaVersion.VERSION_LABEL,
                "Version") + " : " +  this.getVersionField().getSelectedVersion().getReadableName());
        } else {
            this.bottomMsg.setText("");
        }*/
    }

    public void applyRevisionValuesToVersionField(GWTJahiaRevision revision){
        if (getVersionField() == null){
            return;
        }
        if (revision == null){
            versionField.resetSetting();
        }
        versionField.setSelectedVersion(revision.getVersion());
        if (revision.getDate()>0){
            versionField.setValue(new Date(revision.getDate()));
        }
        versionField.setUseVersion(revision.isUseVersion());
        updateVersionLabel();
    }

    private void initWidget(){
        this.bottomMsg = new Label("",false);
        this.updateVersionLabel();
        this.add(this.versionField);
        this.add(this.bottomMsg);
    }

}
