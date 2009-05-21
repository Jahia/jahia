/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
