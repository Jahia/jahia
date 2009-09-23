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
package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;

/**
 * User: rfelden
 * Date: 21 oct. 2008 - 14:38:21
 */
public class ImageCrop extends Window {

    private Linker m_linker ;

    public ImageCrop(final Linker linker, final GWTJahiaNode n) {
        super() ;

        m_linker = linker ;
        int w = n.getWidth();
        int h = n.getHeight();
        if (w > 800) {
            h = h * 800 / w;
            w = 800;
        }
        if (h > 350) {
            w = w * 350 / h;
            h = 350;
        }

        if (w < 328) {
            w = 328 ;
        }
        setSize(w + 12, h + 105);
        setHeading(Messages.getResource("fm_crop"));

        setLayout(new FlowLayout()) ;

        FlowPanel flowPanel = new FlowPanel();
        final NumberField top = new NumberField();
        top.setName("top");
        top.setVisible(false);
        flowPanel.add(top);
        final NumberField left = new NumberField();
        left.setName("left");
        left.setVisible(false);
        flowPanel.add(left);
        final NumberField width = new NumberField();
        width.setName("width");
        width.setVisible(false);
        flowPanel.add(width);
        final NumberField height = new NumberField();
        height.setName("height");
        height.setVisible(false);
        flowPanel.add(height);
        final TextField<String> newname = new TextField<String>();


        flowPanel.add(new HTML("<script type=\"text/javascript\" > crop=0; 	</script>"));
        flowPanel.add(new HTML("<div><img style=\"max-width: 800px; height: expression(this.height > 350 ? 350: true); width: expression(this.width > 800 ? 800: true); max-height: 350px;\" src=\"" + n.getUrl() + "\" id=\"cropimg\" onmouseout=\"crop = 0; \" onmouseover=\"if(crop != 1){ new uvumiCropper('cropimg',{onComplete:function(top,left,width,height){$('" + top.getId() + "').set('value', top);$('" + left.getId() + "').set('value', left);$('" + width.getId() + "').set('value', width);$('" + height.getId() + "').set('value', height);}});  crop=1;}\"  /></div>"));


        FormPanel form = new FormPanel();
        form.setFrame(false);
        form.setHeaderVisible(false);
        form.setBorders(false);
        newname.setName("newname");
        int extIndex = n.getName().lastIndexOf(".") ;
        if (extIndex > 0) {
            String dotExt = n.getName().substring(extIndex) ;
            newname.setValue(n.getName().replaceAll(dotExt, "_crop" + dotExt));
        } else {
            newname.setValue(n.getName() + "_crop");
        }
        newname.setFieldLabel(Messages.getResource("fm_newname"));
        form.add(newname);

        ButtonBar buttons = new ButtonBar() ;
        Button cancel = new Button(Messages.getResource("fm_cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide() ;
            }
        });
        Button submit = new Button(Messages.getResource("fm_ok"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                cropImage(n.getPath(), newname.getValue().toString(),
                        Integer.parseInt(top.getValue().toString()),
                        Integer.parseInt(left.getValue().toString()),
                        Integer.parseInt(width.getValue().toString()),
                        Integer.parseInt(height.getValue().toString()), false) ;

            }
        }) ;
        buttons.add(submit) ;
        buttons.add(cancel) ;
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setBottomComponent(buttons);

        add(flowPanel);
        add(form);

        setModal(true);
        setHeaderVisible(true);
        setAutoHide(false);
    }

    private void cropImage(final String path, final String targetName, final int top, final int left, final int width, final int height, final boolean force) {
         JahiaContentManagementService.App.getInstance().cropImage(path, targetName, top, left, width, height, force, new AsyncCallback() {
             public void onFailure(Throwable throwable) {
                 if (throwable instanceof ExistingFileException) {
                    if (com.google.gwt.user.client.Window.confirm(Messages.getResource("fm_alreadyExists") + "\n" + Messages.getResource("fm_confOverwrite"))) {
                         cropImage(path, targetName, top, left, width, height, true);
                     }
                } else {
                    com.google.gwt.user.client.Window.alert(Messages.getResource("fm_failCrop") + "\n" + throwable.getLocalizedMessage());
                    Log.error(Messages.getResource("fm_failCrop"), throwable);
                }
             }

             public void onSuccess(Object result) {
                hide();
                m_linker.refreshMainComponent();
             }
         });
    }
}