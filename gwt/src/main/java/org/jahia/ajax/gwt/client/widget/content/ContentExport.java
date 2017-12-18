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
package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HorizontalPanel;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.Linker;

/**
 * User: toto
 * Date: Nov 13, 2008 - 7:31:46 PM
 */
public class ContentExport extends Window {

    public ContentExport(final Linker linker, final GWTJahiaNode n) {
        super();

        addStyleName("content-export");
        setHeadingHtml(Messages.get("label.export"));
        setSize(500, 80);
        setResizable(false);
        setLayout(new FitLayout());

        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setId("JahiaGxtContentExport");

        setModal(true);
        final String result = JahiaGWTParameters.getContextPath() + "/cms/export/" + JahiaGWTParameters.getWorkspace() + n.getPath();
        final ProgressBar progressBar = new ProgressBar();
        add(progressBar);
        Button b;
        if (!n.getNodeTypes().contains("jnt:page")) {
            HorizontalPanel p = new HorizontalPanel();
            p.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
            p.add(new Text(Messages.get("label.exportChoose", "Choose export format")));
            add(p);
            b = new Button(Messages.get("label.exportXML", "XML content"));
            b.addStyleName("button-exportxml");
            b.addSelectionListener(new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent ce) {
                    startProgress(progressBar, n);
                    com.google.gwt.user.client.Window.open(result + ".xml?cleanup=simple", "", "");
                }
            });
            addButton(b);
        }

        b = new Button(Messages.get("label.exportZip", "ZIP"));
        b.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                startProgress(progressBar, n);
                com.google.gwt.user.client.Window.Location.assign(result + ".zip?live=false&cleanup=simple");
            }


        });
        b.addStyleName("button-exportzip");
        addButton(b);

        b = new Button(Messages.get("label.exportZipWithLive", "ZIP"));
        b.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                startProgress(progressBar, n);
                com.google.gwt.user.client.Window.Location.assign(result + ".zip?live=true&cleanup=simple");
            }
        });
        b.addStyleName("button-exportziplive");
        addButton(b);

/*
        if (n.getNodeTypes().contains("jnt:virtualsite")) {
            b = new Button(Messages.get("label.exportSite", "Full virtual site"));
            b.addSelectionListener(new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent ce) {
                    com.google.gwt.user.client.Window.Location.assign(result + ".zip?exportformat=site&sitebox="+n.getName());
                }
            });
            addButton(b);
        }
*/

        b = new Button(Messages.get("label.close"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide();
            }
        });
        b.addStyleName("button-close");
        addButton(b);
    }


    private void startProgress(ProgressBar progressBar, final GWTJahiaNode n) {
        progressBar.setIncrement(50);
        progressBar.auto();
        new Timer() {
            @Override
            public void run() {
                String exportedNode = Cookies.getCookie("exportedNode");
                if (exportedNode != null && exportedNode.equals(n.getUUID())) {
                    hide();
                    Cookies.removeCookie("exportedNode", "/");
                    this.cancel();
                }
            }
        }.scheduleRepeating(1000);
    }
}