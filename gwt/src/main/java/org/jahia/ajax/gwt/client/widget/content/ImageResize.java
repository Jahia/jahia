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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;

/**
 *
 *
 * User: toto
 * Date: Nov 13, 2008 - 7:07:20 PM
 */
public class ImageResize extends Window {

    private Linker linker;

    public ImageResize(final Linker linker, final GWTJahiaNode n) {
        super() ;

        this.linker = linker ;

        final int w = Integer.parseInt((String) n.get("j:width"));
        final int h = Integer.parseInt((String) n.get("j:height"));

        setHeading(Messages.get("label.resize"));
        setSize(500, 200);
        setResizable(false);
        ButtonBar buttons = new ButtonBar() ;
        final FormPanel form = new FormPanel() ;
        form.setFrame(false);
        form.setHeaderVisible(false);
        form.setBorders(false);
        setModal(true);


        final NumberField wf = new NumberField();
        wf.setName("width");
        wf.setValue(new Integer(w));
        wf.setFieldLabel(Messages.get("width.label"));
        form.add(wf);

        final NumberField hf = new NumberField();
        hf.setName("height");
        hf.setValue(new Integer(h));
        hf.setFieldLabel(Messages.get("height.label"));
        form.add(hf);

        final CheckBox keepRatio = new CheckBox();
        keepRatio.setName("ratio");
        keepRatio.setValue(true);
        keepRatio.setFieldLabel(Messages.get("ratio.label"));
        form.add(keepRatio);

        hf.addListener(Events.KeyUp, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                if (keepRatio.getValue()) wf.setValue(w * hf.getValue().intValue() / h);
            }
        });
        wf.addListener(Events.KeyUp, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                if (keepRatio.getValue()) hf.setValue(h * wf.getValue().intValue() / w);
            }
        });

        keepRatio.addListener(Events.Change, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                if (keepRatio.getValue()) hf.setValue(h * wf.getValue().intValue() / w);
            }
        });

        final TextField<String> newname = new TextField<String>();
        newname.setName("newname");
        int extIndex = n.getName().lastIndexOf(".") ;
        if (extIndex > 0) {
            String dotExt = n.getName().substring(extIndex) ;
            newname.setValue(n.getName().replaceAll(dotExt, "_resize" + dotExt));
        } else {
            newname.setValue(n.getName() + "_resize");
        }
        newname.setFieldLabel(Messages.get("label.rename"));
        form.add(newname);

        Button cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide() ;
            }
        });
        Button submit = new Button(Messages.get("label.ok"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                resizeImage(n.getPath(), newname.getValue(), wf.getValue().intValue(), hf.getValue().intValue(), false) ;
            }
        }) ;
        buttons.add(submit) ;
        buttons.add(cancel) ;
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setBottomComponent(buttons);

        add(form);
    }

    private void resizeImage(final String path, final String targetName, final int width, final int height, final boolean force) {
         JahiaContentManagementService.App.getInstance().resizeImage(path, targetName, width, height, force, new BaseAsyncCallback() {
             public void onApplicationFailure(Throwable throwable) {
                 if (throwable instanceof ExistingFileException) {
                     if (com.google.gwt.user.client.Window.confirm(Messages.get("alreadyExists.label") + "\n" + Messages.get("confirm.overwrite.label"))) {
                         resizeImage(path, targetName, width, height, true);
                     }
                } else {
                    com.google.gwt.user.client.Window.alert(Messages.get("failure.resize.label") + "\n" + throwable.getLocalizedMessage());
                    Log.error(Messages.get("failure.resize.label"), throwable);
                }
             }

             public void onSuccess(Object result) {
                hide();
                 linker.refresh(Linker.REFRESH_MAIN);
             }
         });
    }
}
