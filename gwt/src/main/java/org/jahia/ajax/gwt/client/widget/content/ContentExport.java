/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

        setHeading(Messages.get("label.export"));
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
        addButton(b);

        b = new Button(Messages.get("label.exportZipWithLive", "ZIP"));
        b.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                startProgress(progressBar, n);
                com.google.gwt.user.client.Window.Location.assign(result + ".zip?live=true&cleanup=simple");
            }
        });
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