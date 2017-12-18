/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.HashMap;
import java.util.Map;

public class SourceControlDialog extends Dialog {

    private RadioGroup scmType;
    private TextField<String> uri;
    private TextField<String> moduleId;
    private TextField<String> sources;
    private TextField<String> branchOrTag;
    private Map<String,Radio> radios;
    private final FormPanel form;
    private SourceControlDialog sourceControlDialog;

    public SourceControlDialog(String heading, boolean viewModuleName, boolean viewBranchOrTag) {
        setHeadingHtml(heading);
        setButtons(Dialog.OKCANCEL);
        setModal(true);
        setHideOnButtonClick(true);
        setWidth(500);
        setHeight(300);

        setLayout(new FitLayout());

        form = new FormPanel();
        form.setHeaderVisible(false);
        form.setFrame(false);
        form.setLabelWidth(175);
        scmType = new RadioGroup("scmType");
        scmType.setFieldLabel(Messages.get("label.scmType", "SCM type"));
        radios = new HashMap<String, Radio>();
        Radio git = new Radio();
        git.setBoxLabel(Messages.get("label.git", "GIT"));
        git.setValue(true);
        git.setValueAttribute("git");
        scmType.add(git);
        radios.put(git.getValueAttribute(), git);

        Radio svn = new Radio();
        svn.setBoxLabel(Messages.get("label.svn", "SVN"));
        svn.setValueAttribute("svn");
        scmType.add(svn);
        radios.put(svn.getValueAttribute(), svn);

        form.add(scmType);

        uri = new TextField<String>();
        uri.setName("uri");
        uri.setFieldLabel(Messages.get("label.uri", "URI"));
        uri.setAllowBlank(false);
        form.add(uri);

        if (viewModuleName) {
            moduleId = new TextField<String>();
            moduleId.setName("moduleId");
            moduleId.setFieldLabel(Messages.get("label.moduleId", "Module ID (artifactId)"));
            moduleId.setAllowBlank(true);
            form.add(moduleId);
            sources = new TextField<String>();
            sources.setName("sources");
            sources.setFieldLabel(Messages.get("label.sources.folder", "Sources folder (optional - will be created with new sources)"));
            form.add(sources);

        }

        if (viewBranchOrTag) {
            branchOrTag = new TextField<String>();
            branchOrTag.setName("branchOrTag");
            branchOrTag.setFieldLabel(Messages.get("label.branchOrTag", "Branch or tag"));
            form.add(branchOrTag);
        }
        add(form);
        sourceControlDialog = this;
    }

    public String getScmType() {
        return scmType.getValue().getValueAttribute();
    }

    public void setScmType(String value) {
        scmType.setValue(radios.get(value));
    }


    public String getUri() {
        return uri.getValue();
    }

    public void setUri(String value) {
        this.uri.setValue(value);
    }

    public String getModuleId() {
        return moduleId.getValue();
    }

    public void setModuleId(String moduleId) {
        this.moduleId.setValue(moduleId);
    }

    public String getSources() {
        return sources.getValue();
    }

    public void setSources(String sources) {
        this.sources.setValue(sources);
    }

    public String getBranchOrTag() {
        return branchOrTag.getValue();
    }

    public void setBranchOrTag(String value) {
        branchOrTag.setValue(value);
    }

    public void addCallback(final Listener<WindowEvent> listener) {
        addListener(Events.Hide, new Listener<WindowEvent>() {
            @Override
            public void handleEvent(WindowEvent be) {
                if (be.getButtonClicked().getItemId().equalsIgnoreCase(Dialog.OK)) {
                    if (form.isValid()) {
                        listener.handleEvent(be);
                    } else {
                        sourceControlDialog.show();
                    }
                }
            }
        });
    }
}
