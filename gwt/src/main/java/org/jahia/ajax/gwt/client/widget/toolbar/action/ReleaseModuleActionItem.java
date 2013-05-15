/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.RpcMap;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

import java.util.*;

/**
 * Action item to export and download the current templates set as a war
 */
public class ReleaseModuleActionItem extends BaseActionItem {

    @Override public void onComponentSelection() {
        final Window window = new Window();
        String versionInfo = JahiaGWTParameters.getSiteNode().get("j:versionInfo");
        window.setHeading(Messages.get("label.releaseWar")+"&nbsp;"+versionInfo+"&nbsp;->&nbsp;"+versionInfo.replace("-SNAPSHOT",""));
        window.setSize(500, 150);
        window.setResizable(false);
        window.setLayout(new FillLayout());
        window.setButtonAlign(Style.HorizontalAlignment.CENTER);
        window.setModal(true);


        final List<Integer> versionNumbers = JahiaGWTParameters.getSiteNode().get("j:versionNumbers");
        final FormPanel formPanel = new FormPanel();
        formPanel.setHeaderVisible(false);
        final TextField<String> vn = new TextField<String>();
        vn.setValue(generateVersionNumber(versionNumbers, 1));
        vn.setFieldLabel(Messages.get("label.nextVersion", "Next iteration version"));
        formPanel.add(vn);
        Button b = new Button(Messages.get("label.minorVersion", "Minor version"));

        b.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                String s = generateVersionNumber(versionNumbers, 1);
                vn.setValue(s);
            }
        });
        window.addButton(b);

        b = new Button(Messages.get("label.majorVersion", "Major version"));
        b.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                String s = generateVersionNumber(versionNumbers, 0);
                vn.setValue(s);
            }
        });
        window.addButton(b);

        b = new Button(Messages.get("label.release", "Release"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                if (!vn.getValue().endsWith("-SNAPSHOT")) {
                    vn.markInvalid(Messages.get("label.snapshotRequired","Working version number must end with -SNAPSHOT"));
                    return;
                }
                window.hide();
                linker.loading("Releasing module...");
                HTML html = new HTML("Generating war, please wait ...");
                window.getButtonBar().setEnabled(false);
                window.add(html);

                JahiaContentManagementService.App.getInstance().releaseModule(JahiaGWTParameters.getSiteKey(), vn.getValue(), new BaseAsyncCallback<RpcMap>() {
                    public void onSuccess(RpcMap result) {
                        linker.loaded();
                        GWTJahiaNode newModule = (GWTJahiaNode) result.get("newModule");
                        String filename = (String) result.get("filename");
                        String url = (String) result.get("downloadUrl");

                        JahiaGWTParameters.getSitesMap().remove(JahiaGWTParameters.getSiteNode().getUUID());
                        JahiaGWTParameters.getSitesMap().put(newModule.getUUID(), newModule);
                        JahiaGWTParameters.setSiteFromNode(newModule, linker);
                        if (((EditLinker) linker).getSidePanel() != null) {
                            Map<String, Object> data = new HashMap<String, Object>();
                            data.put(Linker.REFRESH_ALL, true);
                            ((EditLinker) linker).getSidePanel().refresh(data);
                        }
                        MainModule.staticGoTo(newModule.getPath(), null);
                        SiteSwitcherActionItem.refreshAllSitesList(linker);

                        window.removeAll();
                        HTML link = new HTML(Messages.get("downloadMessage.label") + "<br /><br /><a href=\"" + url + "\" target=\"_new\">" + filename + "</a>");
                        window.add(link);
                        window.show();
                    }

                    public void onApplicationFailure(Throwable caught) {
                        linker.loaded();
                        Info.display("Module release failed", caught.getMessage());
                    }
                });
            }
        });
        window.addButton(b);
        window.add(formPanel);
        window.show();
    }

    private String generateVersionNumber(List<Integer> orderedVersionNumbers, int index) {
        List<Integer> newOrderedVersionNumbers = new ArrayList<Integer>(orderedVersionNumbers);
        newOrderedVersionNumbers.set(index, orderedVersionNumbers.get(index) + 1);
        for (int i = index+1; i < newOrderedVersionNumbers.size(); i++) {
            newOrderedVersionNumbers.set(i, Integer.valueOf(0));
        }
        String s = "";
        for (Integer n : newOrderedVersionNumbers) {
            s += ("." + n);
        }
        s = s.substring(1);
        s += "-SNAPSHOT";
        return s;
    }

    @Override
    public void handleNewLinkerSelection() {
        GWTJahiaNode siteNode = JahiaGWTParameters.getSiteNode();
        String s = siteNode.get("j:versionInfo");
        if (s.endsWith("-SNAPSHOT") && siteNode.get("j:sourcesFolder") != null) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
    }
}
